package com.StockTracker.StockTracker.MemberNumber;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberNumberRepository extends MongoRepository<MemberNumber, String> {
    Optional<MemberNumber> findFirstByTypeOrderByIdAsc(String type);
    void deleteById(String id);
}
