package com.ethiotour.view;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

public class AdminLoginView extends JFrame {
    private static final String ADMIN_USERNAME = "admin";
    private static final char[] ADMIN_PASSWORD = "admin123".toCharArray();
    private static final String[] BACKGROUND_IMAGES = {
            "/images/addis.png",
            "/images/bunna.png",
            "/images/gondar.png",
            "/images/hamer_woman.png",
            "/images/lalibela.png",
            "/images/semien_mount.png"
    };
    private static final int ANIMATION_DELAY_MS = 25;
    private static final int FRAMES_PER_IMAGE = 80;
    private static final int FLIP_START_FRAME = 50;
    private static final int FLIP_FRAME_COUNT = FRAMES_PER_IMAGE - FLIP_START_FRAME;

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final AnimatedBackgroundPanel backgroundPanel = new AnimatedBackgroundPanel();

    public AdminLoginView() {
        initializeComponents();
    }

    private void initializeComponents() {
        setTitle("EthioTour Connect - Admin Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AppTheme.styleWindow(this);
        setMinimumSize(new Dimension(920, 620));

        backgroundPanel.setLayout(new GridBagLayout());
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JPanel loginPanel = createLoginPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        backgroundPanel.add(loginPanel, gbc);

        add(backgroundPanel, BorderLayout.CENTER);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new RoundedPanel(28, new Color(255, 255, 255, 232));
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(34, 38, 34, 38));
        panel.setPreferredSize(new Dimension(390, 430));

        JLabel title = new JLabel("Admin Login", SwingConstants.CENTER);
        title.setFont(AppTheme.TITLE_FONT.deriveFont(Font.BOLD, 30f));
        title.setForeground(AppTheme.PRIMARY);

        JLabel subtitle = new JLabel("EthioTour Connect", SwingConstants.CENTER);
        subtitle.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD, 14f));
        subtitle.setForeground(AppTheme.MUTED_TEXT);

        JLabel usernameLabel = fieldLabel("Username");
        JLabel passwordLabel = fieldLabel("Password");

        styleTextField(usernameField);
        styleTextField(passwordField);
        usernameField.setText(ADMIN_USERNAME);
        passwordField.addActionListener(this::login);

        JButton loginButton = new JButton("Sign In");
        AppTheme.stylePrimaryButton(loginButton);
        loginButton.addActionListener(this::login);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(subtitle, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(usernameLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(usernameField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(passwordLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 28, 0);
        panel.add(passwordField, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(loginButton, gbc);

        return panel;
    }

    private static final class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color fillColor;

        private RoundedPanel(int cornerRadius, Color fillColor) {
            this.cornerRadius = cornerRadius;
            this.fillColor = fillColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.setColor(new Color(255, 255, 255, 190));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD, 13f));
        label.setForeground(AppTheme.TEXT);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(AppTheme.BODY_FONT.deriveFont(15f));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.STRONG_BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
    }

    private void login(ActionEvent event) {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        boolean valid = ADMIN_USERNAME.equals(username) && matchesPassword(password);

        if (valid) {
            backgroundPanel.stopAnimation();
            dispose();
            MainView mainView = new MainView();
            mainView.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid admin username or password.",
                    "Login failed",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }

    private boolean matchesPassword(char[] password) {
        if (password.length != ADMIN_PASSWORD.length) {
            return false;
        }
        for (int i = 0; i < ADMIN_PASSWORD.length; i++) {
            if (password[i] != ADMIN_PASSWORD[i]) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppTheme.apply();

            AdminLoginView loginView = new AdminLoginView();
            loginView.setVisible(true);
        });
    }

    private static final class AnimatedBackgroundPanel extends JPanel {
        private final List<BufferedImage> images = new ArrayList<>();
        private final Timer timer;
        private int currentIndex = 0;
        private int nextIndex = 1;
        private int frame = 0;

        private AnimatedBackgroundPanel() {
            setOpaque(true);
            loadImages();
            timer = new Timer(ANIMATION_DELAY_MS, e -> advanceAnimation());
            timer.start();
        }

        private void loadImages() {
            for (String imagePath : BACKGROUND_IMAGES) {
                URL imageUrl = AdminLoginView.class.getResource(imagePath);
                if (imageUrl == null) {
                    continue;
                }
                try {
                    images.add(ImageIO.read(imageUrl));
                } catch (IOException ignored) {
                    // Missing one decorative background should not prevent login.
                }
            }
        }

        private void advanceAnimation() {
            if (images.size() < 2) {
                repaint();
                return;
            }

            frame++;
            if (frame > FRAMES_PER_IMAGE) {
                frame = 0;
                currentIndex = nextIndex;
                nextIndex = (nextIndex + 1) % images.size();
            }
            repaint();
        }

        private void stopAnimation() {
            timer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (images.isEmpty()) {
                g2.setColor(AppTheme.PRIMARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                float progress = images.size() > 1
                        ? Math.max(0f, (frame - FLIP_START_FRAME) / (float) FLIP_FRAME_COUNT)
                        : 0f;
                if (progress > 0f) {
                    float oldScale = Math.max(0.04f, 1f - progress);
                    float newScale = Math.max(0.04f, progress);
                    paintImage(g2, images.get(currentIndex), oldScale, 1f - progress);
                    paintImage(g2, images.get(nextIndex), newScale, progress);
                } else {
                    paintImage(g2, images.get(currentIndex), 1f, 1f);
                }
            }

            g2.setComposite(AlphaComposite.SrcOver.derive(0.48f));
            g2.setColor(new Color(5, 32, 34));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setComposite(AlphaComposite.SrcOver.derive(0.22f));
            g2.setColor(AppTheme.ACCENT);
            g2.fillRect(0, getHeight() - 120, getWidth(), 120);

            g2.dispose();
        }

        private void paintImage(Graphics2D g2, BufferedImage image, float scaleX, float alpha) {
            if (alpha <= 0f) {
                return;
            }

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            double imageRatio = image.getWidth() / (double) image.getHeight();
            double panelRatio = panelWidth / (double) panelHeight;

            int drawWidth;
            int drawHeight;
            if (imageRatio > panelRatio) {
                drawHeight = panelHeight;
                drawWidth = (int) Math.ceil(panelHeight * imageRatio);
            } else {
                drawWidth = panelWidth;
                drawHeight = (int) Math.ceil(panelWidth / imageRatio);
            }

            int scaledWidth = Math.max(1, Math.round(drawWidth * scaleX));
            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - drawHeight) / 2;

            g2.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f, alpha)));
            g2.drawImage(image, x, y, scaledWidth, drawHeight, (Component) null);
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }
}
