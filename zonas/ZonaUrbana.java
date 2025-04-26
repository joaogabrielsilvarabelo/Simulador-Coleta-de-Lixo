package zonas;

import java.util.Random;

public class ZonaUrbana {
    private String nome;
    private int lixoAcumulado;
    private int lixoMin;
    private int lixoMax;


    public ZonaUrbana(String nome) {
        this.nome = nome;
        this.lixoAcumulado = 0;
        this.lixoMin = 20;
        this.lixoMax = 100;
    }

    public void gerarLixo() {
        int quantidade = new Random().nextInt(lixoMin, lixoMax);
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