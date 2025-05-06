package estruturas;


public class ListaEncadeada {
    private No head;
    int tamanho;

    public ListaEncadeada(){
        this.head = null;
    }

    public void inserirNoInicio(int dado) {
        No novoNo = new No(dado);
        novoNo.prox = head;
        head = novoNo;
    }

    public void inserirNoFinal(int dado){
        No novoNo = new No(dado);
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
            System.out.print(current.dado + "");
            current = current.prox;
        }
        System.out.println(" Lista impressa");
    }
}