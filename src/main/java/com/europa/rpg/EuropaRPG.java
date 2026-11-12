package com.europa.rpg;

import com.europa.rpg.command.RpgCommand;
import com.europa.rpg.entity.CatacombLordEntity;
import com.europa.rpg.network.ModNetworking;
import com.europa.rpg.quest.QuestManager;
import com.europa.rpg.entity.ServantEntity;
import com.europa.rpg.profession.Professions;
import com.europa.rpg.registry.ModArmorMaterials;
import com.europa.rpg.rpg.CombatHooks;
import com.europa.rpg.registry.ModBlocks;
import com.europa.rpg.registry.ModEntities;
import com.europa.rpg.registry.ModItemGroups;
import com.europa.rpg.registry.ModItems;
import com.europa.rpg.rpg.Messages;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgAttachments;
import com.europa.rpg.util.TaskScheduler;
import com.europa.rpg.world.ModStructures;
import com.europa.rpg.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EuropaRPG implements ModInitializer {
    public static final String MOD_ID = "europa";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        RpgAttachments.init();
        com.europa.rpg.content.ClassCatalog.init();
        com.europa.rpg.race.Races.init();
        ModEntities.init();
        ModArmorMaterials.init();
        ModBlocks.init();
        ModItems.init();
        ModItemGroups.init();
        ModStructures.init();
        ModWorldGen.init();
        ModNetworking.init();
        CombatHooks.init();
        Professions.init();
        com.europa.rpg.ability.Summons.init();
        com.europa.rpg.item.RollRarityLootFunction.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> RpgCommand.register(dispatcher));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TaskScheduler.tick(server);
            long now = server.getOverworld().getTime();
            if (now % 1200 == 0) com.europa.rpg.economy.MarketState.get(server).decay();
            if (now % 200 == 0) CombatHooks.cleanupMarks(now);
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) Progression.tick(p, now);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> TaskScheduler.clear());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity p = handler.getPlayer();
            Progression.applyAttributes(p);
            Progression.sync(p);
            Progression.fullSync(p, true);
            if (!RpgAttachments.get(p).hasClass()) Messages.sendClassChoice(p);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            Progression.forget(handler.getPlayer());
            com.europa.rpg.race.Races.forget(handler.getPlayer());
            Professions.forget(handler.getPlayer());
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            Progression.applyAttributes(newPlayer);
            newPlayer.setHealth(newPlayer.getMaxHealth());
            Progression.sync(newPlayer);
            Progression.fullSync(newPlayer, true);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayerEntity || entity instanceof ServantEntity) return;
            if (entity instanceof CatacombLordEntity) {
                // Recompensa compartilhada: todos os jogadores próximos recebem XP e progresso.
                for (ServerPlayerEntity p : entity.getWorld().getEntitiesByClass(ServerPlayerEntity.class,
                        entity.getBoundingBox().expand(32), x -> true)) {
                    Progression.addXp(p, 1500);
                    QuestManager.onKill(p, entity);
                    com.europa.rpg.economy.Factions.addRep(p, "wanderers", 300);
                    com.europa.rpg.economy.Factions.addRep(p, "nameless", 100);
                }
                return;
            }
            ServerPlayerEntity killer = resolveKiller(source.getAttacker());
            if (killer == null) return;
            Progression.addXp(killer, xpFor(entity));
            QuestManager.onKill(killer, entity);
            Professions.onKill(killer, entity);
            com.europa.rpg.item.GearProgress.onKill(killer, entity.getMaxHealth());
            Progression.gain(killer, com.europa.rpg.rpg.Resource.SPIRIT, 10f);
            if (entity.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD)) Progression.gain(killer, com.europa.rpg.rpg.Resource.FAITH, 5f);
        });

        LOGGER.info("Europa RPG carregado.");
    }

    private static ServerPlayerEntity resolveKiller(Entity attacker) {
        if (attacker instanceof ServerPlayerEntity p) return p;
        if (attacker instanceof TameableEntity t && t.getOwner() instanceof ServerPlayerEntity p) return p;
        if (attacker instanceof ServantEntity s && s.getOwner() instanceof ServerPlayerEntity p) return p;
        return null;
    }

    private static int xpFor(LivingEntity e) {
        int base = Math.max(1, Math.round(e.getMaxHealth() / 2f));
        return e instanceof Monster ? base * 2 : base / 2;
    }
}
