/** Allowed availability states of a room (from the case study). */
public enum K2703397_RoomStatus {
    AVAILABLE("Available"),
    RESERVED("Reserved"),
    UNDER_MAINTENANCE("Under Maintenance");

    private final String label;

    K2703397_RoomStatus(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
