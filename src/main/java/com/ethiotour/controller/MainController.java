package com.ethiotour.controller;

//importing the packages required for the controller to function properly

// imports for the views that the controller will manage
import com.ethiotour.view.BookingsView;
import com.ethiotour.view.CalendarView;
import com.ethiotour.view.DestinationsView;
import com.ethiotour.view.MainView;
import com.ethiotour.view.ToursView;
import javax.swing.JOptionPane;

public class MainController {
    private MainView mainView;
    private DestinationsView destinationsView;
    private ToursView toursView;
    private BookingsView bookingsView;
    private CalendarView calendarView;

    public MainController(MainView mainView) {
        this.mainView = mainView;
    }

    public void showDestinationsView() {
        if (destinationsView == null) {
            destinationsView = new DestinationsView(this);
        }
        mainView.setVisible(false);
        destinationsView.setVisible(true);
    }

    public void showToursView() {
        if (toursView == null) {
            toursView = new ToursView(this);
        }
        mainView.setVisible(false);
        toursView.setVisible(true);
    }

    public void showBookingsView() {
        if (bookingsView == null) {
            bookingsView = new BookingsView(this);
        }
        mainView.setVisible(false);
        bookingsView.setVisible(true);
    }

    public void showCalendarView() {
        if (calendarView == null) {
            calendarView = new CalendarView(this);
        }
        mainView.setVisible(false);
        calendarView.setVisible(true);
    }

    public void returnToMain() {
        mainView.setVisible(true);
        if (destinationsView != null)
            destinationsView.setVisible(false);
        if (toursView != null)
            toursView.setVisible(false);
        if (bookingsView != null)
            bookingsView.setVisible(false);
        if (calendarView != null)
            calendarView.setVisible(false);
    }
}
