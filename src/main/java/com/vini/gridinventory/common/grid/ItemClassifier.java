package com.vini.gridinventory.common.grid;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

/**
 * Descobre a forma de um item vanilla automaticamente, sem cadastro item por item.
 * Parte 1: usa apenas classes vanilla (tags e regras de mods ficam para a V2+).
 */
public final class ItemClassifier {

    public enum Rule {
        ARMOR(GridShape.rectangle(2, 2)),
        PICKAXE(GridShape.of("###", ".#.", ".#.")),
        AXE(GridShape.of("##", "##", ".#")),
        HOE(GridShape.of("##", ".#", ".#")),
        SHOVEL(GridShape.of("#", "#", "#")),
        SWORD(GridShape.of("#", "#", "#")),
        SHEARS(GridShape.rectangle(2, 2)),
        FISHING_ROD(GridShape.of("##", "#.", "#.")),
        RANGED(GridShape.of("##", "##", "##")),
        SHIELD(GridShape.of("##", "##", ".#")),
        FOOD(GridShape.rectangle(1, 1)),
        POTION(GridShape.rectangle(1, 2)),
        BUCKET(GridShape.rectangle(1, 2)),
        DEFAULT(GridShape.rectangle(1, 1));

        private final GridShape shape;

        Rule(GridShape shape) {
            this.shape = shape;
        }

        public GridShape shape() {
            return shape;
        }
    }

    private ItemClassifier() {
    }

    public static Rule classify(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ArmorItem) return Rule.ARMOR;
        if (item instanceof PickaxeItem) return Rule.PICKAXE;
        if (item instanceof AxeItem) return Rule.AXE;
        if (item instanceof HoeItem) return Rule.HOE;
        if (item instanceof ShovelItem) return Rule.SHOVEL;
        if (item instanceof SwordItem) return Rule.SWORD;
        if (item instanceof ShearsItem) return Rule.SHEARS;
        if (item instanceof FishingRodItem) return Rule.FISHING_ROD;
        if (item instanceof BowItem || item instanceof CrossbowItem) return Rule.RANGED;
        if (item instanceof ShieldItem) return Rule.SHIELD;
        if (stack.isEdible()) return Rule.FOOD;
        if (item instanceof PotionItem) return Rule.POTION;
        if (item instanceof BucketItem) return Rule.BUCKET;
        return Rule.DEFAULT;
    }

    public static GridShape shapeOf(ItemStack stack) {
        return classify(stack).shape();
    }
}
