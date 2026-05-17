package structures;

public class MyArray implements DataStructure {
    private int[] data;
    private int size;
    private long lastComparisons;
    private String[] lastSteps = new String[32];
    private int stepsCount = 0;

    public MyArray(int capacity) {
        data = new int[capacity];
        size = 0;
    }

    private void addStep(String step) {
        if (stepsCount == lastSteps.length) {
            String[] bigger = new String[lastSteps.length * 2];
            System.arraycopy(lastSteps, 0, bigger, 0, stepsCount);
            lastSteps = bigger;
        }
        lastSteps[stepsCount++] = step;
    }

    @Override
    public String getName() { return "Arreglo"; }

    @Override
    public void insert(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        addStep("Insertar " + key + " en arreglo");
        if (size == data.length) {
            // expandir capacidad
            int[] bigger = new int[data.length * 2];
            System.arraycopy(data, 0, bigger, 0, size);
            data = bigger;
        }
        data[size++] = key;
    }

    @Override
    public boolean search(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        for (int i = 0; i < size; i++) {
            lastComparisons++;
            if (data[i] == key) return true;
        }
        return false;
    }

    @Override
    public boolean delete(int key) {
        lastComparisons = 0;
        stepsCount = 0;
        for (int i = 0; i < size; i++) {
            lastComparisons++;
            if (data[i] == key) {
                // correr elementos hacia la izquierda
                for (int j = i; j < size - 1; j++) {
                    data[j] = data[j + 1];
                }
                size--;
                return true;
            }
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
    public String insertComplexity() { return "O(1) amort."; }

    @Override
    public String searchComplexity() { return "O(n)"; }

    @Override
    public String deleteComplexity() { return "O(n)"; }
}
