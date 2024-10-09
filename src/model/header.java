package model;

public class header {
    private String identificadorHeader;
    private int numeroNos;
    private int somaPesos;

    public String getIdentificadorHeader() {
        return identificadorHeader;
    }
    public int getNumeroNos() {
        return numeroNos;
    }
    public int getSomaPesos() {
        return somaPesos;
    }
    public void setIdentificador(String identificadorHeader) {
        this.identificadorHeader = identificadorHeader;
    }
    public void setNumeroNos(int numeroNos) {
        this.numeroNos = numeroNos;
    }
    public void setSomaPesos(int somaPesos) {
        this.somaPesos = somaPesos;
    }
}
