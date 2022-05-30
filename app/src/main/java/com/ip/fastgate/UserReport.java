package com.ip.fastgate;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserReport {

    private static int index = 0;
    private String marca;
    private String data;
    private String ora_intrare;//maybe vine ca date
    private String ora_iesire;//maybe vine ca date

    public UserReport(String marca, String data, String ora_intrare, String ora_iesire) {
        this.setIndex(++index);
        this.setMarca(marca);
        this.setData(data);
        this.setOra_intrare(ora_intrare);
        this.setOra_iesire(ora_iesire);
    }

    public void setIndex(int index) {
        this.index = index;

    }

    public void setMarca(String marca) {
        this.marca = marca;

    }

    public void setData(String data) {
        this.data = data;

    }

    public void setOra_intrare(String ora_intrare) {
        this.ora_intrare = ora_intrare;

    }

    public void setOra_iesire(String ora_iesire) {
        this.ora_iesire = ora_iesire;

    }

    public int getIndex() {
        return index;
    }

    public String getMarca() {
        return marca;
    }

    public String getData() {
        //
        return data;
    }

    public String getOra_intrare() {
        // return ConvertDateToString(ora_iesire)
        return ora_intrare;
    }

    public String getOra_iesire() {
        // return ConvertDateToString(ora_iesire)
        return ora_iesire;
    }

    public String toString(String index, String marca, String data, String ora_intrare, String ora_iesire) {
        return new StringBuilder().append(index).append(".  Ziua: ").append(data).append("\n     Marca: ").append(marca).append("\n     Ora intrare: ").append(ora_intrare).append("\n     Ora iesire: ").append(ora_iesire).toString();
    }

    // Conversie date to string, poate fi generalizata
    public String ConvertDateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }
}
