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
    private String email;
    private String school;
    private String program;
    private String year_of_study;
    private String birthday;
    private String contact_number;
    private String fileId;
    private String pdfFileId;

    private String imageUrl;
}
