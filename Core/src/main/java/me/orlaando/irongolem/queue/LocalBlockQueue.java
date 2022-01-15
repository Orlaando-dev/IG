package me.orlaando.irongolem.queue;

import com.sk89q.worldedit.world.block.BaseBlock;
import org.jetbrains.annotations.NotNull;

public abstract class LocalBlockQueue {

    public abstract boolean next();

    public abstract int size();

    public abstract long getModified();

    public abstract void setModified(final long modified);

    /**
     * Sets the block at the coordinates provided to the given id.
     *
     * @param x    the x coordinate from from 0 to 15 inclusive
     * @param y    the y coordinate from from 0 (inclusive) - maxHeight(exclusive)
     * @param z    the z coordinate from 0 to 15 inclusive
     * @param data the data to set the block to
     */
    public abstract void setBlock(final int x, final int y, final int z,
        @NotNull final BaseBlock data);

    public abstract String getWorld();

    public void enqueue() {
        GlobalBlockQueue.IMP.enqueue(this);
    }

}
