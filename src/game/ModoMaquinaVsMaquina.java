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

    // La máquina cuyo tablero se muestra en pantalla: la que acaba de jugar.
    private Jugador jugadorMostrado;

    public ModoMaquinaVsMaquina() {
        List<Personaje> personajes = PersonajeFactory.crearPersonajes();
        HistorialConsultas historial = new HistorialConsultas();

        MaquinaAsertiva asertiva = new MaquinaAsertiva(new Tablero(personajes), historial, registro);
        MaquinaAleatoria aleatoria = new MaquinaAleatoria(new Tablero(personajes), historial, registro);

        asertiva.elegirPersonaje(azar(personajes));
        aleatoria.elegirPersonaje(azar(personajes));

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

    public boolean isPartidaTerminada() {
        return motor.isPartidaTerminada();
    }
}