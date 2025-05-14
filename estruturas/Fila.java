package estruturas;

import java.util.NoSuchElementException;

public class Fila<T> {
    private No<T> head;
    private No<T> tail;
    private int tamanho;

    public Fila() {
        this.head = null;
        this.tail = null;
        this.tamanho = 0;
    }

    public void enfileirar(T dado) {
        No<T> novoNo = new No<>(dado);
        if (estaVazia()) {
            head = novoNo;
            tail = novoNo;
            tail.prox = head;
        } else {
            tail.prox = novoNo;
            tail = novoNo;
            tail.prox = head;
        }
        tamanho++;
    }

    public T remover() {
        if (estaVazia()) {
            throw new RuntimeException("Fila vazia!");
        }
        T dadoRemovido = head.dado;
        head = head.prox;
        tamanho--;
        if (head == null) {
            tail = null;
        } else {
            tail.prox = head;
        }
        return dadoRemovido;
    }

    public T primeiroDaFila() {
        if (estaVazia()) {
            throw new NoSuchElementException("A fila está vazia!");
        }
        return head.dado;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void imprimirFila() {
        if (estaVazia()) {
            throw new NoSuchElementException("A fila está vazia!");
        }
        No<T> atual = head;
        do {
            System.out.print(atual.dado + " ");
            atual = atual.prox;
        } while (atual != head);
        System.out.println("Fila impressa");
    }
}

