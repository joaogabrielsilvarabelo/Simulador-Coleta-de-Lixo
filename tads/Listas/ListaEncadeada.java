package tads.Listas;


public class ListaEncadeada {
    private No head;

    public ListaEncadeada(){
        this.head = null;
    }

    public void inserirNoInicio(int valor) {
        No novoNo = new No(valor);
        novoNo.prox = head;
        head = novoNo;
    }

    public void inserirNoFinal(int valor){
        No novoNo = new No(valor);
        if (head == null){
            head = novoNo;
        }
        else {
            No current = head;
            while( current.prox != null){
                current = current.prox;
            }
            current.prox = novoNo;
        }
    }

    public void removerDoInicio(){
        if (head != null){
            head = head.prox;
        }
    }

    public void paresImpares() {
        if (head == null || head.prox == null) {
            return; // Lista vazia ou com apenas um nó, não há o que fazer
        }

        No current = head;
        No lastOdd = head; // Ponteiro para o último nó ímpar

        // Encontra o último nó da lista
        while (lastOdd.prox != null) {
            lastOdd = lastOdd.prox;
        }

        No ending = lastOdd;

        // Percorre a lista até o último nó ímpar
        while (current != ending) {
            if (current.valor % 2 == 0) { // Se o valor for par
                // Remove o nó par da posição atual
                if (current == head) { // Se for o primeiro nó
                    head = current.prox; // Atualiza o head
                } else {
                    // Encontra o nó anterior ao current
                    No temp = head;
                    while (temp.prox != current) {
                        temp = temp.prox;
                    }
                    temp.prox = current.prox; // Remove o nó par da lista
                }

                // Move o nó par para o final
                lastOdd.prox = current;
                lastOdd = current;
                current = current.prox;
                lastOdd.prox = null; // Garante que o último nó aponte para null
            } else {
                current = current.prox;
            }
        }

        // Trata o último nó (que pode ser par)
        if (current.valor % 2 == 0) {
            if (current == head) { // Se for o primeiro nó
                head = current.prox; // Atualiza o head
            } else {
                // Encontra o nó anterior ao current
                No temp = head;
                while (temp.prox != current) {
                    temp = temp.prox;
                }
                temp.prox = current.prox; // Remove o nó par da lista
            }

            // Move o nó par para o final
            lastOdd.prox = current;
            current.prox = null;
        }
    }
    public void imprimirLista(){
        No current = head;
        while (current != null){
            System.out.print(current.valor + "");
            current = current.prox;
        }
        System.out.println(" Lista impressa");
    }
}