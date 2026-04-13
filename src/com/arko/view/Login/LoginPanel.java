package com.arko.view.Login;

import com.arko.controller.Login.AuthController;
import com.arko.controller.Login.ForgotPasswordController;
import com.arko.controller.Login.LoginController;
import com.arko.model.DAO.StaffDAO;
import com.arko.view.MainAppShell;
import com.arko.view.UIStyler;

import java.awt.*;
import javax.swing.*;

public class LoginPanel extends JPanel {

    private final MainAppShell appShell;
    private AuthController authController;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JLabel errorLabel;
    private boolean showPassword = false;
    private Image bgImage;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public LoginPanel(MainAppShell appShell) {
        this.appShell = appShell;
        setLayout(new BorderLayout());
        setOpaque(true);
        java.net.URL bgUrl = getClass().getResource("/com/resources/Login-bg.png");
        if (bgUrl != null) bgImage = new ImageIcon(bgUrl).getImage();

        JPanel main = new JPanel(new GridLayout(1, 2));
        main.setOpaque(false);

        // LEFT PANEL - Optimized for Aspect Ratio and Quality
        JPanel left = new JPanel() {
            // Load at a higher resolution for crispness, but we will scale dynamically
            private Image logo = null;

            {
                java.net.URL url = getClass().getResource("/com/resources/Icons/ARKO-with_text.png");
                if (url != null) logo = new ImageIcon(url).getImage();
            }

            //Logo Left Panel
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (logo != null) {
                    Graphics2D g2 = (Graphics2D) g.create();

                    // Quality Hints
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    double scaleFactor = 0.8;

                    int targetWidth = (int) (getWidth() * scaleFactor);
                    double aspectRatio = (double) logo.getHeight(null) / logo.getWidth(null);
                    int targetHeight = (int) (targetWidth * aspectRatio);

                    // Ensure targetHeight doesn't exceed panel height
                    if (targetHeight > getHeight() * 0.9) {
                        targetHeight = (int) (getHeight() * 0.9);
                        targetWidth = (int) (targetHeight / aspectRatio);
                    }

                    // Center the logo
                    int x = (getWidth() - targetWidth) / 2;
                    int y = (getHeight() - targetHeight) / 2;

                    g2.drawImage(logo, x, y, targetWidth, targetHeight, null);

                    g2.dispose();
                }
            }
        };
        left.setOpaque(false);

        // RIGHT PANEL
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);

        // RIGHT PANEL - Card with Rounded Corners
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Define the roundness (30 is a good balance for modern UI)
                int arc = 30;

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }
        };

// Crucial: Set opaque to false so the corners don't show a boxy background
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(520, 480));
        card.setBackground(UIStyler.BG_LIGHT);
        card.setPreferredSize(new Dimension(520, 480));
        card.setBackground(UIStyler.BG_LIGHT);

        JLabel title = new JLabel("Welcome to A.R.K.O!");
        title.setFont(new Font("Inter", Font.BOLD, 30));
        title.setForeground(UIStyler.PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(40, 20, 440, 60);
        card.add(title);

        int fieldX = 50;
        int fieldWidth = 420;
        int fieldHeight = 40;
        int spacing = 20;
        int startY = 130;

        // USERNAME
        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(fieldX, startY - 20, fieldWidth, 20);
        UIStyler.styleFormLabel(userLabel);
        userLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        card.add(userLabel);

        txtUser = new JTextField();
        txtUser.setBounds(fieldX, startY, fieldWidth, fieldHeight);
        UIStyler.styleFormField(txtUser);
        txtUser.setFont(new Font("Inter", Font.PLAIN, 16));
        card.add(txtUser);

        // PASSWORD
        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(fieldX, startY + fieldHeight + spacing, fieldWidth, 20);
        UIStyler.styleFormLabel(passLabel);
        passLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        card.add(passLabel);

        // PASSWORD PANEL
        int eyeBtnWidth = 25;
        JPanel passPanel = new JPanel(null);
        passPanel.setBounds(fieldX, startY + fieldHeight + spacing + 25, fieldWidth + eyeBtnWidth + 5, fieldHeight);
        passPanel.setOpaque(false);

        txtPass = new JPasswordField();
        txtPass.setBounds(0, 0, fieldWidth, fieldHeight);
        UIStyler.styleFormField(txtPass);
        txtPass.setFont(new Font("Inter", Font.PLAIN, 16));
        passPanel.add(txtPass);

        JButton eyeBtn = new JButton();
        eyeBtn.setBounds(fieldWidth + 5, 0, eyeBtnWidth, fieldHeight);
        eyeBtn.setFocusPainted(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ImageIcon eyeOpen   = new ImageIcon(loadImage("/com/resources/Icons/show.png",   24, 24));
        ImageIcon eyeClosed = new ImageIcon(loadImage("/com/resources/Icons/hidden.png", 24, 24));
        eyeBtn.setIcon(eyeClosed);

        char defaultEcho = txtPass.getEchoChar();
        eyeBtn.addActionListener(e -> {
            showPassword = !showPassword;
            txtPass.setEchoChar(showPassword ? (char) 0 : defaultEcho);
            eyeBtn.setIcon(showPassword ? eyeOpen : eyeClosed);
        });

        passPanel.add(eyeBtn);
        card.add(passPanel);

        // FORGOT PASSWORD
        JLabel forgot = new JLabel("Forgot Password?");
        forgot.setBounds(fieldX, startY + 2 * (fieldHeight + spacing) + 5, 200, fieldHeight);
        forgot.setForeground(UIStyler.PRIMARY);
        forgot.setFont(new Font("Inter", Font.PLAIN, 14));
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(forgot);
        forgot.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showForgotPasswordDialog();
            }
        });

        // ERROR LABEL
        errorLabel = new JLabel(" ");
        errorLabel.setBounds(fieldX, startY + 2 * (fieldHeight + spacing) + 50, fieldWidth, 20);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        card.add(errorLabel);

        // LOGIN BUTTON
        btnLogin = new JButton("Login");
        int btnWidth = 140;
        btnLogin.setBounds(fieldX + fieldWidth - btnWidth, card.getPreferredSize().height - fieldHeight - 30, btnWidth, fieldHeight);
        UIStyler.stylePrimaryButton(btnLogin);
        btnLogin.setFont(new Font("Inter", Font.BOLD, 16));
        card.add(btnLogin);

        // Layout
        right.add(card);
        main.add(left);
        main.add(right);
        add(main, BorderLayout.CENTER);

        StaffDAO staffDAO = new StaffDAO();
        this.authController = new AuthController(staffDAO);
        new LoginController(txtUser, txtPass, btnLogin, errorLabel, appShell, this.authController);
    }

    /** Called when returning from a dashboard after logout. */
    public void prepareForReturn() {
        txtUser.setText("");
        txtPass.setText("");
        errorLabel.setText(" ");
    }

    //FORGOT PASSWORD DIALOG
    private void showForgotPasswordDialog() {
        JTextField usernameInput = new JTextField(20);

        Window win = SwingUtilities.getWindowAncestor(this);
        int result = JOptionPane.showConfirmDialog(
                win != null ? win : this,
                new Object[]{ "Enter your username:", usernameInput },
                "Forgot Password",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        ForgotPasswordController controller = new ForgotPasswordController(authController);
        String error = controller.handleReset(usernameInput.getText().trim());

        if (error != null) {
            JOptionPane.showMessageDialog(win != null ? win : this, error, "Reset Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(win != null ? win : this,
                    "A temporary password has been sent to your registered email.\n" +
                            "You will be required to change it on next login.",
                    "Check Your Email",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private Image loadImage(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            Image img = new ImageIcon(url).getImage();
            if (w > 0 && h > 0) img = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return img;
        }
        System.out.println("Image not found: " + path);
        return null;
    }
}
