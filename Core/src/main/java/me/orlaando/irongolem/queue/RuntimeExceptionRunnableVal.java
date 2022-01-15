package me.orlaando.irongolem.queue;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class RuntimeExceptionRunnableVal<T> extends RunnableVal<RuntimeException> {

    private final RunnableVal<T> function;
    private final AtomicBoolean running;

    public RuntimeExceptionRunnableVal(@NotNull final RunnableVal<T> function, @NotNull final AtomicBoolean running) {
        this.function = function;
        this.running = running;
    }

    @Override public void run(@NotNull final RuntimeException value) {
        try {
            function.run();
        } catch (final RuntimeException e) {
            this.value = e;
        } catch (final Throwable neverHappens) {
            neverHappens.printStackTrace();
        } finally {
            running.set(false);
        }
        synchronized (function) {
            function.notifyAll();
        }
    }

}
