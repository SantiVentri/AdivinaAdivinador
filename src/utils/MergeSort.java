package utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MergeSort {

    private MergeSort() {    }

    // Ordena la lista recibida según el comparador indicado.
    // Algoritmo de División y Conquista, estable, O(n log n).
    public static <T> void ordenar(List<T> lista, Comparator<? super T> comparador) {
        if (lista == null || lista.size() < 2) {
            return;
        }

        dividir(lista, 0, lista.size() - 1, comparador);
    }

    // DIVIDIR: parte la lista a la mitad hasta llegar a sublistas de 1 elemento
    private static <T> void dividir(List<T> lista, int inicio, int fin, Comparator<? super T> comparador) {
        if (inicio >= fin) {
            return; // caso base: 0 o 1 elemento ya está ordenado
        }

        int medio = (inicio + fin) / 2;

        dividir(lista, inicio, medio, comparador);
        dividir(lista, medio + 1, fin, comparador);
        combinar(lista, inicio, medio, fin, comparador);
    }

    // COMBINAR: fusiona dos mitades ya ordenadas en una sola
    private static <T> void combinar(List<T> lista, int inicio, int medio, int fin, Comparator<? super T> comparador) {
        List<T> izquierda = new ArrayList<>(lista.subList(inicio, medio + 1));
        List<T> derecha = new ArrayList<>(lista.subList(medio + 1, fin + 1));

        int i = 0;
        int j = 0;
        int k = inicio;

        while (i < izquierda.size() && j < derecha.size()) {
            // "<= 0" mantiene la estabilidad: ante empate, gana el de la izquierda
            if (comparador.compare(izquierda.get(i), derecha.get(j)) <= 0) {
                lista.set(k, izquierda.get(i));
                i++;
            } else {
                lista.set(k, derecha.get(j));
                j++;
            }
            k++;
        }

        while (i < izquierda.size()) {
            lista.set(k, izquierda.get(i));
            i++;
            k++;
        }

        while (j < derecha.size()) {
            lista.set(k, derecha.get(j));
            j++;
            k++;
        }
    }
}