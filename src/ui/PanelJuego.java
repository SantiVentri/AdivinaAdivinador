package ui;

import game.ModoJugadorVsMaquinas;
import model.FiltroAplicado;
import model.Personaje;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class PanelJuego {
    private static final Dimension TAM_TABLERO = new Dimension(600, 400);
    private static final Dimension TAM_FILTROS = new Dimension(400, 150);
    private static final Dimension TAM_REGISTROS = new Dimension(350, 520);

    private JPanel raiz;
    private JPanel contenedorTablero;
    private JPanel contenedorRegistros;
    private JPanel contenedorFiltros;
    private JButton btnTerminarPartida;
    private JButton btnArriesgarPersonaje;

    private final PanelTablero tablero = new PanelTablero();
    private final PanelFiltros filtros = new PanelFiltros();
    private final PanelLogs logs = new PanelLogs();
    private final VentanaPrincipal ventana;
    private final ModoJugadorVsMaquinas modo;

    public PanelJuego(VentanaPrincipal ventana, ModoJugadorVsMaquinas modo) {
        this.ventana = ventana;
        this.modo = modo;

        fijarTamanio(contenedorTablero, TAM_TABLERO);
        fijarTamanio(contenedorFiltros, TAM_FILTROS);
        fijarTamanio(contenedorRegistros, TAM_REGISTROS);

        contenedorTablero.setLayout(new BorderLayout());
        contenedorTablero.add(tablero, BorderLayout.CENTER);

        contenedorFiltros.setLayout(new BorderLayout());
        contenedorFiltros.add(filtros.getRaiz(), BorderLayout.CENTER);

        GridBagConstraints gbcLogs = new GridBagConstraints();
        gbcLogs.gridx = 0;
        gbcLogs.gridy = 0;
        gbcLogs.weightx = 1.0;
        gbcLogs.weighty = 1.0;
        gbcLogs.fill = GridBagConstraints.BOTH;
        gbcLogs.insets = new Insets(5, 5, 5, 5);
        contenedorRegistros.add(logs.getRaiz(), gbcLogs);

        btnTerminarPartida.addActionListener(e -> terminarPartida());
        btnArriesgarPersonaje.addActionListener(e -> activarModoArriesgo());

        filtros.setAlPreguntar(this::preguntar);
        tablero.mostrar(modo.getTableroJugador().getPersonajesRestantes(), null);
        mostrarMensajes();
    }

    // ---------- Interacción con el modo de juego ----------

    private void preguntar(FiltroAplicado filtro) {
        modo.preguntar(filtro);
        mostrarMensajes();
        actualizarEstado();
    }

    private void actualizarEstado() {
        tablero.mostrar(modo.getTableroJugador().getPersonajesRestantes(), null);

        if (modo.isDesafioTerminado()) {
            filtros.setHabilitado(false);
            btnArriesgarPersonaje.setEnabled(false);
            mostrarResultadoFinal();
        } else if (modo.isEsperandoContinuar()) {
            filtros.setHabilitado(false);
            btnArriesgarPersonaje.setEnabled(false);
            JOptionPane.showMessageDialog(raiz,
                    "¡Ganaste la ronda! Ahora te enfrentás a la Máquina Asertiva.",
                    "Ronda superada", JOptionPane.INFORMATION_MESSAGE);
            modo.continuarARonda2();
            mostrarMensajes();
            tablero.mostrar(modo.getTableroJugador().getPersonajesRestantes(), null);
            filtros.setHabilitado(true);
            btnArriesgarPersonaje.setEnabled(true);
        }
    }

    // El desafío terminó (ganó, perdió o empató la segunda ronda): se lo informa y vuelve al menú principal.
    private void mostrarResultadoFinal() {
        JOptionPane.showOptionDialog(
                raiz,
                modo.getResultadoFinal(),
                "Partida terminada",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"Volver al menú principal"},
                "Volver al menú principal"
        );
        ventana.mostrarPantalla("MENU");
    }

    // El jugador decide terminar la partida en cualquier momento y volver a elegir modo.
    private void terminarPartida() {
        int respuesta = JOptionPane.showConfirmDialog(
                raiz,
                "¿Seguro que querés terminar la partida?",
                "Terminar partida",
                JOptionPane.YES_NO_OPTION
        );
        if (respuesta == JOptionPane.YES_OPTION) {
            ventana.mostrarPantalla("MODOS");
        }
    }

    // Pone el tablero en modo selección: el próximo clic en una tarjeta arriesga ese personaje.
    private void activarModoArriesgo() {
        tablero.mostrar(modo.getTableroJugador().getPersonajesRestantes(),
                e -> confirmarArriesgo((TarjetaPersonaje) e.getSource()));
    }

    private void confirmarArriesgo(TarjetaPersonaje tarjeta) {
        Personaje elegido = tarjeta.getPersonaje();
        int respuesta = JOptionPane.showConfirmDialog(
                raiz,
                "¿Arriesgar \"" + elegido.getNombre() + "\" como el personaje secreto del rival?",
                "Arriesgar personaje",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            modo.arriesgar(elegido);
            mostrarMensajes();
            actualizarEstado();
        } else {
            // Cancela el modo selección sin arriesgar nada.
            tablero.mostrar(modo.getTableroJugador().getPersonajesRestantes(), null);
        }
    }

    // Vuelca al panel de logs los mensajes que dejó pendientes el Registro
    // (respuesta del rival, jugada de la máquina, fin de ronda, etc.).
    private void mostrarMensajes() {
        logs.agregarMensajes(modo.getRegistro().retirarMensajes());
    }

    private static void fijarTamanio(JComponent c, Dimension d) {
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
    }

    public void detener() {

    }

    public JPanel getRaiz() {
        return raiz;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        raiz = new JPanel();
        raiz.setLayout(new GridBagLayout());
        raiz.setBackground(new Color(-1));
        contenedorTablero = new JPanel();
        contenedorTablero.setLayout(new GridBagLayout());
        contenedorTablero.setBackground(new Color(-1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        raiz.add(contenedorTablero, gbc);
        contenedorTablero.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        contenedorFiltros = new JPanel();
        contenedorFiltros.setLayout(new GridBagLayout());
        contenedorFiltros.setBackground(new Color(-1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        raiz.add(contenedorFiltros, gbc);
        contenedorFiltros.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        contenedorRegistros = new JPanel();
        contenedorRegistros.setLayout(new GridBagLayout());
        contenedorRegistros.setBackground(new Color(-1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        raiz.add(contenedorRegistros, gbc);
        contenedorRegistros.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        btnTerminarPartida = new JButton();
        btnTerminarPartida.setText("Terminar partida");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 0, 5);
        contenedorRegistros.add(btnTerminarPartida, gbc);
        btnArriesgarPersonaje = new JButton();
        btnArriesgarPersonaje.setText("Arriesgar personaje");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contenedorRegistros.add(btnArriesgarPersonaje, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return raiz;
    }

}
