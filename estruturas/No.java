package estruturas;

public class No<T> {
    protected No<T> prox;
    protected No<T> ant;
    protected T dado;

    public No(T dado){
        this.dado = dado;
        this.prox = null;
        this.ant = null;
    }
}

