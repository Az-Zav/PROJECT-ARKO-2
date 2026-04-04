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

            /*else {
                new OperationalDashboard(session).setVisible(true);
            }
             */


            Window window = SwingUtilities.getWindowAncestor(caller);
            if (window != null) window.dispose();
        });
    }
}