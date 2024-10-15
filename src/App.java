import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException; // Adicione esta linha
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import model.header;
import model.resumoConexoes;
import model.resumoPesos;
import model.trailer;

public class App {

    private static String dirProcessadoPath;
    private static String dirNaoProcessadoPath;
    private static File configFile = new File("C:\\Teste\\Configuracao\\config.txt");

    // Verifica se o diretório C:\Teste e C:\Teste\Configuracao existem
    private static boolean verifica_diretorio() {
        return new File("C:\\Teste").isDirectory() && new File("C:\\Teste\\Configuracao").isDirectory();
    }

    // Verifica se o arquivo config.txt existe e está no formato correto
    private static boolean verifica_arquivo_config() throws IOException {
        if (!configFile.exists()) {
            throw new IOException("Arquivo config.txt não encontrado no diretório especificado.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String linha1 = reader.readLine();
            String linha2 = reader.readLine();

            if (linha1 == null && linha2 == null) {
                throw new IOException("Arquivo config.txt está vazio. Por favor, verifique as configurações.");
            }

            if (linha1 == null || !linha1.startsWith("Processado=")) {
                throw new IOException("Arquivo config.txt inválido: Linha 1 deve começar com 'Processado='");
            }

            if (linha2 == null || !linha2.startsWith("Não Processado=")) {
                throw new IOException("Arquivo config.txt inválido: Linha 2 deve começar com 'Não Processado='");
            }

            // Pega os diretórios do arquivo config.txt
            dirProcessadoPath = linha1.split("=")[1] + "\\";
            System.out.println("Diretório Processado: " + dirProcessadoPath);
            dirNaoProcessadoPath = linha2.split("=")[1] + "\\";
            System.out.println("Diretório Não Processado: " + dirNaoProcessadoPath);

            return true;
        }
    }

    // Cria os diretórios "Processado" e "NaoProcessado" se não existirem
    private static void criarDiretoriosNecessarios() {
        File dirProcessado = new File(dirProcessadoPath);
        File dirNaoProcessado = new File(dirNaoProcessadoPath);

        if (!dirProcessado.exists()) {
            if (dirProcessado.mkdirs()) {
                System.out.println("Diretório 'Processado' criado com sucesso.");
            } else {
                System.out.println("Falha ao criar o diretório 'Processado'.");
            }
        }

        if (!dirNaoProcessado.exists()) {
            if (dirNaoProcessado.mkdirs()) {
                System.out.println("Diretório 'NaoProcessado' criado com sucesso.");
            } else {
                System.out.println("Falha ao criar o diretório 'NaoProcessado'.");
            }
        }
    }

    // Processa o arquivo rotaNN.txt
    private static void processarArquivo(File arquivo) throws IOException {
        header header = null;
        trailer trailer = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            ArrayList<resumoConexoes> listaConexoes = new ArrayList<>();
            ArrayList<resumoPesos> listaPesos = new ArrayList<>();

            while ((linha = reader.readLine()) != null) {
                if (linha.startsWith("00")) {
                    // Processa o header
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
                    // Processa o resumo de conexões
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
                    // Processa o resumo de pesos
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
                    // Processa o trailer
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

        } catch (IOException e) {
            // Se ocorrer erro durante o processamento, mover para Não Processado
            moverArquivo(arquivo, dirNaoProcessadoPath);
            throw e;
        }

        // Certifique-se de que o arquivo seja completamente liberado pelo sistema antes de movê-lo
        try {
            Thread.sleep(100); // Pausa para garantir que o sistema operacional libere o arquivo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verifica se todos os blocos foram processados corretamente
        if (header != null && trailer != null) {
            System.out.println("Arquivo processado com sucesso.");
            moverArquivo(arquivo, dirProcessadoPath);
        } else {
            moverArquivo(arquivo, dirNaoProcessadoPath);
            throw new IOException("Arquivo incompleto ou incorreto.");
        }
    }

    // Move o arquivo para o diretório de destino, usando FileChannel para garantir que não está sendo usado
    private static void moverArquivo(File arquivo, String destino) throws IOException {
        File dirDestino = new File(destino);

        if (!dirDestino.exists()) {
            if (!dirDestino.mkdirs()) {
                throw new IOException("Falha ao criar o diretório " + destino);
            }
        }

        Path sourcePath = arquivo.toPath();
        Path targetPath = new File(dirDestino, arquivo.getName()).toPath();

        // Usando FileChannel para garantir que o arquivo não está mais em uso
        try (FileInputStream fis = new FileInputStream(arquivo);
             FileChannel fc = fis.getChannel()) {
            fc.lock(); // Trava o arquivo para evitar acessos concorrentes
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (OverlappingFileLockException e) {
            System.out.println("Arquivo já está bloqueado por outro processo.");
        } catch (IOException e) {
            throw new IOException("Falha ao mover o arquivo: " + arquivo.getName(), e);
        }

        System.out.println("Arquivo movido para: " + destino);
    }

    // Gera um novo arquivo rotaNN.txt com número sequencial
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
            writer.write("01C=D");
            writer.newLine();

            // RESUMO DOS PESOS (1 ou mais linhas)
            writer.write("02A;B=1000");
            writer.newLine();
            writer.write("02C;D=2000");
            writer.newLine();

            // TRAILER
            writer.write(String.format("09RC=%02d;RP=%02d;3000", 2, 2));
        }

        System.out.println("Arquivo criado: " + arquivoNovo.getName());
        return arquivoNovo;
    }

    public static void main(String[] args) throws IOException {
        if (verifica_diretorio()) {
            if (verifica_arquivo_config()) {
                // Cria os diretórios 'Processado' e 'NaoProcessado' se não existirem
                criarDiretoriosNecessarios();

                // Gera um novo arquivo sequencial
                File novoArquivo = gerarArquivoSequencial();

                // Processa o arquivo gerado
                processarArquivo(novoArquivo);

                // Verifica se há outros arquivos rotaNN.txt no diretório
                File pastaPrincipal = new File("C:\\Teste");
                File[] arquivos = pastaPrincipal.listFiles((dir, name) -> name.startsWith("rota") && name.endsWith(".txt"));

                if (arquivos != null) {
                    for (File arquivo : arquivos) {
                        try {
                            processarArquivo(arquivo);
                        } catch (Exception e) {
                            System.out.println("Erro ao processar o arquivo " + arquivo.getName() + ": " + e.getMessage());
                            moverArquivo(arquivo, dirNaoProcessadoPath);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Arquivo CONFIG.TXT não encontrado");
            }
        } else {
            JOptionPane.showMessageDialog(null, "EXCEÇÃO: DIRETÓRIO C:\\TESTE ou C:\\TESTE\\CONFIGURACAO não encontrado");
        }
    }
}
