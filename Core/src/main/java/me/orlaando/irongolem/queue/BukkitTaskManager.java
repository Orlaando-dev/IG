package me.orlaando.irongolem.queue;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BukkitTaskManager extends TaskManager {

    private final Plugin bukkitMain;

    public BukkitTaskManager(@NotNull final Plugin bukkitMain) {
        this.bukkitMain = bukkitMain;
    }

    @Override public void taskRepeat(@NotNull final Runnable runnable, final int interval) {
        this.bukkitMain.getServer().getScheduler()
            .scheduleSyncRepeatingTask(this.bukkitMain, runnable, interval, interval);
    }

    @Override public void task(@NotNull final Runnable runnable) {
        this.bukkitMain.getServer().getScheduler().runTask(this.bukkitMain, runnable).getTaskId();
    }

}
