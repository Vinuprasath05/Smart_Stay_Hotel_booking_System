import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Window (Swing) version of SmartStay. Uses exactly the same classes and business rules as the console version. */
public class K2703397_GUI extends JFrame {
    private final K2703397_HotelSystem sys = new K2703397_HotelSystem();
    private K2703397_Guest current;          // signed-in guest
    private boolean adminOn;                 // admin logged in?

    // rooms tab
    private final JComboBox<String> sType = new JComboBox<>(new String[]{"All types", "Standard", "Deluxe", "Suite", "Family Suite"});
    private final JTextField sIn = new JTextField(9), sOut = new JTextField(9);
    private final DefaultTableModel roomModel = model("Room ID", "Type", "Bed", "Daily rate", "Breakfast", "Status");
    // guest tab
    private final JTextField gNic = new JTextField(14), gName = new JTextField(14), gTel = new JTextField(14), gMail = new JTextField(14), gLogin = new JTextField(14);
    private final JLabel who = new JLabel("Not signed in.");
    private final JComboBox<String> bRoom = new JComboBox<>();
    private final JSpinner bGuests = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
    private final JTextField bIn = new JTextField(9), bOut = new JTextField(9);
    private final JComboBox<String> bPay = new JComboBox<>(new String[]{"Card", "Cash"});
    // reservations tab
    private final JTextField rQ = new JTextField(14);
    private final DefaultTableModel resModel = model("Reservation", "Guest", "Room", "Check-in", "Check-out", "Nights", "Guests", "Advance", "Status");
    private final JTable resTable = new JTable(resModel);
    private final JTextArea invoiceArea = new JTextArea(18, 60);
    private String resQuery = "";
    // admin tab
    private final JTextField aId = new JTextField(10);
    private final JPasswordField aPw = new JPasswordField(10);
    private final JPanel adminPanel = new JPanel(new BorderLayout(8, 8));
    private final JPanel adminLogin = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JTextField nId = new JTextField(8);
    private final JComboBox<String> nType = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite", "Family Suite"});
    private final JComboBox<String> nBed = new JComboBox<>(new String[]{"Single", "Double", "Queen", "King"});
    private final DefaultTableModel adminModel = model("Room ID", "Type", "Bed", "Status");
    private final JTable adminTable = new JTable(adminModel);
    private final JComboBox<K2703397_RoomStatus> statusBox = new JComboBox<>(K2703397_RoomStatus.values());

    interface Act { void go() throws Exception; }

    public K2703397_GUI() {
        super("K2703397 - SmartStay Hotel Booking System");
        sys.loadSampleData();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rooms", roomsTab());
        tabs.addTab("Guest", guestTab());
        tabs.addTab("Reservations", reservationsTab());
        tabs.addTab("Admin", adminTab());
        add(tabs);
        refreshAll();
        setSize(960, 640);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ helpers
    private static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    }

    private void safely(Act a) {
        try { a.go(); }
        catch (K2703397_BookingException e) { error(e.getMessage()); }
        catch (DateTimeParseException e) { error("Dates must be in yyyy-mm-dd format, e.g. 2026-12-05."); }
        catch (Exception e) { error("Something went wrong: " + e.getMessage()); }
    }

    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Cannot do that", JOptionPane.ERROR_MESSAGE); }
    private void info(String m)  { JOptionPane.showMessageDialog(this, m, "SmartStay", JOptionPane.INFORMATION_MESSAGE); }
    private static LocalDate date(JTextField f) { return LocalDate.parse(f.getText().trim()); }

    private static void row(JPanel p, String label, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = p.getComponentCount() / 2; g.insets = new Insets(4, 6, 4, 6); g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; p.add(c, g);
    }

    private static JPanel flow(Component... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        for (Component c : cs) p.add(c);
        return p;
    }

    private static JButton button(String text, Runnable r) {
        JButton b = new JButton(text);
        b.addActionListener(e -> r.run());
        return b;
    }

    // --------------------------------------------------------------------- tabs
    private JPanel roomsTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(flow(new JLabel("Room type"), sType, new JLabel("Check-in"), sIn, new JLabel("Check-out"), sOut,
                button("Search available rooms", () -> safely(() -> {
                    String t = sType.getSelectedItem().toString();
                    LocalDate in = date(sIn), out = date(sOut);
                    List<K2703397_Room> list = new java.util.ArrayList<>();
                    for (K2703397_Room r : sys.getAllRoomsSorted()) {
                        boolean typeOk = t.equals("All types") || r.getRoomType().equals(t);
                        if (typeOk && sys.searchAvailableRooms(r.getRoomType(), in, out).contains(r)) list.add(r);
                    }
                    fillRooms(list);
                    info(list.size() + " room(s) available.");
                })),
                button("Show all rooms", this::refreshAll)), BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(roomModel)), BorderLayout.CENTER);
        p.add(new JLabel("Dates use the format yyyy-mm-dd. Check-in must be at least 2 days from today."), BorderLayout.SOUTH);
        return p;
    }

    private JPanel guestTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        row(p, "Status", who);
        row(p, "NIC / Passport", gNic);
        row(p, "Full name", gName);
        row(p, "Contact number", gTel);
        row(p, "E-mail", gMail);
        row(p, "", button("Register", () -> safely(() -> {
            K2703397_Guest g = new K2703397_Guest(gNic.getText().trim(), gName.getText().trim(), gTel.getText().trim(), gMail.getText().trim());
            g.register(sys);
            current = g;
            refreshAll();
            info("Registered and signed in as " + g.getFullName() + ".");
        })));
        row(p, "Already registered? NIC", gLogin);
        row(p, "", flow(button("Sign in", () -> safely(() -> {
            K2703397_Guest g = sys.findGuest(gLogin.getText());
            if (g == null) throw new K2703397_BookingException("Not registered yet. Please register first.");
            current = g;
            refreshAll();
        })), button("Sign out", () -> { current = null; refreshAll(); })));
        row(p, "Room", bRoom);
        row(p, "Number of guests", bGuests);
        row(p, "Check-in (yyyy-mm-dd)", bIn);
        row(p, "Check-out (yyyy-mm-dd)", bOut);
        row(p, "Pay advance by", bPay);
        row(p, "", button("Pay LKR 10,000 advance and book", () -> safely(() -> {
            if (current == null) throw new K2703397_BookingException("Register or sign in first.");
            String id = bRoom.getSelectedItem() == null ? "" : bRoom.getSelectedItem().toString().split(" ")[0];
            K2703397_Reservation r = current.makeReservation(sys, id, (Integer) bGuests.getValue(), date(bIn), date(bOut));
            r.getInvoice().setPaymentMethod(bPay.getSelectedItem().toString());
            refreshAll();
            info("Booked " + r.getReservationId() + ".\nAdvance of LKR 10,000 paid by " + bPay.getSelectedItem()
                    + " (refundable).\nBalance due at check-out: LKR " + String.format("%,.2f", r.getInvoice().calculateTotal()));
        })));
        return p;
    }

    private JPanel reservationsTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(flow(new JLabel("Guest name, booking ID or check-in date"), rQ,
                button("Search", () -> { resQuery = rQ.getText().trim(); fillReservations(); }),
                button("Show all (by check-in date)", () -> { rQ.setText(""); resQuery = ""; fillReservations(); }),
                button("Show invoice", () -> safely(() -> {
                    K2703397_Reservation r = selectedReservation();
                    invoiceArea.setText(r.getInvoice().toText());
                    invoiceArea.setCaretPosition(0);
                })),
                button("Cancel reservation", () -> safely(() -> {
                    K2703397_Reservation r = selectedReservation();
                    if (current == null) throw new K2703397_BookingException("Sign in as the guest who made this reservation.");
                    double refund = current.cancelReservation(sys, r.getReservationId());
                    refreshAll();
                    info("Cancelled. Refund: LKR " + String.format("%,.2f", refund));
                }))), BorderLayout.NORTH);
        invoiceArea.setEditable(false);
        invoiceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(resTable), new JScrollPane(invoiceArea));
        split.setResizeWeight(0.4);
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private K2703397_Reservation selectedReservation() throws K2703397_BookingException {
        int row = resTable.getSelectedRow();
        if (row < 0) throw new K2703397_BookingException("Click a reservation in the table first.");
        return sys.findReservationById(resModel.getValueAt(row, 0).toString());
    }

    private JPanel adminTab() {
        adminLogin.add(new JLabel("Admin ID"));  adminLogin.add(aId);
        adminLogin.add(new JLabel("Password"));  adminLogin.add(aPw);
        adminLogin.add(button("Log in", () -> safely(() -> {
            if (sys.loginAdmin(aId.getText().trim(), new String(aPw.getPassword())) == null)
                throw new K2703397_BookingException("Wrong admin ID or password. (Demo: A001 / admin123)");
            adminOn = true;
            refreshAll();
        })));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        adminPanel.add(flow(new JLabel("New room ID"), nId, nType, nBed,
                button("Add room", () -> safely(() -> {
                    sys.addRoom(sys.createRoom(nType.getSelectedItem().toString(), nId.getText().trim(), nBed.getSelectedItem().toString()));
                    nId.setText("");
                    refreshAll();
                }))), BorderLayout.NORTH);
        adminPanel.add(new JScrollPane(adminTable), BorderLayout.CENTER);
        adminPanel.add(flow(new JLabel("Selected room:"), statusBox,
                button("Update status", () -> safely(() -> {
                    sys.findRoom(selectedAdminRoomId()).updateStatus((K2703397_RoomStatus) statusBox.getSelectedItem());
                    refreshAll();
                })),
                button("Remove room", () -> safely(() -> {
                    sys.removeRoom(selectedAdminRoomId());
                    refreshAll();
                }))), BorderLayout.SOUTH);
        JPanel outer = new JPanel(new BorderLayout());
        outer.add(adminLogin, BorderLayout.NORTH);
        outer.add(adminPanel, BorderLayout.CENTER);
        return outer;
    }

    private String selectedAdminRoomId() throws K2703397_BookingException {
        int row = adminTable.getSelectedRow();
        if (row < 0) throw new K2703397_BookingException("Click a room in the table first.");
        return adminModel.getValueAt(row, 0).toString();
    }

    // ------------------------------------------------------------------ refresh
    private void fillRooms(List<K2703397_Room> list) {
        roomModel.setRowCount(0);
        for (K2703397_Room r : list)
            roomModel.addRow(new Object[]{r.getRoomId(), r.getRoomType(), r.getBedType(),
                    String.format("LKR %,.2f", r.getDailyRate()), r.isBreakfastIncluded() ? "Yes" : "No", r.getAvailabilityStatus()});
    }

    private void fillReservations() {
        resModel.setRowCount(0);
        String q = resQuery.toLowerCase();
        for (K2703397_Reservation r : sys.getReservationsByCheckIn()) {
            boolean visible = adminOn || (current != null && r.getGuest().getNicOrPassport().equalsIgnoreCase(current.getNicOrPassport()));
            boolean match = q.isEmpty() || r.getReservationId().toLowerCase().equals(q) || r.getGuest().getFullName().toLowerCase().contains(q) || r.getCheckInDate().toString().equals(q);
            if (visible && match)
                resModel.addRow(new Object[]{r.getReservationId(), r.getGuest().getFullName(),
                        r.getRoom().getRoomId() + " (" + r.getRoom().getRoomType() + ")", r.getCheckInDate(), r.getCheckOutDate(),
                        r.calculateDuration(), r.getNumberOfGuests(), r.isCancelled() ? "Refunded" : "LKR 10,000",
                        r.isCancelled() ? "Cancelled" : "Confirmed"});
        }
    }

    private void refreshAll() {
        fillRooms(sys.getAllRoomsSorted());
        fillReservations();
        who.setText(current == null ? "Not signed in." : "Signed in as " + current.getFullName() + " (" + current.getNicOrPassport() + ")");
        bRoom.removeAllItems();
        for (K2703397_Room r : sys.getAllRoomsSorted())
            bRoom.addItem(r.getRoomId() + " - " + r.getRoomType() + ", " + r.getBedType() + " (" + r.getAvailabilityStatus() + ")");
        adminModel.setRowCount(0);
        for (K2703397_Room r : sys.getAllRoomsSorted())
            adminModel.addRow(new Object[]{r.getRoomId(), r.getRoomType(), r.getBedType(), r.getAvailabilityStatus()});
        adminLogin.setVisible(!adminOn);
        adminPanel.setVisible(adminOn);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new K2703397_GUI().setVisible(true));
    }
}
