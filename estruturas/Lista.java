package estruturas;

public class Lista<T> {
    private No<T> head;
    private int tamanho;

    public Lista() {
        this.head = null;
        this.tamanho = 0;
    }

    public void adicionar(T dado) {
        No<T> novoNo = new No<>(dado);
        if (head == null) {
            head = novoNo;
        } else {
            No<T> atual = head;
            while (atual.prox != null) {
                atual = atual.prox;
            }
            atual.prox = novoNo;
        }
        tamanho++;
    }

    public T obter(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice inválido: " + indice);
        }
        No<T> atual = head;
        for (int i = 0; i < indice; i++) {
            atual = atual.prox;
        }
        return atual.dado;
    }

    public T remover(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice inválido: " + indice);
        }
        T removido;
        if (indice == 0) {
            removido = head.dado;
            head = head.prox;
        } else {
            No<T> anterior = head;
            for (int i = 0; i < indice - 1; i++) {
                anterior = anterior.prox;
            }
            removido = anterior.prox.dado;
            anterior.prox = anterior.prox.prox;
        }
        tamanho--;
        return removido;
    }

    public void limpar() {
        head = null;
        tamanho = 0;
    }

    public int getTamanho() {
        return tamanho;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }
}


