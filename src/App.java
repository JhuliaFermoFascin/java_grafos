import java.io.*;
import java.nio.file.*;
import javax.swing.JOptionPane;

public class App {

    private static String dirProcessadoPath;
    private static String dirNaoProcessadoPath;
    private static File configFile = new File("C:\\Teste\\Configuracao\\config.txt");
    private static boolean gerarArquivoCorreto = true;

    private static boolean verifica_diretorio() {
        return new File("C:\\Teste").isDirectory() && new File("C:\\Teste\\Configuracao").isDirectory();
    }

    private static boolean verifica_arquivo_config() throws IOException {
        if (!configFile.exists()) {
            throw new IOException("Arquivo config.txt não encontrado no diretório especificado.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String linha1 = reader.readLine();
            String linha2 = reader.readLine();

            if (linha1 == null || linha2 == null) {
                throw new IOException("Arquivo config.txt está vazio ou incompleto.");
            }

            if (!linha1.startsWith("Processado=")) {
                throw new IOException("Arquivo config.txt inválido: Linha 1 deve começar com 'Processado='");
            }

            if (!linha2.startsWith("Não Processado=")) {
                throw new IOException("Arquivo config.txt inválido: Linha 2 deve começar com 'Não Processado='");
            }

            dirProcessadoPath = linha1.split("=")[1].trim() + "\\";
            dirNaoProcessadoPath = linha2.split("=")[1].trim() + "\\";

            System.out.println("Diretório Processado: " + dirProcessadoPath);
            System.out.println("Diretório Não Processado: " + dirNaoProcessadoPath);

            return true;
        }
    }

    private static void criarDiretoriosNecessarios() {
        File dirProcessado = new File(dirProcessadoPath);
        File dirNaoProcessado = new File(dirNaoProcessadoPath);

        if (!dirProcessado.exists() && dirProcessado.mkdirs()) {
            System.out.println("Diretório 'Processado' criado com sucesso.");
        }

        if (!dirNaoProcessado.exists() && dirNaoProcessado.mkdirs()) {
            System.out.println("Diretório 'NaoProcessado' criado com sucesso.");
        }
    }

    private static void processarArquivo(File arquivo) {
        boolean sucesso = false;
        String mensagemErro = "";

        try {
            if (!validarHeader(arquivo)) {
                mensagemErro = "Número totais de nós inválido.";
                throw new Exception(mensagemErro);
            }

            if (!validarSomaPesos(arquivo)) {
                mensagemErro = "Soma dos pesos difere do esperado.";
                throw new Exception(mensagemErro);
            }

            if (!validarTamanhoHeader(arquivo)) {
                mensagemErro = "Header inválido: quantidade de caracteres excedida.";
                throw new Exception(mensagemErro);
            }

            if (!validarResumoConexoes(arquivo)) {
                mensagemErro = "Resumo de conexões inválido.";
                throw new Exception(mensagemErro);
            }

            if (!validarLinhasTrailerConexoes(arquivo)) {
                mensagemErro = "Número de linhas de conexões no trailer não coincide.";
                throw new Exception(mensagemErro);
            }

            if (!validarLinhasTrailerPesos(arquivo)) {
                mensagemErro = "Número de linhas de pesos no trailer não coincide.";
                throw new Exception(mensagemErro);
            }

            sucesso = true;
            System.out.println("Processamento do arquivo " + arquivo.getName() + " foi bem-sucedido!");

        } catch (Exception e) {
            System.out.println("Erro ao processar o arquivo: " + arquivo.getName() + " - " + mensagemErro);
            e.printStackTrace();
        } finally {
            try {
                if (sucesso) {
                    Thread.sleep(10000);
                    moverArquivo(arquivo, "C:\\Teste\\Processado");
                } else {
                    moverArquivo(arquivo, "C:\\Teste\\NaoProcessado");
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Erro ao mover o arquivo " + arquivo.getName());
                e.printStackTrace();
            }
        }
    }

    private static boolean validarHeader(File arquivo) {
        return true;
    }

    private static boolean validarSomaPesos(File arquivo) {
        return true;
    }

    private static boolean validarTamanhoHeader(File arquivo) {
        return true;
    }

    private static boolean validarResumoConexoes(File arquivo) {
        return true;
    }

    private static boolean validarLinhasTrailerConexoes(File arquivo) {
        return true;
    }

    private static boolean validarLinhasTrailerPesos(File arquivo) {
        return true;
    }

    private static void moverArquivo(File arquivo, String destino) throws IOException {
        Path sourcePath = arquivo.toPath();
        Path targetPath = Paths.get(destino, arquivo.getName());

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        try {
            if (!arquivo.delete()) {
                System.out.println("Tentando deletar novamente após 1 segundo...");
                Thread.sleep(1000);

                if (!arquivo.delete()) {
                    throw new IOException("Falha ao deletar o arquivo original após cópia: " + arquivo.getName());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("A thread foi interrompida ao tentar deletar o arquivo: " + arquivo.getName(), e);
        }

        System.out.println("Arquivo movido para: " + destino);
    }

    private static File gerarArquivoSequencial() throws IOException {
        File pasta = new File("C:\\Teste");
        File[] arquivos = pasta.listFiles((dir, name) -> name.startsWith("rota") && name.endsWith(".txt"));

        int sequencial = 1;

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                String nomeArquivo = arquivo.getName();
                String numeroStr = nomeArquivo.replaceAll("\\D", "");
                int numero = Integer.parseInt(numeroStr);
                if (numero >= sequencial) {
                    sequencial = numero + 1;
                }
            }
        }

        String nomeArquivo = String.format("rota%02d.txt", sequencial);
        File arquivoNovo = new File("C:\\Teste\\" + nomeArquivo);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoNovo))) {
            writer.write(String.format("00%02dSP", sequencial));
            writer.newLine();
            writer.write("01A=B");
            writer.newLine();
            writer.write("01C=D");
            writer.newLine();

            if (gerarArquivoCorreto) {
                writer.write("02A;B=1000");
                writer.newLine();
                writer.write("02C;D=2000");
                writer.newLine();
            } else {
                writer.write("02A;B=1000");
                writer.newLine();
            }

            int numLinhasConexoes = gerarArquivoCorreto ? 2 : 1;
            writer.write(String.format("09RC=%02d;RP=%02d;3000", numLinhasConexoes, 2));
        }

        gerarArquivoCorreto = !gerarArquivoCorreto;
        System.out.println("Arquivo criado: " + arquivoNovo.getName());
        return arquivoNovo;
    }

    public static void main(String[] args) throws IOException {
        if (verifica_diretorio()) {
            if (verifica_arquivo_config()) {
                criarDiretoriosNecessarios();
                File novoArquivo = gerarArquivoSequencial();
                processarArquivo(novoArquivo);
            }
        } else {
            JOptionPane.showMessageDialog(null, "O diretório C:\\Teste ou C:\\Teste\\Configuracao não foi encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
