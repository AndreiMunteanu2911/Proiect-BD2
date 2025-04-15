import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class PopulatieInteract {
    private String currentUser ;

    public PopulatieInteract(String currentUser ) {
        this.currentUser  = currentUser ;
    }

    public void adaugaPersoana(Persoana persoana, Adresa adresa) throws SQLException {
        if (!"admin".equals(currentUser )) {
            throw new SecurityException("Only admin can add persons.");
        }
        String insertAddressQuery = "INSERT INTO adrese (strada, oras, stat, cod_postal) VALUES (?, ?, ?, ?)";
        int adresaId;
        try (Connection conexiune = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement addressStatement = conexiune.prepareStatement(insertAddressQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            addressStatement.setString(1, adresa.getStrada());
            addressStatement.setString(2, adresa.getOras());
            addressStatement.setString(3, adresa.getStat());
            addressStatement.setString(4, adresa.getCodPostal());
            addressStatement.executeUpdate();
            try (ResultSet generatedKeys = addressStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    adresaId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating address failed, no ID obtained.");
                }
            }
        }
        String insertPersonQuery = "INSERT INTO persoane (nume, varsta, adresa_id, loc_de_nastere, cnp, cetatenie) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexiune = ConexiuneBazaDeDate.obtineConexiune();
        PreparedStatement personStatement = conexiune.prepareStatement(insertPersonQuery)) {
            personStatement.setString(1, persoana.getNume());
            personStatement.setInt(2, persoana.getVarsta());
            personStatement.setInt(3, adresaId);
            personStatement.setString(4, persoana.getLocDeNastere());
            personStatement.setString(5, persoana.getCnp());
            personStatement.setString(6, persoana.getCetatenie());
            personStatement.executeUpdate();
            logUsage(currentUser , "Adaugare persoana", String.format("INSERT INTO persoane (nume, varsta, adresa_id, loc_de_nastere, cnp, cetatenie) VALUES ('%s', %d, %d, '%s', '%s', '%s')",
                    persoana.getNume(),
                    persoana.getVarsta(),
                    adresaId,
                    persoana.getLocDeNastere(),
                    persoana.getCnp(),
                    persoana.getCetatenie())); }
    }

    public Persoana obtinePersoana(int id) throws SQLException {
        String interogare = "SELECT * FROM persoane WHERE id = ?";
        try (Connection conexiune = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement declaratie = conexiune.prepareStatement(interogare)) {
            declaratie.setInt(1, id);
            ResultSet rezultat = declaratie.executeQuery();
            if (rezultat.next()) {
                int adresaId = rezultat.getInt("adresa_id");
                Adresa adresa = obtineAdresa(adresaId);

                return new Persoana(
                        rezultat.getInt("id"),
                        rezultat.getString("nume"),
                        rezultat.getInt("varsta"),
                        adresaId,
                        rezultat.getString("loc_de_nastere"),
                        rezultat.getString("cnp"),
                        rezultat.getString("cetatenie")
                );
            }
        }
        return null;
    }

    public void actualizeazaPersoana(Persoana persoana, Adresa adresa) throws SQLException {
        if (!"admin".equals(currentUser .trim())) {
            throw new SecurityException("Only admin can update persons.");
        }
        String updateAddressQuery = "UPDATE adrese SET strada = ?, oras = ?, stat = ?, cod_postal = ? WHERE id = ?";
        try (Connection conexiune = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement addressStatement = conexiune.prepareStatement(updateAddressQuery)) {
            addressStatement.setString(1, adresa.getStrada());
            addressStatement.setString(2, adresa.getOras());
            addressStatement.setString(3, adresa.getStat());
            addressStatement.setString(4, adresa.getCodPostal());
            addressStatement.setInt(5, persoana.getAdresaId());
            addressStatement.executeUpdate();
        }
        String updatePersonQuery = "UPDATE persoane SET nume = ?, varsta = ?, adresa_id = ?, loc_de_nastere = ?, cnp = ?, cetatenie = ? WHERE id = ?";
        try (Connection conexiune = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement personStatement = conexiune.prepareStatement(updatePersonQuery)) {
            personStatement.setString(1, persoana.getNume());
            personStatement.setInt(2, persoana.getVarsta());
            personStatement.setInt(3, persoana.getAdresaId());
            personStatement.setString(4, persoana.getLocDeNastere());
            personStatement.setString(5, persoana.getCnp());
            personStatement.setString(6, persoana.getCetatenie());
            personStatement.setInt(7, persoana.getId());
            personStatement.executeUpdate();
            logUsage(currentUser , "Actualizare persoana", String.format("UPDATE persoane SET nume = '%s', varsta = %d, adresa_id = %d, loc_de_nastere = '%s', cnp = '%s', cetatenie = '%s' WHERE id = %d",
                    persoana.getNume(),
                    persoana.getVarsta(),
                    persoana.getAdresaId(),
                    persoana.getLocDeNastere(),
                    persoana.getCnp(),
                    persoana.getCetatenie(),
                    persoana.getId()));
        }
    }

    public void stergePersoana(int id) throws SQLException {
        if (!"admin".equals(currentUser .trim())) {
            throw new SecurityException("Only admin can delete persons.");
        }
        String deleteAddressQuery = "DELETE FROM adrese WHERE id = (SELECT adresa_id FROM persoane WHERE id = ?)";
        try (Connection conexiune = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement addressStatement = conexiune.prepareStatement(deleteAddressQuery)) {
            addressStatement.setInt(1, id);
            addressStatement.executeUpdate();
        }
        String interogare = "DELETE FROM persoane WHERE id = ?";
        try (Connection conexiune = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement declaratie = conexiune.prepareStatement(interogare)) {
            declaratie.setInt(1, id);
            declaratie.executeUpdate();
            logUsage(currentUser , "Stergere persoana", String.format("DELETE FROM persoane WHERE id = %d", id));
        }
    }

    public List<Persoana> filtreazaSiOrdoneazaPersoane(String criteriuFiltrare, String ordineSortare) throws SQLException {
        String interogare = "SELECT * FROM persoane WHERE " + criteriuFiltrare + " ORDER BY " + ordineSortare;
        List<Persoana> persoane = new ArrayList<>();
        try (Connection conexiune = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement declaratie = conexiune.prepareStatement(interogare);
             ResultSet rezultat = declaratie.executeQuery()) {
            while (rezultat.next()) {
                int adresaId = rezultat.getInt("adresa_id");
                Adresa adresa = obtineAdresa(adresaId);
                persoane.add(new Persoana(
                        rezultat.getInt("id"),
                        rezultat.getString("nume"),
                        rezultat.getInt("varsta"),
                        adresaId,
                        rezultat.getString("loc_de_nastere"),
                        rezultat.getString("cnp"),
                        rezultat.getString("cetatenie")
                ));
            }
        }
        return persoane;
    }

    public static void logUsage(String utilizator, String operatie, String comandaSQL) throws SQLException {
        String sql = "INSERT INTO istoric_utilizare (data_ora, utilizator, operatie, comanda_sql) VALUES (NOW(),  ?, ?, ?)";
        try (Connection conn = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, utilizator);
            pstmt.setString(2, operatie);
            pstmt.setString(3, comandaSQL);
            pstmt.executeUpdate();
        }
    }

    public static List<UsageHistory> obtineIstoricUtilizare() throws SQLException {
        List<UsageHistory> istoric = new ArrayList<>();
        String sql = "SELECT * FROM istoric_utilizare ORDER BY data_ora DESC";
        try (Connection conn = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                istoric.add(new UsageHistory(
                        rs.getInt("id"),
                        rs.getTimestamp("data_ora"),
                        rs.getString("utilizator"),
                        rs.getString("operatie"),
                        rs.getString("comanda_sql")
                ));
            }
        }
        return istoric;
    }

    public Adresa obtineAdresa(int adresaId) throws SQLException {
        String interogare = "SELECT * FROM adrese WHERE id = ?";
        try (Connection conexiune = ConexiuneBazaDeDate.obtineConexiune();
             PreparedStatement declaratie = conexiune.prepareStatement(interogare)) {
            declaratie.setInt(1, adresaId);
            ResultSet rezultat = declaratie.executeQuery();
            if (rezultat.next()) {
                return new Adresa(
                        rezultat.getInt("id"),
                        rezultat.getString("strada"),
                        rezultat.getString("oras"),
                        rezultat.getString("stat"),
                        rezultat.getString("cod_postal")
                );
            }
        }
        return null;
    }
}