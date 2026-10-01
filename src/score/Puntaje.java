package score;

// Una fila del marcador: cuántas partidas jugó un jugador, cuántas rondas ganó
// y cuántas partidas completas ganó.
public class Puntaje {
	private final String nombreJugador;
	private int partidasJugadas;
	private int rondasGanadas;
	private int partidasGanadas;

	public Puntaje(String nombreJugador, int partidasJugadas, int rondasGanadas, int partidasGanadas) {
		this.nombreJugador = nombreJugador;
		this.partidasJugadas = partidasJugadas;
		this.rondasGanadas = rondasGanadas;
		this.partidasGanadas = partidasGanadas;
	}

	public void sumarPartidaJugada() {
		partidasJugadas++;
	}

	public void sumarRonda() {
		rondasGanadas++;
	}

	public void sumarPartida() {
		partidasGanadas++;
	}

	// Getters
	public String getNombreJugador() {
		return nombreJugador;
	}

	public int getPartidasJugadas() {
		return partidasJugadas;
	}

	public int getRondasGanadas() {
		return rondasGanadas;
	}

	public int getPartidasGanadas() {
		return partidasGanadas;
	}
}
