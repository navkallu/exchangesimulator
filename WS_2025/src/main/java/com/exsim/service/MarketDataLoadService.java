package com.exsim.service;

import com.exsim.domain.MarketData;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;

public class MarketDataLoadService {
    private static HashMap<String, MarketData> marketDataMap = new java.util.HashMap<>();

    private static void uploadMarketData(){

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            // Load from JSON to HashMap
            marketDataMap = objectMapper.readValue(
                    new File("..\\marketdata\\marketdata.json"),
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, MarketData.class)
            );
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static MarketData getMarketData(String symbol){
        if(marketDataMap.size()==0){
            uploadMarketData();
        }

        return marketDataMap.get(symbol);

    }

    public static double getBidPx(String symbol){

        return (getMarketData(symbol)).getBidpx();
    }

    public static double getAskPx(String symbol){

        return (getMarketData(symbol)).getAskpx();
    }

    public static double getTick(String symbol){

        return (getMarketData(symbol)).getTicksize();
    }
}
