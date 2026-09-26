package ui;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel contenedor = new JPanel(cards);
    private final PanelPuntajes panelPuntajes;
    private PanelJuego panelJuego;

    private String nombreJugador;

    public VentanaPrincipal() {
        super("AdivinaAdivinador");

        // Pantallas fijas
        contenedor.add(new PanelBienvenida(this).getRaiz(), "BIENVENIDA");
        contenedor.add(new PanelMenuPrincipal(this).getRaiz(), "MENU");
        contenedor.add(new PanelModosDeJuego(this).getRaiz(), "MODOS");

        // Pantallas actualizables
        panelPuntajes = new PanelPuntajes(this);
        contenedor.add(panelPuntajes.getRaiz(), "PUNTAJES");

        setContentPane(contenedor);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void mostrarPantalla(String nombre) {
        cards.show(contenedor, nombre);
    }

    public void iniciarPartida(int modo) {
        if (panelJuego != null) {
            panelJuego.detener();
            contenedor.remove(panelJuego.getRaiz());
        }
        panelJuego = new PanelJuego(this, modo);
        contenedor.add(panelJuego.getRaiz(), "JUEGO");
        mostrarPantalla("JUEGO");
    }

    public void mostrarPuntajes() {
        panelPuntajes.cargar();
        mostrarPantalla("PUNTAJES");
    }

    public void salir() {
        int r = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que querés salir?",
                "Salir",
                JOptionPane.YES_NO_OPTION
        );
        if (r == JOptionPane.YES_OPTION) System.exit(0);
    }

    // Getter y setter
    public void setNombreJugador(String nombre) {
        this.nombreJugador = nombre;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }
}