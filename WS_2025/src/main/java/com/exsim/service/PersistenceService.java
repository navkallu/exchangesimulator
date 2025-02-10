package com.exsim.service;

import com.exsim.domain.Market;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;

public class PersistenceService {

    public static String displayOrderBook(HashMap<String, Market> markets){
       String jsonString = "";
       try{
           // Convert to JSON
           ObjectMapper objectMapper = new ObjectMapper();
           jsonString = objectMapper.writeValueAsString(markets);
           objectMapper.writeValue(new File("..\\orderbook\\orderbook.json"), markets);
       }catch(Exception ex){
            ex.printStackTrace();
       }
        return jsonString;

    }
}
