package structures;

/**
 * Interfaz comun para las seis estructuras de datos evaluadas en el benchmark
 * Cada operación debe actualizar las comparaciones internas para que el benchmark
 * pueda leerlas despues de la llamada
 */
public interface DataStructure {

    /** Nombre legible para mostrar en la interfaz de usuario */
    String getName();

    /** Inserta una llave. Reinicia y actualiza las comparaciones internas */
    void insert(int key);

    /**
     * Busca una llave. Retorna true si la encuentra
     * Reinicia y actualiza las comparaciones internas
     */
    boolean search(int key);

    /**
     * Elimina una llave. Retorna true si la llave existia
     * Reinicia y actualiza las comparaciones internas
     * Puede lanzar UnsupportedOperationException (ej. BST)
     */
    boolean delete(int key);

    /** Cuantas comparaciones de llaves realiza la ultima operacion */
    long getLastComparisons();

    /** Numero actual de elementos en la estructura */
    int size();

    /** Altura del arbol, o -1 para estructuras lineales */
    int height();

    /** Elimina todos los elementos */
    void clear();

    /**
     * Retorna una lista de capturas (strings) que describen cada paso dado durante
     * la ULTIMA operacion de insercion (para la visualizacion paso a paso)
     */
    String[] getLastInsertSteps();

    /** Complejidad O(·) teorica para insercion (promedio/esperado) */
    String insertComplexity();

    /** Complejidad O(·) teorica para budqueda (promedio/esperado) */
    String searchComplexity();

    /** Complejidad O(·) teorica para borrado (promedio/esperado), o "N/A" */
    String deleteComplexity();
}