package com.exsim.app;

import com.exsim.domain.Order;
import com.exsim.service.MatchingService;
import com.exsim.util.IdGenerator;
import com.exsim.service.MarketDataLoadService;
import quickfix.*;
import quickfix.Message;
import quickfix.field.*;
import quickfix.fix42.*;

import java.util.ArrayList;

public class ExSimApplication implements quickfix.Application {
    private final MatchingService orderMatcher = new MatchingService();
    private final IdGenerator generator = new IdGenerator();

    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
    }

    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        onMessageSim(message,sessionId);
    }

    public void onMessageSim(Message message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        String clOrdId = getStringFieldValue(message, ClOrdID.FIELD);
        String origClordId = getStringFieldValue(message, OrigClOrdID.FIELD);
        String symbol = getStringFieldValue(message, Symbol.FIELD);
        String msgType = message.getHeader().getString(35);

        char side = 'X';
        if (message.isSetField(Side.FIELD)) {
            side = message.getChar(Side.FIELD);
        }

        char ordType = '9';
        if (message.isSetField(OrdType.FIELD)) {
            ordType = message.getChar(OrdType.FIELD);
        }

        double price = 0;
        if (ordType == OrdType.LIMIT) {
            price = getDoubleFieldValue(message,44);
        }

        double qty = message.getDouble(OrderQty.FIELD);
        char timeInForce = TimeInForce.DAY;
        if (message.isSetField(TimeInForce.FIELD)) {
            timeInForce = message.getChar(TimeInForce.FIELD);
        }

        try {
            if (timeInForce != TimeInForce.DAY) {
                throw new RuntimeException("Unsupported TIF, use Day");
            }

            Order order = new Order(clOrdId, symbol, senderCompId, targetCompId, side, ordType,
                    price, (int) qty, origClordId);
            order.setOrderId(clOrdId);

            if("D".equals(msgType)) {
                processOrder(order);
            }
            if("G".equals(msgType)) {
                processAmendOrder(message);
            }
            if("F".equals(msgType)) {
                processCancelOrder(message);
            }



        } catch (Exception e) {
            rejectOrder(senderCompId, targetCompId, clOrdId, symbol, side, e.getMessage());
        }
    }

    private void rejectOrder(String senderCompId, String targetCompId, String clOrdId,
                             String symbol, char side, String message) {

        ExecutionReport fixOrder = new ExecutionReport(new OrderID(clOrdId), new ExecID(generator
                .genExecutionID()), new ExecTransType(ExecTransType.NEW), new ExecType(
                ExecType.REJECTED), new OrdStatus(ExecType.REJECTED), new Symbol(symbol), new Side(
                side), new LeavesQty(0), new CumQty(0), new AvgPx(0));

        fixOrder.setString(ClOrdID.FIELD, clOrdId);
        fixOrder.setString(Text.FIELD, message);

        try {
            Session.sendToTarget(fixOrder, senderCompId, targetCompId);
        } catch (SessionNotFound e) {
            e.printStackTrace();
        }
    }

    private void processOrder(Order order) {
        System.out.println("Bid Price ="+MarketDataLoadService.getBidPx(order.getSymbol())+"\n");
        System.out.println("Ask Price ="+MarketDataLoadService.getAskPx(order.getSymbol())+"\n");
        System.out.println("Tick Size ="+MarketDataLoadService.getTick(order.getSymbol())+"\n");
        if (orderMatcher.insert(order)) {
            acceptOrder(order);

            ArrayList<Order> orders = new ArrayList<Order>();
            orderMatcher.match(order.getSymbol(), orders);

            while (orders.size() > 0) {
                fillOrder(orders.remove(0));
            }
            orderMatcher.display(order.getSymbol());
            orderMatcher.displayOrderBook();
        } else {
            rejectOrder(order);
        }
    }

    private void processAmendOrder(Message message) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        //Cancel Existing Order and create new Order in order book
        String symbol = getStringFieldValue(message,Symbol.FIELD);
        String id = getStringFieldValue(message,OrigClOrdID.FIELD);
        String clOrdId = getStringFieldValue(message,ClOrdID.FIELD);
        String origClordId = getStringFieldValue(message,OrigClOrdID.FIELD);
        char side = 'X';
        if (message.isSetField(Side.FIELD)) {
            side = message.getChar(Side.FIELD);
        }

        char ordType = '9';
        if (message.isSetField(OrdType.FIELD)) {
            ordType = message.getChar(OrdType.FIELD);
        }

        int qty = message.getInt(OrderQty.FIELD);

        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);

        Order order = orderMatcher.find(symbol, side, id);
        double price = 0;
        if(null == order) {
            //send cancel reject
            price = getDoubleFieldValue(message,44);
            order = new Order(clOrdId, symbol, senderCompId, targetCompId, side, ordType,
                    price,  qty, origClordId);
            updateOrder(order, '9');
            return;
        }
        int origExecutedQty = (int)order.getExecutedQuantity();
        double origExecutedPx = order.getAvgExecutedPrice();
        double origPx = order.getPrice();
        String orderId = order.getOrderId();

        if (ordType == OrdType.LIMIT) {
            price = getDoubleFieldValue(message,44);

            if(price == -9999) {
                price = origPx;
            }

        }
        order.cancel();
        orderMatcher.erase(order);
        //Create new Order
        clOrdId = getStringFieldValue(message,ClOrdID.FIELD);
        origClordId = getStringFieldValue(message,OrigClOrdID.FIELD);


        if(qty<= origExecutedQty){
            //send cancel reject
            order = new Order(clOrdId, symbol, senderCompId, targetCompId, side, ordType,
                    price,  qty, origClordId);
            updateOrder(order, '9');
            return;
        }

        int openQty = qty - origExecutedQty;

        char timeInForce = TimeInForce.DAY;
        if (message.isSetField(TimeInForce.FIELD)) {
            timeInForce = message.getChar(TimeInForce.FIELD);
        }

        try {
            if (timeInForce != TimeInForce.DAY) {
                throw new RuntimeException("Unsupported TIF, use Day");
            }

            order = new Order(clOrdId, symbol, senderCompId, targetCompId, side, ordType,
                    price,  qty, origClordId);
            order.setOpenQuantity(openQty);
            order.setExecutedQuantity(origExecutedQty);
            order.setAvgExecutedPrice(origExecutedPx);
            order.setOrderId(orderId);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception"+e);
            //rejectOrder(senderCompId, targetCompId, clOrdId, symbol, side, e.getMessage());
        }

        //
        if (orderMatcher.insert(order)) {
            amendOrder(order);

            ArrayList<Order> orders = new ArrayList<Order>();
            orderMatcher.match(order.getSymbol(), orders);

            while (orders.size() > 0) {
                fillOrder(orders.remove(0));
            }
            orderMatcher.display(order.getSymbol());
            orderMatcher.displayOrderBook();
        } else {
            rejectOrder(order);
        }
    }

    private void processCancelOrder(Message message) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {

        String symbol = getStringFieldValue(message,Symbol.FIELD);
        String id = getStringFieldValue(message,OrigClOrdID.FIELD);
        char side = 'X';
        if (message.isSetField(Side.FIELD)) {
            side = message.getChar(Side.FIELD);
        }

        Order order = orderMatcher.find(symbol, side, id);
        int openQty = (int)order.getOpenQuantity();
        if(openQty == 0){
            //Send CancelReject
        }

        String orderId = order.getOrderId();
        order.cancel();
        orderMatcher.erase(order);
        order.setClientOrderId(getStringFieldValue(message,11));
        order.setOrigClientOrderId(getStringFieldValue(message,41));
        order.setOrderId(orderId);
        cancelOrder(order);

    }


    private void rejectOrder(Order order) {
        updateOrder(order, OrdStatus.REJECTED);
    }

    private void cancelRejectOrder(Order order) {
        updateOrder(order, '9');
    }

    private void acceptOrder(Order order) {
        updateOrder(order, OrdStatus.NEW);
    }

    private void amendOrder(Order order) {
        updateOrder(order, OrdStatus.REPLACED);
    }

    private void cancelOrder(Order order) {
        updateOrder(order, OrdStatus.CANCELED);
    }

    private void  updateOrder(Order order, char status) {
        String targetCompId = order.getOwner();
        String senderCompId = order.getTarget();

        ExecutionReport fixOrder = new ExecutionReport(new OrderID(order.getOrderId()),
                new ExecID(generator.genExecutionID()), new ExecTransType(ExecTransType.NEW),
                new ExecType(status), new OrdStatus(status), new Symbol(order.getSymbol()),
                new Side(order.getSide()), new LeavesQty(order.getOpenQuantity()), new CumQty(order
                .getExecutedQuantity()), new AvgPx(order.getAvgExecutedPrice()));

        fixOrder.setString(ClOrdID.FIELD, order.getClientOrderId());
        fixOrder.setDouble(OrderQty.FIELD, order.getQuantity());

        fixOrder.setDouble(LastShares.FIELD, 0);
        fixOrder.setDouble(LastPx.FIELD, 0);
        fixOrder.setChar(40, order.getType());

        if (status == OrdStatus.FILLED || status == OrdStatus.PARTIALLY_FILLED) {
            fixOrder.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            fixOrder.setDouble(LastPx.FIELD, order.getPrice());
        }

        if (status == OrdStatus.CANCELED ) {
            fixOrder.setString(OrigClOrdID.FIELD, order.getOrigClientOrderId());
            //fixOrder.setString(37,new OrderID(order.getOrigClientOrderId());
            fixOrder.setChar(39,'4');
            fixOrder.setChar(150,'4');
            fixOrder.setDouble(151,0);
            fixOrder.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            fixOrder.setDouble(LastPx.FIELD, order.getPrice());
        }

        if (status == OrdStatus.REPLACED ) {
            fixOrder.setString(OrigClOrdID.FIELD, order.getOrigClientOrderId());
            fixOrder.setChar(39,'5');
            fixOrder.setChar(150,'5');
            fixOrder.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            if(order.getLastExecutedQuantity()==0){
                fixOrder.setChar(39,'0');
            }

            if(order.getLastExecutedQuantity()>0){
                fixOrder.setChar(39,'1');
            }

            //fixOrder.setDouble(LastPx.FIELD, order.getPrice());
        }

        if (status == 9 ) {
            String origClordId = order.getOrigClientOrderId();
            String clOrdId = order.getClientOrderId();
            char ordStatus = '1';
            String orderID = order.getOrderId();
            CxlRejResponseTo cxlRejResponseTo = new CxlRejResponseTo('2');

            OrderCancelReject CancelReject = new OrderCancelReject(new OrderID(orderID), new ClOrdID(clOrdId), new OrigClOrdID(origClordId), new OrdStatus(ordStatus), cxlRejResponseTo);
            try {
                Session.sendToTarget(CancelReject, senderCompId, targetCompId);
            } catch (SessionNotFound e) {
            }
            return;
        }

        try {
            Session.sendToTarget(fixOrder, senderCompId, targetCompId);
        } catch (SessionNotFound e) {
        }
    }

    private void fillOrder(Order order) {
        updateOrder(order, order.isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
    }

    public void onMessage(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        String symbol = message.getString(Symbol.FIELD);
        char side = message.getChar(Side.FIELD);
        String id = message.getString(OrigClOrdID.FIELD);
        Order order = orderMatcher.find(symbol, side, id);
        order.cancel();
        cancelOrder(order);
        orderMatcher.erase(order);
    }

    public void onMessage(MarketDataRequest message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        MarketDataRequest.NoRelatedSym noRelatedSyms = new MarketDataRequest.NoRelatedSym();

        //String mdReqId = message.getString(MDReqID.FIELD);
        char subscriptionRequestType = message.getChar(SubscriptionRequestType.FIELD);

        if (subscriptionRequestType != SubscriptionRequestType.SNAPSHOT)
            throw new IncorrectTagValue(SubscriptionRequestType.FIELD);
        //int marketDepth = message.getInt(MarketDepth.FIELD);
        int relatedSymbolCount = message.getInt(NoRelatedSym.FIELD);

        for (int i = 1; i <= relatedSymbolCount; ++i) {
            message.getGroup(i, noRelatedSyms);
            String symbol = noRelatedSyms.getString(Symbol.FIELD);
            System.err.println("*** market data: " + symbol);
        }
    }

    public void onCreate(SessionID sessionId) {
    }

    public void onLogon(SessionID sessionId) {
        System.out.println("Logon - " + sessionId);
    }

    public void onLogout(SessionID sessionId) {
        System.out.println("Logout - " + sessionId);
    }

    public void toAdmin(Message message, SessionID sessionId) {
        // empty
    }

    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        // empty
    }

    public MatchingService orderMatcher() {
        return orderMatcher;
    }

    public String getStringFieldValue(Message message,int field){
        String retVal = "";
        try{
            retVal = message.getString(field);
        }catch(Exception ex){

        }
        return retVal;
    }

    public double getDoubleFieldValue(Message message,int field){
        double retVal = -9999;
        try{
            retVal = message.getDouble(field);
        }catch(Exception ex){

        }
        return retVal;
    }
}
