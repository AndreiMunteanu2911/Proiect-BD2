public class Adresa {
    private int id;
    private String strada;
    private String oras;
    private String stat;
    private String codPostal;

    public Adresa(int id, String strada, String oras, String stat, String codPostal) {
        this.id = id;
        this.strada = strada;
        this.oras = oras;
        this.codPostal = codPostal;
        this.stat = stat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStrada() {
        return strada;
    }

    public void setStrada(String strada) {
        this.strada = strada;
    }

    public String getOras() {
        return oras;
    }

    public void setOras(String oras) {
        this.oras = oras;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getCodPostal() {
        return codPostal;
    }

    public void setCodPostal(String codPostal) {
        this.codPostal = codPostal;
    }
}