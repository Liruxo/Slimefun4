package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import io.github.thebusybiscuit.cscorelib2.chat.ChatColors;
import io.github.thebusybiscuit.cscorelib2.skull.SkullBlock;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNet;
import io.github.thebusybiscuit.slimefun4.implementation.tasks.TickerTask;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.energy.ChargableBlock;

public class DebugFishListener implements Listener {

    private final String enabledTooltip;
    private final String disabledTooltip;

    public DebugFishListener(SlimefunPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        enabledTooltip = "&2\u2714";
        disabledTooltip = "&4\u2718";
    }

    @EventHandler
    public void onDebug(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL || e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player p = e.getPlayer();

        if (p.isOp() && SlimefunUtils.isItemSimilar(e.getItem(), SlimefunItems.DEBUG_FISH, true)) {
            e.setCancelled(true);

            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (p.isSneaking()) {
                    if (BlockStorage.hasBlockInfo(e.getClickedBlock())) {
                        BlockStorage.clearBlockInfo(e.getClickedBlock());
                    }
                }
                else {
                    e.setCancelled(false);
                }
            }
            else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (p.isSneaking()) {
                    Block b = e.getClickedBlock().getRelative(e.getBlockFace());
                    b.setType(Material.PLAYER_HEAD);
                    SkullBlock.setFromBase64(b, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTllYjlkYTI2Y2YyZDMzNDEzOTdhN2Y0OTEzYmEzZDM3ZDFhZDEwZWFlMzBhYjI1ZmEzOWNlYjg0YmMifX19");
                }
                else if (BlockStorage.hasBlockInfo(e.getClickedBlock())) {
                    sendInfo(p, e.getClickedBlock());
                }
            }
        }
    }

    private void sendInfo(Player p, Block b) {
        SlimefunItem item = BlockStorage.check(b);

        p.sendMessage(" ");
        p.sendMessage(ChatColors.color("&d" + b.getType() + " &e@ X: " + b.getX() + " Y: " + b.getY() + " Z: " + b.getZ()));
        p.sendMessage(ChatColors.color("&dId: " + "&e" + item.getID()));
        p.sendMessage(ChatColors.color("&dPlugin: " + "&e" + item.getAddon().getName()));

        if (b.getState() instanceof Skull) {
            p.sendMessage(ChatColors.color("&dSkull: " + enabledTooltip));

            // Check if the skull is a wall skull, and if so use Directional instead of Rotatable.
            if (b.getType() == Material.PLAYER_WALL_HEAD) {
                p.sendMessage(ChatColors.color("  &dFacing: &e" + ((Directional) b.getBlockData()).getFacing().toString()));
            }
            else {
                p.sendMessage(ChatColors.color("  &dRotation: &e" + ((Rotatable) b.getBlockData()).getRotation().toString()));
            }
        }

        if (BlockStorage.getStorage(b.getWorld()).hasInventory(b.getLocation())) {
            p.sendMessage(ChatColors.color("&dInventory: " + enabledTooltip));
        }
        else {
            p.sendMessage(ChatColors.color("&dInventory: " + disabledTooltip));
        }

        TickerTask ticker = SlimefunPlugin.getTicker();

        if (item.isTicking()) {
            p.sendMessage(ChatColors.color("&dTicker: " + enabledTooltip));
            p.sendMessage(ChatColors.color("  &dAsync: &e" + (BlockStorage.check(b).getBlockTicker().isSynchronized() ? disabledTooltip : enabledTooltip)));
            p.sendMessage(ChatColors.color("  &dTimings: &e" + ticker.toMillis(ticker.getTimings(b), true)));
            p.sendMessage(ChatColors.color("  &dTotal Timings: &e" + ticker.toMillis(ticker.getTimings(BlockStorage.checkID(b)), true)));
            p.sendMessage(ChatColors.color("  &dChunk Timings: &e" + ticker.toMillis(ticker.getTimings(b.getChunk()), true)));
        }
        else if (item.getEnergyTicker() != null) {
            p.sendMessage(ChatColors.color("&dTicking: " + "&3Indirect"));
            p.sendMessage(ChatColors.color("  &dTimings: &e" + ticker.toMillis(ticker.getTimings(b), true)));
            p.sendMessage(ChatColors.color("  &dChunk Timings: &e" + ticker.toMillis(ticker.getTimings(b.getChunk()), true)));
        }
        else {
            p.sendMessage(ChatColors.color("&dTicker: " + disabledTooltip));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dTicking: " + disabledTooltip));
        }

        if (ChargableBlock.isChargable(b)) {
            p.sendMessage(ChatColors.color("&dChargeable: " + enabledTooltip));
            p.sendMessage(ChatColors.color("  &dEnergy: &e" + ChargableBlock.getCharge(b) + " / " + ChargableBlock.getMaxCharge(b)));
        }
        else {
            p.sendMessage(ChatColors.color("&dChargeable: " + disabledTooltip));
        }

        p.sendMessage(ChatColors.color("  &dEnergyNet Type: &e" + EnergyNet.getComponent(b)));

        p.sendMessage(ChatColors.color("&6" + BlockStorage.getBlockInfoAsJson(b)));
        p.sendMessage(" ");
    }
}
