package me.orlaando.irongolem.queue;

import org.jetbrains.annotations.NotNull;

public abstract class QueueProvider {

    public static QueueProvider of(@NotNull final Class<? extends @NotNull LocalBlockQueue> primary,
        final Class<? extends LocalBlockQueue> fallback) {
        return new QueueProvider() {

            private boolean failed = false;

            @Override public LocalBlockQueue getNewQueue(@NotNull final String world) {
                if (!failed) {
                    try {
                        return (LocalBlockQueue) primary.getConstructors()[0].newInstance(world);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        failed = true;
                    }
                }
                try {
                    return (LocalBlockQueue) fallback.getConstructors()[0].newInstance(world);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public abstract LocalBlockQueue getNewQueue(@NotNull final String world);

}
