package com.arko.view;

import com.arko.model.POJO.Staff;
import com.arko.utils.Login.StaffRoles;
import com.arko.utils.SessionManager;
import com.arko.view.AdminDashboard.AdminDashboard;
import com.arko.view.Login.LoginPanel;
import com.arko.view.OperationalDashboard.OperationalDashboard;

import javax.swing.*;
import java.awt.*;

/**
 * Single top-level frame: login and role dashboards are {@link CardLayout} cards.
 */
public class MainAppShell extends JFrame {

    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_ADMIN = "ADMIN_HOME";
    public static final String CARD_STAFF = "STAFF_HOME";

    private final JPanel deck;
    private final CardLayout deckLayout;
    private final LoginPanel loginPanel;
    private JComponent adminCard;
    private JComponent staffCard;

    public MainAppShell() {
        setTitle("PROJECT ARKO");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        deckLayout = new CardLayout();
        deck = new JPanel(deckLayout);

        loginPanel = new LoginPanel(this);
        deck.add(loginPanel, CARD_LOGIN);

        add(deck);
        deckLayout.show(deck, CARD_LOGIN);
    }

    /**
     * Uses {@link SessionManager} as the source of truth for role routing.
     */
    public void showAfterLogin() {
        SwingUtilities.invokeLater(() -> {
            Staff staff = SessionManager.getInstance().getCurrentStaff();
            if (staff == null) {
                JOptionPane.showMessageDialog(this,
                        "Session could not be established.",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            removeDashboardCards();

            if (StaffRoles.isAdmin(staff.getRole())) {
                adminCard = new AdminDashboard(this);
                deck.add(adminCard, CARD_ADMIN);
                deckLayout.show(deck, CARD_ADMIN);
            } else {
                staffCard = new OperationalDashboard(this);
                deck.add(staffCard, CARD_STAFF);
                deckLayout.show(deck, CARD_STAFF);
            }
            deck.revalidate();
            deck.repaint();
        });
    }

    public void returnToLogin() {
        SwingUtilities.invokeLater(() -> {
            removeDashboardCards();
            loginPanel.prepareForReturn();
            deckLayout.show(deck, CARD_LOGIN);
            deck.revalidate();
            deck.repaint();
        });
    }

    private void removeDashboardCards() {
        if (adminCard != null) {
            deck.remove(adminCard);
            adminCard = null;
        }
        if (staffCard != null) {
            if (staffCard instanceof OperationalDashboard od) {
                od.shutdown();
            }
            deck.remove(staffCard);
            staffCard = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainAppShell().setVisible(true));
    }
}
