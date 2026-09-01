package tests;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;
import players.JugadorHumano;
import utils.PersonajeFactory;

public class JugadorHumanoTest {

	static JugadorHumano nuevo(String guion, List<Personaje> personajes) {
		Scanner sc = new Scanner(new ByteArrayInputStream(guion.getBytes(StandardCharsets.UTF_8)));
		JugadorHumano j = new JugadorHumano("Tester", new Tablero(personajes), sc);
		j.elegirPersonaje(personajes.get(0));
		return j;
	}

	public static void run() {
		List<Personaje> personajes = PersonajeFactory.crearPersonajes();

		T.section("JugadorHumano.arriesgarPersonaje - camino 'hacer pregunta'");
		T.mute();
		JugadorHumano j1 = nuevo("2\n", personajes); // opción 2 = hacer pregunta
		Personaje r1 = j1.arriesgarPersonaje();
		T.unmute();
		T.check(r1 == null, "elegir 'hacer pregunta' -> arriesgarPersonaje devuelve null");

		T.section("JugadorHumano.arriesgarPersonaje - arriesgar por nombre");
		T.mute();
		JugadorHumano j2 = nuevo("1\nHarry Potter\n", personajes); // 1=arriesgar, nombre
		Personaje r2 = j2.arriesgarPersonaje();
		T.unmute();
		T.check(r2 != null && r2.getNombre().equals("Harry Potter"), "arriesga 'Harry Potter' correctamente");

		T.section("JugadorHumano.arriesgarPersonaje - reintenta con entradas inválidas");
		T.mute();
		// basura, opción fuera de rango, después 2 (hacer pregunta)
		JugadorHumano j3 = nuevo("xyz\n9\n0\n-3\n2\n", personajes);
		Throwable t3 = T.capture(() -> {
			Personaje r = j3.arriesgarPersonaje();
			if (r != null) {
				throw new AssertionError("devolvió personaje, esperaba null");
			}
		});
		T.unmute();
		T.check(t3 == null, "menú de acción tolera basura y reintenta hasta una opción válida");

		T.section("JugadorHumano.arriesgarPersonaje - nombre inexistente y '0' para volver");
		T.mute();
		JugadorHumano j4 = nuevo("1\nNo Existe\n1\n0\n2\n", personajes);
		Throwable t4 = T.capture(() -> j4.arriesgarPersonaje());
		T.unmute();
		T.check(t4 == null, "nombre inexistente -> avisa y reintenta; '0' vuelve al menu; termina en 'hacer pregunta'");

		T.section("JugadorHumano.hacerPregunta - arma FiltroAplicado desde el menú");
		T.mute();
		// tipo 1 (GENERO) -> valor 1 (primer enum). Los enums se muestran con name(); el valor vuelve en minúscula.
		JugadorHumano j5 = nuevo("1\n1\n", personajes);
		FiltroAplicado f5 = j5.hacerPregunta();
		T.unmute();
		T.check(f5 != null, "hacerPregunta devuelve un filtro");
		T.check(f5.getValor() != null && f5.getValor().equals(f5.getValor().toLowerCase()),
				"el valor del filtro del humano viene en minúscula (" + (f5 == null ? "?" : f5.getValor()) + ")");

		T.section("JugadorHumano.hacerPregunta - filtro booleano");
		T.mute();
		// buscar el índice de CALVICIE en TipoFiltro.values()
		int idxCalvicie = 0;
		model.TipoFiltro[] tipos = model.TipoFiltro.values();
		for (int i = 0; i < tipos.length; i++) {
			if (tipos[i] == model.TipoFiltro.CALVICIE) {
				idxCalvicie = i + 1;
			}
		}
		JugadorHumano j6 = nuevo(idxCalvicie + "\n1\n", personajes); // CALVICIE -> "1. Sí"
		FiltroAplicado f6 = j6.hacerPregunta();
		T.unmute();
		T.check(f6 != null && f6.getValor().equals("true"), "CALVICIE + 'Sí' -> valor 'true'");

		T.section("JugadorHumano - EOF en medio de la lectura");
		T.mute();
		JugadorHumano j7 = nuevo("1\n", personajes); // pide arriesgar y después se corta el input
		Throwable t7 = T.capture(() -> j7.arriesgarPersonaje());
		T.unmute();
		if (t7 instanceof NoSuchElementException) {
			T.finding("HUM-EOF-1", "MEDIA",
					"JugadorHumano.arriesgarPersonaje/leerOpcionEnRango llaman scanner.nextLine() sin manejar el fin "
							+ "de entrada: si el stdin se cierra o se agota (pipe, redirección, Ctrl+Z/Ctrl+D) el juego "
							+ "corta con NoSuchElementException y stack trace, en vez de salir prolijo.");
		} else if (t7 == null) {
			T.fail("se esperaba que el EOF causara NoSuchElementException, no pasó nada");
		} else {
			T.fail("EOF causó " + t7.getClass().getName() + " (se esperaba NoSuchElementException)");
		}

		T.section("JugadorHumano - no se puede cambiar el personaje elegido");
		JugadorHumano j8 = nuevo("2\n", personajes);
		T.expectThrows(IllegalStateException.class, () -> j8.elegirPersonaje(personajes.get(5)),
				"segundo elegirPersonaje lanza IllegalStateException");
	}
}
