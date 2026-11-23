/** Hotel staff member who manages room records and availability. */
public class K2703397_Admin extends K2703397_User {
    private final String adminId;
    private final String role;
    private final String password;   // simple demo login only

    public K2703397_Admin(String adminId, String role, String password,
                          String fullName, String contactNumber, String email) {
        super(fullName, contactNumber, email);
        this.adminId = adminId;
        this.role = role;
        this.password = password;
    }

    /** Returns true when the entered password matches this administrator's password. */
    public boolean checkPassword(String attempt) { return password.equals(attempt); }
    public String getAdminId() { return adminId; }
    public String getRole()    { return role; }

    /** Adds a new room record by delegating to the hotel system (duplicate IDs are rejected there). */
    public void addRoom(K2703397_HotelSystem s, K2703397_Room room) throws K2703397_BookingException {
        s.addRoom(room);
    }

    /** Updates the bed type of an existing room after the new value has been validated. */
    public void updateRoom(K2703397_HotelSystem s, String roomId, String newBedType) throws K2703397_BookingException {
        s.updateRoom(roomId, newBedType);
    }

    /** Removes a room record; the hotel system refuses if the room has an active reservation. */
    public void removeRoom(K2703397_HotelSystem s, String roomId) throws K2703397_BookingException {
        s.removeRoom(roomId);
    }

    /** Sets a room's availability status (Available, Reserved or Under Maintenance). */
    public void manageAvailability(K2703397_HotelSystem s, String roomId, K2703397_RoomStatus status)
            throws K2703397_BookingException {
        s.findRoom(roomId).updateStatus(status);
    }

    /** Prints every room record sorted by Room ID. */
    public void viewAllRooms(K2703397_HotelSystem s) {
        System.out.println(K2703397_HotelSystem.ROOM_HEADER);
        for (K2703397_Room r : s.getAllRoomsSorted()) {
            System.out.println(r);
        }
    }
}
