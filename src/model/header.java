package model;

public class header {
    private String identificador;
    private int numeroNos;
    private int somaPesos;

    public String getIdentificador() {
        return identificador;
    }
    public int getNumeroNos() {
        return numeroNos;
    }
    public int getSomaPesos() {
        return somaPesos;
    }
    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
    public void setNumeroNos(int numeroNos) {
        this.numeroNos = numeroNos;
    }
    public void setSomaPesos(int somaPesos) {
        this.somaPesos = somaPesos;
    }
}
