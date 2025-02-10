package com.exsim.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketData {
    private String symbol;
    private double bidpx;
    private double askpx;
    private double ticksize;

    @JsonCreator
    public MarketData(@JsonProperty("symbol") String symbol,
                      @JsonProperty("bidpx") double bidpx,
                      @JsonProperty("askpx") double askpx,
                      @JsonProperty("ticksize") double ticksize) {
        this.symbol = symbol;
        this.bidpx = bidpx;
        this.askpx = askpx;
        this.ticksize = ticksize;
    }

    // Getters and setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getBidpx() {
        return bidpx;
    }

    public void setBidpx(double bidpx) {
        this.bidpx = bidpx;
    }

    public double getAskpx() {
        return askpx;
    }

    public void setAskpx(double askpx) {
        this.askpx = askpx;
    }

    public double getTicksize() {
        return ticksize;
    }

    public void setTicksize(double ticksize) {
        this.ticksize = ticksize;
    }
}
