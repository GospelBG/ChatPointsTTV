package me.gosdev.chatpointsttv.EventTests;

import org.bukkit.Material;
import org.bukkit.permissions.Permission;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events;

public class EventTest {
    private ServerMock server;
    private ChatPointsTTV plugin;
    private Permission targetPermission;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(ChatPointsTTV.class);
        targetPermission = new Permission(ChatPointsTTV.permissions.TARGET.permission_id);
    }

    @AfterEach
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @Test
    public void EventGiveTest() {
        PlayerMock targetPlayer = server.addPlayer();
        PlayerMock normalPlayer = server.addPlayer();
        targetPlayer.addAttachment(plugin).setPermission(targetPermission, true);
        
        // Run a test event (Give target player one diamond)
        Events.runAction("GIVE", "DIAMOND 1", "TestUser");

        assertEquals(true, targetPlayer.getInventory().contains(Material.DIAMOND));
        assertEquals(false, normalPlayer.getInventory().contains(Material.DIAMOND));
    }
}
