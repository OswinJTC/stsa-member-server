package com.StockTracker.StockTracker.User;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@CrossOrigin(origins = "https://stsa-member-client.onrender.com/")
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
            // Register user and save the uploaded file
            String token = userService.registerUser(user, file);
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

    @GetMapping("/pending")
    public ResponseEntity<List<User>> getAllPending() {
        List<User> users = userService.getAllPending();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path imagePath = Paths.get("uploads/").resolve(filename).normalize();
            Resource resource = new UrlResource(imagePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // Adjust content type based on your file type
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
