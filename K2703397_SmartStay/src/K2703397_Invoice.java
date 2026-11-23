/**
 * Detailed bill for a reservation.
 * Order of calculation (from the case study):
 * base charge -> + extra guests -> - discount (on base) -> + tax -> - advance.
 */
public class K2703397_Invoice {
    public static final int DISCOUNT_MIN_NIGHTS = 5;
    public static final double DISCOUNT_RATE = 0.12;

    private final String invoiceId;
    private final K2703397_Reservation reservation;
    private double baseRoomCharge;
    private double extraGuestCharge;
    private double discountPrice;
    private double taxAmount;
    private double finalPayableAmount;
    private String paymentMethod = "Pending";

    public K2703397_Invoice(String invoiceId, K2703397_Reservation reservation) {
        this.invoiceId = invoiceId;
        this.reservation = reservation;
    }

    /** Computes every figure on the bill. */
    public void generateInvoice() {
        calculateTotal();
    }

    /** Discount = 12% of the base room charge when staying 5 or more nights. */
    public void applyDiscount() {
        discountPrice = (reservation.calculateDuration() >= DISCOUNT_MIN_NIGHTS)
                ? baseRoomCharge * DISCOUNT_RATE : 0;
    }

    /** Bill steps: base charge, extra guests, discount, tax on the discounted amount, then minus the advance. */
    public double calculateTotal() {
        K2703397_Room room = reservation.getRoom();
        int nights = reservation.calculateDuration();
        baseRoomCharge = room.calculateBaseRate(nights);
        extraGuestCharge = room.calculateRate(nights, reservation.getNumberOfGuests()) - baseRoomCharge;
        applyDiscount();
        double taxable = baseRoomCharge - discountPrice + extraGuestCharge;
        taxAmount = taxable * room.getTaxRate();
        finalPayableAmount = taxable + taxAmount - reservation.getAdvancePaid();
        return finalPayableAmount;
    }

    /** Records how the guest paid (e.g. Cash or Card). */
    public void setPaymentMethod(String method) { this.paymentMethod = method; }

    public double getBaseRoomCharge()     { return baseRoomCharge; }
    public double getExtraGuestCharge()   { return extraGuestCharge; }
    public double getDiscountPrice()      { return discountPrice; }
    public double getTaxAmount()          { return taxAmount; }
    public double getFinalPayableAmount() { return finalPayableAmount; }
    public String getInvoiceId()          { return invoiceId; }

    /** Formats an amount as LKR with thousands separators and two decimals. */
    private static String lkr(double v) { return String.format("LKR %,12.2f", v); }

    /** Builds the invoice text in the format required by the case study. */
    public String toText() {
        calculateTotal();
        K2703397_Room r = reservation.getRoom();
        K2703397_Guest g = reservation.getGuest();
        String line = "=".repeat(58);
        StringBuilder sb = new StringBuilder();
        sb.append(line).append("\n");
        sb.append("               SMARTSTAY HOTEL - INVOICE (K2703397)\n");
        sb.append(line).append("\n");
        sb.append("Invoice ID      : ").append(invoiceId).append("   Reservation: ").append(reservation.getReservationId()).append("\n");
        sb.append("--- Guest ---\n");
        sb.append("Name            : ").append(g.getFullName()).append("\n");
        sb.append("NIC/Passport    : ").append(g.getNicOrPassport()).append("\n");
        sb.append("Contact / Email : ").append(g.getContactNumber()).append(" / ").append(g.getEmail()).append("\n");
        sb.append("--- Room ---\n");
        sb.append("Room ID / Type  : ").append(r.getRoomId()).append(" / ").append(r.getRoomType()).append("\n");
        sb.append("Bed Type        : ").append(r.getBedType()).append("\n");
        sb.append("Daily Rate      : ").append(lkr(r.getDailyRate())).append("\n");
        sb.append("--- Stay ---\n");
        sb.append("Check-in        : ").append(reservation.getCheckInDate()).append("\n");
        sb.append("Check-out       : ").append(reservation.getCheckOutDate()).append("\n");
        sb.append("Nights / Guests : ").append(reservation.calculateDuration()).append(" / ").append(reservation.getNumberOfGuests()).append("\n");
        sb.append("Payment method  : ").append(paymentMethod).append("\n");
        sb.append("--- Charges ---\n");
        sb.append("Base room charge: ").append(lkr(baseRoomCharge)).append("\n");
        sb.append("Extra guests    : ").append(lkr(extraGuestCharge)).append("\n");
        sb.append("Discount (12%)  : -").append(lkr(discountPrice).trim()).append("\n");
        sb.append(String.format("Tax (%.0f%%)       : %s%n", r.getTaxRate() * 100, lkr(taxAmount)));
        sb.append("Advance deducted: -").append(lkr(reservation.getAdvancePaid()).trim()).append("\n");
        sb.append("-".repeat(58)).append("\n");
        sb.append("FINAL PAYABLE   : ").append(lkr(finalPayableAmount)).append("\n");
        sb.append(line);
        return sb.toString();
    }

    /** Prints the invoice to the console. */
    public void print() {
        System.out.println(toText());
    }
}
