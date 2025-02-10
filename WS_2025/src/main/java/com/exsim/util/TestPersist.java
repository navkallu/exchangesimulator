package com.exsim.util;

import com.exsim.domain.MarketData;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;

public class TestPersist {

    public static void main(String args[]){

        try {
            // Create sample data
            HashMap<String, MarketData> marketDataMap = new HashMap<>();
            marketDataMap.put("001.HK", new MarketData("001.HK", 150.25, 150.50, 0.1));
            marketDataMap.put("002.HK", new MarketData("002.HK", 2500.00, 2505.00, 1));
            marketDataMap.put("003.HK", new MarketData("003.HK", 200.00, 205.00, 1));
            marketDataMap.put("004.HK", new MarketData("004.HK", 300.00, 305.00, 1));
            marketDataMap.put("005.HK", new MarketData("005.HK", 30.00, 35.00, 1));
            marketDataMap.put("006.HK", new MarketData("006.HK", 150.25, 150.50, 0.1));
            marketDataMap.put("007.HK", new MarketData("007.HK", 2500.00, 2505.00, 1));
            marketDataMap.put("008.HK", new MarketData("008.HK", 200.00, 205.00, 1));
            marketDataMap.put("009.HK", new MarketData("009.HK", 300.00, 305.00, 1));
            marketDataMap.put("010.HK", new MarketData("010.HK", 30.00, 35.00, 1));
            // Convert to JSON and store in a file
            ObjectMapper objectMapper = new ObjectMapper();
            //objectMapper.writeValue(new File(".\\marketData.json"), marketDataMap);

            //System.out.println("Market data has been written to marketData.json");

            // Load from JSON to HashMap
            HashMap<String, MarketData> loadedMarketDataMap = objectMapper.readValue(
                    new File("marketData.json"),
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, MarketData.class)
            );

            // Print loaded data
            loadedMarketDataMap.forEach((symbol, marketData) -> {
                System.out.println("Symbol: " + symbol + ", Bid Price: " + marketData.getBidpx() + ", Ask Price: " + marketData.getAskpx());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
