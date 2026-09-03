package players;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.FiltroAplicado;

public class HistorialConsultas {
	private List<Consulta> consultas;

	public HistorialConsultas() {
		this.consultas = new ArrayList<Consulta>();
	}

	public void reiniciar() {
		this.consultas.clear();
	}

	public void agregarConsulta(String nombreJugador, FiltroAplicado filtro, boolean respuesta) {
		this.consultas.add(new Consulta(nombreJugador, filtro, respuesta));
	}

	public List<Consulta> obtenerConsultasDe(String nombreJugador) {
        List<Consulta> resultado = new ArrayList<Consulta>();
        for (Consulta c : this.consultas) {
            if (c.getNombreJugador().equalsIgnoreCase(nombreJugador)) {
                resultado.add(c);
            }
        }
        return Collections.unmodifiableList(resultado);
    }

    // Igual que yaFuePreguntado pero mirando solo las preguntas hechas por un jugador puntual.
    // Necesario cuando varios jugadores comparten el mismo historial (Máquina vs Máquina).
    public boolean yaFuePreguntado(String nombreJugador, String pregunta) {
        for (Consulta c : this.consultas) {
            if (c.getNombreJugador().equalsIgnoreCase(nombreJugador)
                    && c.getPregunta().equalsIgnoreCase(pregunta.trim())) {
                return true;
            }
        }
        return false;
    }

    public List<Consulta> getConsultas() {
        return Collections.unmodifiableList(this.consultas);
    }

    public int getCantidadConsultas() {
        return this.consultas.size();
    }

	public void mostrar() {
		System.out.println("\nHistorial de consultas:");
		for (Consulta c : consultas) {
			System.out.println("- [" + c.getNombreJugador() + " preguntó]: " + c.getPregunta() + " --> " + c.getRespuesta() + "\n");
		}
	}


}
