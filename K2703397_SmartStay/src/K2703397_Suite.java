/** Suite room package: LKR 14000 per night, tax 15%. */
public class K2703397_Suite extends K2703397_Room {

    public K2703397_Suite(String roomId, String bedType) {
        super(roomId, "Suite", bedType, 14000, 0.15, true, 2000, 4);
    }

    /** Suite room charge = base charge + extra guest charge. */
    @Override
    public double calculateRate(int nights, int guests) {
        return calculateBaseRate(nights) + calculateExtraGuestCharge(nights, guests);
    }
}
