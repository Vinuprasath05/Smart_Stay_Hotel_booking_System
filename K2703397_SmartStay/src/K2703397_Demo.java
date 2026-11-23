import java.time.LocalDate;
import java.util.List;

/** Runs a complete SmartStay scenario automatically (no typing needed). Dates are relative to today. */
public class K2703397_Demo {
    private static void title(String t) { System.out.println("\n########## " + t + " ##########"); }

    public static void main(String[] args) throws Exception {
        K2703397_HotelSystem sys = new K2703397_HotelSystem();
        sys.loadSampleData();
        LocalDate today = LocalDate.now();

        title("1. ADMIN: view all rooms, add a room, manage availability");
        K2703397_Admin admin = sys.loginAdmin("A001", "admin123");
        admin.viewAllRooms(sys);
        admin.addRoom(sys, sys.createRoom("Deluxe", "D203", "King"));
        admin.manageAvailability(sys, "S102", K2703397_RoomStatus.UNDER_MAINTENANCE);
        System.out.println("\nAfter adding D203 and putting S102 under maintenance:");
        admin.viewAllRooms(sys);

        title("2. GUEST: register");
        K2703397_Guest amal = new K2703397_Guest("200012345678", "Amal Perera", "0771234567", "amal@mail.com");
        K2703397_Guest nimal = new K2703397_Guest("N1234567", "Nimal Silva", "0712345678", "nimal@mail.com");
        amal.register(sys);
        nimal.register(sys);
        System.out.println("Registered: Amal Perera, Nimal Silva");

        title("3. GUEST: search available Standard rooms");
        List<K2703397_Room> found = amal.searchRooms(sys, "Standard", today.plusDays(5), today.plusDays(7));
        found.forEach(System.out::println);

        title("4. GUEST: make reservations (advance LKR 10,000 charged)");
        K2703397_Reservation r1 = amal.makeReservation(sys, "S101", 3, today.plusDays(5), today.plusDays(7));
        K2703397_Reservation r2 = nimal.makeReservation(sys, "D201", 3, today.plusDays(10), today.plusDays(16));
        K2703397_Reservation r3 = amal.makeReservation(sys, "U301", 2, today.plusDays(3), today.plusDays(4));
        System.out.println(r1 + "\n" + r2 + "\n" + r3);

        title("5. RULES: invalid bookings are rejected");
        try { amal.makeReservation(sys, "S101", 2, today.plusDays(20), today.plusDays(22)); }
        catch (K2703397_BookingException e) { System.out.println("Already reserved room  -> " + e.getMessage()); }
        try { amal.makeReservation(sys, "F401", 2, today.plusDays(1), today.plusDays(3)); }
        catch (K2703397_BookingException e) { System.out.println("Less than 2 days ahead -> " + e.getMessage()); }
        try { amal.makeReservation(sys, "S102", 2, today.plusDays(20), today.plusDays(22)); }
        catch (K2703397_BookingException e) { System.out.println("Under maintenance     -> " + e.getMessage()); }

        title("6. SEARCH: by guest name, by booking ID, by check-in date");
        sys.searchReservationsByGuestName("amal").forEach(System.out::println);
        System.out.println(sys.findReservationById("RES-002"));
        System.out.println("-- all, sorted by check-in date --");
        sys.getReservationsByCheckIn().forEach(System.out::println);

        title("7. INVOICES (r1: extra guest | r2: 6 nights, 12% discount)");
        r1.getInvoice().setPaymentMethod("Card");
        r1.getInvoice().print();
        r2.getInvoice().setPaymentMethod("Cash");
        r2.getInvoice().print();

        title("8. CANCELLATION");
        double refund = amal.cancelReservation(sys, r1.getReservationId());
        System.out.printf("RES-001 cancelled. Refund: LKR %,.2f. Room S101 is now: %s%n",
                refund, sys.findRoom("S101").getAvailabilityStatus());
        System.out.println("\nDemo finished.");
    }
}
