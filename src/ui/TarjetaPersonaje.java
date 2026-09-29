package ui;

import model.Personaje;

import javax.swing.*;
import java.awt.*;

// Tarjeta que representa a un personaje en una grilla (tablero de juego o elección del secreto).
// Es un botón para que la pantalla que la use pueda reaccionar al clic si lo necesita.
public class TarjetaPersonaje extends JButton {
    private static final Color FONDO = new Color(217, 217, 217);
    private static final Color FONDO_SELECCIONADA = new Color(214, 221, 248);
    private static final Color BORDE = new Color(160, 160, 160);
    private static final Color BORDE_SELECCIONADA = new Color(52, 84, 209);

    private final Personaje personaje;

    public TarjetaPersonaje(Personaje personaje) {
        super("<html><center>" + personaje.getNombre() + "</center></html>");
        this.personaje = personaje;

        setFont(new Font("SansSerif", Font.BOLD, 11));
        setForeground(Color.BLACK);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);
        setSeleccionada(false);
    }

    public void setSeleccionada(boolean seleccionada) {
        setBackground(seleccionada ? FONDO_SELECCIONADA : FONDO);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(seleccionada ? BORDE_SELECCIONADA : BORDE, seleccionada ? 3 : 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    }

    public Personaje getPersonaje() {
        return personaje;
    }
}