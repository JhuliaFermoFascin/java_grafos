package model;

public class resumoPesos {
    private String identificadorResumoPeso;;
    private String identificadorNoOrigem;
    private String identificadorNoAdjacente;
    private int pesoAresta;

    public String getIdentificadorNoAdjacente() {
        return identificadorNoAdjacente;
    }

    public String getIdentificadorNoOrigem() {
        return identificadorNoOrigem;
    }

    public String getIdentificadorResumoPeso() {
        return identificadorResumoPeso;
    }

    public int getPesoAresta() {
        return pesoAresta;
    }

    public void setIdentificadorNoAdjacente(String identificadorNoAdjacente) {
        this.identificadorNoAdjacente = identificadorNoAdjacente;
    }

    public void setIdentificadorNoOrigem(String identificadorNoOrigem) {
        this.identificadorNoOrigem = identificadorNoOrigem;
    }

    public void setIdentificadorResumoPeso(String identificadorResumoPeso) {
        this.identificadorResumoPeso = identificadorResumoPeso;
    }

    public void setPesoAresta(int pesoAresta) {
        this.pesoAresta = pesoAresta;
    }

    
}
