package com.vini.gridinventory.client;

import com.vini.gridinventory.common.grid.GridLayout;
import com.vini.gridinventory.common.grid.ItemClassifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Parte 1: tela somente de visualizacao.
 * Mostra o grid 9x7 e a forma de cada item do inventario vanilla, distribuidos automaticamente.
 */
public class GridInventoryScreen extends Screen {
    private static final int CELL = 32;
    private static final int GRID_PX_W = GridLayout.WIDTH * CELL;
    private static final int GRID_PX_H = GridLayout.HEIGHT * CELL;

    private ItemStack[] stacks = new ItemStack[0];
    private ItemClassifier.Rule[] rules = new ItemClassifier.Rule[0];
    private GridLayout.Result layout = new GridLayout.Result(List.of(), List.of());

    public GridInventoryScreen() {
        super(Component.translatable("screen.gridinventory.title"));
    }

    @Override
    protected void init() {
        recompute();
    }

    @Override
    public void tick() {
        recompute();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (GridInventoryClient.OPEN_KEY.matches(keyCode, scanCode)
                || (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void recompute() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Inventory inv = mc.player.getInventory();
        int size = inv.getContainerSize();
        stacks = new ItemStack[size];
        rules = new ItemClassifier.Rule[size];
        List<GridLayout.Entry> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ItemStack s = inv.getItem(i);
            stacks[i] = s;
            if (s.isEmpty()) {
                continue;
            }
            ItemClassifier.Rule rule = ItemClassifier.classify(s);
            rules[i] = rule;
            entries.add(new GridLayout.Entry(i, rule.shape()));
        }
        layout = GridLayout.pack(entries);
    }

    private static int colorFor(ItemClassifier.Rule rule) {
        float hue = rule.ordinal() / (float) ItemClassifier.Rule.values().length;
        return 0xC0000000 | Mth.hsvToRgb(hue, 0.55F, 0.85F);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int left = (this.width - GRID_PX_W) / 2;
        int top = (this.height - GRID_PX_H) / 2 - 16;

        g.drawCenteredString(this.font, this.title, this.width / 2, top - 16, 0xFFFFFF);

        // Fundo e celulas vazias do grid
        g.fill(left - 2, top - 2, left + GRID_PX_W + 2, top + GRID_PX_H + 2, 0xFF2B2B2B);
        for (int cy = 0; cy < GridLayout.HEIGHT; cy++) {
            for (int cx = 0; cx < GridLayout.WIDTH; cx++) {
                int x = left + cx * CELL;
                int y = top + cy * CELL;
                g.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF101010);
            }
        }

        // Item sob o mouse (grid ou overflow)
        ItemStack hovered = null;
        GridLayout.Placement hoveredPlacement = null;
        int mcx = Math.floorDiv(mouseX - left, CELL);
        int mcy = Math.floorDiv(mouseY - top, CELL);

        // Formas + icones
        for (GridLayout.Placement p : layout.placed()) {
            int color = colorFor(rules[p.id()]);
            for (int sy = 0; sy < p.shape().height(); sy++) {
                for (int sx = 0; sx < p.shape().width(); sx++) {
                    if (p.shape().occupied(sx, sy)) {
                        int x = left + (p.x() + sx) * CELL;
                        int y = top + (p.y() + sy) * CELL;
                        g.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, color);
                    }
                }
            }
            int[] focus = p.shape().focusCell();
            int ix = left + (p.x() + focus[0]) * CELL + (CELL - 16) / 2;
            int iy = top + (p.y() + focus[1]) * CELL + (CELL - 16) / 2;
            g.renderItem(stacks[p.id()], ix, iy);
            g.renderItemDecorations(this.font, stacks[p.id()], ix, iy);

            if (mouseX >= left && mouseY >= top && p.covers(mcx, mcy)) {
                hovered = stacks[p.id()];
                hoveredPlacement = p;
            }
        }

        // Itens sem espaco (vermelho), abaixo do grid
        int infoY = top + GRID_PX_H + 8;
        if (!layout.overflow().isEmpty()) {
            g.drawString(this.font,
                    Component.translatable("screen.gridinventory.overflow", layout.overflow().size()),
                    left, infoY + 14, 0xFF6060);
            int shown = Math.min(layout.overflow().size(), GridLayout.WIDTH);
            for (int i = 0; i < shown; i++) {
                GridLayout.Entry e = layout.overflow().get(i);
                int x = left + i * CELL;
                int y = infoY + 28;
                g.fill(x, y, x + CELL, y + CELL, 0xFFB03030);
                g.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF301010);
                g.renderItem(stacks[e.id()], x + 8, y + 8);
                g.renderItemDecorations(this.font, stacks[e.id()], x + 8, y + 8);
                if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                    hovered = stacks[e.id()];
                    hoveredPlacement = null;
                }
            }
        }

        // Linha de informacao
        Component info;
        if (hovered != null) {
            GridLayout.Placement p = hoveredPlacement;
            int w;
            int h;
            int cells;
            if (p != null) {
                w = p.shape().width();
                h = p.shape().height();
                cells = p.shape().cellCount();
            } else {
                var shape = ItemClassifier.shapeOf(hovered);
                w = shape.width();
                h = shape.height();
                cells = shape.cellCount();
            }
            info = Component.translatable("screen.gridinventory.info", hovered.getHoverName(), w, h, cells);
        } else {
            info = Component.translatable("screen.gridinventory.hint");
        }
        g.drawString(this.font, info, left, infoY, 0xC0C0C0);

        super.render(g, mouseX, mouseY, partialTick);

        if (hovered != null) {
            g.renderTooltip(this.font, hovered, mouseX, mouseY);
        }
    }
}
