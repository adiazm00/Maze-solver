
public class Nodo implements Comparable<Nodo> {
	private int costo, profundidad, heuristica;
	private int[] id_Estado;
	private String accion, id;
	private Nodo padre;
	private float valor;

	public void setCosto(int costo) {
		this.costo = costo;
	}

	public void setProfundidad(int profundidad) {
		this.profundidad = profundidad;
	}

	public void setHeuristica(int heuristica) {
		this.heuristica = heuristica;
	}

	public void setValor(float valor) {
		this.valor = valor;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public void setId_Estado(int[] id_Estado) {
		this.id_Estado = id_Estado;
	}

	public void set_Padre(Nodo padre) {
		this.padre = padre;

	}

	public int getCosto() {
		return costo;
	}

	public int getProfundidad() {
		return profundidad;
	}

	public int getHeuristica() {
		return heuristica;
	}

	public float getValor() {
		return valor;
	}

	public String getId() {
		return id;
	}

	public Nodo get_Padre() {
		return padre;
	}

	public String getAccion() {
		return accion;
	}

	public int[] getId_Estado() {
		return id_Estado;
	}

	@Override
	public int compareTo(Nodo nodo) {
		int i = 0;
		if (nodo.getValor() < getValor())
			i = +1;
		else if (nodo.getValor() > getValor())
			i = -1;
		else {
			if (nodo.getId_Estado()[0] < getId_Estado()[0])
				i = +1;
			else if (nodo.getId_Estado()[0] > getId_Estado()[0])
				i = -1;
			else {
				if (nodo.getId_Estado()[1] < getId_Estado()[1])
					i = +1;
				else if (nodo.getId_Estado()[1] > getId_Estado()[1])
					i = -1;
				else {
					if(Integer.parseInt(nodo.getId()) < Integer.parseInt(getId()))
						i = +1;
					else if (Integer.parseInt(nodo.getId()) > Integer.parseInt(getId()))
						i = -1;
				}
			}
		}
		return i;
	}

	@Override
	public String toString() {
		return "[" + id + "][" + costo + ",(" + id_Estado[0] + ", " + id_Estado[1] + ")," + padre.getId() + "," + accion
				+ "," + profundidad + "," + heuristica + "," + valor + "]";
	}
}