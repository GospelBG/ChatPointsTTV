package me.gosdev.chatpointsttv.Actions;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;
import me.gosdev.chatpointsttv.Utils.EquipmentParser.ItemData;

public class SpawnAction extends BaseAction {
    private EntityType entity;
    private Integer amount;
    private String name;
    private Player player;
    private boolean shouldGlow;
    private ItemData weapon;
    private ItemData helmet;
    private ItemData chestplate;
    private ItemData leggings;
    private ItemData boots;

    public SpawnAction(EntityType entity, String chatter, Optional<Integer> amount, Player target, Boolean shouldGlow,
                      ItemData weapon, ItemData helmet, ItemData chestplate, ItemData leggings, ItemData boots) {
        this.entity = entity;
        this.name = chatter;
        this.amount = amount.orElse(1);
        this.player = target;
        this.shouldGlow = shouldGlow;
        this.weapon = weapon;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    @Override 
    public void run() {
        for (int i = 0; i < amount; i++) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (player != null) { // Is targeting a specific player?
                    if (!player.isOnline()) {
                        ChatPointsTTV.log.warning("Couldn't find player " + player.getDisplayName() + ".");
                        return;
                    }
                } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;
        
                Bukkit.getScheduler().scheduleSyncDelayedTask(ChatPointsTTV.getPlugin(), () -> { // Entities should only be spawned synchronously
                    Entity e = p.getWorld().spawnEntity(p.getLocation(), entity);
                    e.setGlowing(shouldGlow);
                    if (name != null) {
                        e.setCustomName(name);
                        e.setCustomNameVisible(true);
                    }

                    // Equip entity with items if it's a LivingEntity
                    if (e instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) e;
                        EntityEquipment equipment = living.getEquipment();

                        if (equipment != null) {
                            if (weapon != null) {
                                ItemStack weaponItem = new ItemStack(weapon.material);
                                weapon.enchantments.forEach((ench, level) -> weaponItem.addUnsafeEnchantment(ench, level));
                                equipment.setItemInMainHand(weaponItem);
                            }
                            if (helmet != null) {
                                ItemStack helmetItem = new ItemStack(helmet.material);
                                helmet.enchantments.forEach((ench, level) -> helmetItem.addUnsafeEnchantment(ench, level));
                                equipment.setHelmet(helmetItem);
                            }
                            if (chestplate != null) {
                                ItemStack chestplateItem = new ItemStack(chestplate.material);
                                chestplate.enchantments.forEach((ench, level) -> chestplateItem.addUnsafeEnchantment(ench, level));
                                equipment.setChestplate(chestplateItem);
                            }
                            if (leggings != null) {
                                ItemStack leggingsItem = new ItemStack(leggings.material);
                                leggings.enchantments.forEach((ench, level) -> leggingsItem.addUnsafeEnchantment(ench, level));
                                equipment.setLeggings(leggingsItem);
                            }
                            if (boots != null) {
                                ItemStack bootsItem = new ItemStack(boots.material);
                                boots.enchantments.forEach((ench, level) -> bootsItem.addUnsafeEnchantment(ench, level));
                                equipment.setBoots(bootsItem);
                            }
                        }
                    }
                });
            }     
        }
    }
}