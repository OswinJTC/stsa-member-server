package com.StockTracker.StockTracker.System;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "SYSTEM")
@Data
@AllArgsConstructor
@NoArgsConstructor



public class System {

    @Id
    private ObjectId id;

    private int NUM_OF_TRADE;
    private String KEEPER;


}