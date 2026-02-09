package me.gosdev.chatpointsttv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.gosdev.chatpointsttv.Chat.SpigotSender;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;

public class SpigotPlayer extends SpigotSender implements GenericPlayer {
    public SpigotPlayer(Player player) {
        super(player);
        this.player = player;
    }

    private final Player player;

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public Boolean isOnline() {
        return player.isOnline();
    }


    @Override
    public void spawnEntity(String entity, String name, Boolean glow) {
        Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> { // Entities should only be spawned synchronously
            try {
                Entity e = player.getWorld().spawnEntity(player.getLocation(), EntityType.valueOf(entity));
                e.setGlowing(glow);
                if (name != null) {
                    e.setCustomName(name);
                    e.setCustomNameVisible(true);
                }

            } catch (IllegalArgumentException e) {
                ChatPointsTTV.log.warn("Entity " + entity + " does not exist.");
            }
        });
    }
    
    @Override
    public void sendTitle(String title, String sub) {
        player.sendTitle(title, sub, 10, 70, 20);
    }

    @Override
    public boolean hasPermission(ChatPointsTTV.permissions perm) {
        return player.hasPermission(perm.name());
    }

    @Override
    public void giveItem(String item, Integer amount) {
        Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> { // Inventory must be handled synchronously
            ItemStack stack = new ItemStack(Material.valueOf(item), amount);
            player.getInventory().addItem(stack);
        });
    }

    @Override
    public void giveEffect(String effect, Integer duration, Integer strength) {
        if (effect == null) {
            for (PotionEffect e : player.getActivePotionEffects()) {
                Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
                    player.removePotionEffect(e.getType());
                });
            }
        } else {
            Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
                try {
                    player.addPotionEffect(PotionEffectType.getByName(effect).createEffect(duration * 20, strength));
                } catch (NullPointerException e) {
                    ChatPointsTTV.log.error("Unknown Potion Effect: " + effect);
                }
            });
        }
    }

    @Override
    public void freeze(Integer seconds) {
        new Thread(() -> {
            Boolean allowFlightState = player.getAllowFlight();
            SpigotListeners.frozenPlayers.put(player, player.getLocation());

            Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
                player.setAllowFlight(true);
                player.teleport(player.getLocation().add(0,0.001,0));
                player.setFlying(true);
                player.setFlySpeed(0);
            });

            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException ex) {
            }

            SpigotListeners.frozenPlayers.remove(player);

            Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
                player.setAllowFlight(allowFlightState);
                player.setFlying(false);
                player.setFlySpeed(0.1f);
            });
        }).start();
    }

    @Override
    public void removeItem(int slot) {
        Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
            player.getInventory().setItem(slot, new ItemStack(Material.AIR));
        });
    }

    @Override
    public void exchangeSlots(int slot1, int slot2) {
        Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
            ItemStack itemstack1 = player.getInventory().getItem(slot1);
            ItemStack itemstack2 = player.getInventory().getItem(slot2);
            player.getInventory().setItem(slot1, itemstack1);
            player.getInventory().setItem(slot2, itemstack2);
        });
        
    }

    @Override
    public int getInvSlots() {
        return player.getInventory().getSize();
    }

    @Override
    public Integer getHandSlot() {
        return player.getInventory().getHeldItemSlot();
    }

    @Override
    public Boolean hasItem(int slot) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null) return false;
        return !item.getType().equals(Material.AIR);
    }

    @Override
    public void spawnTnt(Integer fuseTime) {
        Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> { // Entities should only be spawned synchronously
            TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation(), EntityType.PRIMED_TNT);
            tnt.setFuseTicks(fuseTime);
        });
    }

    @Override
    public void playSound(String sound) {
        Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
            player.playSound(player.getLocation(), sound.toLowerCase(), 10, 1);
        });
    }

    @Override
    public void shuffleInventory() {
        ArrayList<ItemStack> inv = new ArrayList<>(Arrays.asList(player.getInventory().getStorageContents()));
        inv.add(player.getInventory().getItemInOffHand());

        Collections.shuffle(inv);

        for (int i = 0; i < inv.size() - 1; i++) { // Don't include last item (offhand)
            player.getInventory().setItem(i, inv.get(i));
        }
        player.getInventory().setItemInOffHand(inv.get(inv.size() - 1));

    }
}
