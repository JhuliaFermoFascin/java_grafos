import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import model.header;
import model.resumoConexoes;
import model.resumoPesos;
import model.trailer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class App {

    

    private static boolean verifica_diretorio(){
        return new File("C:\\Teste").isDirectory() && new File("C:\\Teste\\Configuracao").isDirectory();
    }
    private static String dirProcessadoPath;
    private static String dirNaoProcessadoPath;

    private static boolean verifica_arquivo_conig() throws IOException {
        File configFile = new File("C:\\Teste\\Configuracao\\config.txt");
    
        if (!configFile.isFile()) {
            return false; // Arquivo não existe
        }
    
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String linha1 = reader.readLine();
            String linha2 = reader.readLine();
    
            // verifica se a linha 1 está no formato correto
            if (linha1 == null || !linha1.startsWith("Processado=")) {
                throw new IOException("Arquivo config.txt inválido: Linha 1 deve começar com 'Processado='");
            }
    
            // verifica se a linha 2 está no formato correto
            if (linha2 == null || !linha2.startsWith("Não Processado=")) {
                throw new IOException("Arquivo config.txt inválido: Linha 2 deve começar com 'Não Processado='");
            }
    
            // Extrair os diretórios da configuração
            dirProcessadoPath = linha1.split("=")[1];
            dirNaoProcessadoPath = linha2.split("=")[1];
    
            return true; //arquivo config.txt correto
        }
    }
    
    private static void criarDiretoriosNecessarios() {
    File dirProcessado = new File(dirProcessadoPath);
    File dirNaoProcessado = new File(dirNaoProcessadoPath);

    // Verifica e cria o diretório 'Processado' se não existir
    if (!dirProcessado.exists()) {
        if (dirProcessado.mkdirs()) {
            System.out.println("Diretório 'Processado' criado com sucesso.");
        } else {
            System.out.println("Falha ao criar o diretório 'Processado'.");
        }
    }

    // Verifica e cria o diretório 'NaoProcessado' se não existir
    if (!dirNaoProcessado.exists()) {
        if (dirNaoProcessado.mkdirs()) {
            System.out.println("Diretório 'NaoProcessado' criado com sucesso.");
        } else {
            System.out.println("Falha ao criar o diretório 'NaoProcessado'.");
        }
    }
}


    private static void processarArquivo(File arquivo) throws IOException {
        // Usando try-with-resources para garantir que o BufferedReader seja fechado
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
    
            header header = null;
            ArrayList<resumoConexoes> listaConexoes = new ArrayList<>();
            ArrayList<resumoPesos> listaPesos = new ArrayList<>();
            trailer trailer = null;
    
            while ((linha = reader.readLine()) != null) {
                if (linha.startsWith("00")) {
                    // Processar header
                    if (linha.length() == 9) {
                        header = new header();
                        header.setIdentificador(linha.substring(0, 2));
                        header.setNumeroNos(Integer.parseInt(linha.substring(2, 4)));
                        header.setSomaPesos(Integer.parseInt(linha.substring(4)));
                        System.out.println("Header processado com sucesso.");
                    } else {
                        moverArquivo(arquivo, dirNaoProcessadoPath);
                        throw new IOException("Tamanho inválido no HEADER");
                    }
                } else if (linha.startsWith("01")) {
                    // Processar resumo de conexões
                    if (linha.length() >= 6) {
                        resumoConexoes conexao = new resumoConexoes();
                        conexao.setIdentificadorResumo(linha.substring(0, 2));
                        conexao.setIdentificadorNoOrigem(linha.substring(2, 4));
                        conexao.setIdentificadorNoDestino(linha.substring(5));
                        listaConexoes.add(conexao);
                        System.out.println("Resumo de conexão processado.");
                    } else {
                        moverArquivo(arquivo, dirNaoProcessadoPath);
                        throw new IOException("Tamanho inválido no RESUMO DE CONEXÕES");
                    }
                } else if (linha.startsWith("02")) {
                    // Processar resumo de pesos
                    if (linha.contains(";")) {
                        resumoPesos peso = new resumoPesos();
                        String[] partes = linha.split(";");
                        if (partes.length == 2 && partes[1].contains("=")) {
                            peso.setIdentificadorResumoPeso(linha.substring(0, 2));
                            peso.setIdentificadorNoOrigem(partes[0].substring(2, 4));
                            String[] pesoPartes = partes[1].split("=");
                            peso.setIdentificadorNoAdjacente(pesoPartes[0].substring(0, 2));
                            peso.setPesoAresta(Integer.parseInt(pesoPartes[1]));
                            listaPesos.add(peso);
                            System.out.println("Resumo de pesos processado.");
                        } else {
                            moverArquivo(arquivo, dirNaoProcessadoPath);
                            throw new IOException("Formato inválido no RESUMO DE PESOS");
                        }
                    } else {
                        moverArquivo(arquivo, dirNaoProcessadoPath);
                        throw new IOException("Formato inválido no RESUMO DE PESOS");
                    }
                } else if (linha.startsWith("09")) {
                    // Processar trailer
                    if (linha.contains(";")) {
                        trailer = new trailer();
                        String[] partes = linha.substring(2).split(";");
                        trailer.setIdentificadorTrailer(linha.substring(0, 2));
                        trailer.setNumLinhasRC(Integer.parseInt(partes[0].split("=")[1]));
                        trailer.setNumLinhasRP(Integer.parseInt(partes[1].split("=")[1]));
                        trailer.setSomaPesos(Integer.parseInt(partes[2]));
                        System.out.println("Trailer processado com sucesso.");
                    } else {
                        moverArquivo(arquivo, dirNaoProcessadoPath);
                        throw new IOException("Formato inválido no TRAILER");
                    }
                } else {
                    moverArquivo(arquivo, dirNaoProcessadoPath);
                    throw new IOException("Linha inválida encontrada");
                }
            }
            
    
            // verifica se todos os blocos foram processados corretamente
            if (header != null && trailer != null) {
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
            if (!dirDestino.mkdirs()) {
                throw new IOException("Falha ao criar o diretório " + destino);
            }
        }
        
        Path sourcePath = arquivo.toPath();
        Path targetPath = new File(dirDestino, arquivo.getName()).toPath();
        
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("Arquivo movido para: " + destino);
    }

    private static File gerarArquivoSequencial() throws IOException {
        File pasta = new File("C:\\Teste");
        File[] arquivos = pasta.listFiles((dir, name) -> name.startsWith("rota") && name.endsWith(".txt"));
    
        int sequencial = 1; // número sequencial inicial
    
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                String nomeArquivo = arquivo.getName();
                String numeroStr = nomeArquivo.replaceAll("\\D", "");
                int numero = Integer.parseInt(numeroStr);
                if (numero >= sequencial) {
                    sequencial = numero + 1; // Próximo número disponível
                }
            }

        }
    
        String nomeArquivo = String.format("rota%02d.txt", sequencial);
        File arquivoNovo = new File("C:\\Teste\\" + nomeArquivo);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoNovo))) {
            // HEADER
            writer.write(String.format("00%02dSP", sequencial)); 
            writer.newLine();
            
            // RESUMO DE CONEXÕES (1 ou mais linhas)
            writer.write("01A=B");
            writer.newLine();
            writer.write("01C=D"); // Exemplo de mais conexões
            writer.newLine();
            
            // RESUMO DOS PESOS (1 ou mais linhas)
            writer.write("02A;B=1000");
            writer.newLine();
            writer.write("02C;D=2000"); // Outro exemplo de pesos
            writer.newLine();
            
            // TRAILER
            writer.write(String.format("09RC=%02d;RP=%02d;3000", 2, 2)); // Ajuste para número de linhas RC e RP
        }
    
        System.out.println("Arquivo criado: " + arquivoNovo.getName());
        return arquivoNovo;
    }
    

    public static void main(String[] args) throws IOException  {

        if (verifica_diretorio()) {
            if (verifica_arquivo_conig()) {
                // Criar diretórios 'Processado' e 'NaoProcessado' se não existirem
                criarDiretoriosNecessarios();

                // Gerar um novo arquivo sequencial
                File novoArquivo = gerarArquivoSequencial();

                // Processar o arquivo gerado
                processarArquivo(novoArquivo);

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

