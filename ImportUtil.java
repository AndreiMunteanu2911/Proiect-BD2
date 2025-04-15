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
            clearDatabase();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 10) {
                    System.err.println("Error: Not enough columns in line: " + line);
                    continue;
                }

                try {
                    int id = Integer.parseInt(values[0]);
                    String nume = values[1];
                    int varsta = Integer.parseInt(values[2]);
                    String strada = values[3];
                    String oras = values[4];
                    String stat = values[5];
                    String codPostal = values[6];
                    String locDeNastere = values[7];
                    String cnp = values[8];
                    String cetatenie = values[9];
                    Adresa adresa = new Adresa(0, strada, oras, stat, codPostal);
                    Persoana persoana = new Persoana(id, nume, varsta, 0, locDeNastere, cnp, cetatenie);
                    if (!overwrite) {
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
        PopulatieInteract interact = new PopulatieInteract("admin");
        interact.adaugaPersoana(persoana, adresa);
    }
    private void clearDatabase() throws SQLException {
        String sql = "DELETE FROM persoane";
        try (Connection conn = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }
    private boolean personExists(String cnp) throws SQLException {
        String sql = "SELECT COUNT(*) FROM persoane WHERE cnp = ?";
        try (Connection conn = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cnp);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}