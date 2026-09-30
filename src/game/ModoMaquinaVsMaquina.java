package game;

import java.util.List;
import java.util.Random;

import model.Personaje;
import model.Tablero;
import players.HistorialConsultas;
import players.Jugador;
import players.MaquinaAleatoria;
import players.MaquinaAsertiva;
import utils.PersonajeFactory;
import utils.Registro;

// Controla una partida Máquina Asertiva vs Máquina Aleatoria, en la que el usuario es espectador
// y avanza de a un turno con el botón "Siguiente turno".
public class ModoMaquinaVsMaquina {
    private final Random random = new Random();
    private final Registro registro = new Registro();
    private final MotorJuego motor;

    private final List<Personaje> personajes;
    private final MaquinaAsertiva asertiva;
    private final MaquinaAleatoria aleatoria;
    private final Personaje secretoAsertiva;
    private final Personaje secretoAleatoria;

    // La máquina cuyo tablero se muestra en pantalla: la que acaba de jugar.
    private Jugador jugadorMostrado;
    private int turnosJugados;

    public ModoMaquinaVsMaquina() {
        personajes = PersonajeFactory.crearPersonajes();
        HistorialConsultas historial = new HistorialConsultas();

        asertiva = new MaquinaAsertiva(new Tablero(personajes), historial, registro);
        aleatoria = new MaquinaAleatoria(new Tablero(personajes), historial, registro);

        secretoAsertiva = azar(personajes);
        secretoAleatoria = azar(personajes);
        asertiva.elegirPersonaje(secretoAsertiva);
        aleatoria.elegirPersonaje(secretoAleatoria);

        registro.registrar("########## MÁQUINA vs MÁQUINA (sos espectador) ##########");

        motor = new MotorJuego(asertiva, aleatoria, historial, registro);
        motor.iniciar();
        jugadorMostrado = motor.getJugadorActivo();
    }

    public void siguienteTurno() {
        if (motor.isPartidaTerminada()) {
            return;
        }
        jugadorMostrado = motor.getJugadorActivo();
        motor.jugarTurnoMaquina();
        turnosJugados++;
    }

    private Personaje azar(List<Personaje> personajes) {
        return personajes.get(random.nextInt(personajes.size()));
    }

    // ---------- Consultas para la pantalla ----------

    public Registro getRegistro() {
        return registro;
    }

    public Jugador getJugadorMostrado() {
        return jugadorMostrado;
    }

    // La máquina que juega el próximo turno.
    public Jugador getJugadorActivo() {
        return motor.getJugadorActivo();
    }

    public Jugador getAsertiva() {
        return asertiva;
    }

    public Jugador getAleatoria() {
        return aleatoria;
    }

    public Personaje getSecretoAsertiva() {
        return secretoAsertiva;
    }

    public Personaje getSecretoAleatoria() {
        return secretoAleatoria;
    }

    // Todos los personajes del juego, en el orden del tablero.
    public List<Personaje> getPersonajes() {
        return personajes;
    }

    public int getTurnosJugados() {
        return turnosJugados;
    }

    // null si la partida terminó en empate o todavía no terminó.
    public Jugador getGanador() {
        return motor.getGanador();
    }

    public boolean isPartidaTerminada() {
        return motor.isPartidaTerminada();
    }
}
