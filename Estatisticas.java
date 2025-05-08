public class Estatisticas {
    private int totalLixoColetado = 0;
    private int totalCaminhoesGrandesUsados = 0;
    private int tempoTotalEsperaPequenos = 0;
    private int descarregamentos = 0;

    public void registrarColeta(int kg) {
        totalLixoColetado += kg;
    }

    public void registrarNovoCaminhaoGrande() {
        totalCaminhoesGrandesUsados++;
    }

    public void registrarEspera(int minutos) {
        tempoTotalEsperaPequenos += minutos;
        descarregamentos++;
    }

    public void imprimirRelatorio() {
        System.out.println("===== Estatísticas =====");
        System.out.println("Lixo total coletado: " + totalLixoColetado + " kg");
        System.out.println("Caminhões grandes usados: " + totalCaminhoesGrandesUsados);
        System.out.println("Tempo médio de espera (CP): " +
                (descarregamentos > 0 ? tempoTotalEsperaPequenos / descarregamentos : 0) + " min");
    }
}

