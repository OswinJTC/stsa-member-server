package com.StockTracker.StockTracker.Trade;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends MongoRepository<Trade, ObjectId>{
}
