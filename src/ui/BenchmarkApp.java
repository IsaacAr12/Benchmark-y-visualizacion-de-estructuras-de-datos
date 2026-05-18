package ui;

import benchmark.Benchmark;
import benchmark.Benchmark.ResultRow;
import structures.*;
import structures.tree.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.util.*;
import java.io.File;
import java.io.IOException;

public class BenchmarkApp extends Application {

    private TextField nField, seedField, wField, rField;
    private TableView<ResultRow> table;
    private BarChart<String, Number> timeChart;
    private BarChart<String, Number> compChart;
    private BarChart<String, Number> heightChart;
    private TabPane resultsTabPane;
    private Label statusLabel;

    private Benchmark lastBenchmark;
    private List<ResultRow> lastResults;
    private Button exportCsvBtn;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Benchmark de Estructuras de Datos");

        // ── Panel izquierdo: parámetros + estructuras ──
        Label titleLabel = new Label("Configuración");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        nField = new TextField("1000");
        seedField = new TextField("42");
        wField = new TextField("1");
        rField = new TextField("3");

        GridPane params = new GridPane();
        params.setHgap(10);
        params.setVgap(8);
        params.add(new Label("N (elementos):"), 0, 0);
        params.add(nField, 1, 0);
        params.add(new Label("Semilla:"), 0, 1);
        params.add(seedField, 1, 1);
        params.add(new Label("Warmup (W):"), 0, 2);
        params.add(wField, 1, 2);
        params.add(new Label("Iteraciones (R):"), 0, 3);
        params.add(rField, 1, 3);

        nField.setPrefWidth(120);
        seedField.setPrefWidth(120);
        wField.setPrefWidth(120);
        rField.setPrefWidth(120);

        Label structLabel = new Label("Estructuras");
        structLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        CheckBox bstBox = new CheckBox("BST");
        CheckBox avlBox = new CheckBox("AVL");
        CheckBox splayBox = new CheckBox("Splay");
        CheckBox rbBox = new CheckBox("Red-Black");
        CheckBox arrBox = new CheckBox("Arreglo");
        CheckBox listBox = new CheckBox("Lista Simple");

        // Seleccionar árboles por defecto
        bstBox.setSelected(true);
        avlBox.setSelected(true);
        splayBox.setSelected(true);
        rbBox.setSelected(true);

        VBox structuresBox = new VBox(6, bstBox, avlBox, splayBox, rbBox, arrBox, listBox);

        Button selectAllBtn = new Button("Seleccionar todas");
        selectAllBtn.setOnAction(e -> {
            bstBox.setSelected(true);
            avlBox.setSelected(true);
            splayBox.setSelected(true);
            rbBox.setSelected(true);
            arrBox.setSelected(true);
            listBox.setSelected(true);
        });

        Button runBtn = new Button("▶  Ejecutar Benchmark");
        runBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-cursor: hand;");
        runBtn.setMaxWidth(Double.MAX_VALUE);
        runBtn.setOnAction(e -> runBenchmark(bstBox, avlBox, splayBox, rbBox, arrBox, listBox));

        exportCsvBtn = new Button("Exportar CSV");
        exportCsvBtn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-padding: 8 16; -fx-cursor: hand;");
        exportCsvBtn.setMaxWidth(Double.MAX_VALUE);
        exportCsvBtn.setDisable(true);
        exportCsvBtn.setOnAction(e -> exportLastResults(stage));

        statusLabel = new Label("Listo para ejecutar");
        statusLabel.setStyle("-fx-text-fill: #666;");

        VBox sidebar = new VBox(
                12,
                titleLabel,
                new Separator(),
                params,
                new Separator(),
                structLabel,
                structuresBox,
                selectAllBtn,
                new Separator(),
                runBtn,
                exportCsvBtn,
                statusLabel
        );

        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #f5f5f5;");

        // ── Panel derecho: resultados con tabs ──
        resultsTabPane = new TabPane();
        resultsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        resultsTabPane.setSide(Side.TOP);

        // Tab 1: Tabla
        table = buildTable();
        Tab tableTab = new Tab("Tabla", table);

        // Tab 2: Gráfico de tiempos
        timeChart = buildBarChart("Tiempo promedio por operación", "Estructura", "Tiempo (ns)");
        Tab timeTab = new Tab("Tiempos", timeChart);

        // Tab 3: Gráfico de comparaciones
        compChart = buildBarChart("Comparaciones promedio por operación", "Estructura", "Comparaciones");
        Tab compTab = new Tab("Comparaciones", compChart);

        // Tab 4: Altura y tamaño
        heightChart = buildBarChart("Altura y tamaño final", "Estructura", "Valor");
        Tab heightTab = new Tab("Altura / Tamaño", heightChart);

        resultsTabPane.getTabs().addAll(tableTab, timeTab, compTab, heightTab);

        // ── Layout principal ──
        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(resultsTabPane);

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private TableView<ResultRow> buildTable() {
        TableView<ResultRow> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ResultRow, String> nameCol = new TableColumn<>("Estructura");
        nameCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().name));

        TableColumn<ResultRow, String> insertCol = new TableColumn<>("Insert (ns)");
        insertCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(formatNumber(c.getValue().insertTime)));

        TableColumn<ResultRow, String> searchCol = new TableColumn<>("Search (ns)");
        searchCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(formatNumber(c.getValue().searchTime)));

        TableColumn<ResultRow, String> deleteCol = new TableColumn<>("Delete (ns)");
        deleteCol.setCellValueFactory(c -> {
            ResultRow r = c.getValue();

            if ("N/A".equals(r.deleteO)) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }

            return new javafx.beans.property.SimpleStringProperty(formatNumber(r.deleteTime));
        });

        TableColumn<ResultRow, String> iCompCol = new TableColumn<>("Insert Comp");
        iCompCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(formatNumber(c.getValue().insertComp)));

        TableColumn<ResultRow, String> sCompCol = new TableColumn<>("Search Comp");
        sCompCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(formatNumber(c.getValue().searchComp)));

        TableColumn<ResultRow, String> dCompCol = new TableColumn<>("Delete Comp");
        dCompCol.setCellValueFactory(c -> {
            ResultRow r = c.getValue();

            if ("N/A".equals(r.deleteO)) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }

            return new javafx.beans.property.SimpleStringProperty(formatNumber(r.deleteComp));
        });

        TableColumn<ResultRow, String> insertOCol = new TableColumn<>("Insert O");
        insertOCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().insertO));

        TableColumn<ResultRow, String> searchOCol = new TableColumn<>("Search O");
        searchOCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().searchO));

        TableColumn<ResultRow, String> deleteOCol = new TableColumn<>("Delete O");
        deleteOCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().deleteO));

        TableColumn<ResultRow, String> heightCol = new TableColumn<>("Altura");
        heightCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty("" + c.getValue().height));

        TableColumn<ResultRow, String> sizeCol = new TableColumn<>("Tamaño");
        sizeCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty("" + c.getValue().size));

        tv.getColumns().addAll(
                nameCol,
                insertCol,
                searchCol,
                deleteCol,
                iCompCol,
                sCompCol,
                dCompCol,
                insertOCol,
                searchOCol,
                deleteOCol,
                heightCol,
                sizeCol
        );

        return tv;
    }

    private BarChart<String, Number> buildBarChart(String title, String xLabel, String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setAnimated(true);
        chart.setLegendSide(Side.BOTTOM);

        return chart;
    }

    @SuppressWarnings("unchecked")
    private void updateCharts(List<ResultRow> results) {
        // ── Gráfico de tiempos ──
        timeChart.getData().clear();

        XYChart.Series<String, Number> insertTimeSeries = new XYChart.Series<>();
        insertTimeSeries.setName("Insert");

        XYChart.Series<String, Number> searchTimeSeries = new XYChart.Series<>();
        searchTimeSeries.setName("Search");

        XYChart.Series<String, Number> deleteTimeSeries = new XYChart.Series<>();
        deleteTimeSeries.setName("Delete");

        for (ResultRow r : results) {
            insertTimeSeries.getData().add(new XYChart.Data<>(r.name, r.insertTime));
            searchTimeSeries.getData().add(new XYChart.Data<>(r.name, r.searchTime));

            if (!"N/A".equals(r.deleteO)) {
                deleteTimeSeries.getData().add(new XYChart.Data<>(r.name, r.deleteTime));
            }
        }

        timeChart.getData().addAll(insertTimeSeries, searchTimeSeries, deleteTimeSeries);

        // ── Gráfico de comparaciones ──
        compChart.getData().clear();

        XYChart.Series<String, Number> insertCompSeries = new XYChart.Series<>();
        insertCompSeries.setName("Insert");

        XYChart.Series<String, Number> searchCompSeries = new XYChart.Series<>();
        searchCompSeries.setName("Search");

        XYChart.Series<String, Number> deleteCompSeries = new XYChart.Series<>();
        deleteCompSeries.setName("Delete");

        for (ResultRow r : results) {
            insertCompSeries.getData().add(new XYChart.Data<>(r.name, r.insertComp));
            searchCompSeries.getData().add(new XYChart.Data<>(r.name, r.searchComp));

            if (!"N/A".equals(r.deleteO)) {
                deleteCompSeries.getData().add(new XYChart.Data<>(r.name, r.deleteComp));
            }
        }

        compChart.getData().addAll(insertCompSeries, searchCompSeries, deleteCompSeries);

        // ── Gráfico de altura/tamaño ──
        heightChart.getData().clear();

        XYChart.Series<String, Number> hSeries = new XYChart.Series<>();
        hSeries.setName("Altura");

        XYChart.Series<String, Number> sSeries = new XYChart.Series<>();
        sSeries.setName("Tamaño");

        for (ResultRow r : results) {
            hSeries.getData().add(new XYChart.Data<>(r.name, r.height));
            sSeries.getData().add(new XYChart.Data<>(r.name, r.size));
        }

        heightChart.getData().addAll(hSeries, sSeries);
    }

    private void runBenchmark(CheckBox bstBox, CheckBox avlBox, CheckBox splayBox,
                              CheckBox rbBox, CheckBox arrBox, CheckBox listBox) {
        int N;
        int W;
        int R;
        long seed;

        try {
            N = Integer.parseInt(nField.getText().trim());
            seed = Long.parseLong(seedField.getText().trim());
            W = Integer.parseInt(wField.getText().trim());
            R = Integer.parseInt(rField.getText().trim());
        } catch (NumberFormatException ex) {
            statusLabel.setText("Error: valores numéricos inválidos");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (N <= 0) {
            statusLabel.setText("Error: N debe ser mayor que 0");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (W < 0) {
            statusLabel.setText("Error: W debe ser mayor o igual que 0");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (R < 1) {
            statusLabel.setText("Error: R debe ser mayor o igual que 1");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        List<DataStructure> active = new ArrayList<>();

        if (bstBox.isSelected()) active.add(new BST());
        if (avlBox.isSelected()) active.add(new AVL());
        if (splayBox.isSelected()) active.add(new Splay());
        if (rbBox.isSelected()) active.add(new RedBlackTree());
        if (arrBox.isSelected()) active.add(new MyArray(N));
        if (listBox.isSelected()) active.add(new SimpleLinkedList());

        if (active.isEmpty()) {
            statusLabel.setText("Seleccioná al menos una estructura");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        statusLabel.setText("Ejecutando benchmark...");
        statusLabel.setStyle("-fx-text-fill: #2196F3;");

        Benchmark bm = new Benchmark(N, seed, W, R);
        List<ResultRow> results = bm.run(active);

        lastBenchmark = bm;
        lastResults = results;
        exportCsvBtn.setDisable(false);

        // Actualizar tabla y gráficos
        table.getItems().setAll(results);
        updateCharts(results);

        statusLabel.setText("Completado — " + results.size() + " estructura(s)");
        statusLabel.setStyle("-fx-text-fill: green;");

        // Saltar al tab de tiempos automáticamente
        resultsTabPane.getSelectionModel().select(1);
    }

    private void exportLastResults(Stage stage) {
        if (lastBenchmark == null || lastResults == null || lastResults.isEmpty()) {
            statusLabel.setText("No hay resultados para exportar");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar resultados CSV");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo CSV", "*.csv")
        );

        fileChooser.setInitialFileName("resultados_benchmark.csv");

        File file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            statusLabel.setText("Exportación cancelada");
            statusLabel.setStyle("-fx-text-fill: #666;");
            return;
        }

        try {
            lastBenchmark.exportCSV(lastResults, file.getAbsolutePath());
            statusLabel.setText("CSV exportado correctamente");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (IOException ex) {
            statusLabel.setText("Error al exportar CSV");
            statusLabel.setStyle("-fx-text-fill: red;");
            ex.printStackTrace();
        }
    }

    private String formatNumber(long n) {
        return String.format("%,d", n);
    }

    public static void main(String[] args) {
        launch(args);
    }
}