package com.vini.gridinventory.common.grid;

import java.util.Arrays;

/**
 * Forma fisica de um item: matriz binaria [linha][coluna].
 * true = celula ocupada, false = celula vazia.
 * Sem dependencias do Minecraft (testavel isoladamente).
 */
public final class GridShape {
    private final boolean[][] cells;

    private GridShape(boolean[][] cells) {
        this.cells = cells;
    }

    /** Cria a partir de linhas de texto: '#' = ocupado, qualquer outro caractere = vazio. */
    public static GridShape of(String... rows) {
        if (rows.length == 0) {
            throw new IllegalArgumentException("Forma sem linhas");
        }
        int w = rows[0].length();
        boolean[][] m = new boolean[rows.length][w];
        for (int y = 0; y < rows.length; y++) {
            if (rows[y].length() != w) {
                throw new IllegalArgumentException("Linhas com larguras diferentes");
            }
            for (int x = 0; x < w; x++) {
                m[y][x] = rows[y].charAt(x) == '#';
            }
        }
        return normalize(m);
    }

    public static GridShape rectangle(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Dimensoes invalidas");
        }
        boolean[][] m = new boolean[height][width];
        for (boolean[] row : m) {
            Arrays.fill(row, true);
        }
        return new GridShape(m);
    }

    /** Remove linhas/colunas vazias ao redor da forma. */
    public static GridShape normalize(boolean[][] src) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = -1, maxY = -1;
        for (int y = 0; y < src.length; y++) {
            for (int x = 0; x < src[y].length; x++) {
                if (src[y][x]) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < 0) {
            throw new IllegalArgumentException("Forma vazia");
        }
        boolean[][] out = new boolean[maxY - minY + 1][maxX - minX + 1];
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                out[y - minY][x - minX] = src[y][x];
            }
        }
        return new GridShape(out);
    }

    public int width() {
        return cells[0].length;
    }

    public int height() {
        return cells.length;
    }

    /** Fora dos limites da forma retorna false. */
    public boolean occupied(int x, int y) {
        return y >= 0 && y < height() && x >= 0 && x < width() && cells[y][x];
    }

    public int cellCount() {
        int n = 0;
        for (boolean[] row : cells) {
            for (boolean c : row) {
                if (c) n++;
            }
        }
        return n;
    }

    /** Gira 90 graus no sentido horario, sobre a forma real. */
    public GridShape rotateCW() {
        int oldH = height();
        int oldW = width();
        boolean[][] out = new boolean[oldW][oldH];
        for (int r = 0; r < oldW; r++) {
            for (int c = 0; c < oldH; c++) {
                out[r][c] = cells[oldH - 1 - c][r];
            }
        }
        return new GridShape(out);
    }

    /** Celula ocupada mais proxima do centro da forma (onde o icone do item e desenhado). */
    public int[] focusCell() {
        double cx = (width() - 1) / 2.0;
        double cy = (height() - 1) / 2.0;
        int bestX = 0, bestY = 0;
        double best = Double.MAX_VALUE;
        for (int y = 0; y < height(); y++) {
            for (int x = 0; x < width(); x++) {
                if (cells[y][x]) {
                    double d = (x - cx) * (x - cx) + (y - cy) * (y - cy);
                    if (d < best) {
                        best = d;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }
        return new int[]{bestX, bestY};
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GridShape other && Arrays.deepEquals(cells, other.cells);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(cells);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (boolean[] row : cells) {
            for (boolean c : row) {
                sb.append(c ? '#' : '.');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
