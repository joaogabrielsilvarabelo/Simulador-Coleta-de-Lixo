package tads;

public class ListaDuplamenteEncadeadaCircular {
     No head;
     No tail;

    public ListaDuplamenteEncadeadaCircular() {
        this.head = null;
        this.tail = null;
        //tail.prox = head;
        //head.prox = tail;
    }


    public void inserirNoInicio(String nome) {
        No current = new No(nome);
        if (head == null) { //Ja que isso é na condição que nao tem elemento então tudo aponta para o mesmo lugar
            head = current;
            tail = current;
            head.prox = head;
            head.ant = head;
        } else { //Aqui é mais complicado
            current.prox = head;
            current.ant = tail;
            //Isso é para dar o "valor" do head e do tail para começar e por enquanto a lista fica assim: tail - current - head
            head.ant = current;
            tail.prox = current;
            //Isso pode parecer redundante mas tem sempre que lembrar que os ponteiros de cada nó, são independentes um do outro, então tem que
            //configurar para que os ponteiros de cada elemento apontem para onde devem
            head = current;
            //Aqui finalmente insere o elemento no inicio
        }
    }

    public void inserirNoFinal(String nome) { //Não acredito que era só pegar o inserirNoInicio e trocar o head final pelo tail aaaaaaa
        No current = new No(nome); //Como falei antes, esse negócio é só o inserirNoInicio, com o final inserindo o elemento no tail (final)
        //Ao invés de head (inicio)
        if (head == null) {
            head = current;
            tail = current;
            head.prox = head;
            head.ant = head;
        } else {
            current.prox = head;
            current.ant = tail;
            head.ant = current;
            tail.prox = current;
            tail = current;
        }
    }

    public void moverParaFinal(int valor) {
        //Como sempre, tem que considerar listas nulas ou com um elemento ele retorna imediatamente, pois não pode fazer nada
        if (head == null || head == tail) return;
        //Aqui um do while para fazer um loop de percorrer a lista
        // Com o current começando do head e continuando o loop até chegar no head de novo (circular)
        No current = head;
        do {
            if (current.valor == valor) { //Esse é a condicional para quando o código achar o valor escolhido para mover
                if (current == tail) return; //Aqui é caso ele já está no final ele retorna imediatamente porque não precisa fazer nada

                (current.ant).prox = current.prox;
                (current.prox).ant = current.ant;
                //Essa parte é a que tira o current da posição atual dele, ele pega os ponteiros dos nós vizinhos ao current
                //E os troca para os ponteiros do current, assim "tirando" o current da posição dele
                // Mais ou menos assim
                // Antes: current.ant - current - current.prox --> Depois: current.ant - current.prox

                if (current == head) { // Se o nó removido for o head, o próximo nó vai ser o novo head
                    head = current.prox;
                }
                //Essa parte vai mover o nó para o final realmente
                //Antes: tail <-> head
                tail.prox = current;
                // tail -> current - head
                // |-----------------|
                current.ant = tail;
                current.prox = head;
                // tail <-> current -> head
                head.ant = current;
                // tail <-> current <-> head
                tail = current;
                //O current vira o novo tail
                return;
            }
            current = current.prox;
        } while (current != head);
    }

    //Esse negócio busca o No requerido e retorna ele
    private No buscarNo(int valor) {
        if (head == null) return null; //Auto explicativo, se head for null, vai ter que retornar null também
        //Agora o loop com do while para fazer a varredura pela lista
        No current = head;
        do {
            if (current.valor == valor) {
                return current;
            }
            current = current.prox;
        } while (current != head);
        return null;
    }

    public void deslocarNos(No inicio, No fim) {
        if (inicio == null || fim == null) return;

        // Remove o segmento da sua posição atual:
        (inicio.ant).prox = fim.prox;
        (fim.prox).ant = inicio.ant;

        // Se o segmento inclui o head, atualiza o head
        if (inicio == head) {
            head = fim.prox;
        }

        // Anexa o segmento ao final:
        tail.prox = inicio;
        inicio.ant = tail;
        tail = fim;

        // Reestabelece a circularidade:
        tail.prox = head;
        head.ant = tail;
    }

    //Esse negócio usa a sobrecarga lá de POO para eu poder só botar os valores certinhos no main
    public void deslocarNos(int valorInicio, int valorFim) {
        //Cria novos nós inicio e fim cujos valores são buscados com o buscarNo até encontrar o valor na lista
        No inicio = buscarNo(valorInicio);
        No fim = buscarNo(valorFim);

        //Se não encontrar, o sistema informa o usuário
        if (inicio == null || fim == null) {
            System.out.println("Um ou ambos os nós não foram encontrados.");
            return;
        }

        deslocarNos(inicio, fim);
    }

    public void imprimirLista() {
        if (head == null) { // Isso aqui é pra quando a lista for vazia (lembra de fazer isso em quase tudo pra nao dar o NullPointerException
            System.out.println("A lista ta vazia gg ez");
            return;
        } else {
            No current = head; //Começa normal que nem o outro
            do { //O do while é pra o current começar como head e depois ir indo, pq nn da certo se for que nem uma lista nao circular
                System.out.print(current.valor + "<->");
                current = current.prox;
            }
            while (current != head);

            System.out.println(" ");
        }
        System.out.println(" Lista impressa");
    }
}





