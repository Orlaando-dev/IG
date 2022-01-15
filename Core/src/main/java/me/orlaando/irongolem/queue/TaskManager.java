package me.orlaando.irongolem.queue;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TaskManager {

    public static TaskManager IMP;

    public static void runTaskRepeat(@Nullable final Runnable runnable, final int interval) {
        if (runnable != null) {
            if (IMP == null) {
                throw new IllegalArgumentException("disabled");
            }
            IMP.taskRepeat(runnable, interval);
        }
    }

    public <T> T sync(@NotNull final RunnableVal<T> function) {
        return sync(function, Integer.MAX_VALUE);
    }

    public <T> T sync(@NotNull final RunnableVal<T> function, final int timeout) {
        if (Bukkit.isPrimaryThread()) {
            function.run();
            return function.value;
        }
        final AtomicBoolean running = new AtomicBoolean(true);
        final RuntimeExceptionRunnableVal<T> run =
            new RuntimeExceptionRunnableVal<>(function, running);
        TaskManager.IMP.task(run);
        try {
            synchronized (function) {
                while (running.get()) {
                    function.wait(timeout);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (run.value != null) {
            throw run.value;
        }
        return function.value;
    }

    public abstract void taskRepeat(@NotNull final Runnable runnable, final int interval);

    public abstract void task(@NotNull final Runnable runnable);

}
