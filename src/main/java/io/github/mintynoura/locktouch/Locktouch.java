package io.github.mintynoura.locktouch;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;

import java.nio.file.Paths;

public class Locktouch implements ModInitializer {
	public static final String MOD_ID = "locktouch";
	public static final LocktouchConfig CONFIG = LocktouchConfig.createToml(Paths.get("config"), "", "locktouch", LocktouchConfig.class);
	public static final Identifier LOCKPICK_ID = createId("lockpick");
	public static final Identifier DIAMOND_LOCKPICK_ID = createId("diamond_lockpick");
	public static final Identifier LOCKPICK_CHARM_ID = createId("lockpick_charm");
	public static final Identifier KEY_RECYCLED_SOUND_ID = createId("item.key.recycle");
	public static final Identifier LOCKPICK_RECYCLED_SOUND_ID = createId("item.lockpick.recycle");

	public static final SoundEvent KEY_RECYCLED_SOUND = Registry.register(BuiltInRegistries.SOUND_EVENT, KEY_RECYCLED_SOUND_ID, SoundEvent.createVariableRangeEvent(KEY_RECYCLED_SOUND_ID));
	public static final SoundEvent LOCKPICK_RECYCLED_SOUND = Registry.register(BuiltInRegistries.SOUND_EVENT, LOCKPICK_RECYCLED_SOUND_ID, SoundEvent.createVariableRangeEvent(LOCKPICK_RECYCLED_SOUND_ID));

	public static final Holder<Attribute> RECYCLE_KEY_CHANCE = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, createId("recycle_key_chance"),
			new RangedAttribute("attribute.name.recycle_key_chance", CONFIG.recycleKeyChance.value(), 0.0f, 1.0f));
	public static final Holder<Attribute> RECYCLE_LOCKPICK_CHANCE = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, createId("recycle_lockpick_chance"),
			new RangedAttribute("attribute.name.recycle_lockpick_chance", CONFIG.recycleLockpickChance.value(), 0.0f, 1.0f));
	public static final Holder<Attribute> LOCKPICK_SUCCESS_CHANCE = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, createId("lockpick_success_chance"),
			new RangedAttribute("attribute.name.lockpick_success_chance", CONFIG.lockpickSuccessChance.value(), 0.0f, 1.0f));

	public static final Item LOCKPICK = Registry.register(BuiltInRegistries.ITEM, LOCKPICK_ID, new Item(new Item.Properties()
			.setId(ResourceKey.create(Registries.ITEM, LOCKPICK_ID))
			.stacksTo(16)
			)
	);
	public static final Item DIAMOND_LOCKPICK = Registry.register(BuiltInRegistries.ITEM, DIAMOND_LOCKPICK_ID, new Item(new Item.Properties()
					.setId(ResourceKey.create(Registries.ITEM, DIAMOND_LOCKPICK_ID))
					.stacksTo(16)
			)
	);
	public static final Item LOCKPICK_CHARM = Registry.register(BuiltInRegistries.ITEM, LOCKPICK_CHARM_ID, new Item(new Item.Properties()
			.setId(ResourceKey.create(Registries.ITEM, LOCKPICK_CHARM_ID))
			.durability(25)
			.attributes(ItemAttributeModifiers.builder()
					.add(RECYCLE_KEY_CHANCE, new AttributeModifier(LOCKPICK_CHARM_ID.withSuffix("key"), CONFIG.lockpickCharmSection.recycleKeyModifier.value(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND)
					.add(RECYCLE_LOCKPICK_CHANCE, new AttributeModifier(LOCKPICK_CHARM_ID.withSuffix("lockpick"), CONFIG.lockpickCharmSection.recycleLockpickModifier.value(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.OFFHAND).build())
			)
	);

	public static Identifier createId(String name) {
		return Identifier.fromNamespaceAndPath(MOD_ID, name);
	}

	@Override
	public void onInitialize() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(content -> {
				content.addAfter(Items.OMINOUS_TRIAL_KEY, LOCKPICK);
				content.addAfter(LOCKPICK, LOCKPICK_CHARM);
		});
		LootTableEvents.MODIFY.register((id, tableBuilder, source, registries) -> {
			if (source.isBuiltin()) {
				if (id.equals(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY)) tableBuilder.modifyPools(poolBuilder -> poolBuilder.add(LootItem.lootTableItem(LOCKPICK_CHARM)));
				if (id.equals(EntityType.PILLAGER.getDefaultLootTable().get())) tableBuilder.withPool(LootPool.lootPool().add(LootItem.lootTableItem(LOCKPICK).setWeight(5)).add(EmptyLootItem.emptyItem().setWeight(44)).add(LootItem.lootTableItem(LOCKPICK_CHARM).setWeight(1)));
			}
		});
	}
}