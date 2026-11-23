import java.time.LocalDate;
import java.util.List;

/** Simple automatic test runner. Each check prints its Test Case ID, expected and actual result. */
public class K2703397_TestRunner {
    private static int passed = 0, failed = 0;

    private static void check(String id, String description, boolean ok) {
        System.out.printf("%-6s %-62s %s%n", id, description, ok ? "PASS" : "FAIL");
        if (ok) passed++; else failed++;
    }


    interface Action { void run() throws Exception; }

    private static boolean throwsBookingError(Action a) {
        try { a.run(); return false; }
        catch (K2703397_BookingException e) { return true; }
        catch (Exception e) { return false; }
    }

    private static boolean near(double a, double b) { return Math.abs(a - b) < 0.01; }

    public static void main(String[] args) throws Exception {
        K2703397_HotelSystem sys = new K2703397_HotelSystem();
        sys.loadSampleData();
        LocalDate today = LocalDate.of(2026, 12, 1);
        sys.setToday(today);

        K2703397_Guest amal = new K2703397_Guest("901234567V", "Amal Perera", "0771234567", "amal@mail.com");
        K2703397_Guest nimal = new K2703397_Guest("N1234567", "Nimal Silva", "0712345678", "nimal@mail.com");

        // ---- Registration
        check("TC01", "Register guest with valid details", throwsBookingError(() -> amal.register(sys)) == false && sys.findGuest("901234567V") != null);
        nimal.register(sys);
        check("TC02", "Duplicate NIC/Passport is rejected", throwsBookingError(() ->
                new K2703397_Guest("901234567V", "Other", "0770000000", "o@mail.com").register(sys)));
        check("TC03", "Invalid e-mail is rejected", throwsBookingError(() ->
                new K2703397_Guest("X1", "Bad Mail", "0770000000", "not-an-email").register(sys)));
        check("TC04", "Invalid contact number is rejected", throwsBookingError(() ->
                new K2703397_Guest("X2", "Bad Phone", "abc", "a@b.com").register(sys)));

        // ---- Booking rules
        check("TC05", "Booking less than 2 days ahead is rejected", throwsBookingError(() ->
                amal.makeReservation(sys, "S101", 2, today.plusDays(1), today.plusDays(3))));
        check("TC06", "Check-out before check-in is rejected", throwsBookingError(() ->
                amal.makeReservation(sys, "S101", 2, today.plusDays(10), today.plusDays(8))));
        check("TC07", "Unknown room ID is rejected", throwsBookingError(() ->
                amal.makeReservation(sys, "Z999", 2, today.plusDays(5), today.plusDays(7))));
        check("TC08", "Zero guests is rejected", throwsBookingError(() ->
                amal.makeReservation(sys, "S101", 0, today.plusDays(5), today.plusDays(7))));

        // ---- Billing (hand-calculated expected values)
        K2703397_Reservation r1 = amal.makeReservation(sys, "S101", 2, today.plusDays(5), today.plusDays(7));
        r1.getInvoice().calculateTotal();
        check("TC09", "Standard, 2 nights, 2 guests: final = 3,200.00", near(r1.getInvoice().getFinalPayableAmount(), 3200.00));
        check("TC10", "Advance LKR 10,000 charged and room becomes Reserved",
                near(r1.getAdvancePaid(), 10000) && sys.findRoom("S101").getAvailabilityStatus() == K2703397_RoomStatus.RESERVED);

        K2703397_Reservation r2 = nimal.makeReservation(sys, "D201", 3, today.plusDays(10), today.plusDays(15));
        r2.getInvoice().calculateTotal();
        check("TC11", "Deluxe, 5 nights: 12% discount = 5,400.00", near(r2.getInvoice().getDiscountPrice(), 5400));
        check("TC12", "Deluxe, 5 nights: final payable = 34,352.00", near(r2.getInvoice().getFinalPayableAmount(), 34352.00));

        K2703397_Reservation r3 = amal.makeReservation(sys, "U301", 6, today.plusDays(20), today.plusDays(22));
        r3.getInvoice().calculateTotal();
        check("TC13", "Suite, 6 guests (cap 4), 2 nights: extra guests = 8,000.00", near(r3.getInvoice().getExtraGuestCharge(), 8000));
        check("TC14", "Suite, 6 guests, 2 nights: final payable = 31,400.00", near(r3.getInvoice().getFinalPayableAmount(), 31400.00));

        K2703397_Reservation r4 = nimal.makeReservation(sys, "F401", 4, today.plusDays(30), today.plusDays(34));
        r4.getInvoice().calculateTotal();
        check("TC15", "Family Suite, 4 nights: no discount", near(r4.getInvoice().getDiscountPrice(), 0));
        check("TC16", "Family Suite, 4 nights: final payable = 72,000*1.15-10,000 = 72,800.00", near(r4.getInvoice().getFinalPayableAmount(), 72000 * 1.15 - 10000));

        // ---- Availability
        check("TC17", "Already reserved room cannot be booked again", throwsBookingError(() ->
                nimal.makeReservation(sys, "S101", 1, today.plusDays(40), today.plusDays(41))));
        sys.findRoom("D202").updateStatus(K2703397_RoomStatus.UNDER_MAINTENANCE);
        check("TC18", "Room under maintenance cannot be booked", throwsBookingError(() ->
                amal.makeReservation(sys, "D202", 1, today.plusDays(40), today.plusDays(41))));
        check("TC19", "Search by type returns only available rooms of that type",
                amal.searchRooms(sys, "Standard", today.plusDays(40), today.plusDays(41)).size() == 1
                && amal.searchRooms(sys, "Deluxe", today.plusDays(40), today.plusDays(41)).isEmpty());

        // ---- Cancellation
        double refund = amal.cancelReservation(sys, r1.getReservationId());
        check("TC20", "Eligible cancellation refunds LKR 10,000", near(refund, 10000));
        check("TC21", "Cancelled room becomes Available again", sys.findRoom("S101").isAvailable());
        check("TC22", "Cancelling twice is rejected", throwsBookingError(() -> amal.cancelReservation(sys, r1.getReservationId())));
        check("TC23", "Guest cannot cancel another guest's reservation", throwsBookingError(() -> amal.cancelReservation(sys, r2.getReservationId())));
        sys.setToday(r3.getCheckInDate().minusDays(1));   // now within 24 hours of check-in
        check("TC24", "Cancellation within 24 hours of check-in is rejected", throwsBookingError(() -> amal.cancelReservation(sys, r3.getReservationId())));
        sys.setToday(today);

        // ---- Search / sort
        List<K2703397_Reservation> byName = sys.searchReservationsByGuestName("nimal");
        check("TC25", "Search reservations by guest name (case-insensitive)", byName.size() == 2);
        check("TC26", "Search reservation by booking ID", sys.findReservationById("res-002") == r2);
        List<K2703397_Reservation> sorted = sys.getReservationsByCheckIn();
        boolean order = true;
        for (int i = 1; i < sorted.size(); i++)
            if (sorted.get(i).getCheckInDate().isBefore(sorted.get(i - 1).getCheckInDate())) order = false;
        check("TC27", "Reservations are listed in check-in date order", order);

        // ---- Admin
        check("TC28", "Admin login with correct password", sys.loginAdmin("A001", "admin123") != null);
        check("TC29", "Admin login with wrong password fails", sys.loginAdmin("A001", "wrong") == null);
        sys.addRoom(sys.createRoom("Deluxe", "D203", "King"));
        check("TC30", "Admin adds a new room", sys.findRoom("D203") != null);
        check("TC31", "Duplicate Room ID is rejected", throwsBookingError(() -> sys.addRoom(sys.createRoom("Deluxe", "D203", "King"))));
        sys.updateRoom("D203", "Single");
        check("TC32", "Admin updates room bed type", sys.findRoom("D203").getBedType().equals("Single"));
        check("TC33", "Invalid bed type is rejected", throwsBookingError(() -> sys.updateRoom("D203", "Round")));
        check("TC34", "Room with active reservation cannot be removed", throwsBookingError(() -> sys.removeRoom("D201")));
        sys.removeRoom("D203");
        check("TC35", "Admin removes an unused room", throwsBookingError(() -> sys.findRoom("D203")));

        check("TC36", "Overloaded calculateRate(nights): Deluxe 2 nights = 18,000.00", near(sys.findRoom("D201").calculateRate(2), 18000));
        // ---- Boundary cases
        K2703397_Reservation rb = amal.makeReservation(sys, "S102", 3, today.plusDays(2), today.plusDays(4));
        check("TC37", "Booking exactly 2 days ahead is accepted (boundary)", rb != null);
        check("TC38", "Check-in equal to check-out (0 nights) is rejected", throwsBookingError(() ->
                amal.makeReservation(sys, "U302", 2, today.plusDays(9), today.plusDays(9))));
        rb.getInvoice().calculateTotal();
        check("TC39", "Standard, 2 nights, 3 guests (cap 2): extra guest = 2,000.00", near(rb.getInvoice().getExtraGuestCharge(), 2000));
        sys.setToday(rb.getCheckInDate().minusDays(2));   // exactly 2 days before check-in
        check("TC40", "Cancellation with exactly 2 days remaining is accepted (boundary)", near(amal.cancelReservation(sys, rb.getReservationId()), 10000));
        sys.setToday(today);

        System.out.println("\nRESULT: " + passed + " passed, " + failed + " failed");
        System.out.println("\nSample invoice:");
        r2.getInvoice().print();
    }
}
