package players;

import java.util.Scanner;

import model.CasaHogwarts;
import model.ColorPelo;
import model.Edad;
import model.FiltroAplicado;
import model.Genero;
import model.Personaje;
import model.SangreLimpia;
import model.Tablero;
import model.TipoFiltro;

public class JugadorHumano extends Jugador {
	private final Scanner scanner;

	public JugadorHumano(String nombre, Tablero tablero, Scanner scanner) {
		super(nombre, tablero);
		this.scanner = scanner;
	}

	@Override
	public FiltroAplicado hacerPregunta() {
		TipoFiltro tipo = elegirTipoFiltro();
		String valor = elegirValor(tipo);
		return new FiltroAplicado(tipo, valor);
	}

	@Override
	public Personaje arriesgarPersonaje() {
		getTablero().mostrar();

		while (true) {
			System.out.println("\nEs tu turno. ¿Qué querés hacer?");
			System.out.println("1. Arriesgar un personaje");
			System.out.println("2. Hacer una pregunta");
			int opcion = leerOpcionEnRango(2);

			if (opcion == 2) {
				return null;
			}

			System.out.print("\nEscribí el nombre del personaje (o 0 para volver):\n>> ");
			String entrada = scanner.nextLine().trim();

			if (entrada.equals("0")) {
				continue;
			}

			Personaje elegido = getTablero().buscarPorNombre(entrada);
			if (elegido == null) {
				System.out.println("No encontré ese personaje entre los restantes.");
				continue;
			}
			return elegido;
		}
	}

	private TipoFiltro elegirTipoFiltro() {
		TipoFiltro[] tipos = TipoFiltro.values();
		System.out.println("\n¿Sobre qué característica querés preguntar?");
		for (int i = 0; i < tipos.length; i++) {
			System.out.println((i + 1) + ". " + tipos[i]);
		}
		int opcion = leerOpcionEnRango(tipos.length);
		return tipos[opcion - 1];
	}

	private String elegirValor(TipoFiltro tipo) {
		switch (tipo) {
			case GENERO:
				return elegirValorEnum(Genero.values());
			case EDAD:
				return elegirValorEnum(Edad.values());
			case COLOR_PELO:
				return elegirValorEnum(ColorPelo.values());
			case CASA_HOGWARTS:
				return elegirValorEnum(CasaHogwarts.values());
			case SANGRE_LIMPIA:
				return elegirValorEnum(SangreLimpia.values());
			case CALVICIE:
				return elegirValorBooleano("¿Es calvo/a?");
			case LENTES:
				return elegirValorBooleano("¿Usa lentes?");
			case ALUMNO:
				return elegirValorBooleano("¿Es alumno/a?");
			default:
				throw new IllegalStateException("Tipo de filtro no soportado: " + tipo);
		}
	}

	private <T extends Enum<T>> String elegirValorEnum(T[] valores) {
		System.out.println("\nElegí un valor:");
		for (int i = 0; i < valores.length; i++) {
			System.out.println((i + 1) + ". " + valores[i]);
		}
		int opcion = leerOpcionEnRango(valores.length);
		return valores[opcion - 1].name();
	}

	private String elegirValorBooleano(String pregunta) {
		System.out.println("\n" + pregunta);
		System.out.println("1. Sí");
		System.out.println("2. No");
		int opcion = leerOpcionEnRango(2);
		return String.valueOf(opcion == 1);
	}

	private int leerOpcionEnRango(int max) {
		while (true) {
			System.out.print("\n>> ");
			try {
				int opcion = Integer.parseInt(scanner.nextLine().trim());
				if (opcion >= 1 && opcion <= max) {
					return opcion;
				}
			} catch (NumberFormatException e) {
				// se vuelve a pedir
			}
			System.out.println("Opción inválida, probá de nuevo.");
		}
	}
}
