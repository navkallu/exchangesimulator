package com.exsim.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.exsim.service.MatchingService;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

public class SimulatorMain {
    public static void main(String[] args) {
        try {
            InputStream inputStream = null;
            if (args.length == 0) {
                inputStream = MatchingService.class.getResourceAsStream("exsim.cfg");
            } else if (args.length == 1) {
                inputStream = new FileInputStream(args[0]);
            }
            if (inputStream == null) {
                System.out.println("usage: " + MatchingService.class.getName() + " [configFile].");
                return;
            }
            SessionSettings settings = new SessionSettings(inputStream);

            ExSimApplication application = new ExSimApplication();
            FileStoreFactory storeFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new ScreenLogFactory(settings);
            SocketAcceptor acceptor = new SocketAcceptor(application, storeFactory, settings,
                    logFactory, new DefaultMessageFactory());

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            acceptor.start();
            while (true) {
                System.out.println("type #quit to quit");
                String value = in.readLine();
                if (value != null) {
                    if (value.equals("#symbols")) {
                        application.orderMatcher().display();
                    } else if (value.equals("#quit")) {
                        break;
                    } else {
                        application.orderMatcher().display();
                    }
                }
            }
            acceptor.stop();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
