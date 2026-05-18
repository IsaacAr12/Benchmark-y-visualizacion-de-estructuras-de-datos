package benchmark;

import structures.DataStructure;
import java.util.*;
import java.io.*;

/**
 * Clase Benchmark: ejecuta corridas sobre las estructuras activas
 * con parámetros N, semilla, W (warmup) y R (iteraciones).
 */
public class Benchmark {

    private int N;              // cantidad de claves
    private long seed;          // semilla para reproducibilidad
    private int W;              // warmup runs
    private int R;              // iteraciones medidas
    private int[] keys;         // claves a insertar
    private int[] queries;      // claves a buscar

    public Benchmark(int N, long seed, int W, int R) {
        this.N = N;
        this.seed = seed;
        this.W = W;
        this.R = R;
        generateKeys();
        generateQueries();
    }

    /** Genera secuencia reproducible de inserción */
    private void generateKeys() {
        Random rand = new Random(seed);
        keys = new int[N];

        for (int i = 0; i < N; i++) {
            keys[i] = rand.nextInt(N * 10); // rango amplio
        }
    }

    /** Genera lote de búsquedas automáticas */
    private void generateQueries() {
        Random rand = new Random(seed + 1);
        queries = new int[N / 2]; // mitad de N como ejemplo

        for (int i = 0; i < queries.length; i++) {
            queries[i] = rand.nextInt(N * 10);
        }
    }

    /** Ejecuta benchmark sobre una lista de estructuras */
    public List<ResultRow> run(List<DataStructure> structures) {
        List<ResultRow> results = new ArrayList<>();

        for (DataStructure ds : structures) {

            // Warmup: corridas completas que no se cuentan en la tabla final.
            for (int w = 0; w < W; w++) {
                ds.clear();
                runOnce(ds);
            }

            // Iteraciones medidas.
            long totalInsertTime = 0;
            long totalSearchTime = 0;
            long totalDeleteTime = 0;

            long totalInsertComp = 0;
            long totalSearchComp = 0;
            long totalDeleteComp = 0;

            long lastHeight = 0;
            long lastSize = 0;

            for (int r = 0; r < R; r++) {
                ds.clear();

                long[] metrics = runOnce(ds);

                totalInsertTime += metrics[0];
                totalSearchTime += metrics[1];
                totalDeleteTime += metrics[2];

                totalInsertComp += metrics[3];
                totalSearchComp += metrics[4];
                totalDeleteComp += metrics[5];

                lastHeight = metrics[6];
                lastSize = metrics[7];
            }

            results.add(new ResultRow(
                    ds.getName(),
                    totalInsertTime / R,
                    totalSearchTime / R,
                    totalDeleteTime / R,
                    totalInsertComp / R,
                    totalSearchComp / R,
                    totalDeleteComp / R,
                    ds.insertComplexity(),
                    ds.searchComplexity(),
                    ds.deleteComplexity(),
                    (int) lastHeight,
                    (int) lastSize
            ));
        }

        return results;
    }

    /** Ejecuta una corrida completa sobre una estructura */
    private long[] runOnce(DataStructure ds) {
        long insertTime = 0;
        long searchTime = 0;
        long deleteTime = 0;

        long insertComp = 0;
        long searchComp = 0;
        long deleteComp = 0;

        // Fase 1: inserción
        long t1 = System.nanoTime();

        for (int k : keys) {
            ds.insert(k);
            insertComp += ds.getLastComparisons();
        }

        long t2 = System.nanoTime();
        insertTime = t2 - t1;

        // Fase 2: búsqueda
        long t3 = System.nanoTime();

        for (int q : queries) {
            ds.search(q);
            searchComp += ds.getLastComparisons();
        }

        long t4 = System.nanoTime();
        searchTime = t4 - t3;

        // Capturar altura y tamaño antes de borrar
        long snapHeight = ds.height();
        long snapSize = ds.size();

        // Fase 3: borrado, excepto en Red-Black
        if (!ds.deleteComplexity().equals("N/A")) {
            long t5 = System.nanoTime();

            for (int k : keys) {
                ds.delete(k);
                deleteComp += ds.getLastComparisons();
            }

            long t6 = System.nanoTime();
            deleteTime = t6 - t5;
        }

        return new long[]{
                insertTime,
                searchTime,
                deleteTime,
                insertComp,
                searchComp,
                deleteComp,
                snapHeight,
                snapSize
        };
    }

    /** Exporta resultados a CSV */
    public void exportCSV(List<ResultRow> results, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {

            pw.println(
                    "Estructura,"
                    + "InsertTime(ns),"
                    + "SearchTime(ns),"
                    + "DeleteTime(ns),"
                    + "InsertComp,"
                    + "SearchComp,"
                    + "DeleteComp,"
                    + "InsertO,"
                    + "SearchO,"
                    + "DeleteO,"
                    + "Height,"
                    + "Size,"
                    + "N,"
                    + "Seed,"
                    + "W,"
                    + "R"
            );

            for (ResultRow r : results) {
                pw.println(r.toCSV(N, seed, W, R));
            }
        }
    }

    /** Clase interna para almacenar resultados de una estructura */
    public static class ResultRow {
        public String name;

        public long insertTime;
        public long searchTime;
        public long deleteTime;

        public long insertComp;
        public long searchComp;
        public long deleteComp;

        public String insertO;
        public String searchO;
        public String deleteO;

        public int height;
        public int size;

        public ResultRow(String name, long it, long st, long dt,
                         long ic, long sc, long dc,
                         String io, String so, String do_,
                         int h, int s) {
            this.name = name;

            this.insertTime = it;
            this.searchTime = st;
            this.deleteTime = dt;

            this.insertComp = ic;
            this.searchComp = sc;
            this.deleteComp = dc;

            this.insertO = io;
            this.searchO = so;
            this.deleteO = do_;

            this.height = h;
            this.size = s;
        }

        public String toCSV(int N, long seed, int W, int R) {
            String deleteTimeValue;
            String deleteCompValue;

            if ("N/A".equals(deleteO)) {
                deleteTimeValue = "N/A";
                deleteCompValue = "N/A";
            } else {
                deleteTimeValue = String.valueOf(deleteTime);
                deleteCompValue = String.valueOf(deleteComp);
            }

            return String.format(
                    "%s,%d,%d,%s,%d,%d,%s,%s,%s,%s,%d,%d,%d,%d,%d,%d",
                    name,
                    insertTime,
                    searchTime,
                    deleteTimeValue,
                    insertComp,
                    searchComp,
                    deleteCompValue,
                    insertO,
                    searchO,
                    deleteO,
                    height,
                    size,
                    N,
                    seed,
                    W,
                    R
            );
        }

        public String toCSV() {
            String deleteTimeValue;
            String deleteCompValue;

            if ("N/A".equals(deleteO)) {
                deleteTimeValue = "N/A";
                deleteCompValue = "N/A";
            } else {
                deleteTimeValue = String.valueOf(deleteTime);
                deleteCompValue = String.valueOf(deleteComp);
            }

            return String.format(
                    "%s,%d,%d,%s,%d,%d,%s,%s,%s,%s,%d,%d",
                    name,
                    insertTime,
                    searchTime,
                    deleteTimeValue,
                    insertComp,
                    searchComp,
                    deleteCompValue,
                    insertO,
                    searchO,
                    deleteO,
                    height,
                    size
            );
        }
    }
}