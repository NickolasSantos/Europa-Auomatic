package com.europa.rpg.economy;

import com.europa.rpg.item.Rarity;
import com.europa.rpg.item.RarityRoller;
import com.europa.rpg.registry.ModItems;
import com.europa.rpg.rpg.Messages;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.RpgAttachments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Economia: venda de materiais por Fragmentos de Aster (preço dinâmico) e compra na loja geral/de facções.
 * Compras retiram moeda de circulação (dreno); vendas em massa derrubam o preço (oferta).
 */
public final class Market {
    public static final double ELASTICITY = 200.0;
    private static final Map<Identifier, Double> SELL = new LinkedHashMap<>();

    public record Offer(String id, Identifier item, int count, int price, String faction, int minRank) {}

    public static final List<Offer> OFFERS = List.of(
            new Offer("bread", mc("bread"), 16, 8, null, 0),
            new Offer("arrows", mc("arrow"), 32, 10, null, 0),
            new Offer("iron", mc("iron_ingot"), 4, 12, null, 0),
            new Offer("pearl", mc("ender_pearl"), 1, 15, null, 0),
            new Offer("quest_board", eu("quest_board"), 1, 5, null, 0),
            new Offer("shrine", eu("shrine"), 1, 40, null, 0),
            new Offer("obs_xp", mc("experience_bottle"), 8, 20, "observatory", 1),
            new Offer("obs_eyes", mc("ender_eye"), 4, 60, "observatory", 2),
            new Offer("obs_staff", eu("arcane_staff"), 1, 400, "observatory", 3),
            new Offer("wan_apple", mc("golden_apple"), 1, 30, "wanderers", 1),
            new Offer("wan_key", eu("catacomb_key"), 1, 150, "wanderers", 2),
            new Offer("wan_flame", eu("flamebrand"), 1, 900, "wanderers", 4),
            new Offer("gear_ingot", eu("adamantite_ingot"), 2, 120, "gears", 2),
            new Offer("gear_tome", eu("rebirth_tome"), 1, 350, "gears", 3),
            new Offer("gear_storm", eu("stormbreaker"), 1, 900, "gears", 4),
            new Offer("nam_shards", eu("soul_shard"), 4, 40, "nameless", 1),
            new Offer("nam_frost", eu("frostfang"), 1, 600, "nameless", 3),
            new Offer("nam_reaper", eu("soulreaper"), 1, 1000, "nameless", 4));

    static {
        sell("raw_iron", 0.5); sell("iron_ingot", 0.6); sell("raw_gold", 0.8); sell("gold_ingot", 1); sell("diamond", 8);
        sell("emerald", 4); sell("lapis_lazuli", 0.2); sell("redstone", 0.1); sell("coal", 0.1); sell("copper_ingot", 0.15);
        sell("quartz", 0.2); sell("amethyst_shard", 0.5); sell("echo_shard", 10); sell("netherite_scrap", 25);
        sell("wheat", 0.05); sell("bread", 0.15); sell("leather", 0.3); sell("bone", 0.05); sell("string", 0.05);
        sell("rotten_flesh", 0.02); sell("gunpowder", 0.2); sell("ender_pearl", 1.5); sell("blaze_rod", 1.5); sell("ghast_tear", 4);
        sell("nether_star", 200); sell("prismarine_shard", 0.2); sell("ink_sac", 0.1); sell("slime_ball", 0.3);
        sell("spider_eye", 0.1); sell("cod", 0.1); sell("salmon", 0.12); sell("honeycomb", 0.3); sell("feather", 0.05);
        sell("phantom_membrane", 1.2);
        SELL.put(eu("raw_mithril"), 1.5); SELL.put(eu("mithril_ingot"), 2.0); SELL.put(eu("raw_adamantite"), 4.0);
        SELL.put(eu("adamantite_ingot"), 5.0); SELL.put(eu("soul_shard"), 3.0);
    }

    private Market() {}

    private static Identifier mc(String p) { return Identifier.ofVanilla(p); }
    private static Identifier eu(String p) { return Identifier.of("europa", p); }
    private static void sell(String p, double v) { SELL.put(mc(p), v); }

    public static Offer offer(String id) {
        return OFFERS.stream().filter(o -> o.id.equals(id)).findFirst().orElse(null);
    }

    /** Preço base de venda de uma unidade (ou null se o mercado não compra). */
    public static Double basePrice(ItemStack s) {
        Rarity r = RarityRoller.rarityOf(s);
        if (r != null) return (double) RarityRoller.sellValue(r);
        return SELL.get(Registries.ITEM.getId(s.getItem()));
    }

    public static double currentPrice(ServerPlayerEntity p, ItemStack s) {
        Double base = basePrice(s);
        if (base == null) return 0;
        double pressure = MarketState.get(p.getServer()).pressure.getOrDefault(Registries.ITEM.getId(s.getItem()).toString(), 0.0);
        return base * ELASTICITY / (ELASTICITY + pressure);
    }

    /** Vende 1 unidade ou a pilha inteira. Retorna fragmentos pagos. */
    public static int sell(ServerPlayerEntity p, ItemStack stack, boolean wholeStack) {
        if (stack.isEmpty()) return 0;
        double unit = currentPrice(p, stack);
        if (unit <= 0) {
            p.sendMessage(Text.translatable("rpg.europa.market.not_buying").formatted(Formatting.GRAY), true);
            return 0;
        }
        int count = wholeStack ? stack.getCount() : Math.max(1, (int) Math.ceil(1 / unit));
        count = Math.min(count, stack.getCount());
        int paid = (int) Math.floor(unit * count);
        if (paid <= 0) {
            p.sendMessage(Text.translatable("rpg.europa.market.too_little", (int) Math.ceil(1 / unit)).formatted(Formatting.GRAY), true);
            return 0;
        }
        MarketState state = MarketState.get(p.getServer());
        state.pressure.merge(Registries.ITEM.getId(stack.getItem()).toString(), (double) paid, Double::sum);
        state.markDirty();
        stack.decrement(count);
        give(p, paid);
        p.sendMessage(Text.translatable("rpg.europa.market.sold", count, paid).formatted(Formatting.GREEN), true);
        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.PLAYERS, 0.7f, 1.2f);
        return paid;
    }

    public static int discountedPrice(PlayerRpgData d, Offer o) {
        if (o.faction == null) return o.price;
        int rank = Factions.rank(Factions.rep(d, o.faction));
        return (int) Math.ceil(o.price * (1 - 0.05 * Math.max(0, rank - o.minRank)));
    }

    public static boolean buy(ServerPlayerEntity p, String offerId) {
        Offer o = offer(offerId);
        if (o == null) return false;
        PlayerRpgData d = RpgAttachments.get(p);
        if (o.faction != null && Factions.rank(Factions.rep(d, o.faction)) < o.minRank) {
            p.sendMessage(Text.translatable("rpg.europa.market.rank_needed", Text.translatable(Factions.rankKey(o.minRank))).formatted(Formatting.RED));
            return false;
        }
        int price = discountedPrice(d, o);
        if (balance(p) < price) {
            p.sendMessage(Text.translatable("rpg.europa.market.no_money", price, balance(p)).formatted(Formatting.RED));
            return false;
        }
        take(p, price);
        ItemStack item = new ItemStack(Registries.ITEM.get(o.item), o.count);
        if (!p.getInventory().insertStack(item)) p.dropItem(item, false);
        p.sendMessage(Text.translatable("rpg.europa.market.bought", o.count, item.getName(), price).formatted(Formatting.GREEN));
        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.7f, 1.0f);
        return true;
    }

    // --------------------------------------------------------------- moeda (itens)

    public static int balance(ServerPlayerEntity p) {
        PlayerInventory inv = p.getInventory();
        return inv.count(ModItems.ASTER_FRAGMENT) + 9 * inv.count(ModItems.ASTER_PRISM);
    }

    public static void give(ServerPlayerEntity p, int fragments) {
        int prisms = fragments / 9, rest = fragments % 9;
        insert(p, ModItems.ASTER_PRISM, prisms);
        insert(p, ModItems.ASTER_FRAGMENT, rest);
    }

    private static void insert(ServerPlayerEntity p, Item item, int count) {
        while (count > 0) {
            int n = Math.min(count, 64);
            ItemStack s = new ItemStack(item, n);
            if (!p.getInventory().insertStack(s)) p.dropItem(s, false);
            count -= n;
        }
    }

    /** Remove o valor (fragmentos primeiro, depois prismas, com troco). Assume saldo suficiente. */
    public static void take(ServerPlayerEntity p, int amount) {
        PlayerInventory inv = p.getInventory();
        amount -= remove(inv, ModItems.ASTER_FRAGMENT, amount);
        if (amount > 0) {
            int prisms = (amount + 8) / 9;
            remove(inv, ModItems.ASTER_PRISM, prisms);
            int change = prisms * 9 - amount;
            if (change > 0) insert(p, ModItems.ASTER_FRAGMENT, change);
        }
    }

    private static int remove(PlayerInventory inv, Item item, int max) {
        int removed = 0;
        for (int i = 0; i < inv.size() && removed < max; i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(item)) {
                int n = Math.min(max - removed, s.getCount());
                s.decrement(n);
                removed += n;
            }
        }
        return removed;
    }

    // --------------------------------------------------------------- interface (chat)

    public static void showShop(ServerPlayerEntity p) {
        PlayerRpgData d = RpgAttachments.get(p);
        p.sendMessage(Text.translatable("rpg.europa.market.header", balance(p)).formatted(Formatting.GOLD, Formatting.BOLD));
        String lastFaction = "";
        for (Offer o : OFFERS) {
            String group = o.faction == null ? "" : o.faction;
            if (!group.equals(lastFaction)) {
                Factions.Faction f = Factions.byId(group);
                p.sendMessage(Text.literal(" ").append(Text.translatable(f.translationKey()).formatted(f.color(), Formatting.UNDERLINE))
                        .append(Text.literal(" — ").formatted(Formatting.DARK_GRAY))
                        .append(Text.translatable(Factions.rankKey(Factions.rank(Factions.rep(d, group)))).formatted(Formatting.GRAY)));
                lastFaction = group;
            }
            Item item = Registries.ITEM.get(o.item);
            int price = discountedPrice(d, o);
            boolean locked = o.faction != null && Factions.rank(Factions.rep(d, o.faction)) < o.minRank;
            MutableText line = Text.literal("  " + o.count + "x ").append(item.getName()).append(Text.literal(" — " + price + " ✦").formatted(Formatting.YELLOW));
            if (locked) {
                line.append(Text.literal(" ").append(Text.translatable("rpg.europa.market.locked", Text.translatable(Factions.rankKey(o.minRank)))).formatted(Formatting.DARK_GRAY));
            } else {
                line.append(" ").append(Messages.button(Text.translatable("rpg.europa.market.buy"), "/europa shop buy " + o.id,
                        Text.translatable("rpg.europa.market.buy_hover", price), Formatting.GREEN));
            }
            p.sendMessage(line);
        }
        p.sendMessage(Text.translatable("rpg.europa.market.sell_hint").formatted(Formatting.GRAY));
    }

    public static void showPrice(ServerPlayerEntity p, ItemStack s) {
        double price = currentPrice(p, s);
        Double base = basePrice(s);
        if (base == null) {
            p.sendMessage(Text.translatable("rpg.europa.market.not_buying").formatted(Formatting.GRAY));
            return;
        }
        p.sendMessage(Text.translatable("rpg.europa.market.price", s.getName(), String.format(Locale.ROOT, "%.2f", price),
                String.format(Locale.ROOT, "%.2f", base)).formatted(Formatting.YELLOW));
    }
}
