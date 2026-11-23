import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** A booking of one room by one guest for a date range. Owns exactly one Invoice (composition). */
public class K2703397_Reservation {
    public static final double ADVANCE_AMOUNT = 10_000.00;   // refundable advance (LKR)

    private final String reservationId;
    private final K2703397_Guest guest;
    private final K2703397_Room room;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int numberOfGuests;
    private double advancePaid;
    private boolean cancelled;
    private final K2703397_Invoice invoice;

    public K2703397_Reservation(String reservationId, K2703397_Guest guest, K2703397_Room room,
                                LocalDate checkIn, LocalDate checkOut, int numberOfGuests) {
        this.reservationId = reservationId;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.numberOfGuests = numberOfGuests;
        this.advancePaid = 0;
        this.invoice = new K2703397_Invoice("INV-" + reservationId.substring(4), this);
    }

    /** Number of nights between check-in and check-out. */
    public int calculateDuration() {
        return (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /** Charges the advance and marks the room as reserved. */
    public void confirmReservation() {
        advancePaid = ADVANCE_AMOUNT;
        room.updateStatus(K2703397_RoomStatus.RESERVED);
        invoice.generateInvoice();
    }

    /** Rule: no cancellation/modification within 24 hours of check-in (date based: fewer than 2 days left). */
    public boolean isEligibleForCancellation(LocalDate today) {
        return !cancelled && ChronoUnit.DAYS.between(today, checkInDate) >= 2;
    }

    /** Cancels the booking, releases the room and returns the refunded advance. */
    public double cancelReservation() {
        double refund = calculateRefund();
        cancelled = true;
        advancePaid = 0;
        room.updateStatus(K2703397_RoomStatus.AVAILABLE);
        return refund;
    }

    /** The advance is fully refundable when the cancellation is allowed. */
    public double calculateRefund() {
        return advancePaid;
    }

    public String getReservationId()   { return reservationId; }
    public K2703397_Guest getGuest()   { return guest; }
    public K2703397_Room getRoom()     { return room; }
    public LocalDate getCheckInDate()  { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public int getNumberOfGuests()     { return numberOfGuests; }
    public double getAdvancePaid()     { return advancePaid; }
    public boolean isCancelled()       { return cancelled; }
    public K2703397_Invoice getInvoice() { return invoice; }

    @Override
    public String toString() {
        return String.format("%-9s | %-20s | Room %-5s (%-12s) | %s -> %s | %d nights | %d guests | %s",
                reservationId, guest.getFullName(), room.getRoomId(), room.getRoomType(),
                checkInDate, checkOutDate, calculateDuration(), numberOfGuests,
                cancelled ? "CANCELLED" : "CONFIRMED");
    }
}
