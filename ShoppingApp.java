import java.sql.*;
import java.util.Scanner;

public class ShoppingApp {

    static final String URL =
            "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    static final String USER = "system";
    static final String PASSWORD = "your_password";

    public static void main(String[] args) {

        try (
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            Scanner sc = new Scanner(System.in)
        ) {
            System.out.println("===== SHOPPING APPLICATION =====");

            displayProducts(con);
            con.setAutoCommit(false);

            try {
                int orderId;

                String sql =
                    "INSERT INTO orders (order_id, total_amount) " +
                    "VALUES (order_seq.NEXTVAL, 0)";

                try (Statement st = con.createStatement()) {
                    st.executeUpdate(sql);
                }

                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(
                         "SELECT order_seq.CURRVAL FROM dual")) {
                    rs.next();
                    orderId = rs.getInt(1);
                }

                double total = 0;

                while (true) {
                    System.out.print(
                        "\nEnter product ID (0 to finish): ");
                    int id = sc.nextInt();

                    if (id == 0) {
                        break;
                    }

                    System.out.print("Enter quantity: ");
                    int quantity = sc.nextInt();

                    if (quantity <= 0) {
                        System.out.println(
                            "Quantity must be greater than zero.");
                        continue;
                    }

                    double price = 0;
                    boolean found = false;

                    String findSQL =
                        "SELECT price FROM products WHERE product_id = ?";

                    try (PreparedStatement ps =
                             con.prepareStatement(findSQL)) {
                        ps.setInt(1, id);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                price = rs.getDouble("price");
                                found = true;
                            }
                        }
                    }

                    if (!found) {
                        System.out.println("Invalid product ID.");
                        continue;
                    }

                    String stockSQL =
                        "UPDATE products SET stock = stock - ? " +
                        "WHERE product_id = ? AND stock >= ?";

                    int updated;

                    try (PreparedStatement ps =
                             con.prepareStatement(stockSQL)) {
                        ps.setInt(1, quantity);
                        ps.setInt(2, id);
                        ps.setInt(3, quantity);
                        updated = ps.executeUpdate();
                    }

                    if (updated == 0) {
                        System.out.println("Insufficient stock.");
                        continue;
                    }

                    double subtotal = price * quantity;

                    String itemSQL =
                        "INSERT INTO order_items " +
                        "(order_id, product_id, quantity, subtotal) " +
                        "VALUES (?, ?, ?, ?)";

                    try (PreparedStatement ps =
                             con.prepareStatement(itemSQL)) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, id);
                        ps.setInt(3, quantity);
                        ps.setDouble(4, subtotal);
                        ps.executeUpdate();
                    }

                    total += subtotal;
                    System.out.println("Subtotal: Rs. " + subtotal);
                }

                if (total == 0) {
                    con.rollback();
                    System.out.println("No items purchased.");
                    return;
                }

                String updateSQL =
                    "UPDATE orders SET total_amount = ? WHERE order_id = ?";

                try (PreparedStatement ps =
                         con.prepareStatement(updateSQL)) {
                    ps.setDouble(1, total);
                    ps.setInt(2, orderId);
                    ps.executeUpdate();
                }

                con.commit();

                System.out.println("\n===== FINAL BILL =====");
                System.out.println("Order ID: " + orderId);
                System.out.println("Total Amount: Rs. " + total);
                System.out.println("Order saved successfully!");

            } catch (Exception e) {
                con.rollback();
                System.out.println("Order failed. Transaction rolled back.");
                System.out.println(e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            System.out.println("Database connection error.");
            System.out.println(e.getMessage());
        }
    }

    static void displayProducts(Connection con) throws SQLException {

        String sql = "SELECT * FROM products ORDER BY product_id";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\nID\tProduct\t\tPrice\tStock");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("product_id") + "\t" +
                    rs.getString("product_name") + "\t\t" +
                    rs.getDouble("price") + "\t" +
                    rs.getInt("stock")
                );
            }
        }
    }
}