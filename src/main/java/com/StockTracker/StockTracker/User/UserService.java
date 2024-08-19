package com.StockTracker.StockTracker.User;

import com.StockTracker.StockTracker.PdfCardGenerator;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private RedisTemplate<String, User> redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JavaMailSender mailSender;

    private static final String PENDING_COLLECTION = "pending_users";
    private static final String APPROVED_COLLECTION = "approved_users";

    private static final String UPLOAD_DIR = "uploads/";

    public String registerUser(User user, MultipartFile file) throws IOException {
        String token = UUID.randomUUID().toString();

        // Store the uploaded file
        String imageUrl = storeFile(file, user.getEmail());
        user.setImageUrl(imageUrl);

        // Store user in Redis with a TTL (e.g., 15 minutes)
        redisTemplate.opsForValue().set(token, user, 15, TimeUnit.MINUTES);

        // Send verification email
        sendVerificationEmail(user, token);

        return token;
    }
    private String storeFile(MultipartFile file, String email) throws IOException {
        // Generate a unique file name
        String fileName = email + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR, fileName);  // Save in the UPLOAD_DIR directory
        Files.createDirectories(path.getParent());

        // Save the file locally
        Files.write(path, file.getBytes());

        // Return only the file name (without the UPLOAD_DIR prefix)
        return fileName;
    }


    private void sendVerificationEmail(User user, String token) {
        String subject = "Email Verification";
        String verificationLink = "https://stsa-member-server.onrender.com/userApi/verify?token=" + token;
        String body = "Please verify your email by clicking the following link: " + verificationLink;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("jtc93125@gmail.com");

        mailSender.send(message);
    }

    public boolean verifyUser(String token) {
        // Retrieve user from Redis
        User user = redisTemplate.opsForValue().get(token);

        if (user != null) {
            // Save to pending collection
            mongoTemplate.save(user, PENDING_COLLECTION);
            redisTemplate.delete(token);  // Remove from Redis
            return true;
        }

        return false;
    }

    public boolean approveUser(ObjectId userId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        User user = mongoTemplate.findOne(query, User.class, PENDING_COLLECTION);

        if (user != null) {
            try {
                mongoTemplate.remove(query, PENDING_COLLECTION);
                mongoTemplate.save(user, APPROVED_COLLECTION);

                // Generate PDF for approved user
                generatePdfForUser(user);

                return true;
            } catch (IOException e) {
                System.err.println("Failed to generate PDF for user: " + e.getMessage());
                return false;
            } catch (Exception e) {
                System.err.println("Unexpected error during user approval: " + e.getMessage());
                return false;
            }
        }
        return false;
    }


    private void generatePdfForUser(User user) throws IOException {
        PdfCardGenerator generator = new PdfCardGenerator();
        String logoPath = "https://i1.wp.com/stsa.tw/wp-content/uploads/2021/05/Asset-9@6x.png?resize=768%2C769&ssl=1";
        String qrCodePath = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/QR_code_for_mobile_English_Wikipedia.svg/1920px-QR_code_for_mobile_English_Wikipedia.svg.png";

        // Generate a custom filename for the PDF, for example using the user's name
        String pdfFileName = user.getEnglishName() + "_MemberCard";

        // Pass the custom filename to the generateCard method
        generator.generateCard(user.getEnglishName(), "School: " + user.getSchool(), logoPath, qrCodePath, pdfFileName);
    }


    public boolean rejectUser(ObjectId userId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        User user = mongoTemplate.findOne(query, User.class, PENDING_COLLECTION);

        if (user != null) {
            mongoTemplate.remove(query, PENDING_COLLECTION);  // Remove from pending collection
            return true;
        } else {
            // Log why rejection failed
            // Consider returning an error message or code here as well
            return false;
        }
    }


    public List<User> getAllApprovedMembers() {
        return mongoTemplate.findAll(User.class, APPROVED_COLLECTION);
    }

    public List<User> getAllPending() {
        return mongoTemplate.findAll(User.class, PENDING_COLLECTION);
    }

}
