import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Problema {
	private String maze;
	private int[] initial;
	private int[] objetive;

	public Problema(int[] initial, int[] objetive, String maze) {
		this.initial = initial;
		this.objetive = objetive;
		this.maze = maze;
	}

	public int[] getInitial() {
		return initial;
	}

	public void setInitial(int[] initial) {
		this.initial = initial;
	}

	public int[] getObjetive() {
		return objetive;
	}

	public void setObjetive(int[] objetive) {
		this.objetive = objetive;
	}

	public String getMaze() {
		return maze;
	}

	public void setMaze(String maze) {
		this.maze = maze;
	}

	public ArrayList<String[]> funcionSucesor(int[] estado, JSONObject json) {
		ArrayList<String[]> sucesores = new ArrayList<String[]>();
		int fila = estado[0], columna = estado[1];
		int value;
		JSONArray vecinos = json.getJSONObject("cells").getJSONObject("(" + fila + ", " + columna + ")")
				.getJSONArray("neighbors");
		if ((vecinos.getBoolean(0)) && ((fila - 1) >= 0)) {
			value = json.getJSONObject("cells").getJSONObject("(" + (fila - 1) + ", " + columna + ")").getInt("value");
			sucesores.add(new String[] { "N", (fila - 1) + "," + columna, String.valueOf(value + 1) });
		}
		if ((vecinos.getBoolean(1)) && ((columna + 1 < json.getInt("cols")))) {
			value = json.getJSONObject("cells").getJSONObject("(" + fila + ", " + (columna + 1) + ")").getInt("value");
			sucesores.add(new String[] { "E", fila + "," + (columna + 1), String.valueOf(value + 1) });
		}
		if ((vecinos.getBoolean(2)) && ((fila + 1 < json.getInt("rows")))) {
			value = json.getJSONObject("cells").getJSONObject("(" + (fila + 1) + ", " + columna + ")").getInt("value");
			sucesores.add(new String[] { "S", (fila + 1) + "," + columna, String.valueOf(value + 1) });
		}
		if ((vecinos.getBoolean(3)) && ((columna - 1 >= 0))) {
			value = json.getJSONObject("cells").getJSONObject("(" + fila + ", " + (columna - 1) + ")").getInt("value");
			sucesores.add(new String[] { "O", fila + "," + (columna - 1), String.valueOf(value + 1) });
		}
		return sucesores;
	}

	public boolean funcionObjetivo(int[] estadoActual) {
		boolean esObjetivo = false;
		if (estadoActual[0] == getObjetive()[0] && estadoActual[1] == getObjetive()[1])
			esObjetivo = true;
		return esObjetivo;
	}
}