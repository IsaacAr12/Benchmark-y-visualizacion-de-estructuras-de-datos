package ui;

import benchmark.Benchmark;
import benchmark.Benchmark.ResultRow;
import structures.*;
import structures.tree.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class BenchmarkApp extends Application {

    private TextField nField, seedField, wField, rField;
    private ComboBox<String> searchModeBox;
    private TextArea manualQueriesArea;
    private Label statusLabel, keyPreviewLabel;

    private TableView<ResultRow> table;
    private Label tableFooter;
    private BarChart<String, Number> timeChart, compChart, heightChart;
    private TabPane resultsTabPane;

    private Benchmark lastBenchmark;
    private List<ResultRow> lastResults;
    private Button exportCsvBtn;

    // Secuencia paso a paso
    private ComboBox<String> stepStructBox;
    private Spinner<Integer> stepMaxSpinner;
    private int currentStepIndex = 0;
    private int[] stepKeys;
    private Label stepInfoLabel;
    private TextArea stepTextArea;
    private Canvas stepCanvas;

    // Visualizador de dos árboles
    private ComboBox<String> tree1Box, tree2Box;
    private Canvas canvas1, canvas2;
    private Label treeLabel1, treeLabel2;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Benchmark de Estructuras de Datos");

        // ═══════════ SIDEBAR ═══════════
        Label titleLabel = new Label("Configuración");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        nField = new TextField("1000");
        seedField = new TextField("42");
        wField = new TextField("1");
        rField = new TextField("3");

        GridPane params = new GridPane();
        params.setHgap(10);
        params.setVgap(8);
        params.add(new Label("N (elementos):"), 0, 0);  params.add(nField, 1, 0);
        params.add(new Label("Semilla:"), 0, 1);         params.add(seedField, 1, 1);
        params.add(new Label("Warmup (W):"), 0, 2);      params.add(wField, 1, 2);
        params.add(new Label("Iteraciones (R):"), 0, 3); params.add(rField, 1, 3);
        for (TextField tf : new TextField[]{nField, seedField, wField, rField})
            tf.setPrefWidth(120);

        keyPreviewLabel = new Label("");
        keyPreviewLabel.setWrapText(true);
        keyPreviewLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");
        updateKeyPreview();
        nField.textProperty().addListener((o, a, b) -> updateKeyPreview());
        seedField.textProperty().addListener((o, a, b) -> updateKeyPreview());

        Label searchLabel = new Label("Búsquedas");
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll("Automática", "Manual");
        searchModeBox.setValue("Automática");
        searchModeBox.setMaxWidth(Double.MAX_VALUE);

        manualQueriesArea = new TextArea();
        manualQueriesArea.setPromptText("Ej: 10, 25, 80\nUna por línea o separadas por coma.");
        manualQueriesArea.setPrefRowCount(3);
        manualQueriesArea.setWrapText(true);
        manualQueriesArea.setDisable(true);

        Button loadFileBtn = new Button("Cargar desde archivo");
        loadFileBtn.setStyle("-fx-font-size: 11px; -fx-cursor: hand;");
        loadFileBtn.setDisable(true);
        loadFileBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Cargar claves de búsqueda");
            fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Texto/CSV", "*.txt", "*.csv"),
                new FileChooser.ExtensionFilter("Todos", "*.*"));
            File f = fc.showOpenDialog(stage);
            if (f != null) {
                try {
                    String content = new String(java.nio.file.Files.readAllBytes(f.toPath()));
                    manualQueriesArea.setText(content.trim());
                    statusLabel.setText("Claves cargadas desde " + f.getName());
                    statusLabel.setStyle("-fx-text-fill: #2196F3;");
                } catch (IOException ex) {
                    statusLabel.setText("Error al leer archivo");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });

        searchModeBox.setOnAction(e -> {
            boolean manual = "Manual".equals(searchModeBox.getValue());
            manualQueriesArea.setDisable(!manual);
            loadFileBtn.setDisable(!manual);
        });

        Label structLabel = new Label("Estructuras");
        structLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        CheckBox bstBox = new CheckBox("BST");
        CheckBox avlBox = new CheckBox("AVL");
        CheckBox splayBox = new CheckBox("Splay");
        CheckBox rbBox = new CheckBox("Red-Black");
        CheckBox arrBox = new CheckBox("Arreglo");
        CheckBox listBox = new CheckBox("Lista Simple");
        bstBox.setSelected(true); avlBox.setSelected(true);
        splayBox.setSelected(true); rbBox.setSelected(true);
        VBox structuresBox = new VBox(5, bstBox, avlBox, splayBox, rbBox, arrBox, listBox);

        Button selectAllBtn = new Button("Seleccionar todas");
        selectAllBtn.setOnAction(e -> {
            for (CheckBox cb : new CheckBox[]{bstBox, avlBox, splayBox, rbBox, arrBox, listBox})
                cb.setSelected(true);
        });

        Button exampleBtn = new Button("⚡ Experimento de ejemplo");
        exampleBtn.setStyle("-fx-font-size: 12px; -fx-background-color: #FF9800; -fx-text-fill: white; "
                + "-fx-padding: 8 14; -fx-cursor: hand;");
        exampleBtn.setMaxWidth(Double.MAX_VALUE);
        exampleBtn.setOnAction(e -> {
            nField.setText("500"); seedField.setText("42"); wField.setText("1"); rField.setText("3");
            searchModeBox.setValue("Automática"); manualQueriesArea.setDisable(true);
            for (CheckBox cb : new CheckBox[]{bstBox, avlBox, splayBox, rbBox, arrBox, listBox})
                cb.setSelected(true);
            runBenchmark(bstBox, avlBox, splayBox, rbBox, arrBox, listBox);
        });

        Button runBtn = new Button("▶  Ejecutar Benchmark");
        runBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-background-color: #2196F3; -fx-text-fill: white; "
                + "-fx-padding: 10 20; -fx-cursor: hand;");
        runBtn.setMaxWidth(Double.MAX_VALUE);
        runBtn.setOnAction(e -> runBenchmark(bstBox, avlBox, splayBox, rbBox, arrBox, listBox));

        exportCsvBtn = new Button("Exportar CSV");
        exportCsvBtn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; "
                + "-fx-background-color: #4CAF50; -fx-text-fill: white; "
                + "-fx-padding: 8 16; -fx-cursor: hand;");
        exportCsvBtn.setMaxWidth(Double.MAX_VALUE);
        exportCsvBtn.setDisable(true);
        exportCsvBtn.setOnAction(e -> exportLastResults(stage));

        statusLabel = new Label("Listo para ejecutar");
        statusLabel.setStyle("-fx-text-fill: #666;");

        VBox sidebar = new VBox(10,
            titleLabel, new Separator(),
            params, keyPreviewLabel,
            new Separator(), searchLabel, new Label("Modo:"), searchModeBox, manualQueriesArea, loadFileBtn,
            new Separator(), structLabel, structuresBox, selectAllBtn,
            new Separator(), exampleBtn, runBtn, exportCsvBtn, statusLabel);
        sidebar.setPadding(new Insets(12));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: #f5f5f5;");

        ScrollPane sideScroll = new ScrollPane(sidebar);
        sideScroll.setFitToWidth(true);
        sideScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sideScroll.setPrefWidth(295);

        // ═══════════ TABS ═══════════
        resultsTabPane = new TabPane();
        resultsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        table = buildTable();
        tableFooter = new Label("");
        tableFooter.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-padding: 6;");
        VBox tableBox = new VBox(0, table, tableFooter);
        VBox.setVgrow(table, Priority.ALWAYS);

        timeChart = buildBarChart("Tiempo promedio por operación", "Estructura", "Tiempo (ns)");
        compChart = buildBarChart("Comparaciones promedio", "Estructura", "Comparaciones");
        heightChart = buildBarChart("Altura y tamaño final", "Estructura", "Valor");

        resultsTabPane.getTabs().addAll(
            new Tab("Tabla", tableBox),
            new Tab("Tiempos", timeChart),
            new Tab("Comparaciones", compChart),
            new Tab("Altura / Tamaño", heightChart),
            new Tab("Secuencia", buildStepByStepPane()),
            new Tab("Visualizar árboles", buildTreeVisualizerPane())
        );

        BorderPane root = new BorderPane();
        root.setLeft(sideScroll);
        root.setCenter(resultsTabPane);

        stage.setScene(new Scene(root, 1200, 750));
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════════
    //  PREVIEW DE CLAVES
    // ═══════════════════════════════════════════════════════════════

    private void updateKeyPreview() {
        try {
            int n = Integer.parseInt(nField.getText().trim());
            long s = Long.parseLong(seedField.getText().trim());
            if (n <= 0) { keyPreviewLabel.setText(""); return; }
            Random rand = new Random(s);
            int show = Math.min(n, 12);
            StringBuilder sb = new StringBuilder("Claves: ");
            for (int i = 0; i < show; i++) {
                if (i > 0) sb.append(", ");
                sb.append(rand.nextInt(n * 10));
            }
            if (n > show) sb.append(" ...");
            keyPreviewLabel.setText(sb.toString());
        } catch (NumberFormatException ex) {
            keyPreviewLabel.setText("");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  TABLA
    // ═══════════════════════════════════════════════════════════════

    @SuppressWarnings({"unchecked", "deprecation"})
    private TableView<ResultRow> buildTable() {
        TableView<ResultRow> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getColumns().addAll(
            col("Estructura",  r -> r.name),
            col("Insert (ns)", r -> fmt(r.insertTime)),
            col("Search (ns)", r -> fmt(r.searchTime)),
            col("Delete (ns)", r -> "N/A".equals(r.deleteO) ? "N/A" : fmt(r.deleteTime)),
            col("Ins Comp",    r -> fmt(r.insertComp)),
            col("Srch Comp",   r -> fmt(r.searchComp)),
            col("Del Comp",    r -> "N/A".equals(r.deleteO) ? "N/A" : fmt(r.deleteComp)),
            col("O(ins)",      r -> r.insertO),
            col("O(búsq)",     r -> r.searchO),
            col("O(borr)",     r -> r.deleteO),
            col("Altura",      r -> r.height < 0 ? "—" : "" + r.height),
            col("Tamaño",      r -> "" + r.size)
        );
        return tv;
    }

    private TableColumn<ResultRow, String> col(String title, Function<ResultRow, String> mapper) {
        TableColumn<ResultRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(mapper.apply(d.getValue())));
        return c;
    }

    // ═══════════════════════════════════════════════════════════════
    //  GRÁFICOS
    // ═══════════════════════════════════════════════════════════════

    private BarChart<String, Number> buildBarChart(String title, String xLabel, String yLabel) {
        CategoryAxis x = new CategoryAxis(); x.setLabel(xLabel);
        NumberAxis y = new NumberAxis(); y.setLabel(yLabel);
        BarChart<String, Number> c = new BarChart<>(x, y);
        c.setTitle(title); c.setAnimated(true); c.setLegendSide(Side.BOTTOM);
        return c;
    }

    @SuppressWarnings("unchecked")
    private void updateCharts(List<ResultRow> results) {
        timeChart.getData().clear();
        XYChart.Series<String, Number> si = new XYChart.Series<>(), ss = new XYChart.Series<>(), sd = new XYChart.Series<>();
        si.setName("Insert"); ss.setName("Search"); sd.setName("Delete");
        for (ResultRow r : results) {
            si.getData().add(new XYChart.Data<>(r.name, r.insertTime));
            ss.getData().add(new XYChart.Data<>(r.name, r.searchTime));
            if (!"N/A".equals(r.deleteO)) sd.getData().add(new XYChart.Data<>(r.name, r.deleteTime));
        }
        timeChart.getData().addAll(si, ss, sd);

        compChart.getData().clear();
        XYChart.Series<String, Number> ci = new XYChart.Series<>(), cs = new XYChart.Series<>(), cd = new XYChart.Series<>();
        ci.setName("Insert"); cs.setName("Search"); cd.setName("Delete");
        for (ResultRow r : results) {
            ci.getData().add(new XYChart.Data<>(r.name, r.insertComp));
            cs.getData().add(new XYChart.Data<>(r.name, r.searchComp));
            if (!"N/A".equals(r.deleteO)) cd.getData().add(new XYChart.Data<>(r.name, r.deleteComp));
        }
        compChart.getData().addAll(ci, cs, cd);

        heightChart.getData().clear();
        XYChart.Series<String, Number> hs = new XYChart.Series<>(), sz = new XYChart.Series<>();
        hs.setName("Altura"); sz.setName("Tamaño");
        for (ResultRow r : results) {
            hs.getData().add(new XYChart.Data<>(r.name, Math.max(r.height, 0)));
            sz.getData().add(new XYChart.Data<>(r.name, r.size));
        }
        heightChart.getData().addAll(hs, sz);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SECUENCIA PASO A PASO
    // ═══════════════════════════════════════════════════════════════

    private VBox buildStepByStepPane() {
        Label header = new Label("Secuencia paso a paso de inserción");
        header.setFont(Font.font("System", FontWeight.BOLD, 15));

        stepStructBox = new ComboBox<>();
        stepStructBox.getItems().addAll("BST", "AVL", "Splay", "Red-Black", "Arreglo", "Lista Simple");
        stepStructBox.setValue("BST");

        Label maxLabel = new Label("Claves a insertar:");
        stepMaxSpinner = new Spinner<>(1, 50, 15);
        stepMaxSpinner.setEditable(true);
        stepMaxSpinner.setPrefWidth(80);

        Button startBtn = new Button("Iniciar secuencia");
        startBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        startBtn.setOnAction(e -> startStepSequence());

        HBox controls = new HBox(10, new Label("Estructura:"), stepStructBox, maxLabel, stepMaxSpinner, startBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        Button prevBtn = new Button("◀ Anterior");
        Button nextBtn = new Button("Siguiente ▶");
        stepInfoLabel = new Label("Seleccioná una estructura e iniciá la secuencia.");
        stepInfoLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        prevBtn.setOnAction(e -> navigateStep(-1));
        nextBtn.setOnAction(e -> navigateStep(1));

        HBox navBox = new HBox(10, prevBtn, nextBtn, stepInfoLabel);
        navBox.setAlignment(Pos.CENTER_LEFT);

        stepTextArea = new TextArea();
        stepTextArea.setEditable(false);
        stepTextArea.setPrefRowCount(6);
        stepTextArea.setFont(Font.font("Monospaced", 13));

        stepCanvas = new Canvas(700, 350);
        Group stepGroup = new Group(stepCanvas);
        Slider stepZoom = new Slider(0.3, 3.0, 1.0);
        stepZoom.setPrefWidth(120);
        Button stepZoomReset = new Button("100%");
        stepZoomReset.setOnAction(e -> stepZoom.setValue(1.0));
        stepZoom.valueProperty().addListener((obs, o, nv) -> {
            double z = nv.doubleValue();
            stepGroup.setScaleX(z); stepGroup.setScaleY(z);
        });
        HBox stepZoomBox = new HBox(8, new Label("Zoom:"), stepZoom, stepZoomReset);
        stepZoomBox.setAlignment(Pos.CENTER_LEFT);

        ScrollPane stepScrollPane = buildZoomableScroll(stepGroup, stepZoom);
        VBox.setVgrow(stepScrollPane, Priority.ALWAYS);

        VBox pane = new VBox(12, header, controls, new Separator(), navBox, stepZoomBox, stepTextArea, stepScrollPane);
        pane.setPadding(new Insets(15));
        return pane;
    }

    private DataStructure createDSByName(String name, int n) {
        switch (name) {
            case "BST":           return new BST();
            case "AVL":           return new AVL();
            case "Splay":         return new Splay();
            case "Red-Black":     return new RedBlackTree();
            case "Arreglo":       return new MyArray(n);
            case "Lista Simple":  return new SimpleLinkedList();
            default:              return new BST();
        }
    }

    private void startStepSequence() {
        int n;
        long seed;
        try {
            n = Integer.parseInt(nField.getText().trim());
            seed = Long.parseLong(seedField.getText().trim());
        } catch (NumberFormatException ex) {
            stepInfoLabel.setText("Error: revisá N y Semilla en el panel izquierdo.");
            return;
        }

        int maxKeys = stepMaxSpinner.getValue();
        int count = Math.min(n, maxKeys);

        Random rand = new Random(seed);
        stepKeys = new int[count];
        for (int i = 0; i < count; i++) stepKeys[i] = rand.nextInt(n * 10);

        currentStepIndex = 0;
        renderStep();
    }

    private void navigateStep(int delta) {
        if (stepKeys == null || stepKeys.length == 0) return;
        currentStepIndex = Math.max(0, Math.min(stepKeys.length - 1, currentStepIndex + delta));
        renderStep();
    }

    private void renderStep() {
        if (stepKeys == null) return;
        String name = stepStructBox.getValue();
        int n = stepKeys.length > 0 ? stepKeys.length * 10 : 100;
        DataStructure ds = createDSByName(name, n);

        // Insertar hasta el paso actual
        for (int i = 0; i <= currentStepIndex; i++) {
            ds.insert(stepKeys[i]);
        }

        // Pasos de la última inserción
        String[] steps = ds.getLastInsertSteps();
        StringBuilder sb = new StringBuilder();
        for (String s : steps) sb.append(s).append("\n");

        stepInfoLabel.setText("Paso " + (currentStepIndex + 1) + " de " + stepKeys.length
                + "  —  Insertando clave: " + stepKeys[currentStepIndex]
                + "  —  Tamaño: " + ds.size()
                + (ds.height() >= 0 ? "  —  Altura: " + ds.height() : ""));
        stepTextArea.setText(sb.toString());

        // Dibujar en canvas
        TreeDrawer.draw(stepCanvas, ds);
    }

    // ═══════════════════════════════════════════════════════════════
    //  VISUALIZADOR DE DOS ÁRBOLES
    // ═══════════════════════════════════════════════════════════════

    private VBox buildTreeVisualizerPane() {
        Label header = new Label("Comparación visual de dos árboles");
        header.setFont(Font.font("System", FontWeight.BOLD, 15));

        tree1Box = new ComboBox<>();
        tree2Box = new ComboBox<>();
        for (ComboBox<String> cb : new ComboBox[]{tree1Box, tree2Box})
            cb.getItems().addAll("BST", "AVL", "Splay", "Red-Black");
        tree1Box.setValue("BST");
        tree2Box.setValue("AVL");

        Button buildBtn = new Button("Construir árboles");
        buildBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        buildBtn.setOnAction(e -> buildTwoTrees());

        // Zoom controls
        Label zoomLabel = new Label("Zoom:");
        Slider zoomSlider = new Slider(0.3, 3.0, 1.0);
        zoomSlider.setPrefWidth(150);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(0.5);
        Button zoomResetBtn = new Button("100%");
        zoomResetBtn.setOnAction(e -> zoomSlider.setValue(1.0));

        HBox controls = new HBox(10,
            new Label("Árbol 1:"), tree1Box,
            new Label("Árbol 2:"), tree2Box,
            buildBtn, new Separator(),
            zoomLabel, zoomSlider, zoomResetBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        treeLabel1 = new Label("");
        treeLabel1.setFont(Font.font("System", FontWeight.BOLD, 13));
        treeLabel2 = new Label("");
        treeLabel2.setFont(Font.font("System", FontWeight.BOLD, 13));

        canvas1 = new Canvas(400, 300);
        canvas2 = new Canvas(400, 300);

        Group group1 = new Group(canvas1);
        Group group2 = new Group(canvas2);

        // Aplicar zoom a ambos grupos
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double z = newVal.doubleValue();
            group1.setScaleX(z); group1.setScaleY(z);
            group2.setScaleX(z); group2.setScaleY(z);
        });

        // ScrollPane para cada árbol
        ScrollPane scroll1 = buildZoomableScroll(group1, zoomSlider);
        ScrollPane scroll2 = buildZoomableScroll(group2, zoomSlider);

        VBox box1 = new VBox(5, treeLabel1, scroll1);
        VBox box2 = new VBox(5, treeLabel2, scroll2);
        VBox.setVgrow(scroll1, Priority.ALWAYS);
        VBox.setVgrow(scroll2, Priority.ALWAYS);

        // Apilados verticalmente para que ambos se vean siempre
        SplitPane splitPane = new SplitPane(box1, box2);
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.setDividerPositions(0.5);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        VBox pane = new VBox(12, header, controls, new Separator(), splitPane);
        pane.setPadding(new Insets(15));
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        return pane;
    }

    /** Crea un ScrollPane con zoom por rueda del mouse (Ctrl+scroll). */
    private ScrollPane buildZoomableScroll(Group group, Slider zoomSlider) {
        ScrollPane sp = new ScrollPane(group);
        sp.setPannable(true);
        sp.setStyle("-fx-background-color: white;");
        sp.setFitToWidth(false);
        sp.setFitToHeight(false);

        sp.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();
                double delta = event.getDeltaY() > 0 ? 0.1 : -0.1;
                double newZoom = Math.max(0.3, Math.min(3.0, zoomSlider.getValue() + delta));
                zoomSlider.setValue(newZoom);
            }
        });

        return sp;
    }

    private void buildTwoTrees() {
        int n;
        long seed;
        try {
            n = Integer.parseInt(nField.getText().trim());
            seed = Long.parseLong(seedField.getText().trim());
        } catch (NumberFormatException ex) {
            treeLabel1.setText("Error: revisá N y Semilla.");
            return;
        }

        Random rand = new Random(seed);
        int count = Math.min(n, 200);
        int[] keys = new int[count];
        for (int i = 0; i < count; i++) keys[i] = rand.nextInt(n * 10);

        DataStructure ds1 = createDSByName(tree1Box.getValue(), count);
        DataStructure ds2 = createDSByName(tree2Box.getValue(), count);

        for (int k : keys) { ds1.insert(k); ds2.insert(k); }

        treeLabel1.setText(ds1.getName() + "  —  Altura: " + ds1.height() + "  —  Nodos: " + ds1.size());
        treeLabel2.setText(ds2.getName() + "  —  Altura: " + ds2.height() + "  —  Nodos: " + ds2.size());

        TreeDrawer.draw(canvas1, ds1);
        TreeDrawer.draw(canvas2, ds2);
    }

    // ═══════════════════════════════════════════════════════════════
    //  EJECUTAR BENCHMARK
    // ═══════════════════════════════════════════════════════════════

    private void runBenchmark(CheckBox bstBox, CheckBox avlBox, CheckBox splayBox,
                              CheckBox rbBox, CheckBox arrBox, CheckBox listBox) {
        int N, W, R; long seed;
        try {
            N = Integer.parseInt(nField.getText().trim());
            seed = Long.parseLong(seedField.getText().trim());
            W = Integer.parseInt(wField.getText().trim());
            R = Integer.parseInt(rField.getText().trim());
        } catch (NumberFormatException ex) {
            statusLabel.setText("Error: valores numéricos inválidos");
            statusLabel.setStyle("-fx-text-fill: red;"); return;
        }
        if (N <= 0) { statusLabel.setText("Error: N > 0"); statusLabel.setStyle("-fx-text-fill: red;"); return; }
        if (W < 0)  { statusLabel.setText("Error: W >= 0"); statusLabel.setStyle("-fx-text-fill: red;"); return; }
        if (R < 1)  { statusLabel.setText("Error: R >= 1"); statusLabel.setStyle("-fx-text-fill: red;"); return; }

        int[] manualQueries = null;
        if ("Manual".equals(searchModeBox.getValue())) {
            try { manualQueries = parseManualQueries(manualQueriesArea.getText()); }
            catch (IllegalArgumentException ex) {
                statusLabel.setText(ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;"); return;
            }
        }

        List<DataStructure> active = new ArrayList<>();
        if (bstBox.isSelected())   active.add(new BST());
        if (avlBox.isSelected())   active.add(new AVL());
        if (splayBox.isSelected()) active.add(new Splay());
        if (rbBox.isSelected())    active.add(new RedBlackTree());
        if (arrBox.isSelected())   active.add(new MyArray(N));
        if (listBox.isSelected())  active.add(new SimpleLinkedList());

        if (active.isEmpty()) {
            statusLabel.setText("Seleccioná al menos una estructura");
            statusLabel.setStyle("-fx-text-fill: red;"); return;
        }

        statusLabel.setText("Ejecutando benchmark...");
        statusLabel.setStyle("-fx-text-fill: #2196F3;");

        Benchmark bm = "Manual".equals(searchModeBox.getValue()) && manualQueries != null
                ? new Benchmark(N, seed, W, R, manualQueries)
                : new Benchmark(N, seed, W, R);

        List<ResultRow> results = bm.run(active);
        lastBenchmark = bm; lastResults = results;
        exportCsvBtn.setDisable(false);

        table.getItems().setAll(results);
        updateCharts(results);

        String mode = "Manual".equals(searchModeBox.getValue())
                ? "manual (" + manualQueries.length + " búsquedas)" : "automático";
        tableFooter.setText("N=" + N + "  |  Semilla=" + seed + "  |  W=" + W + "  |  R=" + R
                + "  |  Modo: " + mode + "  |  Borrado: todas las claves en orden de inserción (Red-Black: N/A)");

        statusLabel.setText("Completado — " + results.size() + " estructura(s), modo " + mode);
        statusLabel.setStyle("-fx-text-fill: green;");
        resultsTabPane.getSelectionModel().select(1);
    }

    // ═══════════════════════════════════════════════════════════════
    //  UTILIDADES
    // ═══════════════════════════════════════════════════════════════

    private int[] parseManualQueries(String text) {
        if (text == null || text.trim().isEmpty())
            throw new IllegalArgumentException("Error: escribí al menos una clave manual");
        String norm = text.trim().replaceAll("[,;\n\r\t]+", " ");
        String[] parts = norm.trim().split("\\s+");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try { result[i] = Integer.parseInt(parts[i]); }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Error: '" + parts[i] + "' no es un entero");
            }
        }
        return result;
    }

    private void exportLastResults(Stage stage) {
        if (lastBenchmark == null || lastResults == null || lastResults.isEmpty()) {
            statusLabel.setText("No hay resultados"); statusLabel.setStyle("-fx-text-fill: red;"); return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialFileName("resultados_benchmark.csv");
        File file = fc.showSaveDialog(stage);
        if (file == null) { statusLabel.setText("Exportación cancelada"); return; }
        try {
            lastBenchmark.exportCSV(lastResults, file.getAbsolutePath());
            statusLabel.setText("CSV exportado ✓"); statusLabel.setStyle("-fx-text-fill: green;");
        } catch (IOException ex) {
            statusLabel.setText("Error al exportar"); statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private String fmt(long n) { return String.format("%,d", n); }

    public static void main(String[] args) { launch(args); }
}
