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

package me.orlaando.irongolem.util;

import me.orlaando.irongolem.changes.BlockSubject;
import me.orlaando.irongolem.changes.ChangeReason;
import me.orlaando.irongolem.changes.ChangeSubject;
import me.orlaando.irongolem.changes.ChangeType;
import me.orlaando.irongolem.changes.RestorationSubject;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory that procudes {@link me.orlaando.irongolem.changes.ChangeSubject}
 * from serialized strings
 */
public class SubjectFactory {

    /**
     * Get a subject from three serialized strings
     *
     * @param type Subject type
     * @param from Serialized from state
     * @param to   Serialized to state
     * @return The subject, or null if it couldn't be created
     */
    @Nullable public ChangeSubject<?, ?> getSubject(@NotNull final ChangeReason reason, @NotNull final String type,
        @NotNull final String from, @NotNull final String to, @NotNull final byte[] oldState, @NotNull final byte[] newState) {
        final ChangeType changeType = ChangeType.valueOf(type);
        if (reason == ChangeReason.RESTORATION) {
            return RestorationSubject.of(changeType, Integer.parseInt(to));
        } else {
            if (changeType == ChangeType.BLOCK) {
                return BlockSubject.of(BlockWrapper.of(Bukkit.createBlockData(from), NBTUtils.bytesToCompound(oldState)),
                    BlockWrapper.of(Bukkit.createBlockData(to), NBTUtils.bytesToCompound(newState)));
            }
        }
        return null;
    }

}
