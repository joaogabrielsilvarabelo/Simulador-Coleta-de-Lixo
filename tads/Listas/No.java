package tads.Listas;

public class No {
    int valor;
    No prox;
    No ant;
    String nome;

    public No(int valor){
        this.valor = valor;
        this.prox = null;
        this.ant = null;
    }

    public No(String nome){
        this.nome = nome;
        this.prox = null;
        this.ant = null;
    }

}
