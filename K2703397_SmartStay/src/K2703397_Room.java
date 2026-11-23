/**
 * Abstract parent of all room types. Holds the common attributes and
 * the common pricing rules; each subclass supplies its own package
 * (daily rate, tax, breakfast, extra guest charge) and overrides calculateRate().
 */
public abstract class K2703397_Room {
    private final String roomId;
    private final String roomType;
    private String bedType;
    private final double dailyRate;
    private final double taxRate;            // e.g. 0.10 = 10%
    private final boolean breakfastIncluded;
    private final double extraGuestCharge;   // per extra guest, per night
    private final int capacity;              // assumption: max guests with no extra charge
    private K2703397_RoomStatus availabilityStatus;

    protected K2703397_Room(String roomId, String roomType, String bedType, double dailyRate,
                            double taxRate, boolean breakfastIncluded,
                            double extraGuestCharge, int capacity) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.bedType = bedType;
        this.dailyRate = dailyRate;
        this.taxRate = taxRate;
        this.breakfastIncluded = breakfastIncluded;
        this.extraGuestCharge = extraGuestCharge;
        this.capacity = capacity;
        this.availabilityStatus = K2703397_RoomStatus.AVAILABLE;
    }

    /** Base room charge = daily rate x number of nights (before extras, discount, tax). */
    public double calculateBaseRate(int nights) {
        return dailyRate * nights;
    }

    /** Extra guest charge applies only to guests above the room capacity. */
    public double calculateExtraGuestCharge(int nights, int guests) {
        int extraGuests = Math.max(0, guests - capacity);
        return extraGuests * extraGuestCharge * nights;
    }

    /** Room charge (base + extra guests) for a stay. Each room type overrides this. */
    public abstract double calculateRate(int nights, int guests);

    /** Overloaded version: quick quote for a stay when the number of guests is within the room capacity. */
    public double calculateRate(int nights) {
        return calculateRate(nights, capacity);
    }

    public void updateStatus(K2703397_RoomStatus newStatus) {
        this.availabilityStatus = newStatus;
    }

    public boolean isAvailable() {
        return availabilityStatus == K2703397_RoomStatus.AVAILABLE;
    }

    public String getRoomId()            { return roomId; }
    public String getRoomType()          { return roomType; }
    public String getBedType()           { return bedType; }
    public void setBedType(String bed)   { this.bedType = bed; }
    public double getDailyRate()         { return dailyRate; }
    public double getTaxRate()           { return taxRate; }
    public boolean isBreakfastIncluded() { return breakfastIncluded; }
    public double getExtraGuestCharge()  { return extraGuestCharge; }
    public int getCapacity()             { return capacity; }
    public K2703397_RoomStatus getAvailabilityStatus() { return availabilityStatus; }

    @Override
    public String toString() {
        return String.format("%-6s | %-12s | %-7s | LKR %,9.2f | Breakfast: %-3s | %s",
                roomId, roomType, bedType, dailyRate, breakfastIncluded ? "Yes" : "No", availabilityStatus);
    }
}
