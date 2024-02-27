package com.StockTracker.StockTracker.Trade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "trades")
@Data
@AllArgsConstructor
@NoArgsConstructor



public class Trade {

    @Id
    private ObjectId id;

    private int referenceNumber;
    private String owner;
    private String transactionType;
    private String stockName;
    private double buyPrice;
    private double salePrice;
    private double quantity;
    private LocalDateTime buyDate;
    private LocalDateTime saleDate;


    private double totalBuyAmount;
    private double totalSaleAmount;
    private double percentAmount;
    private double profit;


    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
        calculateTotalBuyAmount();
        calculateProfit();
        calculatePercentBuyAmount();
        this.referenceNumber = -1;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
        calculateTotalSaleAmount();
        calculateProfit();
        calculatePercentBuyAmount();
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        calculateTotalBuyAmount();
        calculateTotalSaleAmount();
        calculateProfit();
        calculatePercentBuyAmount();
    }

    private void calculateTotalBuyAmount() {
        this.totalBuyAmount = quantity * buyPrice;
    }

    private void calculatePercentBuyAmount() {
        double temp = ((salePrice - buyPrice) / buyPrice) * 100;
        this.percentAmount = temp;
    }

    private void calculateTotalSaleAmount() {
        this.totalSaleAmount = quantity * salePrice;
    }

    private void calculateProfit() {
        this.profit = (salePrice - buyPrice) * quantity;
    }

    public void setBuyDate(String buyDate) {
        // Assuming buyDate is in the format "yyyy-MM-dd"
        this.buyDate = LocalDateTime.parse(buyDate + "T00:00:00");
    }

    public void setSaleDate(String saleDate) {
        // Assuming saleDate is in the format "yyyy-MM-dd"
        this.saleDate = LocalDateTime.parse(saleDate + "T00:00:00");
    }
}
