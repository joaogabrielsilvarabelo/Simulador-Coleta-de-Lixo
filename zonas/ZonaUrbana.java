package zonas;

import java.util.Random;

public class ZonaUrbana {
    private String nome;
    private int lixoAcumulado;

    public ZonaUrbana(String nome) {
        this.nome = nome;
        this.lixoAcumulado = 0;
    }

    public void gerarLixo() {
        int quantidade = new Random().nextInt(500) + 100;
        lixoAcumulado += quantidade;
        System.out.println(nome + ": Gerou " + quantidade + "kg de lixo. Total: " + lixoAcumulado + "kg.");
    }

    public int coletarLixo(int quantidade) {
        int coletado = Math.min(quantidade, lixoAcumulado);
        lixoAcumulado -= coletado;
        return coletado;
    }

    public int getLixoAcumulado() {
        return lixoAcumulado;
    }

    public String getNome() {
        return nome;
    }

}