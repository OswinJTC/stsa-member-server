package com.StockTracker.StockTracker.Trade;



import com.StockTracker.StockTracker.System.SystemRepository;
import com.StockTracker.StockTracker.User.User;
import com.StockTracker.StockTracker.System.System;
import com.StockTracker.StockTracker.User.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemRepository systemRepository;



    public Trade createTrade(Trade trade) {

        System system = systemRepository.findAllBy("Oswin").orElseThrow(() -> new IllegalArgumentException("Keeper not found"));

        trade.setReferenceNumber(system.getNUM_OF_TRADE()); // Set the reference number
        Trade savedTrade = tradeRepository.insert(trade);

        // Update the corresponding user with the new trade
        String owner = trade.getOwner();
        User user = userRepository.findByUsername(owner).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.getUser_trades().add(savedTrade);
        userRepository.save(user);


        system.setNUM_OF_TRADE((system.getNUM_OF_TRADE()+1));
        systemRepository.save(system);

        return savedTrade;
    }


    public void deleteTrade(int tradeReference, String owner) {
        Optional<Trade> optionalTrade = tradeRepository.findByReferenceNumber(tradeReference);

        if (optionalTrade.isPresent()) {
            Trade trade = optionalTrade.get();
            tradeRepository.delete(trade);

            User user = userRepository.findByUsername(owner).orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.getUser_trades().removeIf(t -> t.getReferenceNumber() == tradeReference);
            userRepository.save(user);
        } else {
            // Handle case where trade does not exist
        }
    }

    public List<Trade> allTrades() {
        return tradeRepository.findAll();
    }
}
