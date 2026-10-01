package utils;

import java.util.ArrayList;
import java.util.List;

import model.CasaHogwarts;
import model.ColorPelo;
import model.Edad;
import model.Filtro;
import model.Genero;
import model.SangreLimpia;
import model.TipoFiltro;

public class FiltroFactory {
    public static List<Filtro> crearFiltros() {
        String[] siNo = { "true", "false" };
        List<Filtro> filtros = new ArrayList<>();

        filtros.add(new Filtro(TipoFiltro.GENERO, "Género", nombresDe(Genero.values())));
        filtros.add(new Filtro(TipoFiltro.EDAD, "Edad", nombresDe(Edad.values())));
        filtros.add(new Filtro(TipoFiltro.COLOR_PELO, "Color de pelo", nombresDe(ColorPelo.values())));
        filtros.add(new Filtro(TipoFiltro.CALVICIE, "Calvicie", siNo));
        filtros.add(new Filtro(TipoFiltro.LENTES, "Lentes", siNo));
        filtros.add(new Filtro(TipoFiltro.CASA_HOGWARTS, "Casa", nombresDe(CasaHogwarts.values())));
        filtros.add(new Filtro(TipoFiltro.ALUMNO, "Alumno", siNo));
        filtros.add(new Filtro(TipoFiltro.SANGRE_LIMPIA, "Sangre", nombresDe(SangreLimpia.values())));

        return filtros;
    }

    private static <T extends Enum<T>> String[] nombresDe(T[] valores) {
        String[] nombres = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            nombres[i] = valores[i].name();
        }
        return nombres;
    }
}
