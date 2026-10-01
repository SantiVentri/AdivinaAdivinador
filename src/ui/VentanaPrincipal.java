package ui;

import game.ModoJugadorVsMaquinas;
import game.ModoMaquinaVsMaquina;
import model.Personaje;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    private static final Dimension TAM_VENTANA = new Dimension(900, 600);

    private final CardLayout cards = new CardLayout();
    private final JPanel contenedor = new JPanel(cards);
    private final PanelPuntajes panelPuntajes;
    private PanelJuego panelJuego;
    private PanelEspectador panelEspectador;
    private PanelEleccionPersonaje panelEleccion;

    private String nombreJugador;

    public VentanaPrincipal() {
        super("AdivinaAdivinador");
        UIManager.put("Panel.background", Color.WHITE);

        // Pantallas fijas
        contenedor.add(new PanelBienvenida(this).getRaiz(), "BIENVENIDA");
        contenedor.add(new PanelMenuPrincipal(this).getRaiz(), "MENU");
        contenedor.add(new PanelModosDeJuego(this).getRaiz(), "MODOS");

        // Pantallas actualizables
        panelPuntajes = new PanelPuntajes(this);
        contenedor.add(panelPuntajes.getRaiz(), "PUNTAJES");

        setContentPane(contenedor);
        setSize(TAM_VENTANA);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void mostrarPantalla(String nombre) {
        cards.show(contenedor, nombre);

        // Las pantallas de juego definen su propio tamaño (suma de sus paneles);
        // el resto usa el tamaño fijo de la ventana.
        Dimension anterior = getSize();
        if ("JUEGO".equals(nombre)) {
            ajustarA(panelJuego.getRaiz());
        } else if ("ESPECTADOR".equals(nombre)) {
            ajustarA(panelEspectador.getRaiz());
        } else {
            setSize(TAM_VENTANA);
        }
        if (!getSize().equals(anterior)) {
            setLocationRelativeTo(null);
        }
    }

    private void ajustarA(JPanel pantalla) {
        contenedor.setPreferredSize(pantalla.getPreferredSize());
        pack();
    }

    public void mostrarEleccionPersonaje() {
        if (panelEleccion != null) {
            contenedor.remove(panelEleccion.getRaiz());
        }
        panelEleccion = new PanelEleccionPersonaje(this);
        contenedor.add(panelEleccion.getRaiz(), "ELECCION");
        mostrarPantalla("ELECCION");
    }

    public void iniciarJugadorVsMaquinas(Personaje secreto, List<Personaje> personajes) {
        ModoJugadorVsMaquinas modo = new ModoJugadorVsMaquinas(nombreJugador, secreto, personajes);
        iniciarPartida(modo);
    }

    public void iniciarMaquinaVsMaquina() {
        if (panelEspectador != null) {
            contenedor.remove(panelEspectador.getRaiz());
        }
        panelEspectador = new PanelEspectador(this, new ModoMaquinaVsMaquina());
        contenedor.add(panelEspectador.getRaiz(), "ESPECTADOR");
        mostrarPantalla("ESPECTADOR");
    }

    public void iniciarPartida(ModoJugadorVsMaquinas modo) {
        iniciarPantallaDeJuego(modo);
    }

    private void iniciarPantallaDeJuego(ModoJugadorVsMaquinas modo) {
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