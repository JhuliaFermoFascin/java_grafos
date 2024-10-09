//criar um package e adicinar isso lá dentro

import model.header;

private static boolean verifica_diretorio(){
   return new File("C:\\Teste").isDirectory &&  new File("C:\\Teste\Configuracao").isDirectory//encontrar um método para verificar se o arquivo existe
}

private static boolean verifica_caminho(){}


public class App {
    public static void main(String[] args) throws Exception {

        verifica_diretorio();
        verifica_caminho();

        header header = new header();
        //1. ler o arquivo txt chamado de rotaNN.txt
        String linha = "000315";
        

        //se a linha for o header
        if(linha.startsWith("00")){
            
        }

        //se a linha for o resumo de conexões
        else if(linha.startsWith("01")){

        }
        else if(linha.startsWith("02")){

        }
        else if(linha.startsWith("09")){

        }
    }
}