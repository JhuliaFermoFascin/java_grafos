import java.io.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import model.header;
import model.resumoConexoes;
import model.resumoPesos;
import model.trailer;

public class App {

    private static boolean verifica_diretorio(){
        return new File("C:\\Teste").isDirectory() && new File("C:\\Teste\\Configuracao").isDirectory();
    }

    private static boolean verifica_arquivo_conig(){
        return new File("C:\\Teste\\Configuracao\\config.txt").isFile();
    }   

    private static void processarArquivo(File arquivo) throws IOException {
        // Usando try-with-resources para garantir que o BufferedReader seja fechado
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
    
            header header = null;
            ArrayList<resumoConexoes> listaConexoes = new ArrayList<>();
            ArrayList<resumoPesos> listaPesos = new ArrayList<>();
            trailer trailer = null;
    
            while ((linha = reader.readLine()) != null) { // lê o arquivo linha por linha
                // se a linha for o header
                if (linha.startsWith("00")) {
                    if (linha.length() == 9) { // Validar tamanho exato
                        header = new header();
                        header.setIdentificador(linha.substring(0, 2));
                        header.setNumeroNos(Integer.parseInt(linha.substring(2, 4)));
                        header.setSomaPesos(Integer.parseInt(linha.substring(4)));
                        System.out.println("Header processado com sucesso.");
                    } else {
                        moverArquivo(arquivo, "C:\\Teste\\NaoProcessado");
                        throw new IOException("Tamanho inválido no HEADER");
                    }
                }
                // se a linha for o resumo de conexões
                else if (linha.startsWith("01")) {
                    if (linha.length() >= 6) { // Exemplo de validação mínima
                        resumoConexoes conexao = new resumoConexoes();
                        conexao.setIdentificadorResumo(linha.substring(0, 2));
                        conexao.setIdentificadorNoOrigem(linha.substring(2, 4));
                        conexao.setIdentificadorNoDestino(linha.substring(5)); // depois do "="
                        listaConexoes.add(conexao);
                        System.out.println("Resumo de conexão processado.");
                    } else {
                        moverArquivo(arquivo, "C:\\Teste\\NaoProcessado");
                        throw new IOException("Tamanho inválido no RESUMO DE CONEXÕES");
                    }
                }
                // se a linha for o resumo de pesos
                else if (linha.startsWith("02")) {
                    if (linha.contains(";")) {
                        resumoPesos peso = new resumoPesos();
                        peso.setIdentificadorResumoPeso(linha.substring(0, 2));
                        String[] partes = linha.substring(2).split(";");
                        peso.setIdentificadorNoOrigem(partes[0].substring(0, 2));
                        peso.setIdentificadorNoAdjacente(partes[1].substring(0, 2));
                        peso.setPesoAresta(Integer.parseInt(partes[1].split("=")[1]));
                        listaPesos.add(peso);
                        System.out.println("Resumo de pesos processado.");
                    } else {
                        moverArquivo(arquivo, "C:\\Teste\\NaoProcessado");
                        throw new IOException("Tamanho inválido no RESUMO DE PESOS");
                    }
                }
                // se a linha for trailer
                else if (linha.startsWith("09")) {
                    if (linha.contains(";")) {
                        trailer = new trailer();
                        trailer.setIdentificadorTrailer(linha.substring(0, 2));
                        String[] partes = linha.substring(2).split(";");
                        trailer.setNumLinhasRC(Integer.parseInt(partes[0].split("=")[1]));
                        trailer.setNumLinhasRP(Integer.parseInt(partes[1].split("=")[1]));
                        trailer.setSomaPesos(Integer.parseInt(partes[2]));
                        System.out.println("Trailer processado com sucesso.");
                    } else {
                        moverArquivo(arquivo, "C:\\Teste\\NaoProcessado");
                        throw new IOException("Tamanho inválido no TRAILER");
                    }
                } 
                else {
                    moverArquivo(arquivo, "C:\\Teste\\NaoProcessado");
                    throw new IOException("Linha inválida encontrada");
                }
            }
    
            // Verificar se todos os blocos foram processados corretamente
            if (header != null && trailer != null) {
                // Lógica adicional de validação pode ser feita aqui, se necessário
                System.out.println("Arquivo processado com sucesso.");
                moverArquivo(arquivo, "C:\\Teste\\Processado");
            } else {
                moverArquivo(arquivo, "C:\\Teste\\NaoProcessado");
                throw new IOException("Arquivo incompleto ou incorreto.");
            }
        } // O BufferedReader será fechado automaticamente aqui
    }
    

    private static void moverArquivo(File arquivo, String destino) throws IOException {
        File dirDestino = new File(destino);
        if (!dirDestino.exists()) {                                                                                                                                                                                                                                                      
            dirDestino.mkdirs(); // cria o diretório se não existir
        }
        if (arquivo.renameTo(new File(dirDestino, arquivo.getName()))) {
            System.out.println("Arquivo movido para: " + destino);
        } else {
            throw new IOException("Falha ao mover o arquivo para " + destino);
        }
    }

    public static void main(String[] args) throws Exception {

        if (verifica_diretorio()) {
            if (verifica_arquivo_conig()) {
                File pastaPrincipal = new File("C:\\Teste");
                File[] arquivos = pastaPrincipal.listFiles((dir, name) -> name.startsWith("rota") && name.endsWith(".txt"));

                if (arquivos != null) {
                    for (File arquivo : arquivos) {
                        try {
                            processarArquivo(arquivo);
                        } catch (Exception e) {
                            System.out.println("Erro ao processar o arquivo " + arquivo.getName() + ": " + e.getMessage());
                            moverArquivo(arquivo, "C:\\Teste\\NaoProcessado");
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Arquivo CONFIG.TXT não encontrado");
            }
        } else {
            JOptionPane.showMessageDialog(null, "EXCEÇÃO: DIRETÓRIO C:\\TESTE não encontrado");
        }
    }
}
