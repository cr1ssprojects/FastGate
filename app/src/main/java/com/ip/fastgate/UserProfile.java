package com.ip.fastgate;

public class UserProfile {
    private String nume;
    private String divizie;
    private String nrMasina;
    private String orar;
    private String userImage;

    public UserProfile(String nume, String divizie, String nr_masina, String orar_acces, String userImage) {
        setNume(nume);
        setDivizie(divizie);
        setNrMasina(nr_masina);
        setOrar(orar_acces);
        setUserImage(userImage);
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getNume() {
        return nume;
    }

    public String getDivizie() {
        return divizie;
    }

    public void setDivizie(String divizie) {
        this.divizie = divizie;
    }

    public String getNrMasina() {
        return nrMasina;
    }

    public void setNrMasina(String nrMasina) {
        this.nrMasina = nrMasina;
    }

    public void setOrar(String orar) {
        this.orar = orar;
    }

    public String getOrar() {
        // return ConvertDateToString(ora_iesire)
        return orar;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

/*    public String ConvertDateToString(Date date)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }*/
}
