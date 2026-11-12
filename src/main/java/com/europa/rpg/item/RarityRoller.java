package com.europa.rpg.item;

import com.europa.rpg.EuropaRPG;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.TridentItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * Sorteia raridade e afixos de equipamentos. Afixos de atributo viram modificadores no próprio item;
 * afixos especiais (crítico, roubo de vida, poder de habilidade) ficam em CUSTOM_DATA e são lidos em combate.
 */
public final class RarityRoller {
    public static final String KEY = "europa_gear";

    private RarityRoller() {}

    public static boolean isGear(ItemStack s) {
        var i = s.getItem();
        return i instanceof ArmorItem || i instanceof ToolItem || i instanceof RangedWeaponItem || i instanceof ShieldItem
                || i instanceof TridentItem || i instanceof MaceItem;
    }

    public static Rarity rarityOf(ItemStack s) {
        NbtComponent c = s.get(DataComponentTypes.CUSTOM_DATA);
        if (c == null) return null;
        NbtCompound gear = c.copyNbt().getCompound(KEY);
        return gear.isEmpty() ? null : Rarity.byId(gear.getString("rarity"));
    }

    /** Valor de um afixo especial ("crit", "lifesteal", "power") no item. */
    public static double special(ItemStack s, String key) {
        NbtComponent c = s.get(DataComponentTypes.CUSTOM_DATA);
        return c == null ? 0 : c.copyNbt().getCompound(KEY).getDouble(key);
    }

    /** Soma de um afixo especial em mão principal + armadura. */
    public static double equipped(LivingEntity e, String key) {
        double sum = special(e.getMainHandStack(), key);
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            sum += special(e.getEquippedStack(slot), key);
        }
        return sum;
    }

    public static Rarity pick(Random random, int minTier) {
        Rarity[] all = Rarity.values();
        int total = 0;
        for (int i = minTier; i < all.length; i++) total += all[i].weight;
        int r = random.nextInt(Math.max(1, total));
        for (int i = minTier; i < all.length; i++) {
            r -= all[i].weight;
            if (r < 0) return all[i];
        }
        return all[Math.min(minTier, all.length - 1)];
    }

    private record Affix(String id, RegistryEntry<EntityAttribute> attr, double min, double max, String special) {}

    private static final List<Affix> WEAPON = List.of(
            new Affix("damage", EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.5, 1.5, null),
            new Affix("attack_speed", EntityAttributes.GENERIC_ATTACK_SPEED, 0.05, 0.15, null),
            new Affix("luck", EntityAttributes.GENERIC_LUCK, 0.5, 1.0, null),
            new Affix("crit", null, 0.02, 0.04, "crit"),
            new Affix("lifesteal", null, 0.01, 0.03, "lifesteal"),
            new Affix("power", null, 0.03, 0.06, "power"));
    private static final List<Affix> ARMOR = List.of(
            new Affix("health", EntityAttributes.GENERIC_MAX_HEALTH, 1, 2, null),
            new Affix("armor", EntityAttributes.GENERIC_ARMOR, 0.5, 1.5, null),
            new Affix("toughness", EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 0.3, 1.0, null),
            new Affix("knockback", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.02, 0.05, null),
            new Affix("speed", EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.002, 0.004, null),
            new Affix("power", null, 0.02, 0.04, "power"),
            new Affix("crit", null, 0.01, 0.02, "crit"));

    /** Sorteia e aplica raridade se o item for equipamento e ainda não tiver uma. Retorna o item. */
    public static ItemStack roll(ItemStack stack, Random random, int minTier) {
        if (!isGear(stack) || rarityOf(stack) != null) return stack;
        return apply(stack, pick(random, minTier), random);
    }

    public static ItemStack apply(ItemStack stack, Rarity rarity, Random random) {
        setRarity(stack, rarity);
        for (int i = 0; i < rarity.affixes; i++) addAffix(stack, random);
        return stack;
    }

    static NbtCompound gear(ItemStack stack) {
        NbtComponent c = stack.get(DataComponentTypes.CUSTOM_DATA);
        return c == null ? new NbtCompound() : c.copyNbt().getCompound(KEY);
    }

    static void writeGear(ItemStack stack, NbtCompound gear) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.put(KEY, gear));
    }

    /** Define a raridade (sem novos afixos). Épico ou acima ganha brilho de encantamento. */
    public static void setRarity(ItemStack stack, Rarity rarity) {
        NbtCompound g = gear(stack);
        g.putString("rarity", rarity.id());
        writeGear(stack, g);
        if (rarity.ordinal() >= Rarity.EPIC.ordinal()) stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    /** Adiciona um afixo ainda não presente no item, escalado pela raridade atual. Retorna false se não houver mais. */
    public static boolean addAffix(ItemStack stack, Random random) {
        Rarity rarity = rarityOf(stack);
        if (rarity == null) rarity = Rarity.COMMON;
        boolean armor = stack.getItem() instanceof ArmorItem;
        NbtCompound g = gear(stack);
        net.minecraft.nbt.NbtList used = g.getList("affixes", net.minecraft.nbt.NbtElement.STRING_TYPE);
        java.util.Set<String> taken = new java.util.HashSet<>();
        for (int i = 0; i < used.size(); i++) taken.add(used.getString(i));
        List<Affix> pool = new ArrayList<>((armor ? ARMOR : WEAPON).stream().filter(a -> !taken.contains(a.id)).toList());
        if (pool.isEmpty()) return false;
        Affix a = pool.get(random.nextInt(pool.size()));
        double value = (a.min + random.nextDouble() * (a.max - a.min)) * Math.max(1.0, rarity.scale);
        if (a.special != null) {
            g.putDouble(a.special, g.getDouble(a.special) + value);
        } else {
            AttributeModifierSlot slot = armor ? AttributeModifierSlot.forEquipmentSlot(((ArmorItem) stack.getItem()).getSlotType()) : AttributeModifierSlot.MAINHAND;
            String slotName = armor ? ((ArmorItem) stack.getItem()).getSlotType().getName() : "mainhand";
            AttributeModifiersComponent mods = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (mods == null || mods.modifiers().isEmpty()) mods = stack.getItem().getAttributeModifiers();
            mods = mods.with(a.attr, new EntityAttributeModifier(EuropaRPG.id("affix/" + slotName + "/" + a.id), value,
                    EntityAttributeModifier.Operation.ADD_VALUE), slot);
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, mods);
        }
        used.add(net.minecraft.nbt.NbtString.of(a.id));
        g.put("affixes", used);
        writeGear(stack, g);
        return true;
    }

    /** Valor de venda de equipamento com raridade (Fragmentos de Aster). */
    public static int sellValue(Rarity r) {
        int t = r.ordinal() + 1;
        return 2 * t * t;
    }
}
