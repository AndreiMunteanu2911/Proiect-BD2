import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexiuneBazaDeDate {
    private static final String URL = "jdbc:mysql:
    private static final String UTILIZATOR = "root";
    private static final String PAROLA = "%Andreutu7";

    public static Connection obtineConexiune() throws SQLException {
        return DriverManager.getConnection(URL, UTILIZATOR, PAROLA);
    }
}

/*
CREATE TABLE adrese (
    id INT AUTO_INCREMENT PRIMARY KEY,
    strada VARCHAR(255) NOT NULL,
    oras VARCHAR(255) NOT NULL,
    stat VARCHAR(255) NOT NULL,
    cod_postal VARCHAR(20) NOT NULL
);

CREATE TABLE persoane (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nume VARCHAR(255) NOT NULL,
    varsta INT NOT NULL,
    adresa_id INT,
    loc_de_nastere VARCHAR(255) NOT NULL,
    cnp CHAR(13) NOT NULL,
    cetatenie VARCHAR(50) NOT NULL,
    FOREIGN KEY (adresa_id) REFERENCES adrese(id) ON CASCADE DELETE
);

CREATE TABLE istoric_utilizare (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data_ora DATETIME NOT NULL,
    utilizator VARCHAR(255) NOT NULL,
    operatie VARCHAR(255) NOT NULL,
    comanda_sql TEXT NOT NULL
);
*/