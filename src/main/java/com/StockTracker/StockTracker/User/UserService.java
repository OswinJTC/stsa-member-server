package com.StockTracker.StockTracker.User;

import com.StockTracker.StockTracker.PdfCardGenerator;
import com.mongodb.client.gridfs.GridFSBucket;
import jakarta.mail.internet.MimeMessage;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    private static final String PENDING_COLLECTION = "pending_users";
    private static final String APPROVED_COLLECTION = "approved_users";

    public String registerUser(User user) {
        String token = UUID.randomUUID().toString();

        // Store user in Redis with a TTL (e.g., 15 minutes)
        redisTemplate.opsForValue().set(token, user, 15, TimeUnit.MINUTES);

        // Send verification email
        sendVerificationEmail(user, token);

        return token;
    }


    public String storeFile(MultipartFile file) throws IOException {
        // Store the file in GridFS
        InputStream inputStream = file.getInputStream();
        String fileName = file.getOriginalFilename();
        ObjectId fileId = gridFsTemplate.store(inputStream, fileName, file.getContentType());
        return fileId.toString();  // Return the file ID to be stored in the User document
    }

    public InputStream getFileById(String id) throws IOException {
        // Retrieve the file from GridFS by ID
        return gridFSBucket.openDownloadStream(new ObjectId(id));
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
                String pdfFileId = generateAndStorePdf(user);
                user.setPdfFileId(pdfFileId); // Store the PDF file ID in the User document

                mongoTemplate.save(user, APPROVED_COLLECTION); // Save the user with PDF file ID

                // Send the PDF to the user's email
                sendPdfByEmail(user, pdfFileId);

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

    private String generateAndStorePdf(User user) throws IOException {
        PdfCardGenerator generator = new PdfCardGenerator();
        String logoPath = "https://i1.wp.com/stsa.tw/wp-content/uploads/2021/05/Asset-9@6x.png?resize=768%2C769&ssl=1";
        String qrCodePath = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/QR_code_for_mobile_English_Wikipedia.svg/1920px-QR_code_for_mobile_English_Wikipedia.svg.png";

        // Generate the PDF and get the InputStream
        InputStream pdfStream = generator.generateCard(user.getEnglishName(), "School: " + user.getSchool(), logoPath, qrCodePath);

        // Store the PDF in GridFS
        ObjectId pdfFileId = gridFsTemplate.store(pdfStream, user.getEnglishName() + "_MemberCard.pdf", "application/pdf");

        return pdfFileId.toString(); // Return the GridFS file ID
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

    public InputStream getPdfById(String id) throws IOException {
        return gridFSBucket.openDownloadStream(new ObjectId(id));
    }

    private void sendPdfByEmail(User user, String pdfFileId) {
        try {
            // Retrieve the PDF file from GridFS
            InputStream pdfStream = getPdfById(pdfFileId);
            byte[] pdfBytes = pdfStream.readAllBytes();

            // Create a MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Your Membership Card");
            helper.setText("Dear " + user.getEnglishName() + ",\n\nPlease find attached your membership card PDF.\n\nBest regards,\nYour Team");

            // Attach the PDF
            helper.addAttachment(user.getEnglishName() + "_MemberCard.pdf", new ByteArrayResource(pdfBytes));

            // Send the email
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email with PDF: " + e.getMessage());
        }
    }

}
