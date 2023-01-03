package it.ohalee.minecraftgpt;

import com.google.common.cache.*;
import it.ohalee.minecraftgpt.command.ChatCommand;
import it.ohalee.minecraftgpt.handler.PlayerHandlers;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {

    public static Cache<Player, StringBuilder> CACHE;
    public static Cache<Player, Type> USER_TYPE;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        OpenAI.init(getConfig().getString("API_KEY"));

        CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .removalListener((RemovalListener<Player, StringBuilder>) notification -> {
                    if (notification.getKey() == null) {
                        return;
                    }
                    USER_TYPE.invalidate(notification.getKey());
                    if (notification.getCause() == RemovalCause.EXPIRED) {
                        notification.getKey().sendMessage(getConfig().getString("command.toggle.disabled").replace("&", "§"));
                    }
                }).build();
        USER_TYPE = CacheBuilder.newBuilder().build();

        getServer().getPluginManager().registerEvents(new PlayerHandlers(this), this);
        ChatCommand command = new ChatCommand(this);
        PluginCommand chatgpt = getCommand("chatgpt");
        chatgpt.setExecutor(command);
        chatgpt.setTabCompleter(command);

        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

}
