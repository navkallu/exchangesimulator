package com.exsim.service;

import com.exsim.domain.Market;
import com.exsim.domain.Order;

import java.util.ArrayList;
import java.util.HashMap;

public class MatchingService {

    private final HashMap<String, Market> markets = new HashMap<String, Market>();

    private Market getMarket(String symbol) {
        Market m = markets.get(symbol);
        if (m == null) {
            m = new Market();
            markets.put(symbol, m);
        }
        return m;
    }

    public boolean insert(Order order) {
        return getMarket(order.getSymbol()).insert(order);
    }

    public void match(String symbol, ArrayList<Order> orders) {
        getMarket(symbol).match(symbol, orders);
    }

    public Order find(String symbol, char side, String id) {
        return getMarket(symbol).find(symbol, side, id);
    }

    public void erase(Order order) {
        getMarket(order.getSymbol()).erase(order);
    }

    public void display() {
        for (String symbol : markets.keySet()) {
            System.out.println("MARKET: " + symbol);
            display(symbol);
        }
    }

    public void displayOrderBook() {
        String OrderBookJson = PersistenceService.displayOrderBook(getMarkets());
        System.out.println(OrderBookJson);
    }

    public void display(String symbol) {
        getMarket(symbol).display();
    }

    public HashMap<String, Market> getMarkets() {
        return markets;
    }

}

