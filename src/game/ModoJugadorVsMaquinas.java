package game;

import java.util.List;
import java.util.Random;

import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;
import players.Consulta;
import players.HistorialConsultas;
import players.JugadorHumano;
import players.MaquinaAleatoria;
import players.MaquinaAsertiva;
import score.RepositorioPuntajes;
import score.ScoreRepository;
import utils.Registro;

// Controla el desafío Jugador vs Máquinas: ronda 1 contra la Máquina Aleatoria y,
// si el jugador la gana, ronda 2 contra la Máquina Asertiva (que hereda las preguntas de la primera).
// La pantalla de juego llama a preguntar/arriesgar/continuarARonda2 y después consulta el estado.
public class ModoJugadorVsMaquinas {
    private final String nombre;
    private final Personaje secretoJugador;
    private final Random random = new Random();

    private final List<Personaje> personajes;
    private final HistorialConsultas historial = new HistorialConsultas();
    private final Registro registro = new Registro();
    private final RepositorioPuntajes scoreRepository = new ScoreRepository();

    private JugadorHumano jugador;
    private MaquinaAleatoria aleatoria;
    private MotorJuego motor;

    private int ronda;
    private boolean esperandoContinuar;
    private boolean desafioTerminado;
    private String resultadoFinal;

    public ModoJugadorVsMaquinas(String nombre, Personaje secretoJugador, List<Personaje> personajes) {
        this.nombre = nombre;
        this.secretoJugador = secretoJugador;
        this.personajes = personajes;

        registro.registrar("########## JUGADOR vs MÁQUINAS ##########\n");
        registro.registrar("Tu personaje secreto es: " + secretoJugador.getNombre());
        iniciarRonda1();
    }

    // ---------- Acciones del jugador (las llama la pantalla) ----------

    public void preguntar(FiltroAplicado filtro) {
        motor.preguntar(filtro);
        responderMaquina();
    }

    public void arriesgar(Personaje personaje) {
        motor.arriesgar(personaje);
        responderMaquina();
    }

    public void continuarARonda2() {
        if (!esperandoContinuar) {
            throw new IllegalStateException("Todavía no se puede pasar a la segunda ronda.");
        }
        esperandoContinuar = false;
        iniciarRonda2();
    }

    // ---------- Rondas ----------

    private void iniciarRonda1() {
        ronda = 1;
        jugador = new JugadorHumano(nombre, new Tablero(personajes));
        jugador.elegirPersonaje(secretoJugador);

        aleatoria = new MaquinaAleatoria(new Tablero(personajes), historial, registro);
        aleatoria.elegirPersonaje(azar());

        motor = new MotorJuego(jugador, aleatoria, historial, registro);
        motor.iniciar();
    }

    private void iniciarRonda2() {
        ronda = 2;
        jugador = new JugadorHumano(nombre, new Tablero(personajes));
        jugador.elegirPersonaje(secretoJugador);

        Tablero tableroAsertiva = new Tablero(personajes);
        MaquinaAsertiva asertiva = new MaquinaAsertiva(tableroAsertiva, historial, registro);
        asertiva.elegirPersonaje(azar());

        int heredadas = replicarPreguntasPrevias(asertiva.getNombre(), tableroAsertiva);
        registro.registrar("\nLa Máquina Asertiva entra conociendo " + heredadas + " pregunta(s) previa(s); "
                + "arranca con " + tableroAsertiva.cantidadRestante() + " personaje(s) posible(s).");

        motor = new MotorJuego(jugador, asertiva, historial, registro);
        motor.iniciar();
    }

    // Después de la jugada del humano, la máquina juega sola su turno.
    private void responderMaquina() {
        if (!motor.isPartidaTerminada()) {
            motor.jugarTurnoMaquina();
        }
        if (motor.isPartidaTerminada()) {
            procesarFinDeRonda();
        }
    }

    private void procesarFinDeRonda() {
        boolean ganoElJugador = motor.getGanador() == jugador;

        if (ronda == 1) {
            if (ganoElJugador) {
                scoreRepository.registrarVictoria(nombre);
                registro.registrar("\n¡Ganaste la primera ronda! Tocá \"Continuar\" para enfrentar a la Máquina Asertiva.");
                esperandoContinuar = true;
            } else {
                registro.registrar("\nPerdiste contra la Máquina Aleatoria. El desafío termina acá.");
                resultadoFinal = "Perdiste contra la Máquina Aleatoria. El desafío termina acá.";
                desafioTerminado = true;
            }
            return;
        }

        if (ganoElJugador) {
            scoreRepository.registrarVictoria(nombre);
            registro.registrar("\n¡Le ganaste también a la Máquina Asertiva! Desafío completado.");
            resultadoFinal = "¡Le ganaste también a la Máquina Asertiva! Desafío completado.";
        } else if (motor.getGanador() == null) {
            registro.registrar("\nLa segunda ronda terminó en empate.");
            resultadoFinal = "La segunda ronda terminó en empate.";
        } else {
            registro.registrar("\nLa Máquina Asertiva te ganó la segunda ronda.");
            resultadoFinal = "La Máquina Asertiva te ganó la segunda ronda.";
        }
        desafioTerminado = true;
    }

    // La Máquina Asertiva arranca aplicando a su tablero las preguntas que hizo la Aleatoria.
    private int replicarPreguntasPrevias(String nombreAsertiva, Tablero tableroAsertiva) {
        List<Consulta> previas = historial.obtenerConsultasDe(aleatoria.getNombre());
        for (Consulta c : previas) {
            FiltroAplicado f = c.getFiltro();
            tableroAsertiva.aplicarFiltro(f.getTipo(), f.getValor(), c.getRespuesta());
            historial.agregarConsulta(nombreAsertiva, f, c.getRespuesta());
        }
        return previas.size();
    }

    private Personaje azar() {
        return personajes.get(random.nextInt(personajes.size()));
    }

    // ---------- Consultas para la pantalla ----------

    public Registro getRegistro() {
        return registro;
    }

    public Tablero getTableroJugador() {
        return jugador.getTablero();
    }

    public int getRonda() {
        return ronda;
    }

    public boolean esTurnoDelJugador() {
        return motor.esTurnoDelHumano();
    }

    public boolean isEsperandoContinuar() {
        return esperandoContinuar;
    }

    public boolean isDesafioTerminado() {
        return desafioTerminado;
    }

    public String getResultadoFinal() {
        return resultadoFinal;
    }
}