package me.orlaando.irongolem.queue;

import me.orlaando.irongolem.restoration.QueueRestorationHandler;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class BukkitLocalQueue extends BasicLocalBlockQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(BukkitLocalQueue.class);

    public BukkitLocalQueue(String world) {
        super(world);
    }

    @Override public LocalChunk getLocalChunk(int x, int z) {
        return new BasicLocalChunk(this, x, z) {
        };
    }

    @Override public final void setComponents(LocalChunk lc) {
        setBaseBlocks(lc);
    }

    public void setBaseBlocks(LocalChunk localChunk) {
        World worldObj = Bukkit.getWorld(getWorld());
        BukkitWorld bukkitWorld = (BukkitWorld) BukkitAdapter.adapt(worldObj);

        if (worldObj == null) {
            throw new NullPointerException("World cannot be null.");
        }
        final Consumer<Chunk> chunkConsumer = chunk -> {
            try (final EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(bukkitWorld, -1)) {
                for (int layer = 0; layer < localChunk.baseblocks.length; layer++) {
                    BaseBlock[] blocksLayer = localChunk.baseblocks[layer];
                    if (blocksLayer != null) {
                        for (int j = 0; j < blocksLayer.length; j++) {
                            if (blocksLayer[j] != null) {
                                BaseBlock block = blocksLayer[j];
                                int x = (chunk.getX() << 4) + QueueRestorationHandler.x_loc[layer][j];
                                int y = QueueRestorationHandler.y_loc[layer][j];
                                int z = (chunk.getZ() << 4) + QueueRestorationHandler.z_loc[layer][j];
                                try {
                                    editSession.setBlock(BlockVector3.at(x, y, z), block);
                                } catch (final Exception e) {
                                    LOGGER.error("Failed to set block", e);
                                }
                            }
                        }
                    }
                }
            }
        };
        PaperLib.getChunkAtAsync(worldObj, localChunk.getX(), localChunk.getZ(), true)
            .thenAccept(chunkConsumer);
    }

}
