package com.ethiotour.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ethiotour.controller.MainController;
import com.ethiotour.model.Tour;
import com.ethiotour.service.DatabaseServiceFactory;
import com.ethiotour.service.IDatabaseService;
import com.ethiotour.util.EthiopianCalendar;
import com.formdev.flatlaf.FlatClientProperties;

public class MainView extends JFrame {
    private MainController controller;
    private JPanel mainPanel;
    private JLabel headingLabel;
    private JLabel summaryLabel;
    private JLabel ethiopianDateLabel;
    private JLabel gregorianDateLabel;
    private JButton destinationsButton;
    private JButton toursButton;
    private JButton bookingsButton;
    private JButton calendarButton;
    private IDatabaseService dbService;

    public MainView() {
        this.controller = new MainController(this);
        this.dbService = DatabaseServiceFactory.getDatabaseService();
        initializeComponents();
        setupEventHandlers();
        updateDateTimeLabels();
    }

    private void initializeComponents() {
        // Heading
        setTitle("EthioTour Connect - Tourism Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AppTheme.styleWindow(this);

        // Main panel
        mainPanel = new JPanel();
        mainPanel.setBackground(AppTheme.BACKGROUND);
        mainPanel.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(16, 8));
        headerPanel.setBackground(AppTheme.PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(28, 34, 26, 34));

        // Heading and summary
        headingLabel = new JLabel("EthioTour Connect");
        headingLabel.setFont(AppTheme.TITLE_FONT);
        headingLabel.setForeground(Color.WHITE);

        // Summary
        summaryLabel = new JLabel(
                "Operations dashboard for destinations, tours, bookings, and Ethiopian calendar planning.");
        summaryLabel.setFont(AppTheme.BODY_FONT);
        summaryLabel.setForeground(new Color(229, 238, 233));

        // Ethiopian and Gregorian dates
        ethiopianDateLabel = new JLabel("", SwingConstants.CENTER);
        ethiopianDateLabel.setFont(AppTheme.BODY_FONT.deriveFont(Font.BOLD));
        ethiopianDateLabel.setForeground(Color.WHITE);

        // Gregorian date in smaller font
        gregorianDateLabel = new JLabel("", SwingConstants.CENTER);
        gregorianDateLabel.setFont(AppTheme.SMALL_FONT);
        gregorianDateLabel.setForeground(new Color(218, 230, 224));

        // Title and date. Like a mini header within the header panel
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setOpaque(false);
        titlePanel.add(headingLabel);
        titlePanel.add(summaryLabel);

        // Date panel on the right side of the header
        JPanel datePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        datePanel.setOpaque(false);
        datePanel.add(ethiopianDateLabel);
        datePanel.add(gregorianDateLabel);

        // Assemble header which has the title/summary on the left and the date on the right
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(datePanel, BorderLayout.EAST);

        // Content panel with stats and navigation
        JPanel contentPanel = new JPanel(new BorderLayout(22, 22));
        contentPanel.setBackground(AppTheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(26, 34, 26, 34));

        // Stats panel with 4 metrics: Destinations, Tours, Bookings, Open Slots
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 14, 14));
        statsPanel.setBackground(AppTheme.BACKGROUND);
        statsPanel.add(createMetricPanel("Destinations", String.valueOf(dbService.getAllDestinations().size()),
                "Regional metadata and protocols"));
        statsPanel.add(createMetricPanel("Tours", String.valueOf(dbService.getAllTours().size()),
                "Capacity, dates, and pricing"));
        statsPanel.add(createMetricPanel("Bookings", String.valueOf(dbService.getAllBookings().size()),
                "Pending confirmations and payments"));
        statsPanel.add(createMetricPanel("Open Slots", String.valueOf(countOpenSlots()), "Available tour capacity"));

        // Navigation panel with buttons for each module
        JPanel navigationPanel = new JPanel(new GridLayout(2, 2, 16, 16));
        navigationPanel.setBackground(AppTheme.BACKGROUND);

        // Navigation buttons with descriptions
        destinationsButton = createModuleButton("Destinations",
                "Maintain sites, regions, altitude notes, and entrance rules.");
        toursButton = createModuleButton("Tours",
                "Build itineraries with availability, resident pricing, and non-resident pricing.");
        bookingsButton = createModuleButton("Bookings",
                "Create reservations, confirm local payments, and update booking status.");
        calendarButton = createModuleButton("Calendar", "Review Ethiopian dates, holidays, and peak travel seasons.");


        // Assemble navigation panel
        navigationPanel.add(destinationsButton);
        navigationPanel.add(toursButton);
        navigationPanel.add(bookingsButton);
        navigationPanel.add(calendarButton);
        
        // Center panel to hold stats and navigation with some spacing
        JPanel centerPanel = new JPanel(new BorderLayout(0, 18));
        centerPanel.setBackground(AppTheme.BACKGROUND);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(navigationPanel, BorderLayout.CENTER);
        
        // Bottom panel for status messages or quick actions (currently empty but styled)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(AppTheme.BACKGROUND);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        bottomPanel.add(createStatusPanel(), BorderLayout.CENTER);

        // Assemble content panel. The content panel is the main area of the dashboard, it has the stats at the top and the navigation buttons in the center. The bottom panel is reserved for status messages or quick actions.
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Assemble main panel with header and content
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    // Set up event handlers for the navigation buttons to call the appropriate methods in the controller to show the respective views.
    private void setupEventHandlers() {
        // Setting up an event listener for Destinations button to show the Destinations view when clicked
        destinationsButton.addActionListener(e -> controller.showDestinationsView());
        // Setting up an event listener for Tours button to show the Tours view when clicked
        toursButton.addActionListener(e -> controller.showToursView());
        // Setting up an event listener for Bookings button to show the Bookings view when clicked
        bookingsButton.addActionListener(e -> controller.showBookingsView());
        // Setting up an event listener for Calendar button to show the Calendar view when clicked
        calendarButton.addActionListener(e -> controller.showCalendarView());
    }

    // This method updates the Ethiopian and Gregorian date labels with the current date. It also checks if today is an Ethiopian holiday and appends the holiday name to the Ethiopian date label if it is.
    private void updateDateTimeLabels() {
        LocalDate today = LocalDate.now();
        gregorianDateLabel.setText("Gregorian: " + today.toString());
        ethiopianDateLabel.setText("Ethiopian: " + EthiopianCalendar.getEthiopianDateDisplay(today));

        // Check for holidays
        if (EthiopianCalendar.isEthiopianHoliday(today)) {
            String holidayName = EthiopianCalendar.getHolidayName(today);
            ethiopianDateLabel.setText(ethiopianDateLabel.getText() + " - " + holidayName + " Holiday!");
        }
    }

    // This method creates a panel for displaying a single metric (like number of destinations, tours, etc.) with a label, value, and caption. It styles the panel with FlatLaf properties and returns it to be added to the stats panel.
    private JPanel createMetricPanel(String label, String value, String caption) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.SURFACE);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #ffffff;");
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));

        // Create components for the metric panel. The metric panel is designed to show a key performance indicator (KPI) such as the number of destinations, tours, bookings, or open slots. It has a large value at the top, a label below it, and a caption that provides additional context about what the metric represents.
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 32));
        valueLabel.setForeground(AppTheme.PRIMARY);

        // Create a panel to hold the label and caption. The label is styled to be bold and slightly larger than the caption, which is styled with a smaller font and muted text color to indicate that it is supplementary information.
        // This is where the label and caption are created and styled. The label is the main descriptor of the metric (e.g., "Destinations") and the caption provides additional context (e.g., "Regional metadata and protocols"). Both are added to a text panel that is placed below the value in the metric panel.
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Inter", Font.BOLD, 15));
        labelComponent.setForeground(AppTheme.TEXT);

        // The caption is created with a smaller font and a muted text color to indicate that it is supplementary information about the metric. 
        // It is added below the label in the text panel, which is then added to the center of the metric panel.
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(AppTheme.SMALL_FONT);
        captionLabel.setForeground(AppTheme.MUTED_TEXT);

        // Create a panel to hold the label and caption, and add them to the metric panel. The text panel uses a GridLayout to stack the label and caption vertically with some spacing between them. The text panel is set to be transparent (opaque false) so that the background of the metric panel shows through.
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        textPanel.add(labelComponent);
        textPanel.add(captionLabel);

        panel.add(valueLabel, BorderLayout.NORTH);
        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    // This method creates a module button with a title and description. It styles the button with FlatLaf properties and returns it to be added to the UI.
    private JButton createModuleButton(String title, String description) {
        // The module button is designed to navigate to different sections of the application (e.g., Destinations, Tours, Bookings, Calendar). Each button has a title and a description that provides more context about what the module does. The button is styled with a primary background color, white text, and rounded corners using FlatLaf properties. It also has a hover effect that changes the background color when the mouse is over it.
        String text = "<html><div style='font-family:Inter; padding-left:4px;'>"
                + "<div style='font-size:16px; font-weight:bold; color:#ffffff;'>" + title + "</div>"
                + "<div style='font-size:11px; font-weight:normal; color:#ccfbf1; margin-top:4px;'>" + description
                + "</div>"
                + "</div></html>";

        // Create the button with the styled text and apply FlatLaf styling. 
        // The button is created with HTML content to allow for multi-line text and custom styling of the title and description. 
        // The button's background color is set to the primary color defined in the AppTheme, and the text color is set to white. The button also has a hand cursor to indicate that it is clickable, and the focus painting is disabled for a cleaner look.
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(AppTheme.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // FlatLaf styling
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, AppTheme.ACCENT),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));

        addHoverState(button);
        return button;
    }

    // This method creates a status panel that can be used to display status messages or quick actions. 
    // It styles the panel with a light background color and a border, and returns it to be added to the bottom of the content panel.
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(new Color(237, 242, 238));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        return panel;
    }

    // This method adds a mouse listener to a button to change its background color when the mouse is hovered over it. 
    // When the mouse enters the button area, the background color changes to a darker shade of the primary color. When the mouse exits, it reverts back to the original primary color. This provides a visual feedback to the user that the button is interactive.
    private void addHoverState(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(AppTheme.PRIMARY_DARK);
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(AppTheme.PRIMARY);
            }
        });
    }

    private int countOpenSlots() {
        int total = 0;
        List<Tour> tours = dbService.getAllTours();
        for (Tour tour : tours) {
            total += Math.max(0, tour.getAvailableSlots());
        }
        return total;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppTheme.apply();

            MainView mainView = new MainView();
            mainView.setVisible(true);
        });
    }
}
