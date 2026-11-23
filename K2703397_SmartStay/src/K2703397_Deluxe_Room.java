/** Deluxe room package: LKR 9000 per night, tax 12%. */
public class K2703397_Deluxe_Room extends K2703397_Room {

    public K2703397_Deluxe_Room(String roomId, String bedType) {
        super(roomId, "Deluxe", bedType, 9000, 0.12, true, 1500, 3);
    }

    /** Deluxe room charge = base charge + extra guest charge (the shared pricing rule of the case study). */
    @Override
    public double calculateRate(int nights, int guests) {
        return calculateBaseRate(nights) + calculateExtraGuestCharge(nights, guests);
    }
}
