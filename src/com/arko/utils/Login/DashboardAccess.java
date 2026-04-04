package com.arko.utils.Login;

import com.arko.view.AdminDashboard.AdminDashboard;
import com.arko.view.OperationalDashboard.OperationalDashboard;

import javax.swing.*;
import java.awt.*;

public class DashboardAccess {

    public static void route(UserSession session, JComponent caller) {
        SwingUtilities.invokeLater(() -> {
            if (session.isAdmin()) {
                new AdminDashboard(session).setVisible(true);
            }

            else if (session.isStaff()) {
                new OperationalDashboard(session).setVisible(true);
            }
            else {
                // Handle other roles or show an "Access Denied" message
                JOptionPane.showMessageDialog(caller,
                        "Your role (" + session.getRole() + ") does not have a designated dashboard.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }



            Window window = SwingUtilities.getWindowAncestor(caller);
            if (window != null) window.dispose();
        });
    }
}