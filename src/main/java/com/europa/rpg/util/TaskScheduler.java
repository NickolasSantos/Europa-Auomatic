package com.europa.rpg.util;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Agendador simples de tarefas no thread do servidor (efeitos com atraso, invocações temporárias). */
public final class TaskScheduler {
    private record Task(long dueTick, Runnable action) {}

    private static final List<Task> TASKS = new ArrayList<>();
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
    }
}
