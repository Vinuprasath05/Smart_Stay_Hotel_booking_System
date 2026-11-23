/** Standard room package: LKR 6000 per night, tax 10%. */
public class K2703397_Standard_Room extends K2703397_Room {

    public K2703397_Standard_Room(String roomId, String bedType) {
        super(roomId, "Standard", bedType, 6000, 0.10, false, 1000, 2);
    }

    /** Standard room charge = base charge + extra guest charge. */
    @Override
    public double calculateRate(int nights, int guests) {
        return calculateBaseRate(nights) + calculateExtraGuestCharge(nights, guests);
    }
}
