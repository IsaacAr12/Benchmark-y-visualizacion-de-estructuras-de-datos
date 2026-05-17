package structures.tree;

import structures.DataStructure;

/**
 * Implementacion de un Arbol AVL (Arbol Binario de Búsqueda Balanceado).
 * Mantiene una diferencia de altura maxima de 1 entre subarboles para garantizar eficiencia O(log n).
 */
public class AVL implements DataStructure {

    /** Clase interna que representa un nodo con información de altura para el balanceo. */
    private static class Node {
        int key, height;
        Node left, right;
        Node(int k) { 
            key = k; 
            height = 1; // Un nodo nuevo empieza con altura 1
        }
    }

    private Node root;                  // Raiz del arbol
    private int size;                  // Cantidad de nodos
    private long lastComparisons;      // Métricas para el benchmark
    private String[] lastSteps = new String[32]; // Almacen de pasos para la UI
    private int stepsCount = 0;

    /** Registra una accion en el historial de pasos para la visualizacion secuencial. */
    private void addStep(String step) {
        if (stepsCount == lastSteps.length) {
            String[] bigger = new String[lastSteps.length * 2];
            System.arraycopy(lastSteps, 0, bigger, 0, stepsCount);
            lastSteps = bigger;
        }
        lastSteps[stepsCount++] = step;
    }

    // Metodos de utilidad para el balanceo 
    
    private int h(Node n) { return n == null ? 0 : n.height; }
    
    private void updateHeight(Node n) { n.height = 1 + Math.max(h(n.left), h(n.right)); }
    
    /** Calcula el Factor de Equilibrio (Balance Factor). */
    private int bf(Node n) { return h(n.left) - h(n.right); }

    /** Realiza una rotacion simple a la derecha para balancear el arbol. */
    private Node rotateRight(Node y) {
        Node x = y.left, T2 = x.right;
        x.right = y; 
        y.left = T2;
        updateHeight(y); 
        updateHeight(x);
        addStep("    Rotación derecha en " + y.key);
        return x;
    }

    /** Realiza una rotacion simple a la izquierda para balancear el arbol. */
    private Node rotateLeft(Node x) {
        Node y = x.right, T2 = y.left;
        y.left = x; 
        x.right = T2;
        updateHeight(x); 
        updateHeight(y);
        addStep("    Rotación izquierda en " + x.key);
        return y;
    }

    /** Verifica el factor de equilibrio y aplica las rotaciones necesarias (LL, RR, LR, RL). */
    private Node balance(Node node) {
        updateHeight(node);
        int factor = bf(node);
        
        // Caso Izquierda-Izquierda o Izquierda-Derecha
        if (factor > 1) {
            if (bf(node.left) < 0) { // Caso LR
                addStep("    Caso LR en " + node.key);
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        
        // Caso Derecha-Derecha o Derecha-Izquierda
        if (factor < -1) {
            if (bf(node.right) > 0) { // Caso RL
                addStep("    Caso RL en " + node.key);
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    public String getName() { return "AVL"; }

    
    public void insert(int key) {
        lastComparisons = 0; // Reinicio de metricas para la nueva operacion
        stepsCount = 0;
        addStep("Insertar " + key + " en AVL");
        root = insertRec(root, key);
    }

    private Node insertRec(Node node, int key) {
        if (node == null) {
            size++;
            addStep("  Crear nodo(" + key + ")");
            return new Node(key);
        }
        
        lastComparisons++; // Conteo de comparacion para el benchmark
        if (key < node.key) {
            addStep("  " + key + " < " + node.key + " → izquierda");
            node.left = insertRec(node.left, key);
        } else if (key > node.key) {
            addStep("  " + key + " > " + node.key + " → derecha");
            node.right = insertRec(node.right, key);
        } else {
            addStep("  Duplicado " + key + ", ignorar");
            return node;
        }
        
        // Tras insertar, rebalancear el camino hacia la raiz
        return balance(node);
    }

    
    public boolean search(int key) {
        lastComparisons = 0;
        Node cur = root;
        while (cur != null) {
            lastComparisons++; // Cada nodo visitado es una comparacion
            if (key == cur.key) return true;
            cur = key < cur.key ? cur.left : cur.right;
        }
        return false;
    }

    
    public boolean delete(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        int before = size;
        root = deleteRec(root, key);
        return size < before;
    }

    private Node deleteRec(Node node, int key) {
        if (node == null) return null;
        
        lastComparisons++;
        if (key < node.key) {
            node.left = deleteRec(node.left, key);
        } else if (key > node.key) {
            node.right = deleteRec(node.right, key);
        } else {
            // Nodo encontrado para eliminar
            size--;
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            
            // Nodo con dos hijos: buscar el sucesor
            Node successor = minNode(node.right);
            node.key = successor.key;
            node.right = deleteRec(node.right, successor.key);
            size++; // Compensacion por la recursion
        }
        
        // Tras eliminar, rebalancear
        return balance(node);
    }

    private Node minNode(Node n) { while (n.left != null) n = n.left; return n; }

    // Implementacion de metricas de la interfaz

    public long getLastComparisons() { return lastComparisons; }
    public int size() { return size; }
    public int height() { return h(root); }

    public void clear() {
        root = null;
        size = 0;
        lastComparisons = 0;
        stepsCount = 0;
    }

    
    public String[] getLastInsertSteps() {
        String[] copy = new String[stepsCount];
        System.arraycopy(lastSteps, 0, copy, 0, stepsCount);
        return copy;
    }

    // Complejidades teoricas para la tabla del informe[cite: 1]
    public String insertComplexity() { return "O(log n)"; }
    public String searchComplexity() { return "O(log n)"; }
    public String deleteComplexity() { return "O(log n)"; }

    /** 
     * Genera un arreglo con datos de posicion para dibujar el arbol.
     * Incluye: llave, x, y, llave del padre y factor de balanceo.
     */
    public int[] getNodePositionsFlat(int canvasW, int canvasH) {
        int[] flat = new int[size * 5];
        int[] idx = {0};
        int levelH = height() > 0 ? canvasH / (height() + 1) : canvasH;
        collectFlat(root, 0, canvasW, 0, levelH, flat, idx, -1);
        return flat;
    }

    private void collectFlat(Node node, int xMin, int xMax, int depth,
                              int levelH, int[] out, int[] idx, int parentKey) {
        if (node == null || idx[0] + 4 >= out.length) return;
        
        int x = (xMin + xMax) / 2;
        int y = depth * levelH + levelH / 2;
        
        out[idx[0]++] = node.key;
        out[idx[0]++] = x;
        out[idx[0]++] = y;
        out[idx[0]++] = parentKey;
        out[idx[0]++] = bf(node); // Dato extra para mostrar el balanceo en la UI
        
        collectFlat(node.left,  xMin, x,    depth + 1, levelH, out, idx, node.key);
        collectFlat(node.right, x,    xMax, depth + 1, levelH, out, idx, node.key);
    }
}