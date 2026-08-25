package me.gosdev.chatpointsttv.Balm;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Utils.ChatComponent;
import net.blay09.mods.balm.Balm;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class BalmPlayer implements GenericPlayer {
    public BalmPlayer(Player player) {
        this.player = (ServerPlayer) player;
    }

    public ServerPlayer player;

    @Override
    public void sendMessage(String message) {
        player.displayClientMessage(Component.literal(message), false);
    }

    private MutableComponent convertComponent(ChatComponent comp) {
        MutableComponent mc = Component.literal(comp.getText() == null ? "" : comp.getText());

        mc = mc.withStyle(style -> {
            Style s = style;
            if (comp.getClickEvent() != null) {
                try {
                    switch (comp.getClickEvent().getAction()) {
                        case OPEN_URL:
                            s = s.withClickEvent(new ClickEvent.OpenUrl(new java.net.URI(comp.getClickEvent().getValue())));
                            break;
                        case RUN_COMMAND:
                            s = s.withClickEvent(new ClickEvent.RunCommand(comp.getClickEvent().getValue()));
                            break;
                        case COMPLETE_COMMAND:
                            s = s.withClickEvent(new ClickEvent.SuggestCommand(comp.getClickEvent().getValue()));
                            break;
                    }
                } catch (Exception ignored) {}
            }
            if (comp.getHoverEvent() != null) {
                if (comp.getHoverEvent().getAction() == me.gosdev.chatpointsttv.Utils.ChatEvent.HoverAction.SHOW_TEXT) {
                    s = s.withHoverEvent(new HoverEvent.ShowText(Component.literal(comp.getHoverEvent().getValue())));
                }
            }
            return s;
        });

        for (ChatComponent extra : comp.getExtra()) {
            mc.append(convertComponent(extra));
        }
        return mc;
    }

    @Override
    public void sendMessage(ChatComponent comp) {
        player.displayClientMessage(convertComponent(comp), false);
    }

    @Override
    public void runCommand(String cmd) {
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }

        MinecraftServer server = Balm.getRuntime().platform().server();

        if (server != null) {
            String finalCmd = cmd;
            server.execute(() -> {
                server.getCommands().performPrefixedCommand(player.createCommandSourceStackForNameResolution(player.level()), finalCmd);
            });
        }

    }

    @Override
    public boolean hasPermission(ChatPointsTTV.permissions perm) {
        // return player.permissions().hasPermission(); TODO
        return true;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public String getName() {
        return player.getName().getString();
    }

    @Override
    public UUID getUUID() {
        return player.getUUID();
    }

    @Override
    public Boolean isOnline() {
        return true; // TODO: ??
    }

    @Override
    public void spawnTnt(Integer fuseTime) {
        PrimedTnt tnt = new PrimedTnt(player.level(), player.getX(), player.getY(), player.getZ(), player);
        tnt.setFuse(fuseTime);

        player.level().addFreshEntity(tnt);
    }

    @Override
    public void sendTitle(String title, String sub) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub)));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
    }

    @Override
    public void playSound(String sound) {
        Optional<SoundEvent> soundOpt = BuiltInRegistries.SOUND_EVENT.getOptional(Identifier.parse(sound));

        if (soundOpt.isPresent()) {
            Balm.getRuntime().platform().server().execute(() -> {
                player.playSound(soundOpt.get(), 1.0F, 1.0F);
            });
        }
    }

    @Override
    public void giveItem(String item, Integer amount) {
        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(Identifier.parse(item));
        if (itemOpt.isPresent()) {
            ItemStack stack = new ItemStack(itemOpt.get(), amount);

            Balm.getRuntime().platform().server().execute(() -> {
                player.getInventory().add(stack);
            });
        }
    }

    @Override
    public void giveEffect(String effect, Integer duration, Integer strength) {
        Optional<MobEffect> potionOpt = BuiltInRegistries.MOB_EFFECT.getOptional(Identifier.parse(effect));

        if (potionOpt.isPresent()) {
            Balm.getRuntime().platform().server().execute(() -> {
                player.addEffect(new MobEffectInstance(Holder.direct(potionOpt.get()), duration, strength));
            });
        }
    }

    @Override
    public void clearEffects() {
        Balm.getRuntime().platform().server().execute(() -> {
            for (MobEffectInstance effect : player.getActiveEffects()) {
                player.removeEffect(effect.getEffect());
            }
        });
    }

    @Override
    public void freeze(Integer seconds) {

    }


    @Override
    public void removeItem(int slot) {
        Balm.getRuntime().platform().server().execute(() -> {
            player.getInventory().removeItemNoUpdate(slot);
        });
    }

    @Override
    public void exchangeSlots(int slot1, int slot2) {
        Balm.getRuntime().platform().server().execute(() -> {
            ItemStack stack1 = player.getInventory().getItem(slot1);
            ItemStack stack2 = player.getInventory().getItem(slot2);

            player.getInventory().setItem(slot1, stack2);
            player.getInventory().setItem(slot2, stack1);
        });
    }

    @Override
    public int getInvSlots() {
        return player.getInventory().getContainerSize();
    }

    @Override
    public Integer getHandSlot() {
        return player.getInventory().getSelectedSlot();
    }

    @Override
    public Boolean hasItem(int slot) {
        return !player.getInventory().getItem(slot).isEmpty();
    }
}
