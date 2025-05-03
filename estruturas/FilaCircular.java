package estruturas;

public class FilaCircular {
    No head;
    No tail;

    public FilaCircular() {
        this.head = null;
        this.tail = null;
        //tail.prox = head;
        //head.prox = tail;
    }

    public void enfileirar(String nome) {
        No current = new No(nome);
        if (head == null) {
            head = current;
            tail = current;
            tail.prox = head;
        } else {
            tail.prox = current;
            tail = current;
            tail.prox = head;
        }
    }

    public void desenfileirar() {
        if (head == null) {
            System.out.println("Fila vazia");
            return;
        }

        if (head == tail) {
            head = null;
            tail = null;
        } else {
            head = head.prox;
            tail.prox = head;
        }
    }

    public void imprimirFila() {
        if (head == null) {
            System.out.println("A fila está vazia");
            return;
        } else {
            No current = head; //Começa normal que nem o outro
            do {
                System.out.println(current.nome + " ");
                current = current.prox;
            }
            while (current != head);

            System.out.println(" ");
        }
        System.out.println(" Fila impressa");
    }
}
