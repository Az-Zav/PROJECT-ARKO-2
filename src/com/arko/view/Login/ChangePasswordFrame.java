package com.arko.view.Login;

import com.arko.controller.Login.AuthController;
import com.arko.controller.Login.ChangePasswordController;
import com.arko.controller.Login.LoginController;
import com.arko.utils.Login.LoginResult;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog over the main app shell so changing password does not dispose the primary frame.
 */
public class ChangePasswordFrame extends JDialog {

    private final ChangePasswordController controller;

    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel errorLabel;

    private boolean showNewPassword     = false;
    private boolean showConfirmPassword = false;

    public ChangePasswordFrame(Window owner,
                               AuthController authController,
                               LoginResult loginResult,
                               LoginController loginController) {
        super(owner, "A.R.K.O — Change Password", Dialog.ModalityType.APPLICATION_MODAL);
        this.controller = new ChangePasswordController(
                authController, loginResult, loginController);
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(getOwner());
        setResizable(true);
        UIStyler.styleDialogShell(this);
        getContentPane().setBackground(UIStyler.PRIMARY);
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridLayout(1, 2));
        main.setOpaque(false);

        // --- LEFT: Branding ---
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);

        JLabel brand = new JLabel("A.R.K.O", SwingConstants.CENTER);
        brand.setFont(new Font("Inter", Font.BOLD, 48));
        brand.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Automated River-transit & Kommute Operations", SwingConstants.CENTER);
        brandSub.setFont(new Font("Inter", Font.PLAIN, 14));
        brandSub.setForeground(new Color(200, 190, 255));

        JPanel brandStack = new JPanel();
        brandStack.setOpaque(false);
        brandStack.setLayout(new BoxLayout(brandStack, BoxLayout.Y_AXIS));
        brandStack.add(Box.createVerticalGlue());
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandStack.add(brand);
        brandStack.add(Box.createVerticalStrut(10));
        brandStack.add(brandSub);
        brandStack.add(Box.createVerticalGlue());
        left.add(brandStack, BorderLayout.CENTER);

        // --- RIGHT: Form card ---
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(520, 540));
        card.setBackground(UIStyler.BG_LIGHT);
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // -- Heading --
        JLabel heading = new JLabel("Change Your Password");
        heading.setFont(new Font("Inter", Font.BOLD, 28));
        heading.setForeground(UIStyler.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(heading, gbc);

        JLabel subheading = new JLabel("You must set a new password before continuing.");
        subheading.setFont(new Font("Inter", Font.PLAIN, 13));
        subheading.setForeground(Color.GRAY);
        gbc.gridy = 1;
        subheading.setPreferredSize(new Dimension(420, 20)); // Ensure it has height
        gbc.insets = new Insets(0, 0, 30, 0);
        card.add(subheading, gbc);

        // Load icons
        ImageIcon eyeOpen   = loadIcon("/com/resources/Icons/show.png");
        ImageIcon eyeClosed = loadIcon("/com/resources/Icons/hidden.png");

        int fieldWidth  = 420;
        int fieldHeight = 40;

        // -- New Password label --
        JLabel newPassLabel = new JLabel("New Password");
        UIStyler.styleFormLabel(newPassLabel);
        newPassLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(newPassLabel, gbc);

        newPasswordField = new JPasswordField();
        UIStyler.styleFormField(newPasswordField);
        newPasswordField.setFont(new Font("Inter", Font.PLAIN, 16));
        newPasswordField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        char newEcho = newPasswordField.getEchoChar();

        JButton eyeNew = new JButton();
        eyeNew.setIcon(eyeClosed);
        eyeNew.setFocusPainted(false);
        eyeNew.setBorderPainted(false);
        eyeNew.setContentAreaFilled(false);
        eyeNew.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeNew.addActionListener(e -> {
            showNewPassword = !showNewPassword;
            newPasswordField.setEchoChar(showNewPassword ? (char) 0 : newEcho);
            eyeNew.setIcon(showNewPassword ? eyeOpen : eyeClosed);
        });

        // FIXED: Using BorderLayout instead of null layout to prevent height collapse
        JPanel newPassRow = new JPanel(new BorderLayout(5, 0));
        newPassRow.setOpaque(false);
        newPassRow.add(newPasswordField, BorderLayout.CENTER);
        newPassRow.add(eyeNew, BorderLayout.EAST);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(newPassRow, gbc);

        // -- Confirm Password label --
        JLabel confirmLabel = new JLabel("Confirm Password");
        UIStyler.styleFormLabel(confirmLabel);
        confirmLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(confirmLabel, gbc);

        confirmPasswordField = new JPasswordField();
        UIStyler.styleFormField(confirmPasswordField);
        confirmPasswordField.setFont(new Font("Inter", Font.PLAIN, 16));
        confirmPasswordField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        char confirmEcho = confirmPasswordField.getEchoChar();

        JButton eyeConfirm = new JButton();
        eyeConfirm.setIcon(eyeClosed);
        eyeConfirm.setFocusPainted(false);
        eyeConfirm.setBorderPainted(false);
        eyeConfirm.setContentAreaFilled(false);
        eyeConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeConfirm.addActionListener(e -> {
            showConfirmPassword = !showConfirmPassword;
            confirmPasswordField.setEchoChar(showConfirmPassword ? (char) 0 : confirmEcho);
            eyeConfirm.setIcon(showConfirmPassword ? eyeOpen : eyeClosed);
        });

        // FIXED: Using BorderLayout instead of null layout
        JPanel confirmPassRow = new JPanel(new BorderLayout(5, 0));
        confirmPassRow.setOpaque(false);
        confirmPassRow.add(confirmPasswordField, BorderLayout.CENTER);
        confirmPassRow.add(eyeConfirm, BorderLayout.EAST);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(confirmPassRow, gbc);

        // -- Error label --
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(200, 30, 30));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(errorLabel, gbc);

        // -- Save button --
        JButton saveBtn = new JButton("Save New Password");
        saveBtn.setPreferredSize(new Dimension(fieldWidth, 44));
        UIStyler.stylePrimaryButton(saveBtn);
        saveBtn.setFont(new Font("Inter", Font.BOLD, 15));
        saveBtn.addActionListener(e -> onSaveClicked());
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(saveBtn, gbc);

        right.add(card);
        main.add(left);
        main.add(right);
        add(main, BorderLayout.CENTER);
    }

    private void onSaveClicked() {
        String newPass     = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        String error = controller.validate(newPass, confirmPass);
        if (error != null) {
            errorLabel.setText(error);
            return;
        }

        boolean saved = controller.saveNewPassword(newPass);
        if (!saved) {
            errorLabel.setText("Failed to save password. Please try again.");
            return;
        }

        controller.routeAfterChange((JComponent) getContentPane());
        dispose();
    }

    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        System.out.println("Icon not found: " + path);
        return null;
    }
}