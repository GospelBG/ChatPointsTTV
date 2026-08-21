package me.gosdev.chatpointsttv.Balm;

import me.gosdev.chatpointsttv.ConsoleSender;
import me.gosdev.chatpointsttv.Generic.GenericLoader;
import me.gosdev.chatpointsttv.Generic.GenericLogger;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import net.blay09.mods.balm.Balm;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class BalmLoader implements GenericLoader {
    private final BalmLog logger = new BalmLog();
    private final ConsoleSender console = new ConsoleSender();

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final HashMap<Integer, ScheduledFuture> tasks = new HashMap<>();

    @Override
    public String getVersion() {
        return Balm.platform().server().getServerVersion();
    }

    @Override
    public GenericLogger getLogger() {
        return logger;
    }

    @Override
    public List<GenericPlayer> getOnlinePlayers() {
        List <GenericPlayer> players = new ArrayList<>();
        for (ServerPlayer i : Balm.platform().server().getPlayerList().getPlayers()) {
            players.add((new BalmPlayer((i))));
        }
        return players;
    }

    @Override
    public GenericSender consoleSender() {
        return console;
    }

    @Override
    public GenericPlayer getPlayer(String username) {
        return new BalmPlayer(Balm.platform().server().getPlayerList().getPlayer(username));
    }

    @Override
    public List<String> getSounds() {
        return BuiltInRegistries.SOUND_EVENT.keySet().stream().map(Identifier::toString).toList();
    }

    @Override
    public List<String> getEntities() {
        return BuiltInRegistries.ENTITY_TYPE.keySet().stream().map(Identifier::toString).toList();
    }

    @Override
    public List<String> getItems() {
        return BuiltInRegistries.ITEM.keySet().stream().map(Identifier::toString).toList();
    }

    @Override
    public List<String> getPotionEffects() {
        return BuiltInRegistries.POTION.keySet().stream().map(Identifier::toString).toList();
    }

    @Override
    public void runTask(Runnable task) {
        Balm.getRuntime().platform().server().execute(task);
    }

    @Override
    public int scheduleSyncRepeatingTask(Runnable task, int delay, int period) {
        tasks.put(tasks.size(), executor.scheduleAtFixedRate(task, delay, period, TimeUnit.SECONDS));
        return tasks.size() - 1;
    }

    @Override
    public void cancelTask(int taskId) {
        tasks.get(taskId).cancel(true);
    }

    @Override
    public void spawnEntity(GenericPlayer target, String entity, boolean shouldGlow, String name) {
        Balm.getRuntime().platform().server().execute(() -> {
            Player balmPlayer = ((BalmPlayer) target).player;
            ServerLevel level = (ServerLevel) balmPlayer.level();

            Entity e = EntityType.byString(entity).get().create(level, EntitySpawnReason.COMMAND);
            e.setPos(balmPlayer.getX(), balmPlayer.getY(), balmPlayer.getZ());

            e.setGlowingTag(shouldGlow);
            if (name != null) {
                e.setCustomName(Component.literal(name));
            }

            level.addFreshEntity(e);
        });
    }
}
