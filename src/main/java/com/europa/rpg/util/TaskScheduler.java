package com.europa.rpg.util;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Agendador simples de tarefas no thread do servidor (efeitos com atraso, invocações temporárias). */
public final class TaskScheduler {
    private record Task(long dueTick, Runnable action) {}

    private static final List<Task> TASKS = new ArrayList<>();
    private static final java.util.Set<java.util.UUID> TRACKED = new java.util.HashSet<>();
    private static long tick;

    private TaskScheduler() {}

    public static void schedule(int delayTicks, Runnable action) {
        TASKS.add(new Task(tick + Math.max(1, delayTicks), action));
    }

    public static void tick(MinecraftServer server) {
        tick++;
        if (TASKS.isEmpty()) return;
        List<Task> due = new ArrayList<>();
        for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
            Task t = it.next();
            if (t.dueTick <= tick) {
                due.add(t);
                it.remove();
            }
        }
        due.forEach(t -> t.action.run());
    }

    public static void clear() {
        TASKS.clear();
        TRACKED.clear();
    }

    /** Entidades temporárias cujo fim está agendado (para descartá-las se forem recarregadas sem agendamento). */
    public static void track(java.util.UUID id) { TRACKED.add(id); }

    public static void untrack(java.util.UUID id) { TRACKED.remove(id); }

    public static boolean isTracked(java.util.UUID id) { return TRACKED.contains(id); }
}
