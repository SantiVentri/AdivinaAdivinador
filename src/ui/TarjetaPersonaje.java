package ui;

import model.Personaje;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.Normalizer;

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
    private static final String CARPETA_IMAGENES = "/imagenes/personajes/";
    private static final int TAM_IMAGEN = 80;

    private final Personaje personaje;
    private final ImageIcon imagen;
    private final ImageIcon imagenDescartada;

    public TarjetaPersonaje(Personaje personaje) {
        super("<html><center>" + personaje.getNombre() + "</center></html>");
        this.personaje = personaje;
        this.imagen = cargarImagen(personaje);
        this.imagenDescartada = imagen == null ? null
                : new ImageIcon(GrayFilter.createDisabledImage(imagen.getImage()));

        // Imagen arriba y nombre debajo.
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setIconTextGap(2);
        setFont(new Font("SansSerif", Font.BOLD, 10));
        setForeground(Color.BLACK);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);
        setSeleccionada(false);
    }

    public void setSeleccionada(boolean seleccionada) {
        setIcon(imagen);
        setBackground(seleccionada ? FONDO_SELECCIONADA : FONDO);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(seleccionada ? BORDE_SELECCIONADA : BORDE, seleccionada ? 3 : 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    }

    // Personaje que ya no es una opción posible. Si se descartó en el último turno se resalta en rojo
    // para que se vea qué cambió; si no, queda apagado en gris.
    public void setDescartada(boolean recienDescartada) {
        setIcon(imagenDescartada);
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

    // Busca la imagen del personaje a partir de su nombre ("Harry Potter" -> harry_potter.jpg).
    // Si no existe, devuelve null y la tarjeta muestra solo el nombre.
    private static ImageIcon cargarImagen(Personaje personaje) {
        String archivo = Normalizer.normalize(personaje.getNombre(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_") + ".jpg";
        URL url = TarjetaPersonaje.class.getResource(CARPETA_IMAGENES + archivo);
        if (url == null) {
            return null;
        }
        Image escalada = new ImageIcon(url).getImage()
                .getScaledInstance(TAM_IMAGEN, TAM_IMAGEN, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
    }
}