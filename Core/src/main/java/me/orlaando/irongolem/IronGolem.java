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

package me.orlaando.irongolem;

import me.orlaando.irongolem.commands.CommandManager;
import me.orlaando.irongolem.configuration.MessageHandler;
import me.orlaando.irongolem.listeners.BlockListener;
import me.orlaando.irongolem.listeners.InspectorListener;
import me.orlaando.irongolem.listeners.PlayerListener;
import me.orlaando.irongolem.logging.ChangeLogger;
import me.orlaando.irongolem.players.PlayerManager;
import me.orlaando.irongolem.queue.BukkitLocalQueue;
import me.orlaando.irongolem.restoration.FAWERestorationHandler;
import me.orlaando.irongolem.restoration.QueueRestorationHandler;
import me.orlaando.irongolem.restoration.RestorationHandler;
import me.orlaando.irongolem.storage.SQLiteLogger;
import me.orlaando.irongolem.util.UsernameMapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class IronGolem extends JavaPlugin implements IronGolemAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(IronGolem.class);

    private PlayerManager playerManager;
    private ChangeLogger changeLogger;
    private RestorationHandler restorationHandler;
    private UsernameMapper usernameMapper;
    private MessageHandler messageHandler;

    @Override public void onEnable() {
        this.messageHandler = new MessageHandler(this);
        if (!this.getDataFolder().exists() && !this.getDataFolder().mkdir()) {
            LOGGER.error("Failed to create data folder");
        }
        try {
            this.changeLogger = new SQLiteLogger(this, 20);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
            try {
                LOGGER.info("Using the FAWE restoration handler");
                this.restorationHandler = new FAWERestorationHandler(this);
            } catch (final Exception e) {
                LOGGER.error("Failed to initialize the FAWE restoration handler", e);
            }
        } else {
            try {
                LOGGER.info("Using the queueing restoration handler");
                this.restorationHandler = new QueueRestorationHandler(this, BukkitLocalQueue.class);
            } catch (final Exception e) {
                LOGGER.error("Failed to initialize the queueing restoration handler", e);
            }
        }
        try {
            this.usernameMapper = new UsernameMapper(this);
        } catch (final Exception e) {
            LOGGER.error("Failed to load username mapper", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            this.playerManager = new PlayerManager(this);
        } catch (final Exception e) {
            LOGGER.error("Failed to setup player manager", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (this.changeLogger == null || !this.changeLogger.startLogging()) {
            LOGGER.error("Failed to start change logger");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            Bukkit.getPluginManager().registerEvents(new BlockListener(this.changeLogger), this);
            Bukkit.getPluginManager().registerEvents(new InspectorListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
            Objects.requireNonNull(getCommand("irongolem")).setExecutor(new CommandManager(this));
        }
        Bukkit.getServicesManager()
            .register(IronGolemAPI.class, this, this, ServicePriority.Highest);
    }

    @NotNull public ChangeLogger getChangeLogger() {
        return this.changeLogger;
    }

    @NotNull @Override public RestorationHandler getRestorationHandler() {
        return this.restorationHandler;
    }

    @Override public void onDisable() {
        this.changeLogger.stopLogger();
    }

    @NotNull @Override public UsernameMapper getUsernameMapper() {
        return this.usernameMapper;
    }

    @NotNull @Override public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    @NotNull @Override public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

}
