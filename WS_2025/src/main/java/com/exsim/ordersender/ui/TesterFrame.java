package com.exsim.ordersender.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.exsim.ordersender.OrderSender;
import com.exsim.ordersender.OrderSenderApplication;
import com.exsim.ordersender.ExecutionTableModel;
import  com.exsim.ordersender.OrderTableModel;

/**
 * Main application window
 */
public class TesterFrame extends JFrame {

    public TesterFrame(OrderTableModel orderTableModel, ExecutionTableModel executionTableModel,
                       final OrderSenderApplication application) {
        super();
        setTitle("Order Sender");
        setSize(600, 400);

        if (System.getProperties().containsKey("openfix")) {
            createMenuBar(application);
        }
        getContentPane().add(new TesterPanel(orderTableModel, executionTableModel, application),
                BorderLayout.CENTER);
        setVisible(true);
    }

    private void createMenuBar(final OrderSenderApplication application) {
        JMenuBar menubar = new JMenuBar();

        JMenu sessionMenu = new JMenu("Session");
        menubar.add(sessionMenu);

        JMenuItem logonItem = new JMenuItem("Logon");
        logonItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OrderSender.get().logon();
            }
        });
        sessionMenu.add(logonItem);

        JMenuItem logoffItem = new JMenuItem("Logoff");
        logoffItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OrderSender.get().logout();
            }
        });
        sessionMenu.add(logoffItem);

        JMenu appMenu = new JMenu("Application");
        menubar.add(appMenu);

        JMenuItem appAvailableItem = new JCheckBoxMenuItem("Available");
        appAvailableItem.setSelected(application.isAvailable());
        appAvailableItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                application.setAvailable(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        appMenu.add(appAvailableItem);

        JMenuItem sendMissingFieldRejectItem = new JCheckBoxMenuItem("Send Missing Field Reject");
        sendMissingFieldRejectItem.setSelected(application.isMissingField());
        sendMissingFieldRejectItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                application.setMissingField(((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        appMenu.add(sendMissingFieldRejectItem);

        setJMenuBar(menubar);
    }
}
