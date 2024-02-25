package com.StockTracker.StockTracker.Trade;



import com.StockTracker.StockTracker.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Trade createTrade(Trade trade) {

        Trade savedTrade = tradeRepository.insert(trade);

        // Update the corresponding user with the new trade
        mongoTemplate.update(User.class)
                .matching(Criteria.where("username").is(trade.getOwner()))
                .apply(new Update().push("user_trades").value(savedTrade))
                .first();

        return savedTrade;
    }

    public List<Trade> allTrades(){
        return tradeRepository.findAll();
    }
}

