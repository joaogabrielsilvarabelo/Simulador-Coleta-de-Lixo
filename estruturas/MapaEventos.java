package estruturas;

// Estrutura personalizada para mapear tipos de evento a cores
public class MapaEventos {
    // Classe interna para representar um par chave-valor
    private static class ParEventoCor {
        String evento;
        String cor;

        ParEventoCor(String evento, String cor) {
            this.evento = evento;
            this.cor = cor;
        }
    }
    // Lista personalizada para armazenar pares
    private final Lista<ParEventoCor> pares;

    public MapaEventos() {
        this.pares = new Lista<>();
    }
    // Adiciona um mapeamento evento-cor
    public void put(String evento, String cor) {
        // Remove qualquer par existente com o mesmo evento
        for (int i = 0; i < pares.getTamanho(); i++) {
            if (pares.obter(i).evento.equals(evento)) {
                pares.remover(i);
                break;
            }
        }
        // Adiciona o novo par
        pares.adicionar(new ParEventoCor(evento, cor));
    }
    // Obtém a cor associada a um evento, ou null se não encontrado
    public String get(String evento) {
        for (int i = 0; i < pares.getTamanho(); i++) {
            if (pares.obter(i).evento.equals(evento)) {
                return pares.obter(i).cor;
            }
        }
        return null;
    }
}

