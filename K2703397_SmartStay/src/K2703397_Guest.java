import java.time.LocalDate;
import java.util.List;

/** A registered customer. Guest actions are delegated to the HotelSystem (business logic). */
public class K2703397_Guest extends K2703397_User {
    private final String nicOrPassport;

    public K2703397_Guest(String nicOrPassport, String fullName, String contactNumber, String email) {
        super(fullName, contactNumber, email);
        this.nicOrPassport = nicOrPassport;
    }

    public String getNicOrPassport() { return nicOrPassport; }

    /** Registers this guest in the system. */
    public void register(K2703397_HotelSystem system) throws K2703397_BookingException {
        system.registerGuest(this);
    }

    public List<K2703397_Room> searchRooms(K2703397_HotelSystem system, String roomType,
                                           LocalDate checkIn, LocalDate checkOut) throws K2703397_BookingException {
        return system.searchAvailableRooms(roomType, checkIn, checkOut);
    }

    public K2703397_Reservation makeReservation(K2703397_HotelSystem system, String roomId, int guests,
                                                LocalDate checkIn, LocalDate checkOut) throws K2703397_BookingException {
        return system.makeReservation(this, roomId, guests, checkIn, checkOut);
    }

    /** Cancels one of this guest's reservations and returns the refunded advance. */
    public double cancelReservation(K2703397_HotelSystem system, String reservationId) throws K2703397_BookingException {
        return system.cancelReservation(this, reservationId);
    }
}
