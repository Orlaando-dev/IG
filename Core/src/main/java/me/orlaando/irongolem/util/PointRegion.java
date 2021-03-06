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

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * A region consisting of one block only
 */
public class PointRegion extends CuboidRegion {

    protected PointRegion(@NotNull final Vector vector) {
        super(vector, vector);
    }

    public static PointRegion at(@NotNull final Vector position) {
        return new PointRegion(position);
    }

}
