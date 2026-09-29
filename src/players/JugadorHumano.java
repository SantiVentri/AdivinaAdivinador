package players;

import model.Tablero;

// El jugador humano no decide desde el código: sus preguntas e intentos llegan
// desde la pantalla de juego, que se los pasa directamente al MotorJuego.
public class JugadorHumano extends Jugador {

    public JugadorHumano(String nombre, Tablero tablero) {
        super(nombre, tablero);
    }
}