package com.chaos.gravestone.item;

import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.ModBlocks;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Perlas de ender arriba, abajo, izquierda y derecha; brújula de lápida vinculada en el centro.
 * Receta especial porque el resultado debe conservar la lápida a la que apunta la brújula.
 */
public class EnderGraveCompassRecipe extends CustomRecipe {

	public EnderGraveCompassRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		if (input.getWidth() != 3 || input.getHeight() != 3) {
			return false;
		}
		for (int i = 0; i < 9; i++) {
			ItemStack stack = input.getItem(i);
			boolean ok;
			if (i == 4) {
				ok = stack.is(ModBlocks.GRAVE_COMPASS) && GravestoneCompass.read(stack).isPresent();
			} else if (i % 2 == 1) {
				ok = stack.is(Items.ENDER_PEARL);
			} else {
				ok = stack.isEmpty();
			}
			if (!ok) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
		// Copia los componentes (lápida vinculada) a la versión de ender.
		ItemStack result = new ItemStack(ModBlocks.ENDER_GRAVE_COMPASS);
        if (input.getItem(4).hasTag()) result.setTag(input.getItem(4).getTag().copy());
        return result;
	}

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return ModBlocks.ENDER_GRAVE_COMPASS_RECIPE;
	}
}
