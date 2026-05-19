package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import structures.DataStructure;
import structures.tree.*;

import java.util.*;

/**
 * Dibuja árboles en un Canvas de JavaFX usando layout por recorrido inorder.
 * Cada nodo recibe su propia columna horizontal, eliminando solapamientos.
 * Redimensiona el canvas automáticamente según el tamaño del árbol.
 */
public class TreeDrawer {

    private static final int R = 18;
    private static final int DIAMETER = R * 2;
    private static final int H_GAP = 10;
    private static final int V_GAP = 24;
    private static final int NODE_SPACING = DIAMETER + H_GAP;
    private static final int LEVEL_HEIGHT = DIAMETER + V_GAP;
    private static final int PAD = 30;
    private static final Font NODE_FONT = Font.font("Monospaced", 11);

    // ── Nodo interno para reconstruir el árbol ──
    private static class LNode {
        int key;
        int color; // 1=rojo, 0=negro, -1=normal
        LNode left, right;
        int depth;
        int inorderIdx;
        LNode(int key, int color) { this.key = key; this.color = color; }
    }

    private static final int MAX_CANVAS_W = 3200;
    private static final int MAX_CANVAS_H = 2000;

    // Espaciado real usado en el último draw (puede reducirse para árboles grandes)
    private static double usedHSpacing = NODE_SPACING;
    private static double usedVSpacing = LEVEL_HEIGHT;

    /**
     * Dibuja la estructura en el canvas. Redimensiona y ajusta el espaciado
     * automáticamente para que quepa sin exceder los límites de textura de la GPU.
     */
    public static void draw(Canvas canvas, DataStructure ds) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        if (ds.size() == 0) {
            canvas.setWidth(400);
            canvas.setHeight(100);
            gc.clearRect(0, 0, 400, 100);
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font(14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("(vacío)", 200, 50);
            return;
        }

        int[] flat;
        int stride;
        boolean isRB = false;

        if (ds instanceof RedBlackTree) {
            flat = ((RedBlackTree) ds).getNodePositionsFlat(1000, 1000);
            stride = 5; isRB = true;
        } else if (ds instanceof BST) {
            flat = ((BST) ds).getNodePositionsFlat(1000, 1000);
            stride = 4;
        } else if (ds instanceof AVL) {
            flat = ((AVL) ds).getNodePositionsFlat(1000, 1000);
            stride = 5; // key, x, y, parentKey, balanceFactor
        } else if (ds instanceof Splay) {
            flat = ((Splay) ds).getNodePositionsFlat(1000, 1000);
            stride = 4;
        } else {
            drawLinear(canvas, ds);
            return;
        }

        // ── Reconstruir árbol desde flat data ──
        int count = flat.length / stride;
        if (count == 0) { drawLinear(canvas, ds); return; }

        Map<Integer, LNode> nodeMap = new LinkedHashMap<>();
        int rootKey = flat[0]; // primer nodo en preorder es la raíz

        for (int i = 0; i < count; i++) {
            int key = flat[i * stride];
            int color = isRB ? flat[i * stride + 4] : -1;
            nodeMap.put(key, new LNode(key, color));
        }

        // Reconstruir parent-child
        for (int i = 0; i < count; i++) {
            int key = flat[i * stride];
            int parentKey = flat[i * stride + 3];
            if (parentKey >= 0 && nodeMap.containsKey(parentKey)) {
                LNode parent = nodeMap.get(parentKey);
                LNode child = nodeMap.get(key);
                if (key < parentKey) {
                    parent.left = child;
                } else {
                    parent.right = child;
                }
            }
        }

        LNode root = nodeMap.get(rootKey);
        if (root == null) { drawLinear(canvas, ds); return; }

        // ── Calcular profundidad e índice inorder ──
        computeDepth(root, 0);
        int[] idx = {0};
        computeInorder(root, idx);
        int totalNodes = idx[0];

        int maxDepth = 0;
        for (LNode n : nodeMap.values()) {
            if (n.depth > maxDepth) maxDepth = n.depth;
        }

        // ── Calcular espaciado dinámico ──
        double idealW = totalNodes * NODE_SPACING + PAD * 2;
        double idealH = (maxDepth + 1) * LEVEL_HEIGHT + PAD * 2;

        usedHSpacing = NODE_SPACING;
        usedVSpacing = LEVEL_HEIGHT;

        // Si es más ancho que el límite, comprimir horizontalmente
        if (idealW > MAX_CANVAS_W && totalNodes > 1) {
            usedHSpacing = (double)(MAX_CANVAS_W - PAD * 2) / totalNodes;
            usedHSpacing = Math.max(usedHSpacing, 8); // mínimo 8px entre nodos
            idealW = totalNodes * usedHSpacing + PAD * 2;
        }
        // Si es más alto que el límite, comprimir verticalmente
        if (idealH > MAX_CANVAS_H && maxDepth > 0) {
            usedVSpacing = (double)(MAX_CANVAS_H - PAD * 2) / (maxDepth + 1);
            usedVSpacing = Math.max(usedVSpacing, 20);
            idealH = (maxDepth + 1) * usedVSpacing + PAD * 2;
        }

        double canvasW = Math.max(Math.min(idealW, MAX_CANVAS_W), 300);
        double canvasH = Math.max(Math.min(idealH, MAX_CANVAS_H), 150);

        canvas.setWidth(canvasW);
        canvas.setHeight(canvasH);
        gc.clearRect(0, 0, canvasW, canvasH);

        // Ajustar radio de nodo si el espacio es muy reducido
        double drawR = Math.min(R, usedHSpacing * 0.4);
        drawR = Math.max(drawR, 5);

        // ── Dibujar líneas ──
        gc.setStroke(Color.rgb(150, 150, 150));
        gc.setLineWidth(1.0);
        drawEdges(gc, root);

        // ── Dibujar nodos ──
        double fontSize = Math.max(8, Math.min(11, drawR * 0.7));
        gc.setFont(Font.font("Monospaced", fontSize));
        gc.setTextAlign(TextAlignment.CENTER);
        drawNodes(gc, root, drawR);
    }

    private static void computeDepth(LNode node, int d) {
        if (node == null) return;
        node.depth = d;
        computeDepth(node.left, d + 1);
        computeDepth(node.right, d + 1);
    }

    private static void computeInorder(LNode node, int[] idx) {
        if (node == null) return;
        computeInorder(node.left, idx);
        node.inorderIdx = idx[0]++;
        computeInorder(node.right, idx);
    }

    private static double nodeX(LNode n) { return PAD + n.inorderIdx * usedHSpacing + usedHSpacing / 2; }
    private static double nodeY(LNode n) { return PAD + n.depth * usedVSpacing + usedVSpacing / 2; }

    private static void drawEdges(GraphicsContext gc, LNode node) {
        if (node == null) return;
        double px = nodeX(node), py = nodeY(node);
        if (node.left != null) {
            gc.strokeLine(px, py, nodeX(node.left), nodeY(node.left));
            drawEdges(gc, node.left);
        }
        if (node.right != null) {
            gc.strokeLine(px, py, nodeX(node.right), nodeY(node.right));
            drawEdges(gc, node.right);
        }
    }

    private static void drawNodes(GraphicsContext gc, LNode node, double r) {
        if (node == null) return;
        drawNodes(gc, node.left, r);
        drawNodes(gc, node.right, r);

        double x = nodeX(node), y = nodeY(node);
        double d = r * 2;

        if (node.color == 1) {
            gc.setFill(Color.rgb(211, 47, 47));
            gc.setStroke(Color.rgb(183, 28, 28));
        } else if (node.color == 0) {
            gc.setFill(Color.rgb(50, 50, 50));
            gc.setStroke(Color.rgb(30, 30, 30));
        } else {
            gc.setFill(Color.rgb(33, 150, 243));
            gc.setStroke(Color.rgb(25, 118, 192));
        }

        gc.fillOval(x - r, y - r, d, d);
        gc.setLineWidth(1.0);
        gc.strokeOval(x - r, y - r, d, d);

        gc.setFill(Color.WHITE);
        gc.fillText("" + node.key, x, y + r * 0.25);
    }

    /** Representación simple para estructuras lineales */
    private static void drawLinear(Canvas canvas, DataStructure ds) {
        canvas.setWidth(400);
        canvas.setHeight(60);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 400, 60);
        gc.setFill(Color.GRAY);
        gc.setFont(Font.font(14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(ds.getName() + "  —  " + ds.size() + " elementos (estructura lineal)", 200, 30);
    }
}
