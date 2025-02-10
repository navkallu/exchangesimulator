package com.exsim.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import quickfix.field.Side;

public class Order {
    private final double price;
    private long openQuantity;
    private String orderId;
    private final long entryTime;
    @JsonIgnore
    private final long quantity;
    @JsonIgnore
    private String clientOrderId;
    @JsonIgnore
    private String origClientOrderId;
    @JsonIgnore
    private final String symbol;
    @JsonIgnore
    private final String owner;
    @JsonIgnore
    private final String target;
    @JsonIgnore
    private final char side;
    @JsonIgnore
    private final char type;
    @JsonIgnore
    private long executedQuantity;
    @JsonIgnore
    private double avgExecutedPrice;
    @JsonIgnore
    private double lastExecutedPrice;
    @JsonIgnore
    private long lastExecutedQuantity;

    public Order(String clientId, String symbol, String owner, String target, char side, char type,
                 double price, long quantity,String origClientOrderId) {
        super();
        this.clientOrderId = clientId;
        this.symbol = symbol;
        this.owner = owner;
        this.target = target;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        openQuantity = quantity;
        entryTime = System.currentTimeMillis();
        this.origClientOrderId = origClientOrderId;
    }

    public double getAvgExecutedPrice() {
        return avgExecutedPrice;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public String getOrigClientOrderId() {
        return origClientOrderId;
    }

    public long getExecutedQuantity() {
        return executedQuantity;
    }

    public long getLastExecutedQuantity() {
        return lastExecutedQuantity;
    }

    public long getOpenQuantity() {
        return openQuantity;
    }

    public String getOwner() {
        return owner;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public char getSide() {
        return side;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getTarget() {
        return target;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public char getType() {
        return type;
    }

    public boolean isFilled() {
        return quantity == executedQuantity;
    }

    public void cancel() {
        openQuantity = 0;
    }

    public boolean isClosed() {
        return openQuantity == 0;
    }

    public void execute(double price, long quantity) {
        avgExecutedPrice = ((quantity * price) + (avgExecutedPrice * executedQuantity))
                / (quantity + executedQuantity);

        openQuantity -= quantity;
        executedQuantity += quantity;
        lastExecutedPrice = price;
        lastExecutedQuantity = quantity;
    }

    public String toString() {
        return (side == Side.BUY ? "BUY" : "SELL") + " " + quantity + "@$" + price + " (" + openQuantity + ")";
    }

    public long getEntryTime() {
        return entryTime;
    }

    public double getLastExecutedPrice() {
        return lastExecutedPrice;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public void setOrigClientOrderId(String origClientOrderId) {
        this.origClientOrderId = origClientOrderId;
    }

    public void setOpenQuantity(long openQuantity) {
        this.openQuantity = openQuantity;
    }

    public void setExecutedQuantity(long executedQuantity) {
        this.executedQuantity = executedQuantity;
    }

    public void setAvgExecutedPrice(double avgExecutedPrice) {
        this.avgExecutedPrice = avgExecutedPrice;
    }

    public void setLastExecutedPrice(double lastExecutedPrice) {
        this.lastExecutedPrice = lastExecutedPrice;
    }

    public void setLastExecutedQuantity(long lastExecutedQuantity) {
        this.lastExecutedQuantity = lastExecutedQuantity;
    }
}

