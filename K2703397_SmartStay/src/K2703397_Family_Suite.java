/** Family Suite room package: LKR 18000 per night, tax 15%. */
public class K2703397_Family_Suite extends K2703397_Room {

    public K2703397_Family_Suite(String roomId, String bedType) {
        super(roomId, "Family Suite", bedType, 18000, 0.15, true, 2500, 6);
    }

    /** Family Suite room charge = base charge + extra guest charge. */
    @Override
    public double calculateRate(int nights, int guests) {
        return calculateBaseRate(nights) + calculateExtraGuestCharge(nights, guests);
    }
}
