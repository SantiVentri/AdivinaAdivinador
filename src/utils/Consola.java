package utils;

import java.util.Scanner;

public final class Consola {
	private Consola() {
	}

	// Frena la ejecución hasta que el usuario presione Enter, para darle tiempo
	// a leer lo que pasó en el turno anterior.
	public static void esperarEnter(Scanner scanner) {
		System.out.print("\n(Presioná Enter para continuar...)");
		scanner.nextLine();
	}
}
