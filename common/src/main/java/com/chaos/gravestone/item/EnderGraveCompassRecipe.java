package com.chaos.gravestone.item;

import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.ModBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Perlas de ender arriba, abajo, izquierda y derecha; brújula de lápida vinculada en el centro.
 * Receta especial porque el resultado debe conservar la lápida a la que apunta la brújula.
 */
public class EnderGraveCompassRecipe extends CustomRecipe {

	public EnderGraveCompassRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.width() != 3 || input.height() != 3) {
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
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		// Copia los componentes (lápida vinculada) a la versión de ender.
		return input.getItem(4).transmuteCopy(ModBlocks.ENDER_GRAVE_COMPASS, 1);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= 3 && height >= 3;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModBlocks.ENDER_GRAVE_COMPASS_RECIPE;
	}
}
