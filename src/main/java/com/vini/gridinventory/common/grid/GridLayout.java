package com.vini.gridinventory.common.grid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Espacos do inventario: grid WIDTH x HEIGHT e distribuicao (first-fit) das formas.
 * Sem dependencias do Minecraft (testavel isoladamente).
 *
 * Parte 1: o layout e calculado a partir do inventario e nao e persistido.
 * As ancoras persistentes (GridState) entram na proxima parte.
 */
public final class GridLayout {
    public static final int WIDTH = 9;
    public static final int HEIGHT = 7;

    /** id = identificador do chamador (aqui, o indice do slot vanilla). */
    public record Entry(int id, GridShape shape) {
    }

    /** shape ja esta na orientacao final (girada se rotated = true). */
    public record Placement(int id, int x, int y, boolean rotated, GridShape shape) {
        public boolean covers(int cellX, int cellY) {
            return shape.occupied(cellX - x, cellY - y);
        }
    }

    public record Result(List<Placement> placed, List<Entry> overflow) {
    }

    private GridLayout() {
    }

    public static Result pack(List<Entry> entries) {
        return pack(WIDTH, HEIGHT, entries);
    }

    /** Maiores formas primeiro; tenta sem rotacao e, se nao couber, girada. */
    public static Result pack(int gridW, int gridH, List<Entry> entries) {
        boolean[][] occ = new boolean[gridH][gridW];
        List<Entry> order = new ArrayList<>(entries);
        order.sort(Comparator.comparingInt((Entry e) -> e.shape().cellCount()).reversed());

        List<Placement> placed = new ArrayList<>();
        List<Entry> overflow = new ArrayList<>();
        for (Entry e : order) {
            Placement p = tryPlace(occ, gridW, gridH, e, false);
            if (p == null) {
                p = tryPlace(occ, gridW, gridH, e, true);
            }
            if (p == null) {
                overflow.add(e);
            } else {
                mark(occ, p);
                placed.add(p);
            }
        }
        return new Result(placed, overflow);
    }

    private static Placement tryPlace(boolean[][] occ, int gridW, int gridH, Entry e, boolean rotated) {
        GridShape shape = rotated ? e.shape().rotateCW() : e.shape();
        if (rotated && shape.equals(e.shape())) {
            return null; // girar nao muda nada (ex.: 1x1, 2x2)
        }
        for (int y = 0; y + shape.height() <= gridH; y++) {
            for (int x = 0; x + shape.width() <= gridW; x++) {
                if (fits(occ, shape, x, y)) {
                    return new Placement(e.id(), x, y, rotated, shape);
                }
            }
        }
        return null;
    }

    private static boolean fits(boolean[][] occ, GridShape shape, int x, int y) {
        for (int sy = 0; sy < shape.height(); sy++) {
            for (int sx = 0; sx < shape.width(); sx++) {
                if (shape.occupied(sx, sy) && occ[y + sy][x + sx]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void mark(boolean[][] occ, Placement p) {
        for (int sy = 0; sy < p.shape().height(); sy++) {
            for (int sx = 0; sx < p.shape().width(); sx++) {
                if (p.shape().occupied(sx, sy)) {
                    occ[p.y() + sy][p.x() + sx] = true;
                }
            }
        }
    }
}
