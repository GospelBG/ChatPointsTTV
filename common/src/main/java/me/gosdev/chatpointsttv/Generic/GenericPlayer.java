package me.gosdev.chatpointsttv.Generic;

import java.util.UUID;

public interface GenericPlayer extends GenericSender {
    String getName();
    UUID getUUID();
    Boolean isOnline();
    
    void spawnEntity(String entity, String name, Boolean glow);
    void spawnTnt(Integer fuseTime);
    void sendTitle(String title, String sub);
    void playSound(String sound);
    void giveItem(String item, Integer amount);
    void giveEffect(String effect, Integer duration, Integer strength);
    void freeze(Integer seconds);
    void shuffleInventory();

    void removeItem(int slot);
    void exchangeSlots(int slot1, int slot2);
    int getInvSlots();
    Integer getHandSlot();
    Boolean hasItem(int slot);
}
