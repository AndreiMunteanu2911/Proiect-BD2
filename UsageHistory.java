import java.sql.Timestamp;

public class UsageHistory {
    private int id;
    private Timestamp dataOra;
    private String utilizator;
    private String operatie;
    private String comandaSQL;

    public UsageHistory(int id, Timestamp dataOra, String utilizator,  String operatie, String comandaSQL) {
        this.id = id;
        this.dataOra = dataOra;
        this.utilizator = utilizator;
        this.operatie = operatie;
        this.comandaSQL = comandaSQL;
    }
    public int getId() { return id; }
    public Timestamp getDataOra() { return dataOra; }
    public String getUtilizator() { return utilizator; }
    public String getOperatie() { return operatie; }
    public String getComandaSQL() { return comandaSQL; }
}