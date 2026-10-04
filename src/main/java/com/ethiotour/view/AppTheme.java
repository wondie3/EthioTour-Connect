package com.ethiotour.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

final class AppTheme {
    static final Color BACKGROUND = new Color(248, 250, 252);
    static final Color SURFACE = Color.WHITE;
    static final Color TEXT = new Color(15, 23, 42);
    static final Color MUTED_TEXT = new Color(100, 116, 139);
    static final Color PRIMARY = new Color(15, 118, 110); // Teal 700
    static final Color PRIMARY_DARK = new Color(13, 148, 136); // Teal 600
    static final Color PRIMARY_SOFT = new Color(204, 251, 241); // Teal 100
    static final Color ACCENT = new Color(217, 119, 6); // Amber 600
    static final Color BORDER = new Color(226, 232, 240); // Slate 200
    static final Color STRONG_BORDER = new Color(203, 213, 225); // Slate 300
    static final Color DANGER = new Color(225, 29, 72); // Rose 600
    static final Color CARD_BG = Color.WHITE;

    static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 18);
    static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    static void styleWindow(JFrame window) {
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.getContentPane().setBackground(BACKGROUND);
    }
    
    static void styleCard(JPanel panel) {
        panel.setBackground(SURFACE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #ffffff;");
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
    }

    // constructor is private to prevent instantiation
    // This is intended to trigger a compile-time error if someone tries to create an instance of AppTheme, since it is meant to be a utility class with static methods and constants only.
    private AppTheme() {
    }

    static void apply() {
        try {
            FlatLightLaf.setup();
            
            // Custom UI properties for FlatLaf
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Table.selectionBackground", PRIMARY_SOFT);
            UIManager.put("Table.selectionForeground", TEXT);
            
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
            
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
    }

    static void stylePrimaryButton(JButton button) {
        button.setFont(BODY_FONT.deriveFont(Font.BOLD, 13f));
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(spacedBorder(PRIMARY_DARK, 11, 18));
    }

    static void styleSecondaryButton(JButton button) {
        button.setFont(BODY_FONT.deriveFont(Font.BOLD, 13f));
        button.setBackground(new Color(233, 241, 237));
        button.setForeground(PRIMARY_DARK);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(spacedBorder(STRONG_BORDER, 9, 15));
    }

    static void styleDangerButton(JButton button) {
        stylePrimaryButton(button);
        button.setBackground(DANGER);
        button.setBorder(spacedBorder(DANGER.darker(), 10, 18));
    }

    static void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setRowHeight(28);
        table.setGridColor(BORDER);
        table.setSelectionBackground(PRIMARY_DARK);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(BODY_FONT.deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setBackground(new Color(220, 229, 224));
        table.getTableHeader().setForeground(TEXT);
    }

    static void stylePanel(JComponent component) {
        component.setBackground(BACKGROUND);
        component.setFont(BODY_FONT);
    }

    static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT);
        return label;
    }

    static Border panelBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(STRONG_BORDER), title),
            BorderFactory.createEmptyBorder(8, 10, 10, 10)
        );
    }

    private static Border spacedBorder(Color lineColor, int vertical, int horizontal) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(lineColor),
            BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal)
        );
    }
}
