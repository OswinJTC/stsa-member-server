package com.StockTracker.StockTracker.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String id;

    private String taiwaneseName;
    private String englishName;
    private String birthday;
    private String gender;
    private String contact_number;
    private String line_id;
    private String email;
    private String current_citizenship;

    private String school;
    private String program;
    private String education_level;
    private String year_of_study;

    private String date_of_enrollment;
    private String date_of_graduation;

    private String unique_uuid;
    private String member_id;


    private String fileId;
    private String pdfFileId;
    private String imageUrl;
}
