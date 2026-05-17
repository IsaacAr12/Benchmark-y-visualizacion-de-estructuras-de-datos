package ui;

import benchmark.Benchmark;
import benchmark.Benchmark.ResultRow;
import structures.*;
import structures.tree.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class BenchmarkApp extends Application {

    private TextField nField, seedField, wField, rField;
    private TableView<ResultRow> table;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Benchmark de Estructuras de Datos");

        // Campos de parámetros
        nField = new TextField("1000");
        seedField = new TextField("42");
        wField = new TextField("1");
        rField = new TextField("3");

        GridPane params = new GridPane();
        params.setHgap(10);
        params.setVgap(10);
        params.add(new Label("N:"), 0, 0);
        params.add(nField, 1, 0);
        params.add(new Label("Semilla:"), 0, 1);
        params.add(seedField, 1, 1);
        params.add(new Label("Warmup (W):"), 0, 2);
        params.add(wField, 1, 2);
        params.add(new Label("Iteraciones (R):"), 0, 3);
        params.add(rField, 1, 3);

        // Checkboxes para elegir estructuras
        CheckBox bstBox = new CheckBox("BST");
        CheckBox avlBox = new CheckBox("AVL");
        CheckBox splayBox = new CheckBox("Splay");
        CheckBox rbBox = new CheckBox("Red-Black");
        CheckBox arrBox = new CheckBox("Arreglo");
        CheckBox listBox = new CheckBox("Lista Simple");

        VBox structuresBox = new VBox(5, bstBox, avlBox, splayBox, rbBox, arrBox, listBox);

        // Botón de ejecución
        Button runBtn = new Button("Ejecutar Benchmark");
        runBtn.setOnAction(e -> runBenchmark(bstBox, avlBox, splayBox, rbBox, arrBox, listBox));

        // Tabla de resultados
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ResultRow, String> nameCol = new TableColumn<>("Estructura");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().name));

        TableColumn<ResultRow, String> insertCol = new TableColumn<>("Insert(ns)");
        insertCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("" + c.getValue().insertTime));

        TableColumn<ResultRow, String> searchCol = new TableColumn<>("Search(ns)");
        searchCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("" + c.getValue().searchTime));

        TableColumn<ResultRow, String> deleteCol = new TableColumn<>("Delete(ns)");
        deleteCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("" + c.getValue().deleteTime));

        table.getColumns().addAll(nameCol, insertCol, searchCol, deleteCol);

        VBox root = new VBox(15, params, structuresBox, runBtn, table);
        root.setPadding(new javafx.geometry.Insets(15));

        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void runBenchmark(CheckBox bstBox, CheckBox avlBox, CheckBox splayBox,
                              CheckBox rbBox, CheckBox arrBox, CheckBox listBox) {
        int N = Integer.parseInt(nField.getText());
        long seed = Long.parseLong(seedField.getText());
        int W = Integer.parseInt(wField.getText());
        int R = Integer.parseInt(rField.getText());

        Benchmark bm = new Benchmark(N, seed, W, R);

        List<DataStructure> active = new ArrayList<>();
        if (bstBox.isSelected()) active.add(new BST());
        if (avlBox.isSelected()) active.add(new AVL());
        if (splayBox.isSelected()) active.add(new Splay());
        if (rbBox.isSelected()) active.add(new RedBlackTree());
        if (arrBox.isSelected()) active.add(new MyArray(N));
        if (listBox.isSelected()) active.add(new SimpleLinkedList());

        List<ResultRow> results = bm.run(active);

        table.getItems().setAll(results);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
