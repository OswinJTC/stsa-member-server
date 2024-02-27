package com.StockTracker.StockTracker.Trade;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
 
@Repository
public interface TradeRepository extends MongoRepository<Trade, ObjectId>{

    Optional<Trade> findByReferenceNumber(int referenceNumber);


}
