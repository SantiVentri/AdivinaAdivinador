package players;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistorialConsultas {
	private List<Consulta> consultas;
	
	public HistorialConsultas() {
		this.consultas = new ArrayList<Consulta>();
	}
	
	public void reiniciar() {
		this.consultas.clear();
	}
	
	public void agregarConsulta(String nombreJugador, String pregunta, boolean respuesta) {
		this.consultas.add(new Consulta(nombreJugador, pregunta, respuesta));
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

    public boolean yaFuePreguntado(String pregunta) {
        for (Consulta c : this.consultas) {
            if (c.getPregunta().equalsIgnoreCase(pregunta.trim())) {
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
