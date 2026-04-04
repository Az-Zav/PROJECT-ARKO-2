package com.arko.view.Login;

import com.arko.utils.Login.UserSession;
import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {

    public MainDashboard(UserSession session) {
        setTitle("A.R.K.O System — " + session.getRole());
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel placeholder = new JLabel(
                "Logged in as: " + session.getFullName() + " (" + session.getRole() + ")",
                SwingConstants.CENTER
        );
        placeholder.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(placeholder, BorderLayout.CENTER);
    }
}