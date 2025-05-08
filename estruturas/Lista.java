package estruturas;

import java.util.NoSuchElementException;

public class Lista<T> {
    private No head;
    private int tamanho;

    private class No {
        T dado;
        No prox;

        public No(T dado) {
            this.dado = dado;
            this.prox = null;
        }
    }

    public Lista() {
        this.head = null;
        this.tamanho = 0;
    }

    public void adicionar(T dado) {
        No novo = new No(dado);
        if (head == null) {
            head = novo;
        } else {
            No atual = head;
            while (atual.prox != null) {
                atual = atual.prox;
            }
            atual.prox = novo;
        }
        tamanho++;
    }

    public void remover(T dado) {
        if (head == null) {
            throw new NoSuchElementException("A lista está vazia");
        }
        if (head.dado.equals(dado)) {
            head = head.prox;
            tamanho--;
            return;
        }
        No atual = head;
        No anterior = null;
        while (atual != null && !atual.dado.equals(dado)) {
            anterior = atual;
            atual = atual.prox;
        }
        if (atual == null) {
            throw new NoSuchElementException("Elemento não encontrado");
        }
        anterior.prox = atual.prox;
        tamanho--;
    }

    public boolean contem(T dado) {
        No atual = head;
        while (atual != null) {
            if (atual.dado.equals(dado)) {
                return true;
            }
            atual = atual.prox;
        }
        return false;
    }

    public T obter(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice inválido: " + indice);
        }
        No atual = head;
        for (int i = 0; i < indice; i++) {
            atual = atual.prox;
        }
        return atual.dado;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void imprimirLista() {
        if (head == null) {
            throw new NoSuchElementException("A lista está vazia!");
        }
        No atual = head;
        while (atual != null) {
            System.out.println(atual.dado + " ");
            atual = atual.prox;
        }
        System.out.println("Lista impressa");
    }
}
