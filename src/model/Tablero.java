package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tablero {
	// Atributos
    private final List<Personaje> personajesOriginales;
    private List<Personaje> personajesRestantes;

    // Constructor
    public Tablero(List<Personaje> personajesOriginales) {
        this.personajesOriginales = new ArrayList<>(personajesOriginales);
        this.personajesRestantes = new ArrayList<>(this.personajesOriginales);
    }

    // Reiniciar
    public void reiniciar() {
        this.personajesRestantes = new ArrayList<>(personajesOriginales);
    }

    // Filtrado
    public void aplicarFiltro(TipoFiltro tipo, String valor, boolean respuestaEsperada) {
        List<Personaje> nuevosRestantes = new ArrayList<>();
        for (Personaje p : personajesRestantes) {
            if (p.cumpleFiltro(tipo, valor) == respuestaEsperada) {
                nuevosRestantes.add(p);
            }
        }
        this.personajesRestantes = nuevosRestantes;
    }

    public int contarSiSeAplicara(TipoFiltro tipo, String valorEsperado) {
        int contador = 0;

        for (Personaje p : personajesRestantes) {
            if (p.cumpleFiltro(tipo, valorEsperado)) {
                contador++;
            }
        }

        return contador;
    }
    
    public void sacarPersonaje(Personaje personaje) {
    	this.personajesRestantes.remove(personaje);
    }

    // Consultas
    public Personaje buscarPorNombre(String nombre) {
        for (Personaje p : personajesRestantes) {
            if (p.getNombre().equalsIgnoreCase(nombre.trim())) {
                return p;
            }
        }
        return null;
    }

    public Personaje buscarPorId(int id) {
        return buscarPorId(personajesRestantes, 0, personajesRestantes.size() - 1, id);
    }

    // Búsqueda binaria (divide y conquista). Requiere que personajesRestantes
    // se mantenga ordenada por id, invariante que respetan aplicarFiltro,
    // sacarPersonaje y reiniciar.
    private Personaje buscarPorId(List<Personaje> lista, int desde, int hasta, int id) {
        if (desde > hasta) {
            return null;
        }

        int medio = desde + (hasta - desde) / 2;
        Personaje candidato = lista.get(medio);

        if (candidato.getId() == id) {
            return candidato;
        } else if (candidato.getId() > id) {
            return buscarPorId(lista, desde, medio - 1, id);
        } else {
            return buscarPorId(lista, medio + 1, hasta, id);
        }
    }

    public boolean quedaUnoSolo() {
        return personajesRestantes.size() == 1;
    }

    public boolean estaVacio() {
        return personajesRestantes.isEmpty();
    }

    // Getters
    public List<Personaje> getPersonajesRestantes() {
        return Collections.unmodifiableList(personajesRestantes);
    }

    public int cantidadRestante() {
        return personajesRestantes.size();
    }

    // Mostrar
    public void mostrar() {
        System.out.println("----------------------------------------------------------------");
        System.out.println("Quedan " + personajesRestantes.size() + " personaje(s) posible(s):");
        System.out.println("----------------------------------------------------------------");
        for (Personaje p : personajesRestantes) {
            System.out.println(p);
        }
        System.out.println("----------------------------------------------------------------");
    }
}