package main;

import java.util.Scanner;

public class Main {

	private static final Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		System.out.println("----------------------------------------");
		System.out.println("     Bienvenido a AdivinaAdivinador     ");
		System.out.println("----------------------------------------");

		String nombre = pedirNombre();

		boolean salir = false;
		while (!salir) {
			int opcion = mostrarMenuPrincipal();

			switch (opcion) {
				case 1 -> menuModos(nombre);
				case 2 -> mostrarPuntajes();
				case 3 -> salir = true;
				default -> System.out.println("Opción inválida.");
			}
		}

		System.out.println("\nSaliendo...");
		System.out.println("¡Gracias por jugar!");
	}

	private static int mostrarMenuPrincipal() {
		System.out.println("\nSelecciona una opción para continuar:");
		System.out.println("1. Jugar modos");
		System.out.println("2. Ver puntajes");
		System.out.println("3. Salir");
		return leerOpcion();
	}

	private static void menuModos(String nombre) {
		boolean volver = false;
		while (!volver) {
			System.out.println("\nSelecciona un modo para empezar a jugar:");
			System.out.println("1. Jugador vs. Máquina");
			System.out.println("2. Máquina vs. Máquina (espectador)");
			System.out.println("3. Volver atrás");
			int opcion = leerOpcion();

			switch (opcion) {
				case 1 -> menuOponente(nombre);
				case 2 -> iniciarMaquinaVsMaquina();
				case 3 -> volver = true;
				default -> System.out.println("Opción inválida.");
			}
		}
	}

	private static void menuOponente(String nombre) {
		boolean volver = false;
		while (!volver) {
			System.out.println("\nSelecciona tu oponente:");
			System.out.println("1. Máquina Asertiva (Intentará adivinar con preguntas)");
			System.out.println("2. Máquina Aleatoria (Arriesga en cada turno)");
			System.out.println("3. Volver atrás");
			int opcion = leerOpcion();

			switch (opcion) {
				case 1 -> iniciarJugadorVsMaquina(nombre, "Asertiva");
				case 2 -> iniciarJugadorVsMaquina(nombre, "Aleatoria");
				case 3 -> volver = true;
				default -> System.out.println("Opción inválida.");
			}
		}
	}

	private static int leerOpcion() {
		System.out.print("\n>> ");
		try {
			return Integer.parseInt(scanner.nextLine().trim());
		} catch (NumberFormatException e) {
			return -1; // fuerza el caso default
		}
	}

	private static String pedirNombre() {
		while (true) {
			System.out.print("\nIngresá tu nombre:\n>> ");
			String nombre = scanner.nextLine().trim();

			if (nombre.length() < 3) {
				System.out.println("Error: El nombre debe tener mínimo 3 letras.");
			} else if (nombre.length() > 12) {
				System.out.println("Error: El nombre debe tener menos de 12 letras.");
			} else {
				return nombre;
			}
		}
	}

	private static void iniciarJugadorVsMaquina(String nombre, String tipoMaquina) { }
	private static void iniciarMaquinaVsMaquina() { }
	private static void mostrarPuntajes() { }
}