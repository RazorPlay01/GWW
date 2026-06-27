package com.github.razorplay01.item;

import com.github.razorplay01.GWW;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModItems {
    public static final Item ALICATE_CORTACABLES = register("alicate_cortacables", new Item(new Item.Properties()));
    public static final Item COLGANTE_CUADROS = register("colgante_cuadros", new Item(new Item.Properties()));
    public static final Item CUADRO_DE_LILITH_2 = register("cuadro_de_lilith2", new Item(new Item.Properties()));
    public static final Item CUADRO_LILITH = register("cuadro_lilith", new Item(new Item.Properties()));
    public static final Item FUSIBLE_AZUL = register("fusible_azul", new BigItem(new Item.Properties(), new int[]{1, 2}));
    public static final Item FUSIBLE_ROJO = register("fusible_rojo", new BigItem(new Item.Properties(), new int[]{1, 2}));
    public static final Item FUSIBLE_VERDE = register("fusible_verde", new BigItem(new Item.Properties(), new int[]{1, 2}));
    public static final Item GANZUA = register("ganzua", new Item(new Item.Properties()));
    public static final Item HOJA_PISTA = register("hoja_pista", new Item(new Item.Properties()));
    public static final Item LLAVE_ATICO = register("llave_atico", new BigItem(new Item.Properties(), new int[]{2, 2}));

    public static final ResourceKey<CreativeModeTab> CUSTOM_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "item_group"));
    public static final CreativeModeTab CUSTOM_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.ALICATE_CORTACABLES))
            .title(Component.translatable("itemGroup.gww"))
            .build();

    public static Item register(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, id), item);
    }

    public static void registerModItems() {
        GWW.LOGGER.info("Registering Mod Items for " + GWW.MOD_ID);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_ITEM_GROUP_KEY, CUSTOM_ITEM_GROUP);
        ItemGroupEvents.modifyEntriesEvent(CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(ModItems.ALICATE_CORTACABLES);
            itemGroup.accept(ModItems.COLGANTE_CUADROS);
            itemGroup.accept(ModItems.CUADRO_DE_LILITH_2);
            itemGroup.accept(ModItems.CUADRO_LILITH);
            itemGroup.accept(ModItems.FUSIBLE_AZUL);
            itemGroup.accept(ModItems.FUSIBLE_ROJO);
            itemGroup.accept(ModItems.FUSIBLE_VERDE);
            itemGroup.accept(ModItems.GANZUA);
            itemGroup.accept(ModItems.HOJA_PISTA);
            itemGroup.accept(ModItems.LLAVE_ATICO);
        });
    }
}
