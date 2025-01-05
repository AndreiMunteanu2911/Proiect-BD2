import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImportUtil {

    public void importFromCSV(File file, boolean overwrite) throws IOException, SQLException {
        if (overwrite) {
            // Clear existing data in the database
            clearDatabase();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // Skip the header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Assuming comma as the delimiter

                // Ensure the line has the correct number of columns
                if (values.length < 10) {
                    System.err.println("Error: Not enough columns in line: " + line);
                    continue; // Skip this line
                }

                try {
                    // Read data from each column
                    int id = Integer.parseInt(values[0]); // ID
                    String nume = values[1]; // Name
                    int varsta = Integer.parseInt(values[2]); // Age
                    String strada = values[3]; // Street
                    String oras = values[4]; // City
                    String stat = values[5]; // Country
                    String codPostal = values[6]; // Postal Code
                    String locDeNastere = values[7]; // Birthplace
                    String cnp = values[8]; // CNP
                    String cetatenie = values[9]; // Citizenship

                    // Create Adresa and Persoana objects
                    Adresa adresa = new Adresa(0, strada, oras, stat, codPostal);
                    Persoana persoana = new Persoana(id, nume, varsta, 0, locDeNastere, cnp, cetatenie);

                    // Add to database
                    if (!overwrite) {
                        // Check if the person already exists in the database
                        if (!personExists(cnp)) {
                            addPersonToDatabase(persoana, adresa);
                        }
                    } else {
                        addPersonToDatabase(persoana, adresa);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing line: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private void addPersonToDatabase(Persoana persoana, Adresa adresa) throws SQLException {
        // Use your existing methods to add the person and address to the database
        PopulatieInteract interact = new PopulatieInteract("admin"); // Assuming admin access
        interact.adaugaPersoana(persoana, adresa);
    }
    private void clearDatabase() throws SQLException {
        String sql = "DELETE FROM persoane"; // Clear all records from the 'persoane' table
        try (Connection conn = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }
    private boolean personExists(String cnp) throws SQLException {
        String sql = "SELECT COUNT(*) FROM persoane WHERE cnp = ?"; // Check for existence in the 'persoane' table
        try (Connection conn = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cnp); // Set the CNP parameter
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Return true if count is greater than 0
            }
        }
        return false; // Return false if no records found
    }
}