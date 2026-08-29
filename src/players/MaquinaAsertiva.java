package players;

import java.util.List;
import java.util.Random;

import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;

public class MaquinaAsertiva extends Jugador {
	private final Random random = new Random();
	
	public MaquinaAsertiva(Tablero tablero) {
		super("Máquina Asertiva", tablero);
		// TODO Auto-generated constructor stub
	}

	@Override
	public FiltroAplicado hacerPregunta() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Personaje arriesgarPersonaje() {
		List<Personaje> restantes = getTablero().getPersonajesRestantes();

        if (restantes.isEmpty()) {
            return null;
        }

        int indiceRandom = random.nextInt(restantes.size());
        Personaje personajeElegido = restantes.get(indiceRandom);
        return personajeElegido;
	}

}
