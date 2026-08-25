package me.gosdev.chatpointsttv.Spigot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTVSpigot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    public void sendTitle(String title, String sub) {
        player.sendTitle(title, sub, 10, 70, 20);
    }

    @Override
    public void giveItem(String item, Integer amount) {
        Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> { // Inventory must be handled synchronously
            ItemStack stack = new ItemStack(Material.valueOf(item), amount);
            player.getInventory().addItem(stack);
        });
    }

    @Override
    public void giveEffect(String effect, Integer duration, Integer strength) {
        if (effect == null) {
            for (PotionEffect e : player.getActivePotionEffects()) {
                Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
                    player.removePotionEffect(e.getType());
                });
            }
        } else {
            Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
                try {
                    player.addPotionEffect(PotionEffectType.getByName(effect).createEffect(duration * 20, strength));
                } catch (NullPointerException e) {
                    ChatPointsTTV.log.error("Unknown Potion Effect: " + effect);
                }
            });
        }
    }

    @Override
    public void clearEffects() {
        Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
            for (PotionEffect e : player.getActivePotionEffects()) {
                player.removePotionEffect(e.getType());
            }
        });
    }

    @Override
    public void freeze(Integer seconds) {
        new Thread(() -> {
            Boolean allowFlightState = player.getAllowFlight();
            SpigotListeners.frozenPlayers.put(player, player.getLocation());

            Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
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

            Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
                player.setAllowFlight(allowFlightState);
                player.setFlying(false);
                player.setFlySpeed(0.1f);
            });
        }).start();
    }

    @Override
    public void removeItem(int slot) {
        Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
            player.getInventory().setItem(slot, new ItemStack(Material.AIR));
        });
    }

    @Override
    public void exchangeSlots(int slot1, int slot2) {
        Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
            ItemStack itemstack1 = player.getInventory().getItem(slot1);
            ItemStack itemstack2 = player.getInventory().getItem(slot2);
            player.getInventory().setItem(slot1, itemstack2);
            player.getInventory().setItem(slot2, itemstack1);
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
        Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> { // Entities should only be spawned synchronously
            TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation(), EntityType.PRIMED_TNT);
            tnt.setFuseTicks(fuseTime);
        });
    }

    @Override
    public void playSound(String sound) {
        Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
            player.playSound(player.getLocation(), sound.toLowerCase(), 10, 1);
        });
    }
}
