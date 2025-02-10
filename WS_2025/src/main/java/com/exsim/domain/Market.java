package com.exsim.domain;

import quickfix.field.OrdType;
import quickfix.field.Side;

import java.text.DecimalFormat;
import java.util.*;

public class Market {

    private final List<Order> bidOrders = new ArrayList<Order>();
    private final List<Order> askOrders = new ArrayList<Order>();

    public boolean match(String symbol, List<Order> orders) {
        while (true) {
            if (bidOrders.size() == 0 || askOrders.size() == 0) {
                return orders.size() != 0;
            }
            Order bidOrder = bidOrders.get(0);
            Order askOrder = askOrders.get(0);
            if (bidOrder.getType() == OrdType.MARKET || askOrder.getType() == OrdType.MARKET
                    || (bidOrder.getPrice() >= askOrder.getPrice())) {
                match(bidOrder, askOrder);
                if (!orders.contains(bidOrder)) {
                    orders.add(0, bidOrder);
                }
                if (!orders.contains(askOrder)) {
                    orders.add(0, askOrder);
                }

                if (bidOrder.isClosed()) {
                    bidOrders.remove(bidOrder);
                }
                if (askOrder.isClosed()) {
                    askOrders.remove(askOrder);
                }
            } else
                return orders.size() != 0;
        }
    }

    private void match(Order bid, Order ask) {
        double price = ask.getType() == OrdType.LIMIT ? ask.getPrice() : bid.getPrice();
        long quantity = bid.getOpenQuantity() >= ask.getOpenQuantity() ? ask.getOpenQuantity() : bid.getOpenQuantity();

        bid.execute(price, quantity);
        ask.execute(price, quantity);
    }

    public boolean insert(Order order) {

        if(order.getSide() == Side.BUY){
            bidOrders.add(order);
            sortBidOrders();
        }else{
            askOrders.add(order);
            sortAskOrders();
        }

        return true;

    }

    private void sortBidOrders() {
        // Sort orders by price (highest first) and then by entry time (earliest first)
        Collections.sort(bidOrders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                int priceComparison = Double.compare(o2.getPrice(), o1.getPrice());
                if (priceComparison != 0) {
                    return priceComparison;
                } else {
                    return Long.compare(o1.getEntryTime(), o2.getEntryTime());
                }
            }
        });
    }

        private void sortAskOrders(){
            // Sort orders by price (Lowest first) and then by entry time (earliest first)
            Collections.sort(askOrders, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    int priceComparison = Double.compare(o1.getPrice(),o2.getPrice());
                    if (priceComparison != 0) {
                        return priceComparison;
                    } else {
                        return Long.compare(o1.getEntryTime(),o2.getEntryTime());
                    }
                }
            });

    }

    public void erase(Order order) {
        if (order.getSide() == Side.BUY) {
            bidOrders.remove(find(bidOrders, order.getClientOrderId()));
        } else {
            askOrders.remove(find(askOrders, order.getClientOrderId()));
        }
    }

    public Order find(String symbol, char side, String id) {
        return find(side == Side.BUY ? bidOrders : askOrders, id);
    }

    private Order find(List<Order> orders, String clientOrderId) {
        for (Order order : orders) {
            if (order.getClientOrderId().equals(clientOrderId)) {
                return order;
            }
        }
        return null;
    }

    public void display() {
        displaySide(bidOrders, "BIDS");
        displaySide(askOrders, "ASKS");
    }

    private void displaySide(List<Order> orders, String title) {
        DecimalFormat priceFormat = new DecimalFormat("#.00");
        DecimalFormat qtyFormat = new DecimalFormat("######");
        System.out.println(title + ":\n----");
        for (Order order : orders) {
            System.out.println("  $" + priceFormat.format(order.getPrice()) + " "
                    + qtyFormat.format(order.getOpenQuantity()) + " " + order.getOwner() + " "
                    + new Date(order.getEntryTime()));
        }
    }

    public List<Order> getBidOrders() {
        return bidOrders;
    }

    public List<Order> getAskOrders() {
        return askOrders;
    }
}
