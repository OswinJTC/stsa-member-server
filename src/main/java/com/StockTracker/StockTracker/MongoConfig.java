package com.StockTracker.StockTracker;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public GridFSBucket gridFSBucket(MongoTemplate mongoTemplate) {
        MongoDatabase db = mongoTemplate.getDb();
        return GridFSBuckets.create(db);
    }
}
