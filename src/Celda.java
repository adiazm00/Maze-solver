public class Celda {
	private boolean norte, sur, este, oeste, visitado;
	public int fila, columna, valor;

	public Celda(boolean norte, boolean sur, boolean este, boolean oeste, boolean visitado, int fila, int columna,
			int valor) {
		this.norte = norte;
		this.sur = sur;
		this.este = este;
		this.oeste = oeste;
		this.visitado = visitado;
		this.fila = fila;
		this.columna = columna;
		this.valor = valor;
	}

	public int getValor() {
		return valor;
	}

	public void setValor(int valor) {
		this.valor = valor;
	}

	public boolean getVisitado() {
		return visitado;
	}

	public void setVisitado(boolean visitado) {
		this.visitado = visitado;
	}

	public boolean getNorte() {
		return norte;
	}

	public void setNorte(boolean norte) {
		this.norte = norte;
	}

	public boolean getSur() {
		return sur;
	}

	public void setSur(boolean sur) {
		this.sur = sur;
	}

	public boolean getEste() {
		return este;
	}

	public void setEste(boolean este) {
		this.este = este;
	}

	public boolean getOeste() {
		return oeste;
	}

	public void setOeste(boolean oeste) {
		this.oeste = oeste;
	}

	public int getFila() {
		return fila;
	}

	public void setFila(int fila) {
		this.fila = fila;
	}

	public int getColumna() {
		return columna;
	}

	public void setColumna(int columna) {
		this.columna = columna;
	}
}