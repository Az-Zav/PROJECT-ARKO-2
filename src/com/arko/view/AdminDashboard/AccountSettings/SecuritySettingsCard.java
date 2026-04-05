package com.arko.view.AdminDashboard.AccountSettings;

import com.arko.view.ModernCard;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SecuritySettingsCard extends ModernCard {

    // Public so AccountSettingsPanel (and therefore the controller) can wire it
    public JButton btnChange;

    public SecuritySettingsCard() {
        super("Security Settings");
        setPreferredSize(new Dimension(490, 115));
        buildContent();
    }

    private void buildContent() {
        // ── Top separator ──────────────────────────────
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setForeground(new Color(224, 226, 235));
        sep.setBackground(new Color(224, 226, 235));
        container.add(sep, BorderLayout.NORTH);

        // ── Content row ────────────────────────────────
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 4, 8, 4));
        GridBagConstraints gbc = new GridBagConstraints();

        // Lock icon
        JLabel lockIcon = new JLabel("\uD83D\uDD12"); // 🔒
        lockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lockIcon.setPreferredSize(new Dimension(36, 36));
        lockIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lockIcon.setVerticalAlignment(SwingConstants.CENTER);
        gbc.gridx   = 0;
        gbc.gridy   = 0;
        gbc.weightx = 0;
        gbc.anchor  = GridBagConstraints.CENTER;
        gbc.insets  = new Insets(0, 0, 0, 14);
        row.add(lockIcon, gbc);

        // "Password" label
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Inter", Font.BOLD, 14));
        lblPass.setForeground(UIStyler.PRIMARY);
        gbc.gridx   = 1;
        gbc.weightx = 1.0;
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.insets  = new Insets(0, 0, 0, 0);
        row.add(lblPass, gbc);

        // "Change Password" button — amber fill, wired by the controller
        btnChange = new JButton("Change Password");
        UIStyler.styleButton(
                btnChange,
                new Color(0xFFB84C),
                new Color(0xFFA526),
                UIStyler.PRIMARY,
                new Color(0xC0C0C0),
                Color.WHITE
        );
        btnChange.setPreferredSize(new Dimension(148, 33));
        gbc.gridx   = 2;
        gbc.weightx = 0;
        gbc.anchor  = GridBagConstraints.EAST;
        gbc.insets  = new Insets(0, 12, 0, 0);
        row.add(btnChange, gbc);

        container.add(row, BorderLayout.CENTER);
    }
}