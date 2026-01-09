package me.gosdev.chatpointsttv.Utils;

import java.util.*;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;

import me.gosdev.chatpointsttv.Utils.EquipmentParser.ItemData;

/**
 * Utility class for generating random equipment for mobs
 * Supports mob-specific weapon pools, random armor, and random enchantments
 */
public class RandomEquipmentGenerator {
    private static final Random RANDOM = new Random();

    // Mob-specific weapon pools
    private static final Map<EntityType, Material[]> MOB_WEAPON_POOLS = new HashMap<>();

    // Armor materials by slot (helmet, chestplate, leggings, boots)
    private static final Material[][] ARMOR_PIECES_BY_TYPE = {
        // Helmets
        {Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET,
         Material.GOLDEN_HELMET, Material.DIAMOND_HELMET},

        // Chestplates
        {Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE,
         Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE},

        // Leggings
        {Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS,
         Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS},

        // Boots
        {Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS,
         Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS}
    };

    private static final int HELMET_INDEX = 0;
    private static final int CHESTPLATE_INDEX = 1;
    private static final int LEGGINGS_INDEX = 2;
    private static final int BOOTS_INDEX = 3;

    // Enchantment arrays
    private static final Enchantment[] WEAPON_ENCHANTMENTS = {
        Enchantment.DAMAGE_ALL,        // Sharpness
        Enchantment.DAMAGE_UNDEAD,     // Smite
        Enchantment.DAMAGE_ARTHROPODS, // Bane of Arthropods
        Enchantment.KNOCKBACK,
        Enchantment.FIRE_ASPECT,
        Enchantment.LOOT_BONUS_MOBS,   // Looting
        Enchantment.DURABILITY         // Unbreaking
    };

    private static final Enchantment[] BOW_ENCHANTMENTS = {
        Enchantment.ARROW_DAMAGE,      // Power
        Enchantment.ARROW_FIRE,        // Flame
        Enchantment.ARROW_INFINITE,    // Infinity
        Enchantment.ARROW_KNOCKBACK,   // Punch
        Enchantment.DURABILITY         // Unbreaking
    };

    private static final Enchantment[] ARMOR_ENCHANTMENTS = {
        Enchantment.PROTECTION_ENVIRONMENTAL,  // Protection
        Enchantment.PROTECTION_FIRE,           // Fire Protection
        Enchantment.PROTECTION_EXPLOSIONS,     // Blast Protection
        Enchantment.PROTECTION_PROJECTILE,     // Projectile Protection
        Enchantment.THORNS,
        Enchantment.DURABILITY                 // Unbreaking
    };

    private static final Enchantment[] HELMET_SPECIFIC = {
        Enchantment.OXYGEN,           // Respiration
        Enchantment.WATER_WORKER      // Aqua Affinity
    };

    private static final Enchantment[] BOOTS_SPECIFIC = {
        Enchantment.DEPTH_STRIDER
    };

    // Enchantment max levels
    private static final Map<Enchantment, Integer> ENCHANTMENT_MAX_LEVELS = new HashMap<>();

    // Mobs that can equip weapons (1.13.1 compatible)
    private static final Set<EntityType> WEAPON_CAPABLE_MOBS = new HashSet<>(Arrays.asList(
        EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
        EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON,
        EntityType.VINDICATOR, EntityType.GIANT, EntityType.PIG_ZOMBIE
    ));

    // Mobs that can equip armor (1.13.1 compatible)
    private static final Set<EntityType> ARMOR_CAPABLE_MOBS = new HashSet<>(Arrays.asList(
        EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
        EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON,
        EntityType.GIANT, EntityType.PIG_ZOMBIE
    ));

    static {
        // Initialize weapon pools
        Material[] zombieWeapons = {
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE
        };
        MOB_WEAPON_POOLS.put(EntityType.ZOMBIE, zombieWeapons);
        MOB_WEAPON_POOLS.put(EntityType.ZOMBIE_VILLAGER, zombieWeapons);
        MOB_WEAPON_POOLS.put(EntityType.HUSK, zombieWeapons);
        MOB_WEAPON_POOLS.put(EntityType.DROWNED, zombieWeapons);
        MOB_WEAPON_POOLS.put(EntityType.PIG_ZOMBIE, zombieWeapons);

        // Skeletons - bows only (crossbow added in 1.14)
        Material[] skeletonWeapons = {Material.BOW};
        MOB_WEAPON_POOLS.put(EntityType.SKELETON, skeletonWeapons);
        MOB_WEAPON_POOLS.put(EntityType.STRAY, skeletonWeapons);

        // Wither Skeleton - swords only
        MOB_WEAPON_POOLS.put(EntityType.WITHER_SKELETON, new Material[]{
            Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD
        });

        // Vindicators - axes only
        MOB_WEAPON_POOLS.put(EntityType.VINDICATOR, new Material[]{
            Material.IRON_AXE, Material.DIAMOND_AXE
        });

        // Giants - same as zombies
        MOB_WEAPON_POOLS.put(EntityType.GIANT, zombieWeapons);

        // Initialize enchantment max levels
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.DAMAGE_ALL, 5);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.DAMAGE_UNDEAD, 5);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.DAMAGE_ARTHROPODS, 5);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.KNOCKBACK, 2);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.FIRE_ASPECT, 2);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.LOOT_BONUS_MOBS, 3);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.ARROW_DAMAGE, 5);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.ARROW_FIRE, 1);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.ARROW_INFINITE, 1);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.ARROW_KNOCKBACK, 2);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.PROTECTION_FIRE, 4);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.PROTECTION_EXPLOSIONS, 4);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.PROTECTION_PROJECTILE, 4);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.THORNS, 3);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.OXYGEN, 3);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.WATER_WORKER, 1);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.DEPTH_STRIDER, 3);
        ENCHANTMENT_MAX_LEVELS.put(Enchantment.DURABILITY, 3);
    }

    /**
     * Check if entity type can equip weapons
     */
    public static boolean canEquipWeapon(EntityType entityType) {
        return WEAPON_CAPABLE_MOBS.contains(entityType);
    }

    /**
     * Check if entity type can equip armor
     */
    public static boolean canEquipArmor(EntityType entityType) {
        return ARMOR_CAPABLE_MOBS.contains(entityType);
    }

    /**
     * Generate random weapon for specific mob type
     * Returns null if mob has no weapon pool
     */
    public static ItemData generateRandomWeapon(EntityType entityType, boolean addEnchantments) {
        if (!canEquipWeapon(entityType)) {
            return null;
        }

        Material[] weaponPool = MOB_WEAPON_POOLS.get(entityType);
        if (weaponPool == null || weaponPool.length == 0) {
            return null;
        }

        Material weapon = weaponPool[RANDOM.nextInt(weaponPool.length)];
        ItemData itemData = new ItemData(weapon);

        if (addEnchantments) {
            applyRandomWeaponEnchantments(itemData);
        }

        return itemData;
    }

    /**
     * Generate random armor piece for specific slot
     */
    public static ItemData generateRandomArmorPiece(int slotIndex, boolean addEnchantments) {
        if (slotIndex < 0 || slotIndex >= ARMOR_PIECES_BY_TYPE.length) {
            return null;
        }

        Material[] pieces = ARMOR_PIECES_BY_TYPE[slotIndex];
        Material armor = pieces[RANDOM.nextInt(pieces.length)];
        ItemData itemData = new ItemData(armor);

        if (addEnchantments) {
            applyRandomArmorEnchantments(itemData, slotIndex);
        }

        return itemData;
    }

    /**
     * Apply 1-3 random weapon enchantments with random levels
     */
    private static void applyRandomWeaponEnchantments(ItemData itemData) {
        Enchantment[] availableEnchants = getWeaponEnchantments(itemData.material);
        if (availableEnchants.length == 0) {
            return;
        }

        // Determine number of enchantments (1-3)
        int enchantCount = RANDOM.nextInt(3) + 1;
        enchantCount = Math.min(enchantCount, availableEnchants.length);

        // Shuffle and pick random enchantments
        List<Enchantment> enchantmentList = new ArrayList<>(Arrays.asList(availableEnchants));
        Collections.shuffle(enchantmentList);

        for (int i = 0; i < enchantCount; i++) {
            Enchantment enchant = enchantmentList.get(i);
            int maxLevel = ENCHANTMENT_MAX_LEVELS.getOrDefault(enchant, 5);
            int level = RANDOM.nextInt(maxLevel) + 1;
            itemData.addEnchantment(enchant, level);
        }
    }

    /**
     * Apply 1-3 random armor enchantments with random levels
     */
    private static void applyRandomArmorEnchantments(ItemData itemData, int slotIndex) {
        List<Enchantment> availableEnchants = new ArrayList<>(Arrays.asList(ARMOR_ENCHANTMENTS));

        // Add slot-specific enchantments
        if (slotIndex == HELMET_INDEX) {
            availableEnchants.addAll(Arrays.asList(HELMET_SPECIFIC));
        } else if (slotIndex == BOOTS_INDEX) {
            availableEnchants.addAll(Arrays.asList(BOOTS_SPECIFIC));
        }

        if (availableEnchants.isEmpty()) {
            return;
        }

        // Determine number of enchantments (1-3)
        int enchantCount = RANDOM.nextInt(3) + 1;
        enchantCount = Math.min(enchantCount, availableEnchants.size());

        // Shuffle and pick random enchantments
        Collections.shuffle(availableEnchants);

        for (int i = 0; i < enchantCount; i++) {
            Enchantment enchant = availableEnchants.get(i);
            int maxLevel = ENCHANTMENT_MAX_LEVELS.getOrDefault(enchant, 5);
            int level = RANDOM.nextInt(maxLevel) + 1;
            itemData.addEnchantment(enchant, level);
        }
    }

    /**
     * Get appropriate enchantments for weapon type
     */
    private static Enchantment[] getWeaponEnchantments(Material material) {
        if (material == Material.BOW) {
            return BOW_ENCHANTMENTS;
        } else {
            return WEAPON_ENCHANTMENTS;
        }
    }
}
