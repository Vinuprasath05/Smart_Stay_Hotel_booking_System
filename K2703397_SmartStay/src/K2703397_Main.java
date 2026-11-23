import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Console user interface for the SmartStay Hotel Booking System. */
public class K2703397_Main {
    private static final Scanner in = new Scanner(System.in);
    private static final K2703397_HotelSystem system = new K2703397_HotelSystem();

    /** Loads the sample data and shows the main menu until the user exits. */
    public static void main(String[] args) {
        system.loadSampleData();
        while (true) {
            System.out.println("\n===== SMARTSTAY HOTEL BOOKING SYSTEM (K2703397) =====");
            System.out.println("1. Admin login");
            System.out.println("2. Guest: register");
            System.out.println("3. Guest: login (NIC/Passport)");
            System.out.println("0. Exit");
            switch (ask("Choose: ")) {
                case "1": adminLogin(); break;
                case "2": registerGuest(); break;
                case "3": guestLogin(); break;
                case "0": System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    // --------------------------------------------------------------- helpers
    private static String ask(String prompt) { System.out.print(prompt); return in.nextLine().trim(); }

    /** Asks again until the user types a valid yyyy-mm-dd date. */
    private static LocalDate askDate(String prompt) {
        while (true) {
            try { return LocalDate.parse(ask(prompt + " (yyyy-mm-dd): ")); }
            catch (DateTimeParseException e) { System.out.println("Invalid date format."); }
        }
    }

    /** Asks again until the user types a whole number. */
    private static int askInt(String prompt) {
        while (true) {
            try { return Integer.parseInt(ask(prompt)); }
            catch (NumberFormatException e) { System.out.println("Please enter a whole number."); }
        }
    }

    /** Prints a table of rooms, or a message when the list is empty. */
    private static void printRooms(List<K2703397_Room> rooms) {
        if (rooms.isEmpty()) { System.out.println("No rooms found."); return; }
        System.out.println(K2703397_HotelSystem.ROOM_HEADER);
        rooms.forEach(System.out::println);
    }

    // ----------------------------------------------------------------- admin
    private static void adminLogin() {
        K2703397_Admin admin = system.loginAdmin(ask("Admin ID: "), ask("Password: "));
        if (admin == null) { System.out.println("Wrong ID or password."); return; }
        System.out.println("Welcome, " + admin.getFullName() + " (" + admin.getRole() + ")");
        while (true) {
            System.out.println("\n--- ADMIN MENU ---\n1. Add room\n2. Update room (bed type)\n3. Remove room"
                    + "\n4. Manage availability\n5. View all rooms\n0. Logout");
            try {
                switch (ask("Choose: ")) {
                    case "1":
                        String type = ask("Room type (Standard/Deluxe/Suite/Family Suite): ");
                        String id = ask("Room ID: ");
                        String bed = ask("Bed type (Single/Double/Queen/King): ");
                        K2703397_HotelSystem.validateBedType(bed);
                        admin.addRoom(system, system.createRoom(type, id, bed));
                        System.out.println("Room added."); break;
                    case "2":
                        admin.updateRoom(system, ask("Room ID: "), ask("New bed type: "));
                        System.out.println("Room updated."); break;
                    case "3":
                        admin.removeRoom(system, ask("Room ID: "));
                        System.out.println("Room removed."); break;
                    case "4":
                        String rid = ask("Room ID: ");
                        System.out.println("1. Available  2. Reserved  3. Under Maintenance");
                        K2703397_RoomStatus st = K2703397_RoomStatus.values()[askInt("Status: ") - 1];
                        admin.manageAvailability(system, rid, st);
                        System.out.println("Status updated."); break;
                    case "5": admin.viewAllRooms(system); break;
                    case "0": return;
                    default: System.out.println("Invalid option.");
                }
            } catch (K2703397_BookingException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Error: invalid status choice.");
            }
        }
    }

    // ----------------------------------------------------------------- guest
    private static void registerGuest() {
        K2703397_Guest g = new K2703397_Guest(ask("NIC/Passport: "), ask("Full name: "),
                ask("Contact number: "), ask("E-mail: "));
        try { g.register(system); System.out.println("Registered successfully."); }
        catch (K2703397_BookingException e) { System.out.println("Error: " + e.getMessage()); }
    }

    /** Guest menu: search, book, look up, cancel and print invoices for the signed-in guest. */
    private static void guestLogin() {
        K2703397_Guest g = system.findGuest(ask("NIC/Passport: "));
        if (g == null) { System.out.println("Not registered. Please register first."); return; }
        System.out.println("Welcome, " + g.getFullName());
        while (true) {
            System.out.println("\n--- GUEST MENU ---\n1. Search available rooms\n2. Make reservation"
                    + "\n3. Search reservations (name / booking ID)\n4. View reservations by check-in date"
                    + "\n5. Cancel reservation\n6. Print invoice\n0. Logout");
            try {
                switch (ask("Choose: ")) {
                    case "1":
                        printRooms(g.searchRooms(system, ask("Room type: "), askDate("Check-in"), askDate("Check-out")));
                        break;
                    case "2":
                        String roomId = ask("Room ID: ");
                        int n = askInt("Number of guests: ");
                        K2703397_Reservation r = g.makeReservation(system, roomId, n, askDate("Check-in"), askDate("Check-out"));
                        System.out.println("Booked! " + r.getReservationId() + ". Advance of LKR 10,000 charged (refundable).");
                        break;
                    case "3": searchReservations(g); break;
                    case "4":
                        List<K2703397_Reservation> list = mine(system.getReservationsByCheckIn(), g);
                        if (list.isEmpty()) System.out.println("No reservations.");
                        list.forEach(System.out::println); break;
                    case "5":
                        double refund = g.cancelReservation(system, ask("Reservation ID: "));
                        System.out.printf("Cancelled. Refund: LKR %,.2f%n", refund); break;
                    case "6":
                        K2703397_Reservation res = system.findReservationById(ask("Reservation ID: "));
                        if (res == null || !mine(java.util.Collections.singletonList(res), g).contains(res)) System.out.println("Reservation not found.");
                        else { res.getInvoice().setPaymentMethod(ask("Payment method (Cash/Card): ")); res.getInvoice().print(); }
                        break;
                    case "0": return;
                    default: System.out.println("Invalid option.");
                }
            } catch (K2703397_BookingException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /** Only the signed-in guest's own reservations. */
    private static List<K2703397_Reservation> mine(List<K2703397_Reservation> all, K2703397_Guest g) {
        List<K2703397_Reservation> result = new java.util.ArrayList<>();
        for (K2703397_Reservation r : all) {
            if (r.getGuest().getNicOrPassport().equalsIgnoreCase(g.getNicOrPassport())) result.add(r);
        }
        return result;
    }

    /** Search by guest name, booking ID or check-in date (yyyy-mm-dd). */
    private static void searchReservations(K2703397_Guest g) {
        String q = ask("Enter guest name, booking ID or check-in date (yyyy-mm-dd): ");
        List<K2703397_Reservation> found = new java.util.ArrayList<>();
        K2703397_Reservation byId = system.findReservationById(q);
        if (byId != null) found.add(byId);
        try { found.addAll(system.getReservationsOnCheckIn(LocalDate.parse(q))); } catch (DateTimeParseException e) { /* not a date */ }
        found.addAll(system.searchReservationsByGuestName(q));
        found = mine(new java.util.ArrayList<>(new java.util.LinkedHashSet<>(found)), g);
        if (found.isEmpty()) System.out.println("No matching reservations.");
        else found.forEach(System.out::println);
    }
}
