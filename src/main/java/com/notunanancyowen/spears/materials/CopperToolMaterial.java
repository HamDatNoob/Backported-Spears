package com.notunanancyowen.spears.materials;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.TagKey;

public class CopperToolMaterial implements ToolMaterial {
    public static final CopperToolMaterial INSTANCE = new CopperToolMaterial();

    @Override
    public int getDurability(){
        return 190;
    }

    @Override
    public float getMiningSpeedMultiplier(){
        return 5f;
    }

    @Override
    public float getAttackDamage(){
        return 1f;
    }

    @Override
    public TagKey<Block> getInverseTag(){
        return null;
    }

    @Override
    public int getEnchantability(){
        return 13;
    }

    @Override
    public Ingredient getRepairIngredient(){
        return Ingredient.ofItems(Items.COPPER_INGOT);
    }
}
