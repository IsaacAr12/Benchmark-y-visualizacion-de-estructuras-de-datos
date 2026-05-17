package structures.tree;

import structures.DataStructure;

/**
 * Arbol Rojo-Negro - insercion y busqueda unicamente (borrado = N/A segun especificacion).
 * Invariantes:
 *   1. Cada nodo es ROJO o NEGRO.
 *   2. La raiz es NEGRA.
 *   3. No puede haber dos nodos ROJOS consecutivos.
 *   4. Todo camino raiz => null tiene el mismo numero de nodos NEGROS.
 */
public class RedBlackTree implements DataStructure {

    static final boolean RED = true;
    static final boolean BLACK = false;

    static class Node {
        int key;
        boolean color;
        Node left, right, parent;
        Node(int k, boolean c) { key = k; color = c; }
    }

    private Node root;
    private int size;
    private long lastComparisons;
    private String[] lastSteps = new String[32];
    private int stepsCount = 0;

    private void addStep(String step) {
        if (stepsCount == lastSteps.length) {
            String[] bigger = new String[lastSteps.length * 2];
            System.arraycopy(lastSteps, 0, bigger, 0, stepsCount);
            lastSteps = bigger;
        }
        lastSteps[stepsCount++] = step;
    }

    //  Metodos auxiliares 
    private boolean isRed(Node n) { return n != null && n.color == RED; }

    private void rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        if (y.left != null) y.left.parent = x;
        y.parent = x.parent;
        if (x.parent == null) root = y;
        else if (x == x.parent.left) x.parent.left = y;
        else x.parent.right = y;
        y.left = x;
        x.parent = y;
    }

    private void rotateRight(Node y) {
        Node x = y.left;
        y.left = x.right;
        if (x.right != null) x.right.parent = y;
        x.parent = y.parent;
        if (y.parent == null) root = x;
        else if (y == y.parent.left) y.parent.left = x;
        else y.parent.right = x;
        x.right = y;
        y.parent = x;
    }

    private void fixInsert(Node z) {
        while (z.parent != null && z.parent.color == RED) {
            Node p = z.parent;
            Node g = p.parent;
            if (g == null) break;
            if (p == g.left) {
                Node uncle = g.right;
                if (isRed(uncle)) {
                    // Caso 1: tio es rojo => recolorear
                    p.color = BLACK; uncle.color = BLACK; g.color = RED;
                    addStep("  Arreglo: recolorear en " + g.key);
                    z = g;
                } else {
                    if (z == p.right) {
                        // Caso 2: tio negro, z es hijo derecho
                        z = p; rotateLeft(z);
                        addStep("  Arreglo: rotacion izquierda en " + z.key);
                        p = z.parent; g = p.parent;
                    }
                    // Caso 3
                    p.color = BLACK; g.color = RED;
                    rotateRight(g);
                    addStep("  Arreglo: rotacion derecha en " + g.key);
                }
            } else {
                Node uncle = g.left;
                if (isRed(uncle)) {
                    p.color = BLACK; uncle.color = BLACK; g.color = RED;
                    addStep("  Arreglo: recolorear en " + g.key);
                    z = g;
                } else {
                    if (z == p.left) {
                        z = p; rotateRight(z);
                        addStep("  Arreglo: rotacion derecha en " + z.key);
                        p = z.parent; g = p.parent;
                    }
                    p.color = BLACK; g.color = RED;
                    rotateLeft(g);
                    addStep("  Arreglo: rotacion izquierda en " + g.key);
                }
            }
        }
        root.color = BLACK;
    }

    // DataStructure 
    public String getName() { return "Rojo-Negro"; }

    public void insert(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        addStep("Insertar " + key + " en Arbol Rojo-Negro");
        Node z = new Node(key, RED);
        if (root == null) {
            root = z; root.color = BLACK; size++;
            addStep("  Arbol vacio → raiz negra = " + key);
            return;
        }
        Node cur = root, parent = null;
        while (cur != null) {
            lastComparisons++;
            parent = cur;
            if (key < cur.key) { addStep("  " + key + " < " + cur.key + " → izquierda"); cur = cur.left; }
            else if (key > cur.key) { addStep("  " + key + " > " + cur.key + " → derecha"); cur = cur.right; }
            else { addStep("  Duplicado " + key + ", ignorado"); return; }
        }
        z.parent = parent;
        if (key < parent.key) parent.left = z;
        else parent.right = z;
        size++;
        addStep("  Nodo insertado como ROJO, ajustando invariantes");
        fixInsert(z);
    }

    public boolean search(int key) {
        lastComparisons = 0;
        Node cur = root;
        while (cur != null) {
            lastComparisons++;
            if (key == cur.key) return true;
            cur = key < cur.key ? cur.left : cur.right;
        }
        return false;
    }

    public boolean delete(int key) {
        throw new UnsupportedOperationException("Rojo-Negro: borrado N/A segun especificacion");
    }

    public long getLastComparisons() { return lastComparisons; }
    public int size() { return size; }

    public int height() { return heightRec(root); }
    private int heightRec(Node n) {
        if (n == null) return 0;
        return 1 + Math.max(heightRec(n.left), heightRec(n.right));
    }

    public void clear() { root = null; size = 0; lastComparisons = 0; stepsCount = 0; }

    public String[] getLastInsertSteps() {
        String[] copy = new String[stepsCount];
        System.arraycopy(lastSteps, 0, copy, 0, stepsCount);
        return copy;
    }

    public String insertComplexity() { return "O(log n)"; }
    public String searchComplexity() { return "O(log n)"; }
    public String deleteComplexity() { return "N/A"; }

    /** Retorna array plano: [key, x, y, parentKey, color(1=rojo, 0=negro), ...] por nodo */
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
        out[idx[0]++] = node.color == RED ? 1 : 0;
        collectFlat(node.left,  xMin, x,    depth + 1, levelH, out, idx, node.key);
        collectFlat(node.right, x,    xMax, depth + 1, levelH, out, idx, node.key);
    }
}