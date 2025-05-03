package estruturas;


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

    public void removerDoFinal(){
        if (head.prox == null) {
            head = null;
        }
        No current = head;
        while ((current.prox).prox != null) {
            current = current.prox;
        }
        current.prox = null;
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