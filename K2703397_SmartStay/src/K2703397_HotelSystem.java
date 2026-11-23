import java.time.LocalDate;
import java.util.*;

/**
 * Core of SmartStay: stores all data and enforces the business rules.
 * Data structures used:
 *   HashMap  - rooms by Room ID, guests by NIC/Passport, reservations by ID (O(1) lookup)
 *   ArrayList - ordered list of reservations
 * Algorithms used: sorting with Comparators, linear search (name), date arithmetic.
 */
public class K2703397_HotelSystem {
    public static final String ROOM_HEADER =
            String.format("%-6s | %-12s | %-7s | %-13s | %-14s | %s",
                    "ID", "Type", "Bed", "Daily Rate", "Breakfast", "Status");
    private static final int MIN_DAYS_IN_ADVANCE = 2;

    private final Map<String, K2703397_Room> rooms = new HashMap<>();
    private final Map<String, K2703397_Guest> guests = new HashMap<>();
    private final Map<String, K2703397_Reservation> reservationsById = new HashMap<>();
    private final List<K2703397_Reservation> reservations = new ArrayList<>();
    private final List<K2703397_Admin> admins = new ArrayList<>();
    private int reservationCounter = 0;
    private LocalDate fixedToday = null;   // lets the test runner control "today"

    // ---------------------------------------------------------- setup / clock
    public void setToday(LocalDate date) { this.fixedToday = date; }
    /** Returns the current date, or the fixed test date when one has been set. */
    public LocalDate today() { return fixedToday != null ? fixedToday : LocalDate.now(); }

    /** Loads the demo administrator account and the eight sample rooms used at start-up. */
    public void loadSampleData() {
        admins.add(new K2703397_Admin("A001", "Manager", "admin123", "Hotel Manager", "0112223334", "admin@smartstay.lk"));
        String[][] spec = {
            {"S101", "Standard", "Single"}, {"S102", "Standard", "Double"},
            {"D201", "Deluxe", "Queen"},    {"D202", "Deluxe", "Double"},
            {"U301", "Suite", "King"},      {"U302", "Suite", "Queen"},
            {"F401", "Family Suite", "King"}, {"F402", "Family Suite", "Queen"}};
        for (String[] s : spec) {
            try { addRoom(createRoom(s[1], s[0], s[2])); } catch (K2703397_BookingException e) { /* sample data is valid */ }
        }
    }

    /** Factory: builds the correct Room subclass for a type name. */
    public K2703397_Room createRoom(String type, String id, String bed) throws K2703397_BookingException {
        switch (type.trim().toLowerCase()) {
            case "standard":     return new K2703397_Standard_Room(id, bed);
            case "deluxe":       return new K2703397_Deluxe_Room(id, bed);
            case "suite":        return new K2703397_Suite(id, bed);
            case "family suite": return new K2703397_Family_Suite(id, bed);
            default: throw new K2703397_BookingException("Unknown room type: " + type);
        }
    }

    // ------------------------------------------------------------------ admin
    public K2703397_Admin loginAdmin(String adminId, String password) {
        for (K2703397_Admin a : admins) {
            if (a.getAdminId().equalsIgnoreCase(adminId) && a.checkPassword(password)) return a;
        }
        return null;
    }

    /** Adds a room; the Room ID (case-insensitive) must be unique. */
    public void addRoom(K2703397_Room room) throws K2703397_BookingException {
        if (rooms.containsKey(room.getRoomId().toUpperCase()))
            throw new K2703397_BookingException("Room ID already exists: " + room.getRoomId());
        rooms.put(room.getRoomId().toUpperCase(), room);
    }

    /** Changes a room's bed type after validating the new value. */
    public void updateRoom(String roomId, String newBedType) throws K2703397_BookingException {
        validateBedType(newBedType);
        findRoom(roomId).setBedType(newBedType);
    }

    /** Removes a room unless it has an active (not cancelled) reservation. */
    public void removeRoom(String roomId) throws K2703397_BookingException {
        K2703397_Room room = findRoom(roomId);
        for (K2703397_Reservation r : reservations) {
            if (!r.isCancelled() && r.getRoom() == room)
                throw new K2703397_BookingException("Room has an active reservation and cannot be removed.");
        }
        rooms.remove(roomId.toUpperCase());
    }

    /** HashMap lookup of a room by ID; throws BookingException when it does not exist. */
    public K2703397_Room findRoom(String roomId) throws K2703397_BookingException {
        K2703397_Room room = rooms.get(roomId == null ? "" : roomId.trim().toUpperCase());
        if (room == null) throw new K2703397_BookingException("Room not found: " + roomId);
        return room;
    }

    /** Rooms sorted by Room ID (sorting algorithm via Comparator). */
    public List<K2703397_Room> getAllRoomsSorted() {
        List<K2703397_Room> list = new ArrayList<>(rooms.values());
        list.sort(Comparator.comparing(K2703397_Room::getRoomId));
        return list;
    }

    /** Accepts only Single, Double, Queen or King (case-insensitive). */
    public static void validateBedType(String bed) throws K2703397_BookingException {
        List<String> ok = Arrays.asList("single", "double", "queen", "king");
        if (!ok.contains(bed.trim().toLowerCase()))
            throw new K2703397_BookingException("Bed type must be Single, Double, Queen or King.");
    }

    // ------------------------------------------------------------------ guest
    public void registerGuest(K2703397_Guest g) throws K2703397_BookingException {
        if (g.getNicOrPassport().isBlank() || g.getFullName().isBlank())
            throw new K2703397_BookingException("NIC/Passport and full name are required.");
        if (!g.getContactNumber().matches("\\+?\\d{9,15}"))
            throw new K2703397_BookingException("Contact number must contain 9-15 digits.");
        if (!g.getEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
            throw new K2703397_BookingException("Invalid e-mail address.");
        if (guests.containsKey(g.getNicOrPassport().toUpperCase()))
            throw new K2703397_BookingException("A guest with this NIC/Passport is already registered.");
        guests.put(g.getNicOrPassport().toUpperCase(), g);
    }

    /** Returns the registered guest with this NIC/Passport, or null. */
    public K2703397_Guest findGuest(String nicOrPassport) {
        return guests.get(nicOrPassport.trim().toUpperCase());
    }

    /** Available rooms of a given type for the requested dates. */
    public List<K2703397_Room> searchAvailableRooms(String type, LocalDate in, LocalDate out)
            throws K2703397_BookingException {
        validateDates(in, out);
        List<K2703397_Room> result = new ArrayList<>();
        for (K2703397_Room r : getAllRoomsSorted()) {
            if (r.getRoomType().equalsIgnoreCase(type.trim()) && r.isAvailable()) result.add(r);
        }
        return result;
    }

    /** Rules: dates are present, check-out is after check-in, and check-in is at least 2 days away. */
    private void validateDates(LocalDate in, LocalDate out) throws K2703397_BookingException {
        if (in == null || out == null) throw new K2703397_BookingException("Dates are required.");
        if (!out.isAfter(in)) throw new K2703397_BookingException("Check-out must be after check-in.");
        if (in.isBefore(today().plusDays(MIN_DAYS_IN_ADVANCE)))
            throw new K2703397_BookingException("Rooms must be booked at least 2 days in advance.");
    }

    public K2703397_Reservation makeReservation(K2703397_Guest guest, String roomId, int numberOfGuests,
                                                LocalDate in, LocalDate out) throws K2703397_BookingException {
        if (guest == null || findGuest(guest.getNicOrPassport()) == null)
            throw new K2703397_BookingException("Guest must be registered before booking.");
        if (numberOfGuests < 1) throw new K2703397_BookingException("At least 1 guest is required.");
        validateDates(in, out);
        K2703397_Room room = findRoom(roomId);
        if (!room.isAvailable())
            throw new K2703397_BookingException("Room " + room.getRoomId() + " is not available (" + room.getAvailabilityStatus() + ").");

        String id = String.format("RES-%03d", ++reservationCounter);
        K2703397_Reservation res = new K2703397_Reservation(id, guest, room, in, out, numberOfGuests);
        res.confirmReservation();
        reservations.add(res);
        reservationsById.put(id, res);
        return res;
    }

    /** Cancels an eligible reservation and returns the refunded advance. */
    public double cancelReservation(K2703397_Guest guest, String reservationId) throws K2703397_BookingException {
        K2703397_Reservation res = reservationsById.get(reservationId.trim().toUpperCase());
        if (res == null) throw new K2703397_BookingException("Reservation not found: " + reservationId);
        if (!res.getGuest().getNicOrPassport().equalsIgnoreCase(guest.getNicOrPassport()))
            throw new K2703397_BookingException("This reservation belongs to another guest.");
        if (res.isCancelled()) throw new K2703397_BookingException("Reservation is already cancelled.");
        if (!res.isEligibleForCancellation(today()))
            throw new K2703397_BookingException("Cannot cancel within 24 hours of check-in.");
        return res.cancelReservation();
    }

    // ---------------------------------------------------------------- queries
    public K2703397_Reservation findReservationById(String id) {
        return reservationsById.get(id.trim().toUpperCase());
    }

    /** Linear search: every reservation whose guest name contains the text (case-insensitive). */
    public List<K2703397_Reservation> searchReservationsByGuestName(String name) {
        List<K2703397_Reservation> result = new ArrayList<>();
        for (K2703397_Reservation r : reservations) {
            if (r.getGuest().getFullName().toLowerCase().contains(name.trim().toLowerCase())) result.add(r);
        }
        return result;
    }

    /** Reservations sorted by check-in date (earliest first). */
    public List<K2703397_Reservation> getReservationsByCheckIn() {
        List<K2703397_Reservation> sorted = new ArrayList<>(reservations);
        sorted.sort(Comparator.comparing(K2703397_Reservation::getCheckInDate)
                              .thenComparing(K2703397_Reservation::getReservationId));
        return sorted;
    }

    /** All reservations whose check-in date equals the given date. */
    public List<K2703397_Reservation> getReservationsOnCheckIn(LocalDate date) {
        List<K2703397_Reservation> result = new ArrayList<>();
        for (K2703397_Reservation r : getReservationsByCheckIn()) {
            if (r.getCheckInDate().equals(date)) result.add(r);
        }
        return result;
    }
}
