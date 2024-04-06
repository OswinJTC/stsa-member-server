package com.StockTracker.StockTracker.Trade;

 
import com.StockTracker.StockTracker.User.User;
import com.StockTracker.StockTracker.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;

import java.util.List;
import java.util.Map;
 

@RequestMapping("/tradeApi")
@RestController
@CrossOrigin(origins = "https://ntu-us-stock-tracker.onrender.com")

//https://ntu-us-stock-tracker.onrender.com

public class TradeController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private UserService userService;



    @PostMapping("/addTrade")
    public ResponseEntity<Trade> createTrade(@RequestBody Trade trade) {
        String retrievedUserEmail = userService.findUserEmailByUsername(trade.getOwner());

        if (trade != null && retrievedUserEmail != null && !retrievedUserEmail.isEmpty()) {
            try {
                // Formatter for the dates
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Formatting buyDate and saleDate
                String formattedBuyDate = trade.getBuyDate().format(formatter);
                String formattedSaleDate = trade.getSaleDate().format(formatter);

                // Determine the color for profit
                String profitColor = trade.getProfit() > 0 ? "red" : "green";

                // Email content with formatted dates and added color
                String emailContent = "<div>尊敬的 " + trade.getOwner() + " 先生/女士，" + "</div><br><div>您好！這是一封自動通知郵件。<br>您在我們系統中成功新增了一筆交易紀錄。交易概述如下：</div><br>" +
                        "<div style='font-weight:bold; color:navy;'>---- 交易詳情 ----</div>" +
                        "<div><div><span style='font-weight:bold;'>股票代號：</span> <span style='color:maroon;'>" + trade.getStockName() + "</span></div>" +
                        "<div><span style='font-weight:bold;'>購入日期：</span> " + formattedBuyDate + "</div>" +
                        "<div><span style='font-weight:bold;'>賣出日期：</span> " + formattedSaleDate + "</div>" +
                        "<div><span style='font-weight:bold;'>購入價：</span> " + trade.getBuyPrice() + " 美金</div>" +
                        "<div><span style='font-weight:bold;'>賣出價：</span> " + trade.getSalePrice() + " 美金</div>" +
                        "<div><span style='font-weight:bold;'>平倉數量：</span> " + trade.getQuantity() + " 股</div>" +
                        "<div><span style='font-weight:bold;'>獲利：</span> <span style='color:" + profitColor + ";'>" + String.format("%.2f", trade.getProfit()) + " 美金" + " (" + String.format("%.2f", trade.getPercentAmount()) + "%)</span></div></div>" +
                        "<div style='font-weight:bold; color:navy;'>----------------------</div><br>" +
                        "<div>請檢查上述交易詳情，確保所有信息準確無誤。若有任何疑問，請不吝聯繫我們。\n" +
                        "\n</div><br><br><div>祝 商祺，</div><div>StockAnalyzer團隊敬上</div>";



                // Assuming you have a method to send HTML email
                tradeService.sendTradeEmail(retrievedUserEmail, "交易新增成功\uD83D\uDCC8\uD83D\uDCC9\uD83D\uDE80", emailContent);

                System.out.println("Email sent successfully to: " + retrievedUserEmail);
            } catch (Exception e) {
                System.err.println("Failed to send email to: " + retrievedUserEmail);
                e.printStackTrace();
            }

            Trade savedTrade = tradeService.createTrade(trade); // Assuming createTrade can handle user
            return new ResponseEntity<>(savedTrade, HttpStatus.CREATED);
        } else {
            System.err.println("Invalid trade details or user information");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }




    @GetMapping("/allTrades")
    public ResponseEntity<List<Trade>> getAllMovies(){
        return new ResponseEntity<List<Trade>>(tradeService.allTrades(), HttpStatus.OK);
    }

    @DeleteMapping("/deleteTrade")
    public ResponseEntity<String> deleteTrade(@RequestBody Map<String, String> requestBody) {
        String tradeReferenceString = requestBody.get("tradeReference");
        int tradeReference = Integer.parseInt(tradeReferenceString); // Parse as integer
        String ownerUsername = requestBody.get("owner");

        tradeService.deleteTrade(tradeReference, ownerUsername);
        return ResponseEntity.ok("Trade with reference " + tradeReference + " belonging to user " + ownerUsername + " deleted successfully.");
    }








}
