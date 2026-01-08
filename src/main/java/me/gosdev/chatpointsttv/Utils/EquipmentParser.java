package me.gosdev.chatpointsttv.Utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import me.gosdev.chatpointsttv.ChatPointsTTV;

/**
 * Utility class for parsing equipment specifications in various formats:
 * - Key-value: weapon:DIAMOND_SWORD helmet:LEATHER_HELMET
 * - Equipment sets: @knight
 * - Mixed: @knight helmet:GOLD_HELMET
 * - Legacy: diamond_sword,leather_helmet,null,null,null
 */
public class EquipmentParser {

    private static final String[] VALID_KEYS = {"weapon", "helmet", "chestplate", "leggings", "boots"};
    private static Map<String, EquipmentSlots> equipmentSets = new HashMap<>();

    /**
     * Data class to hold item with material and enchantments
     */
    public static class ItemData {
        public Material material;
        public Map<Enchantment, Integer> enchantments;

        public ItemData(Material material) {
            this.material = material;
            this.enchantments = new HashMap<>();
        }

        public void addEnchantment(Enchantment enchantment, int level) {
            enchantments.put(enchantment, level);
        }
    }

    /**
     * Data class to hold equipment for all five slots
     */
    public static class EquipmentSlots {
        public ItemData weapon;
        public ItemData helmet;
        public ItemData chestplate;
        public ItemData leggings;
        public ItemData boots;

        public EquipmentSlots() {
            // All slots default to null
        }

        /**
         * Apply non-null values from override slots
         */
        public void merge(EquipmentSlots override) {
            if (override.weapon != null) this.weapon = override.weapon;
            if (override.helmet != null) this.helmet = override.helmet;
            if (override.chestplate != null) this.chestplate = override.chestplate;
            if (override.leggings != null) this.leggings = override.leggings;
            if (override.boots != null) this.boots = override.boots;
        }
    }

    /**
     * Result of equipment parsing, includes both equipment and optional target player
     */
    public static class ParseResult {
        private final EquipmentSlots equipment;
        private final Player target;

        public ParseResult(EquipmentSlots equipment, Player target) {
            this.equipment = equipment;
            this.target = target;
        }

        public EquipmentSlots getEquipment() {
            return equipment;
        }

        public Player getTarget() {
            return target;
        }
    }

    /**
     * Load equipment sets from configuration during plugin initialization
     */
    public static void loadEquipmentSets(FileConfiguration config) {
        equipmentSets.clear();

        if (!config.contains("EQUIPMENT_SETS")) {
            return;
        }

        ConfigurationSection setsSection = config.getConfigurationSection("EQUIPMENT_SETS");
        if (setsSection == null) {
            return;
        }

        for (String setName : setsSection.getKeys(false)) {
            ConfigurationSection setSection = setsSection.getConfigurationSection(setName);
            if (setSection == null) {
                continue;
            }

            EquipmentSlots slots = new EquipmentSlots();

            String weaponStr = setSection.getString("weapon");
            if (weaponStr != null) {
                slots.weapon = parseItemWithEnchantments(weaponStr, "");
            }

            String helmetStr = setSection.getString("helmet");
            if (helmetStr != null) {
                slots.helmet = parseItemWithEnchantments(helmetStr, "");
            }

            String chestplateStr = setSection.getString("chestplate");
            if (chestplateStr != null) {
                slots.chestplate = parseItemWithEnchantments(chestplateStr, "");
            }

            String leggingsStr = setSection.getString("leggings");
            if (leggingsStr != null) {
                slots.leggings = parseItemWithEnchantments(leggingsStr, "");
            }

            String bootsStr = setSection.getString("boots");
            if (bootsStr != null) {
                slots.boots = parseItemWithEnchantments(bootsStr, "");
            }

            equipmentSets.put(setName.toLowerCase(), slots);
            ChatPointsTTV.log.info("Loaded equipment set: " + setName);
        }
    }

    /**
     * Main parsing method - handles all equipment formats
     *
     * @param parts Command parts array
     * @param startIndex Index to start parsing equipment from
     * @param config Twitch configuration for loading equipment sets
     * @param errorStr Error message prefix for warnings
     * @return ParseResult containing equipment and optional target player
     */
    public static ParseResult parseEquipment(String[] parts, int startIndex, FileConfiguration config, String errorStr) {
        EquipmentSlots baseEquipment = new EquipmentSlots();
        EquipmentSlots overrideEquipment = new EquipmentSlots();
        Player target = null;
        boolean legacyFormatDetected = false;

        // Parse through all parts starting from startIndex
        for (int i = startIndex; i < parts.length; i++) {
            String part = parts[i];

            // Check for legacy comma-separated format
            if (part.contains(",") && !part.contains(":")) {
                legacyFormatDetected = true;
                baseEquipment = parseLegacy(part, errorStr);
                continue;
            }

            // Check for equipment set reference (@setname)
            if (part.startsWith("@")) {
                String setName = part.substring(1).toLowerCase();
                EquipmentSlots loadedSet = loadEquipmentSet(setName, errorStr);
                if (loadedSet != null) {
                    baseEquipment = loadedSet;
                }
                continue;
            }

            // Check for key-value equipment (key:VALUE)
            if (part.contains(":")) {
                parseKeyValue(part, overrideEquipment, errorStr);
                continue;
            }

            // Otherwise, treat as player target name
            target = Bukkit.getPlayer(part);
            if (target == null || !target.isOnline()) {
                ChatPointsTTV.log.warning(errorStr + "Couldn't find player " + part + ".");
            }
            break; // Player name should be last parameter
        }

        // Show deprecation notice for legacy format
        if (legacyFormatDetected) {
            ChatPointsTTV.log.info("Legacy equipment format detected. Consider using key-value syntax for better readability.");
        }

        // Merge base equipment with overrides
        baseEquipment.merge(overrideEquipment);

        return new ParseResult(baseEquipment, target);
    }

    /**
     * Parse key-value equipment format: key:VALUE
     */
    private static void parseKeyValue(String keyValue, EquipmentSlots slots, String errorStr) {
        String[] split = keyValue.split(":", 2);

        if (split.length != 2) {
            ChatPointsTTV.log.warning(errorStr + "Invalid equipment format '" + keyValue + "'. Use 'key:VALUE' syntax (e.g., weapon:DIAMOND_SWORD)");
            return;
        }

        String key = split[0].toLowerCase().trim();
        String value = split[1].trim(); // Don't convert to uppercase yet, need to preserve case for parsing

        // Validate equipment key
        if (!isValidEquipmentKey(key)) {
            String suggestion = suggestEquipmentKey(key);
            if (suggestion != null) {
                ChatPointsTTV.log.warning(errorStr + "Unknown equipment slot: '" + key + "' (did you mean '" + suggestion + "'?)");
            } else {
                ChatPointsTTV.log.warning(errorStr + "Unknown equipment slot: '" + key + "'. Valid slots: weapon, helmet, chestplate, leggings, boots");
            }
            return;
        }

        // Check for empty value
        if (value.isEmpty()) {
            ChatPointsTTV.log.warning(errorStr + "Equipment slot '" + key + "' has no value specified.");
            return;
        }

        // Parse material and enchantments (format: MATERIAL:enchant:level:enchant:level...)
        ItemData itemData = parseItemWithEnchantments(value, errorStr);
        if (itemData == null) {
            return;
        }

        // Assign to appropriate slot
        switch (key) {
            case "weapon":
                slots.weapon = itemData;
                break;
            case "helmet":
                slots.helmet = itemData;
                break;
            case "chestplate":
                slots.chestplate = itemData;
                break;
            case "leggings":
                slots.leggings = itemData;
                break;
            case "boots":
                slots.boots = itemData;
                break;
        }
    }

    /**
     * Parse legacy comma-separated format: weapon,helmet,chestplate,leggings,boots
     */
    private static EquipmentSlots parseLegacy(String equipmentString, String errorStr) {
        EquipmentSlots slots = new EquipmentSlots();
        String[] equipment = equipmentString.split(",", -1); // -1 keeps trailing empty strings

        if (equipment.length < 1 || equipment.length > 5) {
            ChatPointsTTV.log.warning(errorStr + "Equipment must have 1-5 slots (weapon,helmet,chestplate,leggings,boots). Got " + equipment.length + " slots.");
            return slots;
        }

        if (equipment.length >= 1) {
            slots.weapon = parseItemWithEnchantments(equipment[0], errorStr);
        }
        if (equipment.length >= 2) {
            slots.helmet = parseItemWithEnchantments(equipment[1], errorStr);
        }
        if (equipment.length >= 3) {
            slots.chestplate = parseItemWithEnchantments(equipment[2], errorStr);
        }
        if (equipment.length >= 4) {
            slots.leggings = parseItemWithEnchantments(equipment[3], errorStr);
        }
        if (equipment.length >= 5) {
            slots.boots = parseItemWithEnchantments(equipment[4], errorStr);
        }

        return slots;
    }

    /**
     * Parse item with enchantments (format: MATERIAL:enchant:level:enchant:level...)
     * Examples: BOW:power:5:flame:1, DIAMOND_SWORD:sharpness:5
     */
    private static ItemData parseItemWithEnchantments(String itemString, String errorStr) {
        if (itemString == null || itemString.equalsIgnoreCase("null") || itemString.trim().isEmpty()) {
            return null;
        }

        String[] parts = itemString.split(":");
        if (parts.length == 0) {
            return null;
        }

        // First part is the material
        String materialStr = parts[0].toUpperCase().trim();
        if (!EnumUtils.isValidEnum(Material.class, materialStr)) {
            if (!errorStr.isEmpty()) {
                ChatPointsTTV.log.warning(errorStr + "Item " + parts[0] + " does not exist. Skipping equipment slot.");
            }
            return null;
        }

        Material material = Material.valueOf(materialStr);
        ItemData itemData = new ItemData(material);

        // Parse enchantments (pairs of enchant:level)
        for (int i = 1; i < parts.length; i += 2) {
            if (i + 1 >= parts.length) {
                if (!errorStr.isEmpty()) {
                    ChatPointsTTV.log.warning(errorStr + "Incomplete enchantment specification for " + materialStr + ". Expected enchantment:level pairs.");
                }
                break;
            }

            String enchantName = parts[i].toLowerCase().trim();
            String levelStr = parts[i + 1].trim();

            // Parse enchantment
            Enchantment enchantment = parseEnchantment(enchantName);
            if (enchantment == null) {
                if (!errorStr.isEmpty()) {
                    ChatPointsTTV.log.warning(errorStr + "Unknown enchantment: '" + enchantName + "'. Skipping.");
                }
                continue;
            }

            // Parse level
            try {
                int level = Integer.parseInt(levelStr);
                if (level < 1) {
                    if (!errorStr.isEmpty()) {
                        ChatPointsTTV.log.warning(errorStr + "Enchantment level must be at least 1 for '" + enchantName + "'. Skipping.");
                    }
                    continue;
                }

                itemData.addEnchantment(enchantment, level);
            } catch (NumberFormatException e) {
                if (!errorStr.isEmpty()) {
                    ChatPointsTTV.log.warning(errorStr + "Invalid enchantment level '" + levelStr + "' for enchantment '" + enchantName + "'. Skipping.");
                }
            }
        }

        return itemData;
    }

    /**
     * Parse enchantment name to Enchantment
     */
    private static Enchantment parseEnchantment(String name) {
        // Try direct match first (using Bukkit's enchantment key)
        try {
            return Enchantment.getByName(name.toUpperCase());
        } catch (Exception e) {
            // Fallback to common aliases
            switch (name.toLowerCase()) {
                case "power":
                case "arrow_damage":
                    return Enchantment.ARROW_DAMAGE;
                case "flame":
                case "arrow_fire":
                    return Enchantment.ARROW_FIRE;
                case "infinity":
                case "arrow_infinite":
                    return Enchantment.ARROW_INFINITE;
                case "punch":
                case "arrow_knockback":
                    return Enchantment.ARROW_KNOCKBACK;
                case "sharpness":
                case "damage_all":
                    return Enchantment.DAMAGE_ALL;
                case "smite":
                case "damage_undead":
                    return Enchantment.DAMAGE_UNDEAD;
                case "bane_of_arthropods":
                case "damage_arthropods":
                    return Enchantment.DAMAGE_ARTHROPODS;
                case "knockback":
                    return Enchantment.KNOCKBACK;
                case "fire_aspect":
                case "fire":
                    return Enchantment.FIRE_ASPECT;
                case "looting":
                case "loot_bonus_mobs":
                    return Enchantment.LOOT_BONUS_MOBS;
                case "protection":
                case "protection_environmental":
                    return Enchantment.PROTECTION_ENVIRONMENTAL;
                case "fire_protection":
                case "protection_fire":
                    return Enchantment.PROTECTION_FIRE;
                case "blast_protection":
                case "protection_explosions":
                    return Enchantment.PROTECTION_EXPLOSIONS;
                case "projectile_protection":
                case "protection_projectile":
                    return Enchantment.PROTECTION_PROJECTILE;
                case "respiration":
                case "oxygen":
                    return Enchantment.OXYGEN;
                case "aqua_affinity":
                case "water_worker":
                    return Enchantment.WATER_WORKER;
                case "thorns":
                    return Enchantment.THORNS;
                case "depth_strider":
                case "depth":
                    return Enchantment.DEPTH_STRIDER;
                case "unbreaking":
                case "durability":
                    return Enchantment.DURABILITY;
                default:
                    return null;
            }
        }
    }

    /**
     * Load equipment set from static map
     */
    private static EquipmentSlots loadEquipmentSet(String setName, String errorStr) {
        if (!equipmentSets.containsKey(setName)) {
            StringBuilder availableSets = new StringBuilder();
            for (String name : equipmentSets.keySet()) {
                if (availableSets.length() > 0) {
                    availableSets.append(", ");
                }
                availableSets.append(name);
            }

            if (availableSets.length() > 0) {
                ChatPointsTTV.log.warning(errorStr + "Equipment set '" + setName + "' not found. Available sets: " + availableSets.toString());
            } else {
                ChatPointsTTV.log.warning(errorStr + "Equipment set '" + setName + "' not found. No equipment sets are configured.");
            }
            return null;
        }

        // Return a copy to prevent modifications to the original
        EquipmentSlots original = equipmentSets.get(setName);
        EquipmentSlots copy = new EquipmentSlots();
        copy.weapon = original.weapon;
        copy.helmet = original.helmet;
        copy.chestplate = original.chestplate;
        copy.leggings = original.leggings;
        copy.boots = original.boots;
        return copy;
    }

    /**
     * Parse a material string, handling null/empty values
     */
    private static Material parseMaterial(String item, String errorStr) {
        if (item == null || item.equalsIgnoreCase("null") || item.trim().isEmpty()) {
            return null;
        }

        String itemUpper = item.toUpperCase().trim();
        if (!EnumUtils.isValidEnum(Material.class, itemUpper)) {
            if (!errorStr.isEmpty()) {
                ChatPointsTTV.log.warning(errorStr + "Item " + item + " does not exist. Skipping equipment slot.");
            }
            return null;
        }

        return Material.valueOf(itemUpper);
    }

    /**
     * Check if equipment key is valid
     */
    private static boolean isValidEquipmentKey(String key) {
        for (String validKey : VALID_KEYS) {
            if (validKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Suggest correct equipment key for typos using Levenshtein distance
     */
    private static String suggestEquipmentKey(String invalidKey) {
        // Simple prefix matching first
        for (String validKey : VALID_KEYS) {
            if (validKey.startsWith(invalidKey.toLowerCase())) {
                return validKey;
            }
        }

        // Find closest match by edit distance
        String closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (String validKey : VALID_KEYS) {
            int distance = levenshteinDistance(invalidKey.toLowerCase(), validKey);
            if (distance < minDistance) {
                minDistance = distance;
                closest = validKey;
            }
        }

        // Only suggest if it's a close match (2 or fewer edits)
        return minDistance <= 2 ? closest : null;
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private static int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1),     // insertion
                    dp[i - 1][j - 1] + cost // substitution
                );
            }
        }

        return dp[len1][len2];
    }
}
