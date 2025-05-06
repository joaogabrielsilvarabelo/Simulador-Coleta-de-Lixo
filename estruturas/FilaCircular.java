package estruturas;

import java.util.NoSuchElementException;

@SuppressWarnings({"unchecked", "rawtypes"})
public class FilaCircular<T> {
    No head;
    No tail;
    int tamanho;

    public FilaCircular() {
        this.head = null;
        this.tail = null;
        this.tamanho = 0;
    }

    public void enfileirar(T dado) {
        No current = new No(dado);
        if (estaVazia()) {
            head = current;
            tail = current;
            tail.prox = head;
        } else {
            tail.prox = current;
            tail = current;
            tail.prox = head;
        }
        tamanho++;
    }

    public void remover() {
        if (estaVazia()) {
            throw new NoSuchElementException("A fila está vazia");
        }
        if (head == tail) {
            head = null;
            tail = null;
        } else {
            head = head.prox;
            tail.prox = head;
        }
        tamanho--;
    }

    public T primeiroDaFila() {
        if (estaVazia()) {
            throw new NoSuchElementException("A fila está vazia!");
        }
        return (T) head.dado;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int getTamanho(){
        return tamanho;
    }

    public void imprimirFila() {
        if (head == null) {
            throw new NoSuchElementException("A fila está vazia!");
        } else {
            No current = head; //Começa normal que nem o outro
            do {
                System.out.println(current.dado + " ");
                current = current.prox;
            }
            while (current != head);

            System.out.println(" ");
        }
        System.out.println(" Fila impressa");
    }
}
