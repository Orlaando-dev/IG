package me.orlaando.irongolem.queue;

import me.orlaando.irongolem.restoration.QueueRestorationHandler;
import me.orlaando.irongolem.util.MathUtils;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;

public abstract class BasicLocalBlockQueue extends LocalBlockQueue {

    private final String world;
    private final ConcurrentHashMap<Long, LocalChunk> blockChunks = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<LocalChunk> chunks = new ConcurrentLinkedDeque<>();
    private long modified;
    private LocalChunk lastWrappedChunk;
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;

    public BasicLocalBlockQueue(String world) {
        this.world = world;
        this.modified = System.currentTimeMillis();
    }

    public abstract LocalChunk getLocalChunk(int x, int z);

    public abstract void setComponents(LocalChunk lc)
        throws ExecutionException, InterruptedException;

    @Override public final String getWorld() {
        return world;
    }

    @Override public final boolean next() {
        lastX = Integer.MIN_VALUE;
        lastZ = Integer.MIN_VALUE;
        try {
            if (this.blockChunks.size() == 0) {
                return false;
            }
            synchronized (blockChunks) {
                LocalChunk chunk = chunks.poll();
                if (chunk != null) {
                    blockChunks.remove(chunk.longHash());
                    return this.execute(chunk);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public final boolean execute(@NotNull LocalChunk lc)
        throws ExecutionException, InterruptedException {
        this.setComponents(lc);
        return true;
    }

    @Override public final int size() {
        return chunks.size();
    }

    @Override public final long getModified() {
        return modified;
    }

    @Override public final void setModified(final long modified) {
        this.modified = modified;
    }

    @Override public void setBlock(final int x, final int y, final int z, @NotNull final BaseBlock data) {
        if ((y > 255) || (y < 0)) {
            return;
        }
        int cx = x >> 4;
        int cz = z >> 4;
        if (cx != lastX || cz != lastZ) {
            lastX = cx;
            lastZ = cz;
            long pair = (long) (cx) << 32 | (cz) & 0xFFFFFFFFL;
            lastWrappedChunk = this.blockChunks.get(pair);
            if (lastWrappedChunk == null) {
                lastWrappedChunk = this.getLocalChunk(x >> 4, z >> 4);
                lastWrappedChunk.setBlock(x & 15, y, z & 15, data);
                LocalChunk previous = this.blockChunks.put(pair, lastWrappedChunk);
                if (previous == null) {
                    chunks.add(lastWrappedChunk);
                    return;
                }
                this.blockChunks.put(pair, previous);
                lastWrappedChunk = previous;
            }
        }
        lastWrappedChunk.setBlock(x & 15, y, z & 15, data);
    }

    public abstract static class LocalChunk {
        public final BasicLocalBlockQueue parent;
        public final int z;
        public final int x;

        public BaseBlock[][] baseblocks;

        public LocalChunk(BasicLocalBlockQueue parent, int x, int z) {
            this.parent = parent;
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public abstract void setBlock(final int x, final int y, final int z, final BaseBlock block);

        public long longHash() {
            return MathUtils.pairInt(x, z);
        }

        @Override public int hashCode() {
            return MathUtils.pair((short) x, (short) z);
        }
    }


    public static class BasicLocalChunk extends LocalChunk {

        public BasicLocalChunk(BasicLocalBlockQueue parent, int x, int z) {
            super(parent, x, z);
            baseblocks = new BaseBlock[16][];
        }

        @Override public void setBlock(int x, int y, int z, BaseBlock block) {
            this.setInternal(x, y, z, block);
        }

        private void setInternal(final int x, final int y, final int z, final BaseBlock baseBlock) {
            final int i = QueueRestorationHandler.CACHE_I[y][x][z];
            final int j = QueueRestorationHandler.CACHE_J[y][x][z];
            BaseBlock[] array = baseblocks[i];
            if (array == null) {
                array = (baseblocks[i] = new BaseBlock[4096]);
            }
            array[j] = baseBlock;
        }
    }
}
