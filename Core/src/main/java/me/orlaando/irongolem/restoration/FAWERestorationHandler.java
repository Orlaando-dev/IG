//
// IronGolem - A Minecraft block logging plugin
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package me.orlaando.irongolem.restoration;

import com.boydti.fawe.util.EditSessionBuilder;
import me.orlaando.irongolem.IronGolem;
import me.orlaando.irongolem.changes.BlockSubject;
import me.orlaando.irongolem.changes.Change;
import me.orlaando.irongolem.changes.ChangeSource;
import me.orlaando.irongolem.changes.ChangeSubject;
import me.orlaando.irongolem.changes.ChangeType;
import me.orlaando.irongolem.changes.Changes;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class FAWERestorationHandler implements RestorationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FAWERestorationHandler.class);

    private final Object regionLock = new Object();
    private final IronGolem ironGolem;
    private final Collection<me.orlaando.irongolem.util.CuboidRegion> regions;

    public FAWERestorationHandler(@NotNull final IronGolem ironGolem) {
        this.ironGolem = ironGolem;
        this.regions = new HashSet<>();
    }

    @Override
    public void restore(@NotNull final Changes changes, @NotNull final ChangeSource source,
        @NotNull final Runnable completionTask) throws RegionLockedException {
        if (!changes.isDistinct()) {
            throw new IllegalArgumentException("Only distinct change sets can be restored to");
        }
        if (!this.createRegionLock(changes.getRegion())) {
            throw new RegionLockedException(changes.getRegion());
        }
        Bukkit.getScheduler().runTaskAsynchronously(ironGolem, () -> {
            final Collection<Change> restorationChanges = changes.getRestorationChangeSet(source);
            try {
                final com.sk89q.worldedit.world.World weWorld =
                    BukkitAdapter.adapt(changes.getWorld());
                final EditSession session =
                    new EditSessionBuilder(weWorld).checkMemory(false).fastmode(true)
                        .limitUnlimited().changeSetNull().autoQueue(false).build();

                for (final Change change : changes.getChanges()) {
                    final BlockVector3 location = BukkitAdapter.asBlockVector(change.getLocation());
                    final ChangeSubject<?, ?> subject = change.getSubject();
                    if (subject.getType() != ChangeType.BLOCK) {
                        continue;
                    }
                    final BlockSubject blockSubject = (BlockSubject) subject;
                    final BaseBlock block = blockSubject.getFromFull();
                    session.setBlock(location, block);
                }

                /*
                final long start = System.currentTimeMillis();
                final CuboidRegion region = convertRegion(changes.getRegion());
                final long start1_1 = System.currentTimeMillis();
                final ChangeExtent changeExtent = new ChangeExtent(weWorld, region, changes.getChanges());
                final long start1_2 = System.currentTimeMillis();
                final ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(changeExtent, region, session, region.getMinimumPoint());
                forwardExtentCopy.setCopyingEntities(false);
                forwardExtentCopy.setCopyingBiomes(false);
                forwardExtentCopy.setRemovingEntities(false);
                // Copy the blocks over to the edit session
                try {
                    Operations.complete(forwardExtentCopy);
                } catch (final WorldEditException e) {
                    LOGGER.error("Failed to restore region", e);
                }
                final long start2 = System.currentTimeMillis();
                 */
                // Persist the changes
                session.flushSession();

                // Inform the client
                completionTask.run();
                // Log the restoration
                ironGolem.getChangeLogger().logChanges(restorationChanges);
            } catch (final Exception e) {
                LOGGER.error("Failed to restore region", e);
            } finally {
                this.freeRegion(changes.getRegion());
            }
        });
    }

    @Override public boolean createRegionLock(
        @NotNull final me.orlaando.irongolem.util.CuboidRegion region) {
        synchronized (this.regionLock) {
            for (final me.orlaando.irongolem.util.CuboidRegion lockingRegion : this.regions) {
                if (lockingRegion.intersects(region)) {
                    return false;
                }
            }
            this.regions.add(region);
        }
        return true;
    }

    @Override public void freeRegion(
        @NotNull final me.orlaando.irongolem.util.CuboidRegion region) {
        synchronized (this.regionLock) {
            this.regions.remove(region);
        }
    }

    private static CuboidRegion convertRegion(
        @NotNull final me.orlaando.irongolem.util.CuboidRegion region) {
        final Vector minPoint = region.getMinimumPoint();
        final Vector maxPoint = region.getMaximumPoint();
        return new CuboidRegion(
            BlockVector3.at(minPoint.getBlockX(), minPoint.getBlockY(), minPoint.getBlockZ()),
            BlockVector3.at(maxPoint.getBlockX(), maxPoint.getBlockY(), maxPoint.getBlockZ()));
    }

    private static final class ChangeExtent implements Extent {

        private final CuboidRegion region;
        private final World world;
        private final BaseBlock[][][] blocks;

        private ChangeExtent(final World world, final CuboidRegion region,
            final Collection<Change> changes) {
            this.region = region;
            this.world = world;

            this.blocks = new BaseBlock[region.getWidth()][region.getHeight()][region.getLength()];

            for (final Change change : changes) {
                final BlockVector3 location = BukkitAdapter.asBlockVector(change.getLocation());
                final ChangeSubject<?, ?> subject = change.getSubject();
                if (subject.getType() != ChangeType.BLOCK) {
                    continue;
                }
                final BlockSubject blockSubject = (BlockSubject) subject;
                final BaseBlock block = blockSubject.getFromFull();
                this.blocks[location.getBlockX() - region.getMinimumPoint().getBlockX()][
                    location.getBlockY() - region.getMinimumPoint().getBlockY()][
                    location.getBlockZ() - region.getMinimumPoint().getBlockZ()] = block;
            }
        }

        @Override public BlockVector3 getMinimumPoint() {
            return this.region.getMinimumPoint();
        }

        @Override public BlockVector3 getMaximumPoint() {
            return this.region.getMaximumPoint();
        }

        @Override public List<? extends Entity> getEntities(final Region region) {
            return Collections.emptyList();
        }

        @Override public List<? extends Entity> getEntities() {
            return Collections.emptyList();
        }

        @Nullable @Override public Entity createEntity(Location location, BaseEntity entity) {
            return null;
        }

        @Override public BlockState getBlock(final BlockVector3 position) {
            return this.getFullBlock(position).toImmutableState();
        }

        @Override public BaseBlock getFullBlock(BlockVector3 location) {
            final BaseBlock block =
                this.blocks[location.getBlockX() - region.getMinimumPoint().getBlockX()][
                    location.getBlockY() - region.getMinimumPoint().getBlockY()][
                    location.getBlockZ() - region.getMinimumPoint().getBlockZ()];
            if (block != null) {
                return block;
            }
            return world.getFullBlock(location);
        }

        @Override public BiomeType getBiome(BlockVector2 position) {
            return BiomeTypes.OCEAN;
        }

        @Override
        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) {
            return false;
        }

        @Override public boolean setBiome(BlockVector2 position, BiomeType biome) {
            return false;
        }

        @Nullable @Override public Operation commit() {
            return null;
        }
    }

}
