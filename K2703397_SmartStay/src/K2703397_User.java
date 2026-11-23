/**
 * Base class for every person who uses the SmartStay system.
 * Abstract: a "User" on its own is never created, only a Guest or an Admin.
 */
public abstract class K2703397_User {
    private String fullName;
    private String contactNumber;   // String (not int) so leading zeros / "+94" are kept
    private String email;

    public K2703397_User(String fullName, String contactNumber, String email) {
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    /** Updates the profile details shared by all users. */
    public void updateProfile(String fullName, String contactNumber, String email) {
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    public String getFullName()      { return fullName; }
    public String getContactNumber() { return contactNumber; }
    public String getEmail()         { return email; }
}
