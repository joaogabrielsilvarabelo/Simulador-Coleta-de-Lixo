import caminhoes.CaminhaoPequeno;
import estruturas.Lista;
import zonas.ZonaUrbana;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class Simulador implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient Timer timer;
    private int tempoSimulado = 0;
    private boolean pausado = false;

    public void iniciar() {
        System.out.println("Simulação iniciada...");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!pausado) {
                    tempoSimulado++;
                    atualizarSimulacao();
                }
            }
        }, 0, 1000);
    }

    public void pausar() {
        System.out.println("Simulação pausada.");
        pausado = true;
    }

    public void continuarSimulacao() {
        System.out.println("Simulação retomada.");
        pausado = false;
    }

    public void encerrar() {
        System.out.println("Simulação encerrada.");
        if (timer != null) timer.cancel();
    }

    public void gravar(String caminho) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminho))) {
            oos.writeObject(this);
            System.out.println("Simulação salva.");
        }
    }

    public static Simulador carregar(String caminho) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminho))) {
            Simulador sim = (Simulador) ois.readObject();
            sim.timer = new Timer();
            return sim;
        }
    }

    private void atualizarSimulacao() {
        System.out.println("Tempo simulado: " + tempoSimulado + " minutos");
    }

    public void distribuirCaminhoesGeograficamente(Lista<CaminhaoPequeno> disponiveis, Lista<ZonaUrbana> zonas) {
        for (int i = 0; i < disponiveis.getTamanho(); i++) {
            CaminhaoPequeno pequeno = disponiveis.obter(i);
            ZonaUrbana melhor = null;
            int menorDistancia = Integer.MAX_VALUE;
            for (int j = 0; j < zonas.getTamanho(); j++) {
                ZonaUrbana z = zonas.obter(j);
                if (z.getLixoAcumulado() > 3000) {
                    int dist = ZonaUrbana.getDistancia(pequeno.getZonaBase().getNome(), z.getNome());
                    if (dist < menorDistancia) {
                        menorDistancia = dist;
                        melhor = z;
                    }
                }
            }
            if (melhor != null) {
                pequeno.determinarEstado(2); // COLETANDO
                disponiveis.remover(pequeno);
                pequeno.setZonaAtual(melhor);
                melhor.incrementarCaminhoesAtivos();
                System.out.println("Caminhão " + pequeno.getPlaca() + " enviado para " + melhor.getNome());
            }
        }
    }

    public boolean isHorarioDePico(int tempoMinutoSimulado) {
        int minutoNoDia = tempoMinutoSimulado % 1440;
        return (minutoNoDia >= 420 && minutoNoDia <= 539) || (minutoNoDia >= 1020 && minutoNoDia <= 1139);
    }

    public int tempoViagemZona(ZonaUrbana zona, boolean pico) {
        return pico ? zona.getVariacaoPico() : zona.getVariacaoNormal();
    }
}