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

import me.orlaando.irongolem.changes.ChangeSource;
import me.orlaando.irongolem.changes.PlayerSource;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Factory that produces {@link me.orlaando.irongolem.changes.ChangeSource}
 * from serialized strings
 */
public class SourceFactory {

    public ChangeSource getSource(@NotNull final String string) {
        if  (string.startsWith("__")) {
            // it's a special source
            return null; // TODO: Fix
        }
        return PlayerSource.of(UUID.fromString(string));
    }

}
