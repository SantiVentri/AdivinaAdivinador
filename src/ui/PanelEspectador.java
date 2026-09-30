package ui;

import game.ModoMaquinaVsMaquina;
import model.Personaje;
import players.Jugador;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Pantalla del modo Máquina vs. Máquina: el usuario es espectador y avanza de a un turno.
// Muestra el tablero de la máquina que acaba de jugar, cuántos personajes le quedan a cada una
// y los logs de la partida.
public class PanelEspectador {
    private static final Dimension TAM_TABLERO = new Dimension(600, 400);
    private static final Dimension TAM_CONTROLES = new Dimension(400, 150);
    private static final Dimension TAM_REGISTROS = new Dimension(250, 520);

    private JPanel raiz;
    private JPanel contenedorTablero;
    private JPanel contenedorControles;
    private JPanel contenedorRegistros;
    private JLabel lblTurno;
    private JLabel lblDetalle;
    private JButton btnSiguiente;
    private JButton btnTerminarPartida;

    private final PanelTablero tablero = new PanelTablero();
    private final PanelLogs logs = new PanelLogs();
    private final VentanaPrincipal ventana;
    private final ModoMaquinaVsMaquina modo;

    // Últimos personajes restantes vistos de cada máquina, para saber qué descartó en su turno.
    private final Map<Jugador, List<Personaje>> ultimosRestantes = new HashMap<>();

    public PanelEspectador(VentanaPrincipal ventana, ModoMaquinaVsMaquina modo) {
        this.ventana = ventana;
        this.modo = modo;

        fijarTamanio(contenedorTablero, TAM_TABLERO);
        fijarTamanio(contenedorControles, TAM_CONTROLES);
        fijarTamanio(contenedorRegistros, TAM_REGISTROS);

        lblTurno.setFont(lblTurno.getFont().deriveFont(Font.BOLD, 16f));
        btnSiguiente.setFont(btnSiguiente.getFont().deriveFont(Font.BOLD, 14f));

        contenedorTablero.setLayout(new BorderLayout());
        contenedorTablero.add(tablero, BorderLayout.CENTER);

        GridBagConstraints gbcLogs = new GridBagConstraints();
        gbcLogs.gridx = 0;
        gbcLogs.gridy = 0;
        gbcLogs.weightx = 1.0;
        gbcLogs.weighty = 1.0;
        gbcLogs.fill = GridBagConstraints.BOTH;
        gbcLogs.insets = new Insets(5, 5, 5, 5);
        contenedorRegistros.add(logs.getRaiz(), gbcLogs);

        btnSiguiente.addActionListener(e -> alTocarSiguiente());
        btnTerminarPartida.addActionListener(e -> terminarPartida());

        mostrarMensajes();
        actualizarPantalla();
    }

    private void alTocarSiguiente() {
        if (modo.isPartidaTerminada()) {
            ventana.mostrarPantalla("MENU");
            return;
        }
        modo.siguienteTurno();
        mostrarMensajes();
        actualizarPantalla();
    }

    private void actualizarPantalla() {
        actualizarTablero();

        if (modo.isPartidaTerminada()) {
            mostrarResultadoFinal();
            return;
        }

        if (modo.getTurnosJugados() == 0) {
            lblTurno.setText("¡Empieza la partida!");
        } else {
            lblTurno.setText("Turno " + modo.getTurnosJugados() + " · jugó " + modo.getJugadorMostrado().getNombre());
        }
        lblDetalle.setText("<html>" + textoRestantes() + "<br>Próximo turno: "
                + modo.getJugadorActivo().getNombre() + "</html>");
    }

    // Muestra el tablero de la máquina que acaba de jugar, marcando lo que descartó en ese turno.
    private void actualizarTablero() {
        Jugador mostrado = modo.getJugadorMostrado();
        List<Personaje> restantes = mostrado.getTablero().getPersonajesRestantes();
        List<Personaje> anteriores = ultimosRestantes.getOrDefault(mostrado, modo.getPersonajes());

        List<Personaje> recienDescartados = new ArrayList<>(anteriores);
        recienDescartados.removeAll(restantes);
        ultimosRestantes.put(mostrado, new ArrayList<>(restantes));

        tablero.mostrarConDescartados(modo.getPersonajes(), restantes, recienDescartados);
        ((TitledBorder) contenedorTablero.getBorder()).setTitle("Tablero de " + mostrado.getNombre());
        contenedorTablero.repaint();
    }

    private String textoRestantes() {
        return "Personajes posibles: " + modo.getAsertiva().getNombre() + " "
                + modo.getAsertiva().getTablero().cantidadRestante() + " · "
                + modo.getAleatoria().getNombre() + " "
                + modo.getAleatoria().getTablero().cantidadRestante();
    }

    // La partida terminó: se anuncia el resultado, se revelan los secretos y el botón pasa a volver al menú.
    private void mostrarResultadoFinal() {
        Jugador ganador = modo.getGanador();
        lblTurno.setText(ganador != null ? "¡Ganó " + ganador.getNombre() + "!" : "¡Empate!");
        lblDetalle.setText("<html>Secreto de " + modo.getAsertiva().getNombre() + ": "
                + modo.getSecretoAsertiva().getNombre() + "<br>Secreto de " + modo.getAleatoria().getNombre() + ": "
                + modo.getSecretoAleatoria().getNombre() + "</html>");

        // En el tablero del ganador se resalta el personaje que adivinó (el secreto del rival).
        if (ganador != null) {
            Personaje adivinado = ganador == modo.getAsertiva() ? modo.getSecretoAleatoria() : modo.getSecretoAsertiva();
            for (TarjetaPersonaje tarjeta : tablero.getTarjetas()) {
                tarjeta.setSeleccionada(tarjeta.getPersonaje().equals(adivinado));
                if (!tarjeta.getPersonaje().equals(adivinado)) {
                    tarjeta.setDescartada(false);
                }
            }
        }
        btnSiguiente.setText("Volver al menú");
        btnTerminarPartida.setEnabled(false);
    }

    // El usuario decide terminar la partida en cualquier momento y volver a elegir modo.
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

    private void mostrarMensajes() {
        logs.agregarMensajes(modo.getRegistro().retirarMensajes());
    }

    private static void fijarTamanio(JComponent c, Dimension d) {
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
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
        contenedorControles = new JPanel();
        contenedorControles.setLayout(new GridBagLayout());
        contenedorControles.setBackground(new Color(-1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        raiz.add(contenedorControles, gbc);
        contenedorControles.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        lblTurno = new JLabel();
        lblTurno.setForeground(new Color(-16777216));
        lblTurno.setText("Turno");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 15, 5, 10);
        contenedorControles.add(lblTurno, gbc);
        lblDetalle = new JLabel();
        lblDetalle.setForeground(new Color(-12566464));
        lblDetalle.setText("Detalle");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 15, 10, 10);
        contenedorControles.add(lblDetalle, gbc);
        btnSiguiente = new JButton();
        btnSiguiente.setText("Siguiente turno");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.ipadx = 30;
        gbc.ipady = 15;
        gbc.insets = new Insets(10, 10, 10, 15);
        contenedorControles.add(btnSiguiente, gbc);
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
        gbc.insets = new Insets(5, 5, 5, 5);
        contenedorRegistros.add(btnTerminarPartida, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return raiz;
    }
}
