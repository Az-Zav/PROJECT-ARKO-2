package com.arko.view.AdminDashboard.AccountSettings;

import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AccountSettingsPanel extends JPanel {

    // ── Theme colours ──────────────────────────────────────────────────────────
    private static final Color BG_COLOR = new Color(240, 242, 245);
    private static final Color TEXT_DIM = new Color(110, 117, 125);

    // ── Public data labels — written by AccountSettingsController ──────────────
    public JLabel lblStaffName;
    public JLabel lblStaffIDData;
    public JLabel lblStaffRoleData;
    public JLabel lblStaffEmailData;
    public JLabel lblStaffStationData;
    public JLabel lblStaffContactData;

    // ── Card reference — kept so we can delegate getBtnChangePassword() ────────
    private final SecuritySettingsCard securityCard;

    public AccountSettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        securityCard = new SecuritySettingsCard();

        add(createHeaderPanel(),   BorderLayout.NORTH);
        add(createBodyContainer(), BorderLayout.CENTER);
    }

    // ── Controller access points ───────────────────────────────────────────────
    public JButton getBtnChangePassword() {
        return securityCard.btnChange;
    }

    // ─────────────────────────────────────────────
    // HEADER — name + info grid
    // ─────────────────────────────────────────────
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 40);

        // Name (full width) — public, populated by controller
        lblStaffName = new JLabel("Loading...");
        lblStaffName.setFont(new Font("Inter", Font.BOLD, 30));
        lblStaffName.setForeground(UIStyler.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        header.add(lblStaffName, gbc);

        // ── Row 1: Staff ID | Role ──
        gbc.gridwidth = 1; gbc.gridy = 1;

        lblStaffIDData   = dataLabel();
        lblStaffRoleData = dataLabel();

        gbc.gridx = 0; header.add(tagLabel("Staff ID:"), gbc);
        gbc.gridx = 1; header.add(lblStaffIDData,        gbc);
        gbc.gridx = 2; header.add(tagLabel("Role:"),     gbc);
        gbc.gridx = 3; header.add(lblStaffRoleData,      gbc);

        // ── Row 2: Email | Station ──
        gbc.gridy = 2;

        lblStaffEmailData   = dataLabel();
        lblStaffStationData = dataLabel();

        gbc.gridx = 0; header.add(tagLabel("Email:"),   gbc);
        gbc.gridx = 1; header.add(lblStaffEmailData,    gbc);
        gbc.gridx = 2; header.add(tagLabel("Station:"), gbc);
        gbc.gridx = 3; header.add(lblStaffStationData,  gbc);

        // ── Row 3: Contact No ──
        gbc.gridy = 3;

        lblStaffContactData = dataLabel();

        gbc.gridx = 0; header.add(tagLabel("Contact No:"), gbc);
        gbc.gridx = 1; header.add(lblStaffContactData,     gbc);

        // Spacer
        gbc.gridx = 4; gbc.weightx = 1.0;
        header.add(Box.createHorizontalGlue(), gbc);

        return header;
    }

    // ─────────────────────────────────────────────
    // BODY — section title + SecuritySettingsCard
    // ─────────────────────────────────────────────
    private JPanel createBodyContainer() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(32, 0, 0, 0));

        JLabel lblSection = new JLabel("Security Settings");
        lblSection.setFont(new Font("Inter", Font.PLAIN, 15));
        lblSection.setForeground(UIStyler.PRIMARY);
        lblSection.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 212, 225)),
                new EmptyBorder(0, 0, 18, 0)
        ));
        body.add(lblSection, BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 20));
        wrapper.setOpaque(false);
        wrapper.add(securityCard);
        body.add(wrapper, BorderLayout.CENTER);

        return body;
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    /** Dim tag label — e.g. "Staff ID:" */
    private JLabel tagLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.PLAIN, 15));
        lbl.setForeground(TEXT_DIM);
        return lbl;
    }

    /** Primary-coloured data label — starts as a dash, populated by controller. */
    private JLabel dataLabel() {
        JLabel lbl = new JLabel("—");
        lbl.setFont(new Font("Inter", Font.PLAIN, 15));
        lbl.setForeground(UIStyler.PRIMARY);
        return lbl;
    }
}