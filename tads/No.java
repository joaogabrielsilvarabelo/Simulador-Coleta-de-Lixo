package tads;

public class No {
    int valor;
    No prox;
    No ant;
    String nome;

    public No(String nome){
        this.nome = nome;
        this.prox = null;
        this.ant = null;
    }

    public No(int valor){
        this.valor = valor;
        this.prox = null;
        this.ant = null;
    }

}
