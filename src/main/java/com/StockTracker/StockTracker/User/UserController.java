package com.StockTracker.StockTracker.User;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;



@RestController
@CrossOrigin(origins = "https://member.stsa.tw/")
@RequestMapping("/userApi")
public class UserController {

    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public ResponseEntity<?> createUser(
            @RequestPart("user") User user,
            @RequestPart("file") MultipartFile file) {

        // Validate the input fields
        if (user.getTaiwaneseName() == null || user.getTaiwaneseName().isEmpty() ||
                user.getEnglishName() == null || user.getEnglishName().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getSchool() == null || user.getSchool().isEmpty() ||
                user.getYear_of_study() == null || user.getYear_of_study().isEmpty() ||
                user.getContact_number() == null || user.getContact_number().isEmpty() ||
                user.getBirthday() == null || user.getBirthday().isEmpty() ||
                user.getProgram() == null || user.getProgram().isEmpty()) {

            return new ResponseEntity<>("Missing or empty parameters", HttpStatus.BAD_REQUEST);
        }

        try {
            String fileId = userService.storeFile(file);
            user.setFileId(fileId); // Store the GridFS file ID in the User document

            String token = userService.registerUser(user); // Now pass only the User object
            return new ResponseEntity<>("Verification email sent. Please check your inbox.", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        boolean isVerified = userService.verifyUser(token);
        if (isVerified) {
            return new ResponseEntity<>("Email verified successfully!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid or expired token", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveUser(@PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            boolean success = userService.approveUser(objectId);
            if (success) {
                return new ResponseEntity<>("User approved and moved to the approved collection.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found or already approved.", HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid ID format", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectUser(@PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            boolean success = userService.rejectUser(objectId);
            if (success) {
                return new ResponseEntity<>("User rejected and application deleted.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found or already rejected.", HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid ID format", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/members")
    public ResponseEntity<List<User>> getAllApprovedMembers() {
        List<User> users = userService.getAllApprovedMembers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/member/{uuid}")
    public ResponseEntity<User> getMemberByUUID(@PathVariable String uuid) {
        User user = userService.getUserByUUID(uuid);

        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<User>> getAllPending() {
        List<User> users = userService.getAllPending();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> serveImage(@PathVariable String id) {
        try {
            InputStream stream = userService.getFileById(id);
            if (stream != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // Adjust content type based on your file type
                        .body(new InputStreamResource(stream));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<Resource> getUserPdf(@PathVariable String id) {
        try {
            InputStream stream = userService.getPdfById(id);
            if (stream != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF) // Set content type to PDF
                        .body(new InputStreamResource(stream));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
