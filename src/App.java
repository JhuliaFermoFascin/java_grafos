//criar um package e adicinar isso lá dentro
import java.io.File;
import java.util.ArrayList;

import model.header;
import model.resumoConexoes;

public class App {
    private static boolean verifica_diretorio(){
        return new File("C:\\Teste").isDirectory() &&  new File("C:\\Teste\\Configuracao").isDirectory();
    }
    private static boolean verifica_arquivo_conig(){
        return new File("C:\\Teste\\Configuracao\\config.txt").isFile();
    }   

    public static void main(String[] args) throws Exception {

        if(verifica_diretorio()){
            if(verifica_arquivo_conig()){
                try{
                    while(true){
                        String linha = "000315"; //conseguir ler a primeira linha do arquivo. 000315 é um exemplo, é como se fosse a primeira linha, logo, é necessário achar uma função que consigar ler isso
                                        
                        //se a linha for o header
                        if(linha.startsWith("00")){
                            if(linha.length() <= 9){
                                header header = new header();
                                header.setIdentificador(linha.substring(0,2));
                                header.setNumeroNos(Integer.parseInt(linha.substring(2,4)));
                                header.setSomaPesos(Integer.parseInt(linha.substring(4)));
                            }
                            else{
                                //fazer a excessão de tamanho inválido
                                //remover o arquivo para a pasta de não necessário
                            }
                        }

                        //se a linha for o resumo de conexões
                        else if(linha.startsWith("01")){
                            ArrayList<resumoConexoes> resumoConexoesList = new ArrayList<>();

                            resumoConexoes resumo = new resumoConexoes();

                            resumo.setIdentificadorNoOrigem(linha.substring(2, 4));
                            resumo.setIdentificadorNoDestino(linha.substring(4, 6));


                            resumoConexoesList.add(resumo);

                        }

                        //se a linha for o resumo de pesos
                        else if(linha.startsWith("02")){

                        }

                        //se a linha for trailer
                        else if(linha.startsWith("09")){

                        }
                        else{
                            //lançar excessão e mover o arquivo para a pasta de não processado
                        }
                    }
                }
                catch(Exception e){
                    //mover para não processado
                }
            }
        }
    }
}