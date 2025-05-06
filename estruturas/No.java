package estruturas;

@SuppressWarnings("rawtypes")
class No<T> {
    No prox;
    No ant;
    T dado;

    public No(T dado){
        this.dado = dado;
        this.prox = null;
        this.ant = null;
    }

}
