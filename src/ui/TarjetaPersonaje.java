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
    private static final Color FONDO_DESCARTADA = new Color(242, 242, 242);
    private static final Color TEXTO_DESCARTADA = new Color(175, 175, 175);
    private static final Color BORDE_DESCARTADA = new Color(225, 225, 225);
    private static final Color FONDO_RECIEN_DESCARTADA = new Color(248, 215, 215);
    private static final Color TEXTO_RECIEN_DESCARTADA = new Color(150, 50, 50);
    private static final Color BORDE_RECIEN_DESCARTADA = new Color(200, 70, 70);

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

    // Personaje que ya no es una opción posible. Si se descartó en el último turno se resalta en rojo
    // para que se vea qué cambió; si no, queda apagado en gris.
    public void setDescartada(boolean recienDescartada) {
        if (recienDescartada) {
            setBackground(FONDO_RECIEN_DESCARTADA);
            setForeground(TEXTO_RECIEN_DESCARTADA);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE_RECIEN_DESCARTADA, 2),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        } else {
            setBackground(FONDO_DESCARTADA);
            setForeground(TEXTO_DESCARTADA);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE_DESCARTADA, 1),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        }
    }

    public Personaje getPersonaje() {
        return personaje;
    }
}