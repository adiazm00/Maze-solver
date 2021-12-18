import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import org.json.*;

import java.io.FileNotFoundException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.util.NoSuchElementException;

public class Laberinto {
	static Scanner teclado = new Scanner(System.in);

	public static void main(String[] args) throws IOException {
		int opcion;
		BufferedImage lienzoValores;
		char fin = 'a';
		String estrategia = "";
		Problema problema = null;
		JSONObject json = null;
		Stack<Nodo> camino = new Stack<Nodo>();
		do {
			try {
				if (!comprobarRuta())
					throw new FileNotFoundException();
				opcion = elegirOpcion();
				switch (opcion) {
				case 1:
					json = crearLaberinto();
					problema = crearProblema(json);
					break;
				case 2:
					problema = leerProblema();
					json = leerJSON(problema.getMaze());
					break;
				}
				estrategia = elegirEstrategia();
				lienzoValores = crearImagenLoop(json);
				camino = busqueda(problema, 1000000, estrategia, json, lienzoValores);
				if (opcion == 1) {
					crearArchivoMaze(json);
					crearArchivoProblema(problema, json);
				}
				crearArchivoCamino(camino, estrategia, json);
			} catch (FileNotFoundException e) {
				System.out.println("\nERROR: El sistema no ha podido encontrar la ruta o el archivo especificado.");
			} catch (NullPointerException e) {
				System.out.println("\nERROR: El programa no se ha podido ejecutar correctamente.");
			}
			fin = finalizarPrograma();
		} while (fin == 'n');
	}

	// Muestra el menu y devuelve la opcion elegida.
	public static int elegirOpcion() throws IOException {
		System.out.println("Todos los archivos que creados se encontraran en " + System.getProperty("user.dir")
				+ "\\ArchivosPrograma."
				+ "\nEn dicha carpeta tambien deben estar los archivos que sean necesarios para la lectura de un problema.\n");
		int opcion;
		do {
			System.out.println("Introduzca la opcion del programa que desee:" + "\n1) Generar un laberinto aleatorio."
					+ "\n2) Leer un problema.");
			opcion = comprobarExcepcionEntero();
		} while (opcion < 1 || opcion > 2);
		return opcion;
	}

	// Este metodo se encarga de pedir si el cliente si quiere finalizar el
	// programa.
	public static char finalizarPrograma() {
		char fin = 'a';
		do {
			System.out.println("\nDesea finalizar el programa?('s'/'n'):");
			fin = teclado.next().toLowerCase().charAt(0);
		} while (fin != 's' && fin != 'n');
		return fin;
	}

	// Este metodo se encarga de realizar todo el proceso de crear el laberinto
	// mediante llamadas a otros metodos;
	public static JSONObject crearLaberinto() throws IOException {
		Celda actual;
		Celda[][] matriz;
		JSONObject json = new JSONObject();
		matriz = inicializarMatriz(json);
		ArrayList<Celda> camino = new ArrayList<Celda>();
		ArrayList<String> movimientos = new ArrayList<String>();
		matriz = casillaInicial(matriz);
		do {
			movimientos.clear();
			camino.clear();
			actual = casillaAleatoria(matriz);
			camino = algoritmoWilson(matriz, actual, camino, movimientos);
			matriz = ponerVisitado(camino, matriz);
			for (int i = 0; i < movimientos.size(); i++)
				matriz = quitarParedes(movimientos.get(i), matriz, camino.get(i).getFila(), camino.get(i).getColumna());
		} while (repetirAlgoritmo(matriz));
		json = crearJSON(matriz, json);
		return json;
	}

	// Inicializa la matriz pidiendo las filas y las columnas deseadas.
	public static Celda[][] inicializarMatriz(JSONObject json) {
		int filas, columnas;
		Random rnd = new Random();
		do {
			System.out.println("\nIndique el numero de filas del laberinto (1 < filas < 101):");
			filas = comprobarExcepcionEntero();
		} while (filas < 2 || filas > 100);
		json.put("rows", filas);
		do {
			System.out.println("Indique el numero de columnas del laberinto (1 < columnas < 101):");
			columnas = comprobarExcepcionEntero();
		} while (columnas < 2 || columnas > 100);
		json.put("cols", columnas);
		Celda[][] matriz = new Celda[filas][columnas];
		for (int f = 0; f < filas; f++) {
			for (int c = 0; c < columnas; c++) {
				matriz[f][c] = new Celda(false, false, false, false, false, f, c, rnd.nextInt(4));
			}
		}
		return matriz;
	}

	// Comprueba si el valor introducido valido, en este caso si es entero.
	public static int comprobarExcepcionEntero() {
		int opcion = -1;
		try {
			opcion = teclado.nextInt();
		} catch (InputMismatchException ime) {
			System.out.println("\nERROR: El valor introducido debe ser un entero.");
			teclado.next();
		}
		return opcion;
	}

	// Se encarga de escoger la casilla inicial.
	public static Celda[][] casillaInicial(Celda[][] matriz) {
		Random rnd = new Random();
		int filaAux, columnaAux;
		filaAux = rnd.nextInt(matriz.length);
		columnaAux = rnd.nextInt(matriz[0].length);
		matriz[filaAux][columnaAux].setVisitado(true);
		return matriz;
	}

	// Se encarga de escoger una casilla aleatoria de la matriz.
	public static Celda casillaAleatoria(Celda[][] matriz) {
		Random rnd = new Random();
		int filaAux, columnaAux;
		do {
			filaAux = rnd.nextInt(matriz.length);
			columnaAux = rnd.nextInt(matriz[0].length);
		} while (matriz[filaAux][columnaAux].getVisitado());
		return matriz[filaAux][columnaAux];
	}

	// Se trata del algoritmo principal, en este caso es el algoritmo de Wilson
	public static ArrayList<Celda> algoritmoWilson(Celda[][] matriz, Celda actual, ArrayList<Celda> camino,
			ArrayList<String> movimientos) {
		Celda vecino;
		Celda celdaAux;
		while (!actual.getVisitado()) {
			camino.add(actual);
			vecino = buscarVecino(actual, matriz, movimientos);
			actual = vecino;
			if (existeBucle(vecino, camino)) {
				for (int i = camino.size() - 1; i >= 0; i--) {
					celdaAux = camino.get(i);
					camino.remove(i);
					movimientos.remove(i);
					if (celdaAux.equals(vecino)) {
						actual = celdaAux;
					}
				}
			}

		}
		return camino;
	}

	// Se encarga de comprobar si existe un bucle con el nuevo vecino seleccionado.
	public static boolean existeBucle(Celda vecino, ArrayList<Celda> camino) {
		boolean bucle = false;
		Celda celdaAux = null;
		Iterator<Celda> itCamino = camino.iterator();
		while (itCamino.hasNext()) {
			celdaAux = itCamino.next();
			if (vecino.equals(celdaAux))
				bucle = true;
		}
		return bucle;
	}

	// Busca un vecino aleatorio de la casilla actual.
	public static Celda buscarVecino(Celda actual, Celda[][] matriz, ArrayList<String> movimientos) {
		Random rndVecino = new Random();
		String charAux = null;
		Boolean vecinoValido = false;
		Celda vecino = null;
		do {
			switch (rndVecino.nextInt(4)) {
			case 0:
				vecinoValido = comprobarVecino(matriz, actual.getFila() - 1, actual.getColumna());
				if (vecinoValido) {
					charAux = "N";
					vecino = matriz[actual.getFila() - 1][actual.getColumna()];
				}
				break;
			case 1:
				vecinoValido = comprobarVecino(matriz, actual.getFila(), actual.getColumna() + 1);
				if (vecinoValido) {
					charAux = "E";
					vecino = matriz[actual.getFila()][actual.getColumna() + 1];
				}
				break;
			case 2:
				vecinoValido = comprobarVecino(matriz, actual.getFila() + 1, actual.getColumna());
				if (vecinoValido) {
					charAux = "S";
					vecino = matriz[actual.getFila() + 1][actual.getColumna()];
				}
				break;
			case 3:
				vecinoValido = comprobarVecino(matriz, actual.getFila(), actual.getColumna() - 1);
				if (vecinoValido) {
					charAux = "O";
					vecino = matriz[actual.getFila()][actual.getColumna() - 1];
				}
				break;
			}
		} while (!vecinoValido);
		movimientos.add(charAux);
		return vecino;
	}

	// Comprueba si el nuevo vecino se encuentra dentro de la matriz.
	public static Boolean comprobarVecino(Celda[][] matriz, int fila, int columna) {
		Boolean sePuede = true;
		if (fila < 0 || fila > matriz.length - 1 || columna < 0 || columna > matriz[0].length - 1)
			sePuede = false;
		return sePuede;
	}

	// Establece como visitadas las casillas seleccionadas por el algoritmo de
	// Wilson.
	public static Celda[][] ponerVisitado(ArrayList<Celda> camino, Celda[][] matriz) {
		Iterator<Celda> itAux = camino.iterator();
		Celda aux;
		while (itAux.hasNext()) {
			aux = itAux.next();
			matriz[aux.getFila()][aux.getColumna()].setVisitado(true);
		}
		return matriz;
	}

	// Se encarga de establecer cada una de las paredes de cada casilla como true o
	// false.
	public static Celda[][] quitarParedes(String pared, Celda[][] matriz, int fila, int columna) {
		if (pared == "N") {
			matriz[fila][columna].setNorte(true);
			matriz[fila - 1][columna].setSur(true);
		} else if (pared == "E") {
			matriz[fila][columna].setEste(true);
			matriz[fila][columna + 1].setOeste(true);
		} else if (pared == "S") {
			matriz[fila][columna].setSur(true);
			matriz[fila + 1][columna].setNorte(true);
		} else if (pared == "O") {
			matriz[fila][columna].setOeste(true);
			matriz[fila][columna - 1].setEste(true);
		}
		return matriz;
	}

	// Comprueba si no quedan mas casillas sin visitar en la matriz. Si es asi, no
	// se repite mas el algoritmo.
	public static boolean repetirAlgoritmo(Celda[][] matriz) {
		boolean repetir = false;
		for (int f = 0; f < matriz.length; f++) {
			for (int c = 0; c < matriz[0].length; c++) {
				if (!matriz[f][c].getVisitado())
					repetir = true;
			}
		}
		return repetir;
	}

	// Se encarga de leer un fichero JSON.
	public static JSONObject leerJSON(String fichero) throws IOException {
		JSONObject obj;
		try {
			JSONTokener tokener = new JSONTokener(
					new FileReader(System.getProperty("user.dir") + "\\ArchivosPrograma\\" + fichero));
			obj = new JSONObject(tokener);
			obj.getInt("rows");
			obj.getInt("cols");
			obj.getInt("max_n");
			obj.getJSONArray("mov");
			obj.getJSONArray("id_mov");
			obj.getJSONObject("cells");
			if (JSONValido(obj)) {
				System.out.println("\nEl valor de la semantica del archivo maze.json es correcto.");
				return obj;
			} else {
				System.out.println("\nEl valor de la semantica del archivo maze.json es incorrecto.");
				return null;
			}
		} catch (FileNotFoundException e) {
			System.out.println("\nERROR: El archivo maze.json no se ha encontrado.");
			return null;
		} catch (JSONException e) {
			System.out.println("\nERROR: El formato del archivo maze.json no es válido.");
			return null;
		}
	}

	// Se encarga de validar la semantica del JSON.
	public static boolean JSONValido(JSONObject json) {
		boolean valido = true;
		boolean norteAux, esteAux, surAux, oesteAux;
		JSONObject cell;
		JSONArray neighbors;
		JSONObject cells = json.getJSONObject("cells");
		for (int x = 0; x < json.getInt("rows"); x++) {
			for (int y = 0; y < json.getInt("cols"); y++) {
				cell = cells.getJSONObject("(" + x + ", " + y + ")");
				neighbors = cell.getJSONArray("neighbors");
				norteAux = neighbors.getBoolean(0);
				esteAux = neighbors.getBoolean(1);
				surAux = neighbors.getBoolean(2);
				oesteAux = neighbors.getBoolean(3);
				if (x != 0) {
					if (norteAux != cells.getJSONObject("(" + (x - 1) + ", " + y + ")").getJSONArray("neighbors")
							.getBoolean(2))
						valido = false;
				} else {
					if (norteAux)
						valido = false;
				}
				if (y != 0) {
					if (oesteAux != cells.getJSONObject("(" + x + ", " + (y - 1) + ")").getJSONArray("neighbors")
							.getBoolean(1))
						valido = false;
				} else {
					if (oesteAux)
						valido = false;
				}
				if (x == json.getInt("rows") - 1)
					if (surAux)
						valido = false;
				if (y == json.getInt("cols") - 1)
					if (esteAux)
						valido = false;
			}
		}
		return valido;
	}

	// Crea el archivo JSON del laberinto que hemos creado anteriormente.
	public static JSONObject crearJSON(Celda[][] matriz, JSONObject json) throws IOException {
		JSONObject jposicion = new JSONObject();
		anadirConstantesJSON(json);
		for (int f = 0; f < matriz.length; f++) {
			for (int c = 0; c < matriz[0].length; c++) {
				String aux = "(" + f + ", " + c + ")";
				JSONArray jvecinos = new JSONArray();
				JSONObject jceldas = new JSONObject();
				jvecinos.put(matriz[f][c].getNorte());
				jvecinos.put(matriz[f][c].getEste());
				jvecinos.put(matriz[f][c].getSur());
				jvecinos.put(matriz[f][c].getOeste());
				jceldas.put("neighbors", jvecinos);
				jceldas.put("value", matriz[f][c].getValor());
				jposicion.put(aux, jceldas);
			}
		}
		json.put("cells", jposicion);
		return json;
	}

	// Se encarga de crear el archivo .json del laberinto que hemos creado.
	public static void crearArchivoMaze(JSONObject json) throws IOException {
		String rutaescritura = System.getProperty("user.dir") + "\\ArchivosPrograma";
		String file = "problema_" + json.getInt("rows") + "x" + json.getInt("cols") + "_maze.json";
		FileWriter fw = new FileWriter(new File(rutaescritura, file));
		fw.write(json.toString());
		fw.close();
		System.out.println("\nEl archivo problema_" + json.getInt("rows") + "x" + json.getInt("cols")
				+ "_maze.json se ha creado correctamente.");
	}

	// Este metodo añade las constantes que tiene el archivo .json
	public static void anadirConstantesJSON(JSONObject json) {
		JSONArray jpos = new JSONArray();
		JSONArray jmov = new JSONArray();
		jpos.put("N");
		jpos.put("E");
		jpos.put("S");
		jpos.put("O");
		json.put("id_mov", jpos);
		jmov.put("[-1,0]");
		jmov.put("[0,1]");
		jmov.put("[1,0]");
		jmov.put("[0,-1]");
		json.put("mov", jmov);
		json.put("max_n", 4);
	}

	// Se encarga de leer un problema ya creado.
	public static Problema leerProblema() {
		Problema problema = null;
		StringTokenizer st;
		String filaAux, columnaAux;
		int[] initial = new int[2];
		int[] objective = new int[2];
		String maze = null;
		System.out.println("\nIndique el nombre del 'problema' que desee leer sin la extension (p.ej: problema_5x5): ");
		teclado.nextLine();
		String nombreArchivo = teclado.nextLine();
		JSONObject obj;
		try {
			JSONTokener tokener = new JSONTokener(
					new FileReader(System.getProperty("user.dir") + "\\ArchivosPrograma\\" + nombreArchivo + ".json"));
			obj = new JSONObject(tokener);
			st = new StringTokenizer(obj.getString("INITIAL"));
			filaAux = st.nextToken(",");
			columnaAux = st.nextToken(",");
			initial[0] = Integer.parseInt(filaAux.substring(1));
			initial[1] = Integer.parseInt(columnaAux.substring(1, columnaAux.length() - 1));
			st = new StringTokenizer(obj.getString("OBJETIVE"));
			filaAux = st.nextToken(",");
			columnaAux = st.nextToken(",");
			objective[0] = Integer.parseInt(filaAux.substring(1));
			objective[1] = Integer.parseInt(columnaAux.substring(1, columnaAux.length() - 1));
			maze = obj.getString("MAZE");
		} catch (FileNotFoundException e) {
			System.out.println("\nERROR: El archivo problema_YxZ.json no se ha encontrado.");
			return null;
		} catch (JSONException e) {
			System.out.println("\nERROR: Error en el formato del archivo problema_YxZ.json.");
			return null;
		} catch (NumberFormatException e) {
			System.out.println("\nERROR: Error de formato al leer el archivo problema.json.");
			return null;
		} catch (NoSuchElementException e) {
			System.out.println("\nERROR: Error de formato al crear el archivo JSON.");
			return null;
		}
		System.out.println("El valor de la semantica del archivo problema.json es correcto.");
		problema = new Problema(initial, objective, maze);
		return problema;
	}

	// Se encarga de crear un problema pidiendo los datos al cliente.
	public static Problema crearProblema(JSONObject json) throws IOException {
		String maze = "problema_" + json.getInt("rows") + "x" + json.getInt("cols") + "_maze.json";
		int[] initial = pedirInitial(json);
		int[] objective = pedirObjective(json, initial);
		Problema problema = new Problema(initial, objective, maze);
		return problema;
	}

	public static void crearArchivoProblema(Problema problema, JSONObject json) throws IOException {
		JSONObject jproblema = new JSONObject();
		jproblema.put("INITIAL", "(" + problema.getInitial()[0] + ", " + problema.getInitial()[1] + ")");
		jproblema.put("OBJETIVE", "(" + problema.getObjetive()[0] + ", " + problema.getObjetive()[1] + ")");
		jproblema.put("MAZE", problema.getMaze());
		String rutaescritura = System.getProperty("user.dir") + "\\ArchivosPrograma";
		String file = "problema_" + json.getInt("rows") + "x" + json.getInt("cols") + ".json";
		FileWriter fw = new FileWriter(new File(rutaescritura, file));
		fw.write(jproblema.toString());
		fw.close();
		System.out.println("\nEl archivo problema_" + json.getInt("rows") + "x" + json.getInt("cols")
				+ ".json se ha creado correctamente.");
	}

	// Este metodo pide al usuario que indique la casilla inicial del problema.
	public static int[] pedirInitial(JSONObject json) {
		int[] arrayAux = new int[2];
		do {
			System.out.println("\nIndique la fila de la casilla inicial del problema:");
			arrayAux[0] = comprobarExcepcionEntero();
		} while (arrayAux[0] < 0 || arrayAux[0] > (json.getInt("rows") - 1));
		do {
			System.out.println("Indique la columna de la casilla inicial del problema:");
			arrayAux[1] = comprobarExcepcionEntero();
		} while (arrayAux[1] < 0 || arrayAux[1] > (json.getInt("cols") - 1));
		return arrayAux;
	}

	// Este metodo pide al usuario que indique la objetivo inicial del problema.
	public static int[] pedirObjective(JSONObject json, int[] initial) {
		int[] arrayAux = new int[2];
		do {
			System.out.println("\nRecuerde, la casilla objetivo no puede ser la misma que la inicial.");
			do {
				System.out.println("Indique la fila de la casilla objetivo del problema:");
				arrayAux[0] = comprobarExcepcionEntero();
			} while (arrayAux[0] < 0 || arrayAux[0] > (json.getInt("rows") - 1));
			do {
				System.out.println("Indique la columna de la casilla objetivo del problema:");
				arrayAux[1] = comprobarExcepcionEntero();
			} while (arrayAux[1] < 0 || arrayAux[1] > (json.getInt("cols") - 1));
		} while (arrayAux[0] == initial[0] && arrayAux[1] == initial[1]);
		return arrayAux;
	}

	// Este metodo se encarga de realizar la solucion del problema.
	public static Stack<Nodo> busqueda(Problema problema, int profundidad, String estrategia, JSONObject json,
			BufferedImage lienzo) throws IOException {
		ArrayList<int[]> visitado = new ArrayList<int[]>();
		ArrayList<Nodo> nodosHijos = new ArrayList<Nodo>();
		Stack<Nodo> camino = new Stack<Nodo>();
		Stack<Nodo> caminoAux = new Stack<Nodo>();
		Frontera frontera = new Frontera();
		Nodo nodo = new Nodo();
		Nodo nodoAux = new Nodo();
		nodoAux.setId("None");
		nodo.set_Padre(nodoAux);
		nodo.setId_Estado(problema.getInitial());
		nodo.setCosto(0);
		nodo.setProfundidad(0);
		nodo.setAccion("None");
		nodo.setHeuristica(calcularHeuristica(problema, nodo.getId_Estado()));
		nodo.setValor(calcularValor(estrategia, nodo));
		frontera.insertarNodo(nodo);
		boolean solucion = false;
		while (!frontera.getNodosOrdenados().isEmpty() && !solucion) {
			nodo = frontera.obtenerPrimerNodo();
			if (problema.funcionObjetivo(nodo.getId_Estado()))
				solucion = true;
			else if (!comprobarVisitado(visitado, nodo) && nodo.getProfundidad() < profundidad) {
				visitado.add(nodo.getId_Estado());
				nodosHijos = expandirNodo(problema, nodo, estrategia, json);
				for (int i = 0; i < nodosHijos.size(); i++) {
					frontera.insertarNodo(nodosHijos.get(i));
				}
			}
		}
		if (solucion) {
			while (nodo.get_Padre() != null) {
				camino.add(nodo);
				caminoAux.add(nodo);
				nodo = nodo.get_Padre();
			}
			crearImagenSolucion(json, frontera, caminoAux, visitado, estrategia, lienzo);
			return camino;
		} else {
			System.out.println("No existe un camino entre " + Arrays.toString(problema.getInitial()) + " y "
					+ Arrays.toString(problema.getObjetive()) +".");
			return null;
		}
	}

	// Este metodo se encarga de comprobar si el nodo esta visitado.
	public static boolean comprobarVisitado(ArrayList<int[]> visitados, Nodo nodo) {
		boolean esta = false;
		for (int i = 0; i < visitados.size(); i++) {
			if (nodo.getId_Estado()[0] == visitados.get(i)[0] && nodo.getId_Estado()[1] == visitados.get(i)[1])
				esta = true;
		}
		return esta;
	}

	// Este metodo se encarga de expandir un nodo.
	public static ArrayList<Nodo> expandirNodo(Problema problema, Nodo nodo, String estrategia, JSONObject maze) {
		ArrayList<Nodo> nodosHijos = new ArrayList<Nodo>();
		ArrayList<String[]> sucesores = problema.funcionSucesor(nodo.getId_Estado(), maze);
		for (int i = 0; i < sucesores.size(); i++) {
			Nodo nodoHijo = new Nodo();
			String[] estadoHijo = sucesores.get(i)[1].split(",");
			String accionHijo = sucesores.get(i)[0];
			int costoHijo = Integer.parseInt(sucesores.get(i)[2]);
			nodoHijo.setId_Estado(new int[] { Integer.parseInt(estadoHijo[0]), Integer.parseInt(estadoHijo[1]) });
			nodoHijo.set_Padre(nodo);
			nodoHijo.setAccion(accionHijo);
			nodoHijo.setProfundidad(nodo.getProfundidad() + 1);
			nodoHijo.setCosto(nodo.getCosto() + costoHijo);
			nodoHijo.setHeuristica(calcularHeuristica(problema, nodoHijo.getId_Estado()));
			nodoHijo.setValor(calcularValor(estrategia, nodoHijo));
			nodosHijos.add(nodoHijo);
		}
		return nodosHijos;
	}

	// Este metodo calcula la heuristica del nodo.
	public static int calcularHeuristica(Problema problema, int[] estado) {
		return (Math.abs(estado[0] - problema.getObjetive()[0]) + Math.abs(estado[1] - problema.getObjetive()[1]));
	}

	// Se encarga de calcular el valor del nodo dependiendo de la estrategia.
	public static float calcularValor(String estrategia, Nodo nodo) {
		float valor = 0;
		switch (estrategia) {
		case "BREADTH":
			valor = nodo.getProfundidad();
			break;
		case "DEPTH":
			valor = (float) 1 / (nodo.getProfundidad() + 1);
			break;
		case "UNIFORM":
			valor = nodo.getCosto();
			break;
		case "GREEDY":
			valor = nodo.getHeuristica();
			break;
		case "A":
			valor = nodo.getCosto() + nodo.getHeuristica();
			break;
		}
		return valor;
	}

	// Este metodo pide al usuario que selccione la estrategia que quiere utilizar.
	public static String elegirEstrategia() {
		int opcion = 0;
		String estrategia = "";
		do {
			System.out.println(
					"\nSeleccione la estrategia que desea utilizar para resolver el laberinto:" + "\n1) Anchura."
							+ "\n2) Profundidad acotada." + "\n3) Coste uniforme." + "\n4) Voraz." + "\n5) A*.");
			opcion = comprobarExcepcionEntero();
		} while (opcion < 1 || opcion > 5);
		switch (opcion) {
		case 1:
			estrategia = "BREADTH";
			break;
		case 2:
			estrategia = "DEPTH";
			break;
		case 3:
			estrategia = "UNIFORM";
			break;
		case 4:
			estrategia = "GREEDY";
			break;
		case 5:
			estrategia = "A";
			break;
		}
		return estrategia;
	}

	// Este metodo crea el archivo .txt con la solucion del problema.
	public static void crearArchivoCamino(Stack<Nodo> camino, String estrategia, JSONObject json) throws IOException {
		String ruta = System.getProperty("user.dir") + "\\ArchivosPrograma";
		String file = "sol_" + json.getInt("rows") + "x" + json.getInt("cols") + "_" + estrategia + ".txt";
		String contenido = "";
		contenido += "[id][cost,state,father_id,action,depth,h,value]";
		while (!camino.isEmpty())
			contenido += "\n" + camino.pop().toString();
		FileWriter fw = new FileWriter(new File(ruta, file));
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(contenido);
		bw.close();
		System.out.println("\nEl archivo sol_" + json.getInt("rows") + "x" + json.getInt("cols") + "_" + estrategia
				+ ".txt se ha creado correctamente.");
	}
	
	//Este metodo se encarga de comprobar si la ruta especificada existe o no. Si no existe, se propone crearla.
	public static boolean comprobarRuta() {
		boolean existe = true;
		char crearCarpeta = 'a';
		File ruta = new File(System.getProperty("user.dir") + "\\ArchivosPrograma");
		if (!ruta.exists()) {
			System.out.println(
					"Para ejecutar el programa es necesario que exista la carpeta 'ArchivosPrograma' en la ruta '"
							+ System.getProperty("user.dir") + "\\'.");
			do {
				System.out.println("¿Desea crear dicha carpeta?('s'/'n'):");
				crearCarpeta = teclado.next().toLowerCase().charAt(0);
			} while (crearCarpeta != 's' && crearCarpeta != 'n');
			if (crearCarpeta == 's') {
				ruta.mkdir();
				System.out.println("Se ha creado la carpeta correctamente.\n");
			} else if (crearCarpeta == 'n')
				existe = false;
		}
		return existe;
	}

	//Este metodo se encarga de crear la imagen en la cual se aprecia la solucion.
	public static void crearImagenSolucion(JSONObject json, Frontera frontera, Stack<Nodo> camino,
			ArrayList<int[]> visitado, String estrategia, BufferedImage lienzo) throws IOException {
		try {
			int longCelda;
			JSONArray neighbors;
			JSONObject cells = json.getJSONObject("cells");
			Nodo nodoAux;
			if ((float) json.getInt("rows") < json.getInt("cols"))
				longCelda = 500 / json.getInt("cols");
			else
				longCelda = 500 / json.getInt("rows");
			Graphics2D pincelParedes = lienzo.createGraphics();
			pincelParedes.setStroke(new BasicStroke(2));
			pincelParedes.setColor(java.awt.Color.BLACK);
			Graphics2D pincelSolucion = lienzo.createGraphics();
			pincelSolucion.setStroke(new BasicStroke(2));
			pincelSolucion.setColor(java.awt.Color.RED);
			Graphics2D pincelFrontera = lienzo.createGraphics();
			pincelFrontera.setStroke(new BasicStroke(2));
			pincelFrontera.setColor(java.awt.Color.BLUE);
			Graphics2D pincelArbolInterior = lienzo.createGraphics();
			pincelArbolInterior.setStroke(new BasicStroke(2));
			pincelArbolInterior.setColor(java.awt.Color.GREEN);
			while (!frontera.getNodosOrdenados().isEmpty()) {
				nodoAux = frontera.obtenerPrimerNodo();
				pincelFrontera.fillRect(nodoAux.getId_Estado()[1] * longCelda, nodoAux.getId_Estado()[0] * longCelda,
						longCelda, longCelda);
			}
			for (int i = 0; i < visitado.size(); i++) {
				pincelArbolInterior.fillRect(visitado.get(i)[1] * longCelda, visitado.get(i)[0] * longCelda, longCelda,
						longCelda);
			}
			while (!camino.isEmpty()) {
				nodoAux = camino.pop();
				pincelSolucion.fillRect(nodoAux.getId_Estado()[1] * longCelda, nodoAux.getId_Estado()[0] * longCelda,
						longCelda, longCelda);
			}
			for (int x = 0; x < json.getInt("rows"); x++) {
				for (int y = 0; y < json.getInt("cols"); y++) {
					JSONObject cell = cells.getJSONObject("(" + x + ", " + y + ")");
					neighbors = cell.getJSONArray("neighbors");
					if (!neighbors.getBoolean(0))
						pincelParedes.drawLine(y * longCelda, x * longCelda, (y + 1) * longCelda, x * longCelda);
					if (!neighbors.getBoolean(1))
						pincelParedes.drawLine((y + 1) * longCelda, x * longCelda, (y + 1) * longCelda,
								(x + 1) * longCelda);
					if (!neighbors.getBoolean(2))
						pincelParedes.drawLine(y * longCelda, (x + 1) * longCelda, (y + 1) * longCelda,
								(x + 1) * longCelda);
					if (!neighbors.getBoolean(3))
						pincelParedes.drawLine(y * longCelda, x * longCelda, y * longCelda, (x + 1) * longCelda);
				}
			}
			ImageIO.write(lienzo, "png", new File(System.getProperty("user.dir") + "\\ArchivosPrograma\\solution_"
					+ json.getInt("rows") + "x" + json.getInt("cols") + "_" + estrategia + ".png"));
			System.out.println("\nEl archivo solution_" + json.getInt("rows") + "x" + json.getInt("cols") + "_"
					+ estrategia + ".png se ha creado correctamente.");
		} catch (FileNotFoundException e) {
			System.out.println("\nERROR: No se ha encontrado la ruta especificada.");
		}
	}

	// Se encarga de crear la imagen que muestra el valor de cada casilla mediante
	// colores.
	public static BufferedImage crearImagenLoop(JSONObject json) throws JSONException, IOException {
		int value;
		JSONArray neighbors;
		JSONObject cells = json.getJSONObject("cells");
		int longCelda;
		if ((float) json.getInt("rows") < json.getInt("cols"))
			longCelda = 500 / json.getInt("cols");
		else
			longCelda = 500 / json.getInt("rows");
		BufferedImage lienzoLoop = new BufferedImage((int) (longCelda * json.getInt("cols") * 1.1),
				(int) (longCelda * json.getInt("rows") * 1.1), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D pincelParedesLoop = lienzoLoop.createGraphics();
		pincelParedesLoop.setStroke(new BasicStroke(2));
		pincelParedesLoop.setColor(java.awt.Color.BLACK);
		Graphics2D pincelHierba = lienzoLoop.createGraphics();
		pincelHierba.setStroke(new BasicStroke(2));
		pincelHierba.setColor(new Color(102, 255, 102));
		Graphics2D pincelAsfalto = lienzoLoop.createGraphics();
		pincelAsfalto.setStroke(new BasicStroke(2));
		pincelAsfalto.setColor(java.awt.Color.WHITE);
		Graphics2D pincelTierra = lienzoLoop.createGraphics();
		pincelTierra.setStroke(new BasicStroke(2));
		pincelTierra.setColor(new Color(245, 222, 179));
		Graphics2D pincelAgua = lienzoLoop.createGraphics();
		pincelAgua.setStroke(new BasicStroke(2));
		pincelAgua.setColor(new Color(51, 204, 255));
		for (int x = 0; x < json.getInt("rows"); x++) {
			for (int y = 0; y < json.getInt("cols"); y++) {
				JSONObject cell = cells.getJSONObject("(" + x + ", " + y + ")");
				neighbors = cell.getJSONArray("neighbors");
				value = cell.getInt("value");
				if (value == 0)
					pincelAsfalto.fillRect(y * longCelda, x * longCelda, longCelda, longCelda);
				if (value == 1)
					pincelTierra.fillRect(y * longCelda, x * longCelda, longCelda, longCelda);
				if (value == 2)
					pincelHierba.fillRect(y * longCelda, x * longCelda, longCelda, longCelda);
				if (value == 3)
					pincelAgua.fillRect(y * longCelda, x * longCelda, longCelda, longCelda);
				if (!neighbors.getBoolean(0))
					pincelParedesLoop.drawLine(y * longCelda, x * longCelda, (y + 1) * longCelda, x * longCelda);
				if (!neighbors.getBoolean(1))
					pincelParedesLoop.drawLine((y + 1) * longCelda, x * longCelda, (y + 1) * longCelda,
							(x + 1) * longCelda);
				if (!neighbors.getBoolean(2))
					pincelParedesLoop.drawLine(y * longCelda, (x + 1) * longCelda, (y + 1) * longCelda,
							(x + 1) * longCelda);
				if (!neighbors.getBoolean(3))
					pincelParedesLoop.drawLine(y * longCelda, x * longCelda, y * longCelda, (x + 1) * longCelda);
			}
		}
		ImageIO.write(lienzoLoop, "png", new File(System.getProperty("user.dir") + "\\ArchivosPrograma\\puzzle_loop_"
				+ json.getInt("rows") + "x" + json.getInt("cols") + ".png"));
		System.out.println("\nEl archivo puzzle_loop_" + json.getInt("rows") + "x" + json.getInt("cols")
				+ ".png se ha creado correctamente.");
		return lienzoLoop;
	}
}