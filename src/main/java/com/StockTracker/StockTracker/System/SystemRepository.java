package com.StockTracker.StockTracker.System;


import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemRepository extends MongoRepository<System, ObjectId>{

    Optional<System> findAllBy(String name);


}
