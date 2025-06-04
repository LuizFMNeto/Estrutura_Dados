import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.text.Normalizer;

public class ProcessadorArquivos {
    private TabelaHash tabelaHash;
    private Set<String> palavrasChave;
    
    public ProcessadorArquivos() {
        this.tabelaHash = new TabelaHash();
        this.palavrasChave = new HashSet<>();
    }
    
    public void processarArquivos() throws IOException {
        // Read files with UTF-8 encoding
        List<String> linhasPalavrasChave = Files.readAllLines(Paths.get("palavras-chave.txt"), StandardCharsets.UTF_8);
        for (String palavra : linhasPalavrasChave) {
            palavrasChave.add(palavra.trim().toLowerCase());
        }
        
        List<String> textoLinhas = Files.readAllLines(Paths.get("texto.txt"), StandardCharsets.UTF_8);
        for (int numeroLinha = 0; numeroLinha < textoLinhas.size(); numeroLinha++) {
            processarLinha(textoLinhas.get(numeroLinha), numeroLinha + 1);
        }
        
        gerarArquivoIndice();
    }
    
    private String normalizarPalavra(String palavra) {
        // Remove acentos mantendo as letras
        String normalizada = Normalizer.normalize(palavra, Normalizer.Form.NFD);
        normalizada = normalizada.replaceAll("\\p{M}", "");
        return normalizada.toLowerCase();
    }
    
    private void processarLinha(String linha, int numeroLinha) {
        // Updated regex to include all Portuguese characters
        String[] palavras = linha.toLowerCase()
                                .replaceAll("[^a-záàâãéèêíïóôõöúüçñ\\s-]", "")
                                .split("\\s+");
        
        for (String palavra : palavras) {
            if (!palavra.isEmpty()) {
                String palavraNormalizada = normalizarPalavra(palavra);
                if (palavrasChave.contains(palavraNormalizada)) {
                    Palavra palavraExistente = tabelaHash.buscar(palavra);
                    if (palavraExistente == null) {
                        Palavra novaPalavra = new Palavra(palavra);
                        novaPalavra.adicionarOcorrencia(numeroLinha);
                        tabelaHash.inserir(novaPalavra);
                    } else {
                        palavraExistente.adicionarOcorrencia(numeroLinha);
                    }
                }
            }
        }
    }
    
    private void gerarArquivoIndice() throws IOException {
        List<Palavra> palavrasOrdenadas = new ArrayList<>();
        
        String caracteres = "abcdefghijklmnopqrstuvwxyzáàâãéèêíïóôõöúüçñ";
        for (int i = 0; i < caracteres.length(); i++) {
            ArvoreBinaria arvore = tabelaHash.getArvore(i);
            if (arvore != null) {
                arvore.percorrerEmOrdem(palavrasOrdenadas);
            }
        }
        
        palavrasOrdenadas.sort((p1, p2) -> p1.getPalavra().compareTo(p2.getPalavra()));
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream("indice.txt"), StandardCharsets.UTF_8))) {
            for (Palavra palavra : palavrasOrdenadas) {
                writer.println(palavra.getPalavra() + " " + palavra.getOcorrencias().toString());
            }
        }
        
        System.out.println("Índice remissivo gerado com sucesso em 'indice.txt'");
    }
    
    public static void main(String[] args) {
        ProcessadorArquivos processador = new ProcessadorArquivos();
        try {
            processador.processarArquivos();
        } catch (IOException e) {
            System.err.println("Erro ao processar os arquivos: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 