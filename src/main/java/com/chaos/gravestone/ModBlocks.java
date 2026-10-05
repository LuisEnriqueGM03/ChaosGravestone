package com.chaos.gravestone;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.chaos.gravestone.block.GravestoneBlock;
import com.chaos.gravestone.block.GravestoneBlockEntity;
import com.chaos.gravestone.item.EnderGraveCompassItem;
import com.chaos.gravestone.item.GraveCompassItem;
import com.chaos.gravestone.item.EnderGraveCompassRecipe;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {

	public static final Map<GravestoneVariant, GravestoneBlock> GRAVESTONES = new EnumMap<>(GravestoneVariant.class);
	/** La clásica; se mantiene con el id "gravestone" para no romper mundos existentes. */
	public static GravestoneBlock GRAVESTONE;
	public static BlockEntityType<GravestoneBlockEntity> GRAVESTONE_ENTITY;
	public static Item GRAVE_COMPASS;
	public static Item ENDER_GRAVE_COMPASS;
	public static Item PURPLE_SKULL;
	public static RecipeSerializer<EnderGraveCompassRecipe> ENDER_GRAVE_COMPASS_RECIPE;
	public static CreativeModeTab TAB;
	private static final List<ItemStack> TAB_ICONS = new ArrayList<>();

	private ModBlocks() {}

	public static void register() {
		for (GravestoneVariant variant : GravestoneVariant.values()) {
			ResourceLocation id = id(variant.id());
			// Dureza -1: las lápidas con dueño solo se "abren". Las decorativas (sin dueño) se rompen
			// con normalidad, ver GravestoneBlock#getDestroyProgress.
			GravestoneBlock block = Registry.register(BuiltInRegistries.BLOCK, id,
					new GravestoneBlock(BlockBehaviour.Properties.of()
							.strength(-1.0F, 3600000.0F)
							.sound(variant.sound())
							.lightLevel(state -> variant.light())
							.noLootTable()
							.noOcclusion()));
			Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()));
			GRAVESTONES.put(variant, block);
		}
		GRAVESTONE = GRAVESTONES.get(GravestoneVariant.CLASSIC);

		GRAVESTONE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("gravestone"),
				BlockEntityType.Builder.of(GravestoneBlockEntity::new,
						GRAVESTONES.values().toArray(GravestoneBlock[]::new)).build(null));
		GRAVE_COMPASS = Registry.register(BuiltInRegistries.ITEM, id("grave_compass"),
				new GraveCompassItem(new Item.Properties().stacksTo(1)));
		ENDER_GRAVE_COMPASS = Registry.register(BuiltInRegistries.ITEM, id("ender_grave_compass"),
				new EnderGraveCompassItem(new Item.Properties().stacksTo(1)));
		// Solo para la animación del tótem al teletransportarse; no aparece en la pestaña.
		PURPLE_SKULL = Registry.register(BuiltInRegistries.ITEM, id("purple_skull"),
				new Item(new Item.Properties()));
		ENDER_GRAVE_COMPASS_RECIPE = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("ender_grave_compass"),
				new SimpleCraftingRecipeSerializer<>(EnderGraveCompassRecipe::new));

		// El icono rota entre las variantes: ver CreativeModeTabMixin.
		TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("gravestones"), FabricItemGroup.builder()
				.title(Component.translatable("itemGroup.chaosgravestone"))
				.icon(() -> new ItemStack(GRAVESTONE))
				.displayItems((params, output) -> {
					GRAVESTONES.values().forEach(output::accept);
					// Sin lápida asignada: la aguja gira sin control, como una brújula vanilla sin destino.
					output.accept(GRAVE_COMPASS);
					output.accept(ENDER_GRAVE_COMPASS);
				})
				.build());
		for (GravestoneBlock block : GRAVESTONES.values()) {
			TAB_ICONS.add(new ItemStack(block));
		}
	}

	/** Icono de la pestaña: una variante distinta cada segundo. */
	public static ItemStack tabIcon() {
		return TAB_ICONS.get((int) (System.currentTimeMillis() / 1000L % TAB_ICONS.size()));
	}

	public static GravestoneBlock get(GravestoneVariant variant) {
		return GRAVESTONES.get(variant);
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ChaosGravestone.MOD_ID, path);
	}
}
