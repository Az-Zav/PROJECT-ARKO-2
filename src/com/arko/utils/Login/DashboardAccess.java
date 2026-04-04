package com.arko.utils.Login;

import com.arko.view.Login.MainDashboard;
import javax.swing.*;
import java.awt.*;

public class DashboardAccess {

    public static void route(UserSession session, JComponent caller) {
        SwingUtilities.invokeLater(() -> {
            MainDashboard dashboard = new MainDashboard(session);
            dashboard.setVisible(true);

            Window window = SwingUtilities.getWindowAncestor(caller);
            if (window != null) window.dispose();
        });
    }
}