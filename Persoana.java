public class Persoana {
    private int id;
    private String nume;
    private int varsta;
    private int adresaId;
    private String locDeNastere;
    private String cnp;
    private String cetatenie;

    public Persoana(int id, String nume, int varsta, int adresaId, String locDeNastere, String cnp, String cetatenie) {
        this.id = id;
        this.nume = nume;
        this.varsta = varsta;
        this.adresaId = adresaId;
        this.locDeNastere = locDeNastere;
        this.cnp = cnp;
        this.cetatenie = cetatenie;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public int getVarsta() {
        return varsta;
    }

    public void setVarsta(int varsta) {
        this.varsta = varsta;
    }

    public int getAdresaId() {
        return adresaId;
    }

    public void setAdresaId(int adresaId) {
        this.adresaId = adresaId;
    }

    public String getLocDeNastere() {
        return locDeNastere;
    }

    public void setLocDeNastere(String locDeNastere) {
        this.locDeNastere = locDeNastere;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getCetatenie() {
        return cetatenie;
    }

    public void setCetatenie(String cetatenie) {
        this.cetatenie = cetatenie;
    }
}