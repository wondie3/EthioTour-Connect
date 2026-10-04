# EthioTour Connect

EthioTour Connect is a Java Swing tourism management prototype for Ethiopian travel operations. It manages destinations, tours, bookings, local pricing tiers, booking status, and Ethiopian calendar awareness.

## Current Capabilities

- Manage destinations with region, altitude, description, and entrance protocol details.
- Manage tours with destination, travel dates, participant capacity, current availability, and resident/non-resident pricing.
- Create bookings with customer contact information and participant count.
- Validate bookings before saving so tours cannot be overbooked.
- Track booking statuses: pending confirmation, confirmed, paid, cancelled, and completed.
- Confirm bookings with local payment method/reference fields.
- Process simulated payments.
- Display Ethiopian calendar dates, major holiday notes, and peak tourism season status.
- Run either as a Swing desktop app or as a console demo.

## How To Run

### Option 1: Maven (Recommended)
This project now uses Maven for dependency management and building.

```bash
mvn clean package
java -jar target/ethiotour-connect-1.0-SNAPSHOT.jar
```

### Option 2: Docker Compose (Browser GUI)
You can build and run the application alongside a PostgreSQL database using Docker Compose. The application will be accessible directly from your web browser, removing the need for any local GUI setup or X11 forwarding.

```bash
# Build and start the services in the background
docker-compose up -d

# Access the application
# Open your web browser and navigate to:
http://localhost:5800
```

### Option 3: Standard Batch File
The legacy `run.bat` is still available for simple local runs without Maven.
```bat
run.bat
```

## CI/CD
The project includes a GitHub Actions workflow in `.github/workflows/build.yml` that automatically builds the project and uploads the JAR artifact on every push.

## Project Structure

```text
|-- pom.xml
|-- Dockerfile
|-- .github/workflows/build.yml
|-- README.md
|-- run.bat
|-- .gitignore
|-- doc/
|   |-- project_description.md
|   `-- database_setup.md
`-- src/main/java/com/ethiotour/
    |-- EthioTourApp.java
    |-- DemoApp.java
    |-- controller/
    |   `-- MainController.java
    |-- model/
    |   |-- Destination.java
    |   |-- Tour.java
    |   |-- Booking.java
    |   `-- User.java
    |-- service/
    |   |-- DatabaseService.java
    |   `-- BookingService.java
    |-- util/
    |   `-- EthiopianCalendar.java
    `-- view/
        |-- AppTheme.java
        |-- MainView.java
        |-- DestinationsView.java
        |-- ToursView.java
        |-- BookingsView.java
        `-- CalendarView.java
```

## File Guide

### Root Files

| File | Purpose |
| --- | --- |
| `README.md` | Main project explanation for the team. Describes capabilities, structure, and how to run the app. |
| `run.bat` | Windows launcher. Compiles Java files into `build/classes` and starts the Swing desktop app. |
| `.gitignore` | Keeps generated build files and `.class` files out of version control. |

### Documentation

| File | Purpose |
| --- | --- |
| `doc/project_description.md` | Original project idea and assignment description. This explains the business goal: Ethiopian tourism management with local calendar, pricing, and booking needs. |
| `doc/database_setup.md` | Database integration notes. Includes suggested tables, JDBC connection example, and transaction advice for bookings. |

### Application Entry Points

| File | Purpose |
| --- | --- |
| `EthioTourApp.java` | Main desktop application entry point. It starts `MainView`, which opens the Swing GUI. |
| `DemoApp.java` | Console-based demo. It prints sample destinations, tours, booking validation, booking creation, confirmation, and payment flow. Useful for showing functionality without opening the GUI. |

### Controller

| File | Purpose |
| --- | --- |
| `MainController.java` | Controls navigation between screens. It opens destinations, tours, bookings, and calendar windows, and returns users to the main dashboard. |

### Models

| File | Purpose |
| --- | --- |
| `Destination.java` | Represents a tourist destination. Stores name, description, region, altitude, entrance protocol, active status, and created date. |
| `Tour.java` | Represents a tour package. Stores destination, date range, guide id, max/current participants, pricing, status, and availability helpers. |
| `Booking.java` | Represents a customer booking. Stores customer info, tour id, participant count, total price, payment info, status, booking date, and last updated time. |
| `User.java` | Represents system users such as admin, tour guide, or customer. Currently seeded for future role/login features. |

### Services

| File | Purpose |
| --- | --- |
| `DatabaseService.java` | Temporary in-memory data store. Seeds sample destinations, tours, users, and holds lists of destinations, tours, bookings, and users. |
| `BookingService.java` | Business logic for bookings. Validates tour availability, calculates resident/non-resident price, creates bookings, confirms bookings, processes payment, and cancels bookings. |

### Utilities

| File | Purpose |
| --- | --- |
| `EthiopianCalendar.java` | Utility for simplified Ethiopian/Gregorian date conversion, holiday lookup, peak season detection, and days-until-holiday calculations. |

### Views

| File | Purpose |
| --- | --- |
| `AppTheme.java` | Shared UI styling: colors, fonts, table styling, panel borders, and button styles. Keeps the interface consistent. |
| `MainView.java` | Main dashboard screen. Shows metrics, current Ethiopian/Gregorian date, module buttons, and demo-data status. |
| `DestinationsView.java` | Destination management screen. Shows destination table and add/edit/delete form. |
| `ToursView.java` | Tour management screen. Shows tour table and add/edit/delete form with destination selection, dates, capacity, and pricing. |
| `BookingsView.java` | Booking management screen. Creates bookings, updates booking status, confirms payment reference, processes simulated payment, and cancels bookings. |
| `CalendarView.java` | Ethiopian calendar screen. Shows current converted date, holiday/season info, and date conversion form. |

## Important Notes For The Team

- The app currently stores data in memory. Data resets when the program restarts.
- PostgreSQL is the default persistent database mode. SQLite and in-memory modes remain available for local demos.
- Keep booking validation in `BookingService.java`; do not move business rules into Swing screens.
- Booking creation should use a real database transaction before production use.
- Ethiopian calendar conversion is simplified for prototype/demo purposes.
- Chapa test checkout is available from the bookings screen. Use `Pay with Chapa`, complete the hosted checkout, then use `Verify Chapa Payment` to mark successful transactions as paid.
- The UI is desktop-only because it uses Java Swing.

## Database Integration

For database integration notes, schema, and JDBC examples, see:

```text
doc/database_setup.md
```
