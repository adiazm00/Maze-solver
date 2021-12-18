import java.util.PriorityQueue;

public class Frontera {
	private int id = -1;
	private PriorityQueue<Nodo> nodosOrdenados = new PriorityQueue<Nodo>();

	public PriorityQueue<Nodo> getNodosOrdenados() {
		return nodosOrdenados;
	}

	public void insertarNodo(Nodo nodo) {
		nodo.setId(String.valueOf(++id));
		nodosOrdenados.add(nodo);
	}

	public Nodo obtenerPrimerNodo() {
		return nodosOrdenados.poll();
	}
}