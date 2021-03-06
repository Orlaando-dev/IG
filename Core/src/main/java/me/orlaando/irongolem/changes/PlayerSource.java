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

package me.orlaando.irongolem.changes;

import com.google.common.base.Preconditions;
import me.orlaando.irongolem.players.IGPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Used when a player (with a UUID) is the source of an event
 */
public class PlayerSource extends ChangeSource {

    private final UUID uuid;

    private PlayerSource(@NotNull final Player player) {
        this(player.getUniqueId());
    }

    private PlayerSource(@NotNull final UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Create a new player source
     *
     * @param player Player
     * @return Created source
     */
    @NotNull public static PlayerSource of(@NotNull final Player player) {
        return new PlayerSource(Preconditions.checkNotNull(player, "Player not be null"));
    }

    /**
     * Create a new player source
     *
     * @param player Player
     * @return Created source
     */
    @NotNull public static PlayerSource of(@NotNull final IGPlayer player) {
        return new PlayerSource(Preconditions.checkNotNull(player.getPlayer(), "Player not be null"));
    }

    /**
     * Create a new player source
     *
     * @param uuid Player UUID
     * @return Created source
     */
    @NotNull public static PlayerSource of(@NotNull final UUID uuid) {
        return new PlayerSource(Preconditions.checkNotNull(uuid, "UUID may not be null"));
    }

    @Override public String getName() {
        return this.uuid.toString();
    }

}
