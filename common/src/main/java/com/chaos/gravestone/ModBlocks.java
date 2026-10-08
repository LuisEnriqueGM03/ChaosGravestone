package com.chaos.gravestone;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chaos.gravestone.block.GravestoneBlock;
import com.chaos.gravestone.block.GravestoneBlockEntity;
import com.chaos.gravestone.item.EnderGraveCompassItem;
import com.chaos.gravestone.item.EnderGraveCompassRecipe;
import com.chaos.gravestone.item.GraveCompassItem;
import com.chaos.gravestone.platform.Registrar;
import com.chaos.gravestone.platform.Services;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Bloques, ítems y demás objetos del mod. Cada cargador llama a los register* con su Registrar,
 * en este orden: bloques, ítems, block entities, recetas y pestaña creativa.
 */
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

	public static void registerBlocks(Registrar<Block> registrar) {
		for (GravestoneVariant variant : GravestoneVariant.values()) {
			// Dureza -1: las lápidas con dueño solo se "abren". Las decorativas (sin dueño) se rompen
			// con normalidad, ver GravestoneBlock#getDestroyProgress.
			GravestoneBlock block = new GravestoneBlock(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, id(variant.id())))
					.strength(-1.0F, 3600000.0F)
					.sound(variant.sound())
					.lightLevel(state -> variant.light())
					.noLootTable()
					.noOcclusion());
			registrar.register(id(variant.id()), block);
			GRAVESTONES.put(variant, block);
		}
		GRAVESTONE = GRAVESTONES.get(GravestoneVariant.CLASSIC);
	}

	public static void registerItems(Registrar<Item> registrar) {
		GRAVESTONES.forEach((variant, block) ->
				registrar.register(id(variant.id()), new BlockItem(block, itemProps(variant.id()).useBlockDescriptionPrefix())));
		GRAVE_COMPASS = new GraveCompassItem(itemProps("grave_compass").stacksTo(1));
		registrar.register(id("grave_compass"), GRAVE_COMPASS);
		ENDER_GRAVE_COMPASS = new EnderGraveCompassItem(itemProps("ender_grave_compass").stacksTo(1));
		registrar.register(id("ender_grave_compass"), ENDER_GRAVE_COMPASS);
		// Solo para la animación del tótem al teletransportarse; no aparece en la pestaña.
		PURPLE_SKULL = new Item(itemProps("purple_skull"));
		registrar.register(id("purple_skull"), PURPLE_SKULL);
	}

	public static void registerBlockEntities(Registrar<BlockEntityType<?>> registrar) {
		GRAVESTONE_ENTITY = Services.PLATFORM.blockEntityType(GravestoneBlockEntity::new, Set.copyOf(GRAVESTONES.values()));
		registrar.register(id("gravestone"), GRAVESTONE_ENTITY);
	}

	public static void registerRecipeSerializers(Registrar<RecipeSerializer<?>> registrar) {
		ENDER_GRAVE_COMPASS_RECIPE = new CustomRecipe.Serializer<>(EnderGraveCompassRecipe::new);
		registrar.register(id("ender_grave_compass"), ENDER_GRAVE_COMPASS_RECIPE);
	}

	public static void registerCreativeTabs(Registrar<CreativeModeTab> registrar) {
		// El icono rota entre las variantes: ver CreativeModeTabMixin.
		TAB = Services.PLATFORM.creativeTabBuilder()
				.title(Component.translatable("itemGroup.chaosgravestone"))
				.icon(() -> new ItemStack(GRAVESTONE))
				.displayItems((params, output) -> {
					GRAVESTONES.values().forEach(output::accept);
					// Sin lápida asignada: la aguja gira sin control, como una brújula vanilla sin destino.
					output.accept(GRAVE_COMPASS);
					output.accept(ENDER_GRAVE_COMPASS);
				})
				.build();
		registrar.register(id("gravestones"), TAB);
	}

	/** Icono de la pestaña: una variante distinta cada segundo. */
	public static ItemStack tabIcon() {
		// Se crea al primer uso: en NeoForge la pestaña puede registrarse antes que los ítems.
		if (TAB_ICONS.isEmpty()) {
			GRAVESTONES.values().forEach(block -> TAB_ICONS.add(new ItemStack(block)));
		}
		return TAB_ICONS.get((int) (System.currentTimeMillis() / 1000L % TAB_ICONS.size()));
	}

	public static GravestoneBlock get(GravestoneVariant variant) {
		return GRAVESTONES.get(variant);
	}

	/** Desde 1.21.2 cada ítem lleva su id en las propiedades al crearse. */
	private static Item.Properties itemProps(String path) {
		return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(path)));
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ChaosGravestone.MOD_ID, path);
	}
}
