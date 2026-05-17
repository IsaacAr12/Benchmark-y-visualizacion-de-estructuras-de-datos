package structures;

/**
 * Implementación de una Lista Enlazada Simple.
 * Cada nodo apunta al siguiente; operaciones lineales.
 */
public class SimpleLinkedList implements DataStructure {

    private static class Node {
        int key;
        Node next;
        Node(int k) { key = k; }
    }

    private Node head;
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

    @Override
    public String getName() { return "Lista Simple"; }

    @Override
    public void insert(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        addStep("Insertar " + key + " en lista");
        Node n = new Node(key);
        if (head == null) {
            head = n;
        } else {
            Node cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = n;
        }
        size++;
    }

    @Override
    public boolean search(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        Node cur = head;
        while (cur != null) {
            lastComparisons++;
            if (cur.key == key) return true;
            cur = cur.next;
        }
        return false;
    }

    @Override
    public boolean delete(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        if (head == null) return false;

        if (head.key == key) {
            head = head.next;
            size--;
            return true;
        }

        Node cur = head;
        while (cur.next != null) {
            lastComparisons++;
            if (cur.next.key == key) {
                cur.next = cur.next.next;
                size--;
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    @Override
    public long getLastComparisons() { return lastComparisons; }

    @Override
    public int size() { return size; }

    @Override
    public int height() { return -1; } // lineal

    @Override
    public void clear() {
        head = null;
        size = 0;
        lastComparisons = 0;
        stepsCount = 0;
    }

    @Override
    public String[] getLastInsertSteps() {
        String[] copy = new String[stepsCount];
        System.arraycopy(lastSteps, 0, copy, 0, stepsCount);
        return copy;
    }

    @Override
    public String insertComplexity() { return "O(n)"; }

    @Override
    public String searchComplexity() { return "O(n)"; }

    @Override
    public String deleteComplexity() { return "O(n)"; }
}
