package structures.tree;

import structures.DataStructure;

/**
 * Implementación de un Splay Tree
 * Su característica principal es que después de cada acceso (inserción, búsqueda o borrado),
 * el nodo afectado se mueve a la raíz mediante una operación llamada "Splaying"
 */
public class Splay implements DataStructure {

    private static class Node {
        int key;
        Node left, right, parent; // El puntero 'parent' facilita las rotaciones durante el splay
        Node(int k) { key = k; }
    }

    private Node root;
    private int size;
    private long lastComparisons;
    private String[] lastSteps = new String[32];
    private int stepsCount = 0;

    /** Manejo dinamico del historial de pasos para el visualizador */
    private void addStep(String step) {
        if (stepsCount == lastSteps.length) {
            String[] bigger = new String[lastSteps.length * 2];
            System.arraycopy(lastSteps, 0, bigger, 0, stepsCount);
            lastSteps = bigger;
        }
        lastSteps[stepsCount++] = step;
    }

    //  Rotaciones Basicas (ajustan los punteros parent) 

    private void rotateRight(Node x) {
        Node y = x.left;
        x.left = y.right;
        if (y.right != null) y.right.parent = x;
        y.parent = x.parent;
        if (x.parent == null) root = y;
        else if (x == x.parent.right) x.parent.right = y;
        else x.parent.left = y;
        y.right = x;
        x.parent = y;
    }

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

    /**
     * Operacion Splay: Sube el nodo x a la raiz usando casos Zig, Zig-Zig o Zig-Zag
     */
    private void splay(Node x) {
        while (x.parent != null) {
            Node p = x.parent;
            Node g = p.parent;
            if (g == null) {
                // Caso ZIG: El padre es la raíz
                if (x == p.left) rotateRight(p);
                else rotateLeft(p);
                addStep("  Splay zig en " + x.key);
            } else if (x == p.left && p == g.left) {
                // Caso ZIG-ZIG: x y p son hijos del mismo lado
                rotateRight(g); rotateRight(p);
                addStep("  Splay zig-zig en " + x.key);
            } else if (x == p.right && p == g.right) {
                // Caso ZIG-ZIG simétrico
                rotateLeft(g); rotateLeft(p);
                addStep("  Splay zig-zig (der) en " + x.key);
            } else if (x == p.right && p == g.left) {
                // Caso ZIG-ZAG: x y p son hijos de lados opuestos
                rotateLeft(p); rotateRight(g);
                addStep("  Splay zig-zag en " + x.key);
            } else {
                // Caso ZIG-ZAG simetrico
                rotateRight(p); rotateLeft(g);
                addStep("  Splay zig-zag (izq) en " + x.key);
            }
        }
    }

      public String getName() { return "Splay"; }

     
    public void insert(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        addStep("Insertar " + key + " en Splay Tree");
        
        if (root == null) {
            root = new Node(key);
            size++;
            addStep("  Árbol vacío → raíz = " + key);
            return;
        }

        Node cur = root;
        Node last = null;
        boolean wentLeft = false;

        // Busqueda estandar de BST para encontrar la posicion de insercion
        while (cur != null) {
            lastComparisons++;
            last = cur;
            if (key < cur.key) {
                addStep("  " + key + " < " + cur.key + " → izq");
                cur = cur.left;
                wentLeft = true;
            } else if (key > cur.key) {
                addStep("  " + key + " > " + cur.key + " → der");
                cur = cur.right;
                wentLeft = false;
            } else {
                addStep("  Duplicado " + key + ", hacer splay");
                splay(last); // Incluso si es duplicado, el ultimo nodo visitado sube
                return;
            }
        }

        Node node = new Node(key);
        node.parent = last;
        if (wentLeft) last.left = node;
        else last.right = node;
        size++;
        
        addStep("  Insertar nodo(" + key + "), hacer splay al root");
        splay(node); // El nodo recien insertado se convierte en la nueva raiz
    }

     
    public boolean search(int key) {
        lastComparisons = 0;
        if (root == null) return false;
        
        Node cur = root;
        Node last = null;
        while (cur != null) {
            lastComparisons++;
            last = cur;
            if (key == cur.key) { 
                splay(cur); // Si lo encuentra, sube a la raiz
                return true; 
            }
            cur = key < cur.key ? cur.left : cur.right;
        }
        
        if (last != null) splay(last); // Si NO lo encuentra, el ultimo nodo visitado sube
        return false;
    }

     
    public boolean delete(int key) {
        lastComparisons = 0;
        // El search(key) ya hace el splay del nodo si existe
        if (!search(key)) return false; 
        
        // Ahora el nodo a eliminar es la raiz
        Node left = root.left;
        Node right = root.right;
        
        if (left == null) {
            root = right;
            if (right != null) right.parent = null;
        } else {
            root = left;
            left.parent = null;
            // Splay del maximo del subarbol izquierdo para unir con el derecho
            Node maxLeft = left;
            while (maxLeft.right != null) maxLeft = maxLeft.right;
            splay(maxLeft);
            root.right = right;
            if (right != null) right.parent = root;
        }
        size--;
        return true;
    }

    //  Metricas e Interfaz

      public long getLastComparisons() { return lastComparisons; }
      public int size() { return size; }
      public int height() { return heightRec(root); }

    private int heightRec(Node n) {
        if (n == null) return 0;
        return 1 + Math.max(heightRec(n.left), heightRec(n.right));
    }

     
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

      public String insertComplexity() { return "O(log n) amort."; }
      public String searchComplexity() { return "O(log n) amort."; }
      public String deleteComplexity() { return "O(log n) amort."; }

    public int[] getNodePositionsFlat(int canvasW, int canvasH) {
        int[] flat = new int[size * 4];
        int[] idx = {0};
        int levelH = height() > 0 ? canvasH / (height() + 1) : canvasH;
        collectFlat(root, 0, canvasW, 0, levelH, flat, idx, -1);
        return flat;
    }

    private void collectFlat(Node node, int xMin, int xMax, int depth,
                              int levelH, int[] out, int[] idx, int parentKey) {
        if (node == null || idx[0] + 3 >= out.length) return;
        int x = (xMin + xMax) / 2;
        int y = depth * levelH + levelH / 2;
        out[idx[0]++] = node.key;
        out[idx[0]++] = x;
        out[idx[0]++] = y;
        out[idx[0]++] = parentKey;
        collectFlat(node.left,  xMin, x,    depth + 1, levelH, out, idx, node.key);
        collectFlat(node.right, x,    xMax, depth + 1, levelH, out, idx, node.key);
    }
}