package com.StockTracker.StockTracker.User;

import com.StockTracker.StockTracker.MemberNumber.MemberNumber;
import com.StockTracker.StockTracker.MemberNumber.MemberNumberService;
import com.StockTracker.StockTracker.PdfCardGenerator;
import com.google.zxing.WriterException;
import com.mongodb.client.gridfs.GridFSBucket;
import jakarta.mail.internet.MimeMessage;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
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

    @Autowired
    private MemberNumberService memberNumberService;
    
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
        String subject = "會員郵件驗證";
        String verificationLink = "http://localhost:8080/userApi/verify?token=" + token;
        String body = "您好，請點選連結即可完成郵件驗證: " + verificationLink;

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

                String uniqueId = UUID.randomUUID().toString();
                user.setUnique_uuid(uniqueId);

                MemberNumber memberNumber = memberNumberService.getNextAvailableNumber(user.getCurrent_citizenship());

                if(user.getCurrent_citizenship().equals("Taiwanese")){
                    user.setMember_id("T2024"+ memberNumber.getNumber() + "W");
                }else if(user.getCurrent_citizenship().equals("Singaporean")){
                    user.setMember_id("S2024"+ memberNumber.getNumber() + "G");
                }else if(user.getCurrent_citizenship().equals("PR")){
                    user.setMember_id("P2024"+ memberNumber.getNumber() + "R");
                }

                //Remove the assigned number from the collection
                memberNumberService.deleteNumberById(memberNumber.getId());

                mongoTemplate.remove(query, PENDING_COLLECTION);
                mongoTemplate.save(user, APPROVED_COLLECTION);


                // Generate PDF for approved user
                String pdfFileId = generateAndStorePdf(user);
                user.setPdfFileId(pdfFileId);


                // Send the PDF to the user's email
                sendPdfByEmail(user, pdfFileId);


                return true;


            } catch (Exception e) {
                System.err.println("Unexpected error during user approval: " + e.getMessage());
                return false;
            }
        }
        return false;
    }


    private String generateAndStorePdf(User user) throws IOException, WriterException {
        PdfCardGenerator generator = new PdfCardGenerator();

        // Generate the PDF and get the InputStream
        InputStream pdfStream = generator.generateCard(user.getTaiwaneseName(), user.getMember_id(), user.getUnique_uuid());

        // Store the PDF in GridFS
        ObjectId pdfFileId = gridFsTemplate.store(pdfStream, user.getEnglishName() + "_MemberCard.pdf", "application/pdf");

        // Create a query to find the document by its ID
        Query query = new Query(Criteria.where("_id").is(pdfFileId));

        // Create an update object to add the unique_uuid field
        Update update = new Update();
        update.set("unique_uuid", user.getUnique_uuid());

        // Perform the update operation
        mongoTemplate.updateFirst(query, update, "fs.files");

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

    public User getUserByUUID(String uuid) {
        Query query = new Query(Criteria.where("unique_uuid").is(uuid));
        return mongoTemplate.findOne(query, User.class, APPROVED_COLLECTION);
    }

    public List<User> getAllPending() {
        return mongoTemplate.findAll(User.class, PENDING_COLLECTION);
    }

    public InputStream getPdfById(String id) throws IOException {
        return gridFSBucket.openDownloadStream(new ObjectId(id));
    }

    public InputStream getPdfByUUID(String uuid) throws IOException {
        // Create a query to find the PDF by unique_uuid
        Query query = new Query(Criteria.where("unique_uuid").is(uuid));
        Document fileDocument = mongoTemplate.findOne(query, Document.class, "fs.files");

        if (fileDocument != null) {
            ObjectId fileId = fileDocument.getObjectId("_id");
            return gridFSBucket.openDownloadStream(fileId);
        } else {
            return null; // No PDF found for the given unique_uuid
        }
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
            helper.setSubject("STSA會員申請成功通知");
            helper.setText(user.getTaiwaneseName() + " 先生/小姐 您好，" + ",\n\n感謝您申請成為我們的會員！您的會員卡已經生成，請查閱附件中的 PDF 文件。\n\n如果有任何疑問，歡迎隨時聯繫我們。\n\n新加坡學生總會\n敬上");

            // Attach the PDF
            helper.addAttachment(user.getTaiwaneseName() + "_STSA會員卡.pdf", new ByteArrayResource(pdfBytes));

            // Send the email
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email with PDF: " + e.getMessage());
        }
    }


}
