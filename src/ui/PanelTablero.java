package ui;

import model.Personaje;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Grilla de 4 filas x 6 columnas con una tarjeta por personaje.
// Se usa en el tablero de juego y en la pantalla de elección del personaje secreto.
public class PanelTablero extends JPanel {
    private static final int FILAS = 4;
    private static final int COLUMNAS = 6;
    private static final int SEPARACION = 10;

    private final List<TarjetaPersonaje> tarjetas = new ArrayList<>();

    public PanelTablero() {
        super(new GridLayout(FILAS, COLUMNAS, SEPARACION, SEPARACION));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(SEPARACION, SEPARACION, SEPARACION, SEPARACION));
    }

    public void mostrar(List<Personaje> personajes, ActionListener alHacerClic) {
        removeAll();
        tarjetas.clear();

        for (Personaje personaje : personajes) {
            TarjetaPersonaje tarjeta = new TarjetaPersonaje(personaje);
            if (alHacerClic != null) {
                tarjeta.addActionListener(alHacerClic);
            } else {
                tarjeta.setFocusable(false);
            }
            tarjetas.add(tarjeta);
            add(tarjeta);
        }

        for (int i = personajes.size(); i < FILAS * COLUMNAS; i++) {
            JPanel vacio = new JPanel();
            vacio.setOpaque(false);
            add(vacio);
        }

        revalidate();
        repaint();
    }

    // Muestra todos los personajes en posiciones fijas: los que ya no son opción quedan apagados
    // y los descartados en el último turno se resaltan. Lo usa la pantalla de espectador.
    public void mostrarConDescartados(List<Personaje> todos, List<Personaje> restantes,
                                      Collection<Personaje> recienDescartados) {
        mostrar(todos, null);
        for (TarjetaPersonaje tarjeta : tarjetas) {
            Personaje personaje = tarjeta.getPersonaje();
            if (!restantes.contains(personaje)) {
                tarjeta.setDescartada(recienDescartados.contains(personaje));
            }
        }
    }

    public List<TarjetaPersonaje> getTarjetas() {
        return tarjetas;
    }
}