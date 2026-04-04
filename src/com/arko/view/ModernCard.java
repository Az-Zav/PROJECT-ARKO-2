package com.arko.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModernCard extends JPanel {

    // THE SAFE ZONE: Subclasses will add their UI components HERE, not directly to 'this'
    protected JPanel container;

    public ModernCard(String title) {
        setOpaque(false); // Essential so the rounded corners can be drawn smoothly
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 20, 15, 20)); // Padding inside the card

        // 1. The Title Header
        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Inter", Font.BOLD, 12));
        lblTitle.setForeground(new Color(0, 102, 204)); // Project Arko Accent Blue
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0)); // Spacing below the title
        add(lblTitle, BorderLayout.NORTH);

        // 2. The Content Container
        container = new JPanel(new BorderLayout());
        container.setOpaque(false); // Let the white background shine through
        add(container, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // We do not call super.paintComponent(g) here because we are completely redrawing the panel
        Graphics2D g2 = (Graphics2D) g.create();

        // Turn on Anti-Aliasing for smooth, non-pixelated corners
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the pure white rounded background
        g2.setColor(new Color(250, 250, 250));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

        // Draw the thin, modern gray-blue border
        g2.setColor(new Color(224, 226, 235));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

        g2.dispose();
    }
}