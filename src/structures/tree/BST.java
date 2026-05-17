package structures.tree;

import structures.DataStructure;

/**
 * Arbol Binario de Busqueda (BST - Binary Search Tree)
 * Mantiene la propiedad: izq < raiz < der en cada nodo
 */
public class BST implements DataStructure {

    /** Nodo interno del arbol. */
    private static class Node {
        int key;
        Node left, right;
        Node(int k) { key = k; }
    }

    private Node root;           // Raiz del arbol
    private int size;            // Cantidad de nodos
    private long lastComparisons; // Comparaciones de la ultima operacion
    private String[] lastSteps = new String[32]; // Pasos del ultimo insert (para visualizar)
    private int stepsCount = 0;

    /** Agrega un paso al registro, expandiendo el arreglo si hace falta */
    private void addStep(String step) {
        if (stepsCount == lastSteps.length) {
            String[] bigger = new String[lastSteps.length * 2];
            System.arraycopy(lastSteps, 0, bigger, 0, stepsCount);
            lastSteps = bigger;
        }
        lastSteps[stepsCount++] = step;
    }

    //  INSERT

    /** Inserta una clave en el arbol (ignora duplicados) */
    public void insert(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        addStep("Insertar " + key + " en BST");
        root = insertRec(root, key);
    }

    /**
     * Insercion recursiva
     * Navega izquierda si key < nodo, derecha si key > nodo,
     * o crea un nodo nuevo al llegar a null
     */
    private Node insertRec(Node node, int key) {
        if (node == null) {
            size++;
            addStep("  => Nodo nulo: crear nodo(" + key + ")");
            return new Node(key);
        }
        lastComparisons++;
        if (key < node.key) {
            addStep("  " + key + " < " + node.key + " => ir a la izquierda");
            node.left = insertRec(node.left, key);
        } else if (key > node.key) {
            addStep("  " + key + " > " + node.key + " => ir a la derecha");
            node.right = insertRec(node.right, key);
        } else {
            addStep("  " + key + " == " + node.key + " => duplicado, ignorar");
        }
        return node;
    }

    //  SEARCH

    /** Retorna true si la clave existe en el arbol */
    public boolean search(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        return searchRec(root, key);
    }

    /** Busqueda recursiva estandar de BST */
    private boolean searchRec(Node node, int key) {
        if (node == null) return false;
        lastComparisons++;
        if (key == node.key) return true;
        if (key < node.key) return searchRec(node.left, key);
        return searchRec(node.right, key);
    }

    //  DELETE

    /** Elimina una clave; retorna true si existia */
    public boolean delete(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        int before = size;
        root = deleteRec(root, key);
        return size < before;
    }

    /**
     * Eliminacion recursiva con tres casos:
     *  1. Sin hijo izquierdo => reemplazar con hijo derecho
     *  2. Sin hijo derecho  => reemplazar con hijo izquierdo
     *  3. Dos hijos         => sustituir con el sucesor in-order
     *                         (mínimo del subárbol derecho)
     */
    private Node deleteRec(Node node, int key) {
        if (node == null) return null;
        lastComparisons++;
        if (key < node.key) {
            node.left = deleteRec(node.left, key);
        } else if (key > node.key) {
            node.right = deleteRec(node.right, key);
        } else {
            size--;
            if (node.left == null) return node.right;  // caso 1
            if (node.right == null) return node.left;  // caso 2
            // Caso 3: encontrar sucesor, copiar su clave y eliminar el sucesor
            Node successor = minNode(node.right);
            node.key = successor.key;
            node.right = deleteRec(node.right, successor.key);
            size++; // la llamada recursiva decremento size de mas; corregir
        }
        return node;
    }

    /** Retorna el nodo con la clave minima (el más a la izquierda) */
    private Node minNode(Node node) {
        while (node.left != null) node = node.left;
        return node;
    }

    //  UTILIDADES

    public long getLastComparisons() { return lastComparisons; }
    public int size() { return size; }

    /** Altura del arbol (nodos en el camino mas largo desde la raiz) */
    public int height() { return heightRec(root); }

    private int heightRec(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(heightRec(node.left), heightRec(node.right));
    }

    /** Vacia el arbol completamente */
    public void clear() {
        root = null;
        size = 0;
        lastComparisons = 0;
        stepsCount = 0;
    }

    /** Retorna los pasos registrados durante el ultimo insert */
    public String[] getLastInsertSteps() {
        String[] copy = new String[stepsCount];
        System.arraycopy(lastSteps, 0, copy, 0, stepsCount);
        return copy;
    }

    //  METADATA (para la interfaz)

    public String getName() { return "BST"; }
    public String insertComplexity() { return "O(n) peor, O(log n) prom."; }
    public String searchComplexity() { return "O(n) peor, O(log n) prom."; }
    public String deleteComplexity() { return "O(n) peor, O(log n) prom."; }

    //  VISUALIZACION

    /**
     * Retorna la posicion (x, y) en el canvas de un nodo con clave 'key'
     * Divide el ancho disponible a la mitad en cada nivel
     */
    public int[] getNodePosition(int canvasW, int canvasH, int key) {
        return findPosition(root, 0, canvasW, 0,
                canvasH / (height() + 1), key);
    }

    /**
     * Recorre todos los nodos y retorna un arreglo plano:
     *   [key, x, y, parentKey,  key, x, y, parentKey, ...]
     * Util para dibujar el arbol completo de una sola vez
     */
    public int[] getNodePositionsFlat(int canvasW, int canvasH) {
        int count = size;
        int[] flat = new int[count * 4];
        int[] idx = {0}; // índice actual en el arreglo (array para poder modificarlo en recursión)
        int levelH = height() > 0 ? canvasH / (height() + 1) : canvasH;
        collectFlat(root, 0, canvasW, 0, levelH, flat, idx, -1);
        return flat;
    }

    /** Rellena 'out' con los datos de posicion de cada nodo en pre-order */
    private void collectFlat(Node node, int xMin, int xMax, int depth,
                             int levelH, int[] out, int[] idx, int parentKey) {
        if (node == null || idx[0] + 3 >= out.length) return;
        int x = (xMin + xMax) / 2;          // centro horizontal del rango asignado
        int y = depth * levelH + levelH / 2; // centro vertical del nivel
        out[idx[0]++] = node.key;
        out[idx[0]++] = x;
        out[idx[0]++] = y;
        out[idx[0]++] = parentKey;
        collectFlat(node.left,  xMin, x,    depth + 1, levelH, out, idx, node.key);
        collectFlat(node.right, x,    xMax, depth + 1, levelH, out, idx, node.key);
    }

    /** Busca recursivamente la posicion (x, y) de la clave 'target' */
    private int[] findPosition(Node node, int xMin, int xMax,
                               int depth, int levelH, int target) {
        if (node == null) return null;
        int x = (xMin + xMax) / 2;
        int y = depth * levelH + levelH / 2;
        if (node.key == target) return new int[]{x, y};
        if (target < node.key) return findPosition(node.left,  xMin, x,    depth+1, levelH, target);
        return                        findPosition(node.right, x,    xMax, depth+1, levelH, target);
    }
}