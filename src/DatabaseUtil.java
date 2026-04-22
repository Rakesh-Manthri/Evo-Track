import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/carbon_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "rakes75045";

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Connected to database automatically. Schema is managed by setup_db.sql");
            seedDefaultData(conn);
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("❌ Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedDefaultData(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Activity_Categories");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("🌱 Seeding default lookup data...");
                stmt.executeUpdate("INSERT INTO Activity_Categories (category_name) VALUES ('Transportation'), ('Energy'), ('Diet')");
                
                stmt.executeUpdate("INSERT INTO Emission_Factors (factor_name, co2_per_unit, unit) VALUES " +
                                   "('Gasoline Car', 0.192, 'km'), " +
                                   "('Bus', 0.089, 'km'), " +
                                   "('Electricity', 0.453, 'kWh'), " +
                                   "('Meat Meal', 2.0, 'meal')");

                stmt.executeUpdate("INSERT INTO Activity_Types (category_id, name, default_unit, typical_emission_factor_id) VALUES " +
                                   "(1, 'Driving (Car)', 'km', 1), " +
                                   "(1, 'Taking the Bus', 'km', 2), " +
                                   "(2, 'Home Electricity', 'kWh', 3), " +
                                   "(3, 'Eating Meat', 'meal', 4)");
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Rewards");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("🏅 Seeding default rewards data...");
                stmt.executeUpdate("INSERT INTO Rewards (reward_name, description, condition_type, condition_value, badge_icon_url) VALUES " +
                                   "('Eco Starter', 'Keep total emissions below 50kg for a month', 'below_threshold', 50.00, '⭐'), " +
                                   "('Carbon Saver', 'Keep total emissions below 30kg for a month', 'below_threshold', 30.00, '🌟'), " +
                                   "('Earth Champion', 'Keep total emissions below 10kg for a month', 'below_threshold', 10.00, '🏆')");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
