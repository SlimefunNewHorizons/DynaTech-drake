package me.profelements.dynatech.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.api.events.AsyncMachineOperationFinishEvent;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.profelements.dynatech.DynaTech;
import me.profelements.dynatech.items.tools.AutoOutputUpgrade;
import me.profelements.dynatech.registries.Items;

public class UpgradesListener implements Listener {

    public UpgradesListener(DynaTech plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMachineFinish(AsyncMachineOperationFinishEvent e) {
        if (!(e.getOperation() instanceof CraftingOperation)) {
            return;
        }

        checkInputUpgrade(e);

        Location l = e.getPosition().toLocation();
        String upgrades = BlockStorage.getLocationInfo(l, "upgrades");

        if (upgrades == null) {
            return;
        }

        int upgradeIdx = upgrades.indexOf("{id:auto_output");
        if (upgradeIdx == -1) {
            return;
        }

        int upgradeIdx2 = upgrades.indexOf("}", upgradeIdx);

        String upgradeString = upgrades.substring(upgradeIdx, upgradeIdx2 + 1);

        if (upgrades != null && upgrades.contains("id:auto_output")) {
            int index = upgradeString.indexOf("face:");
            int index2 = upgradeString.indexOf("}");
            BlockFace face = AutoOutputUpgrade.stringToBlockFace(upgradeString.substring(index, index2));
            // DynaTech.getInstance().getLogger().info(face.toString());
            // Grab menu and then grab output slots
            if (e.getProcessor().getOwner() instanceof AContainer cont
                    && e.getOperation() instanceof CraftingOperation op && op.isFinished()) {
                ItemStack[] outputItems = op.getResults();
                if (face != BlockFace.SELF && l.getBlock().getRelative(face).getType() == Material.CHEST) {
                    // This event can be asynchronous. Reserve and transfer the exact results on
                    // the server thread so an output is never consumed before its destination fits.
                    DynaTech.runSync(() -> transferCompletedOutput(l, face, cont, outputItems));
                }
            }
        }
    }

    private static void transferCompletedOutput(Location location, BlockFace face, AContainer container,
            ItemStack[] operationResults) {
        BlockState state = location.getBlock().getRelative(face).getState();
        if (!(state instanceof Chest chest) || operationResults == null) {
            return;
        }

        BlockMenu menu = BlockStorage.getInventory(location);
        if (menu == null) {
            return;
        }

        List<ItemStack> results = new ArrayList<>();
        for (ItemStack result : operationResults) {
            if (result != null && !result.getType().isAir() && result.getAmount() > 0) {
                results.add(result.clone());
            }
        }
        if (results.isEmpty() || !InvUtils.fitAll(chest.getBlockInventory(), results.toArray(ItemStack[]::new))) {
            return;
        }

        Map<Integer, Integer> consumptions = reserveOutputSlots(menu, container.getOutputSlots(), results);
        if (consumptions == null) {
            return;
        }

        consumptions.forEach(menu::consumeItem);
        Map<Integer, ItemStack> leftovers = chest.getBlockInventory().addItem(results.toArray(ItemStack[]::new));
        if (!leftovers.isEmpty()) {
            // fitAll and addItem run in the same server task, so this path indicates another
            // plugin mutated the chest during the call. Keep the remainder visible instead of
            // silently deleting it; the operation is logged for an administrator to inspect.
            leftovers.values().forEach(item -> chest.getWorld().dropItemNaturally(chest.getLocation(), item));
            DynaTech.getInstance().getLogger().severe("Auto-output detectó una mutación externa del cofre; se soltó el remanente para evitar pérdida silenciosa.");
            return;
        }
        chest.update(true, false);
    }

    private static Map<Integer, Integer> reserveOutputSlots(BlockMenu menu, int[] outputSlots, List<ItemStack> results) {
        Map<Integer, Integer> available = new HashMap<>();
        Map<Integer, Integer> consumptions = new HashMap<>();

        for (int slot : outputSlots) {
            ItemStack stack = menu.getItemInSlot(slot);
            if (stack != null && !stack.getType().isAir()) {
                available.put(slot, stack.getAmount());
            }
        }

        for (ItemStack result : results) {
            int remaining = result.getAmount();
            for (int slot : outputSlots) {
                ItemStack stack = menu.getItemInSlot(slot);
                int amount = available.getOrDefault(slot, 0);
                if (amount == 0 || !SlimefunUtils.isItemSimilar(stack, result, true)) {
                    continue;
                }

                int consumed = Math.min(amount, remaining);
                available.put(slot, amount - consumed);
                consumptions.merge(slot, consumed, Integer::sum);
                remaining -= consumed;
                if (remaining == 0) {
                    break;
                }
            }
            if (remaining != 0) {
                return null;
            }
        }
        return consumptions;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Location l = e.getBlock().getLocation();
        String upgrades = BlockStorage.getLocationInfo(l, "upgrades");

        if (upgrades != null && upgrades.contains("auto_output")) {
            l.getWorld().dropItemNaturally(l, Items.AUTO_OUTPUT_UPGRADE.stack());
        }

        if (upgrades != null && upgrades.contains("auto_input")) {
            l.getWorld().dropItemNaturally(l, Items.AUTO_INPUT_UPGRADE.stack());
        }
    }

    private static void checkInputUpgrade(AsyncMachineOperationFinishEvent e) {
        Location l = e.getPosition().toLocation();
        String upgrades = BlockStorage.getLocationInfo(l, "upgrades");

        if (upgrades == null) {
            return;
        }

        int upgradeIdx = upgrades.indexOf("{id:auto_input");
        if (upgradeIdx == -1) {
            return;
        }

        int upgradeIdx2 = upgrades.indexOf("}", upgradeIdx);

        String upgradeString = upgrades.substring(upgradeIdx, upgradeIdx2 + 1);

        if (upgradeString.contains("id:auto_input")) {
            DynaTech.getInstance().getLogger().info("GOT TO INPUT FOUND");
            // Grab face
            int index = upgradeString.indexOf("face:");
            int index2 = upgradeString.indexOf("}");
            BlockFace face = AutoOutputUpgrade.stringToBlockFace(upgradeString.substring(index, index2));
            if (face == BlockFace.SELF) {
                return;
            }

            DynaTech.runSync(() -> {
                BlockState state = l.getBlock().getRelative(face).getState();
                if (state instanceof Chest chest && e.getProcessor().getOwner() instanceof AContainer acont) {
                    BlockMenu inv = BlockStorage.getInventory(l);
                    int[] slots = acont.getInputSlots();
                    for (int slot : slots) {
                        Inventory chsInv = chest.getBlockInventory();
                        ItemStack inputStack = inv.getItemInSlot(slot);
                        for (ItemStack stack : chsInv.getContents()) {
                            if (inputStack == null && stack != null
                                    || inputStack != null && stack != null && stack.isSimilar(inputStack)) {
                                int chsAmount = stack.getAmount();

                                if (inputStack == null) {

                                    DynaTech.getInstance().getLogger().info("GOT TO NULLY FOUND");
                                    inv.pushItem(stack, acont.getInputSlots());
                                    chsInv.remove(stack);
                                } else {
                                    if (inputStack.getAmount() == inputStack.getMaxStackSize()) {
                                        return;
                                    } else {
                                        int diff = inputStack.getMaxStackSize() - inputStack.getAmount();
                                        if (diff >= chsAmount) {

                                            DynaTech.getInstance().getLogger().info("GOT TO DIFFY FOUND");
                                            inputStack.setAmount(inputStack.getAmount() + chsAmount);
                                            chsInv.remove(stack);
                                        } else {

                                            DynaTech.getInstance().getLogger().info("GOT TO DIFFY2 FOUND");
                                            inputStack.setAmount(inputStack.getAmount() + diff);
                                            stack.setAmount(chsAmount - diff);
                                        }
                                    }
                                }

                            }
                        }
                    }
                }

            });
        }
    }
}
