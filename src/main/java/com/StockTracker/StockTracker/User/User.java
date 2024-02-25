package com.StockTracker.StockTracker.User;


import com.StockTracker.StockTracker.Trade.Trade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private ObjectId id;

    private String username;
    private String nickname = "";
    private String email;
    private String password;

    @DocumentReference
    private List<Trade> user_trades;



}