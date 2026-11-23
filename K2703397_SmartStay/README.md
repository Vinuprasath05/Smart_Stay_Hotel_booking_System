# K2703397 - SmartStay Hotel Booking System (CI6115 - Java application)

## How to run
```
javac -d out src/*.java
java -cp out K2703397_Main          # the application
java -cp out K2703397_TestRunner    # automatic test cases (TC01 - TC40)
```
Admin login: ID `A001`, password `admin123`. Eight sample rooms are loaded at start-up.



## Class structure (matches the class diagram)
| Class | Role |
|---|---|
| K2703397_User (abstract) | name, contact, e-mail, updateProfile() |
| K2703397_Guest / K2703397_Admin | inherit User |
| K2703397_Room (abstract) | room attributes + pricing; abstract calculateRate() |
| K2703397_Standard_Room, Deluxe_Room, Suite, Family_Suite | inherit Room (polymorphism) |
| K2703397_Reservation | booking, owns one Invoice (composition) |
| K2703397_Invoice | bill calculation + printing |
| K2703397_HotelSystem | business rules, data structures (HashMap, ArrayList), sorting/searching |
| K2703397_Main / TestRunner | console UI / tests |

## Assumptions
1. Room capacity (guests without extra charge): Standard 2, Deluxe 3, Suite 4, Family Suite 6.
2. Extra guest charge is per extra guest, per night.
3. Tax is applied on (base charge – discount + extra guest charges).
4. A room becomes *Reserved* when booked and *Available* again when the booking is cancelled (one active booking per room).
5. "Within 24 hours" is checked on dates only: cancellation is allowed when 2 or more days remain before check-in.
6. Contact number is stored as String (not int) so that leading zeros are kept.
7. Data is held in memory only (no database/file storage).
