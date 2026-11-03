package me.profelements.dynatech.items.abstracts;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

public abstract class AbstractMachine extends AbstractTickingContainer implements MachineProcessHolder<CraftingOperation>, RecipeDisplayItem {
        
    private static final Set<CraftingOperation> CLAIMED_OPERATIONS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    private static final Method MARK_REFUNDED_METHOD = resolveOperationMethod("markRefunded");
    private static final Method IS_REFUNDED_METHOD = resolveOperationMethod("isRefunded");

    private final MachineProcessor<CraftingOperation> processor = new MachineProcessor<>(this); 

    protected AbstractMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        
        processor.setProgressBar(getProgressBar()); 
    }

    private static Method resolveOperationMethod(String name) {
        try {
            return CraftingOperation.class.getMethod(name);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    /**
     * Marks a CraftingOperation as refunded/completed once across both Slimefun cores with native
     * markRefunded() and universal 26.x cores without it.
     */
    public static boolean markOperationRefunded(@Nullable CraftingOperation op) {
        if (op == null) {
            return false;
        }
        if (MARK_REFUNDED_METHOD != null) {
            try {
                return Boolean.TRUE.equals(MARK_REFUNDED_METHOD.invoke(op));
            } catch (ReflectiveOperationException ignored) {
                // Fall back to local weak set
            }
        }
        return CLAIMED_OPERATIONS.add(op);
    }

    public static boolean isOperationRefunded(@Nullable CraftingOperation op) {
        if (op == null) {
            return false;
        }
        if (IS_REFUNDED_METHOD != null) {
            try {
                return Boolean.TRUE.equals(IS_REFUNDED_METHOD.invoke(op));
            } catch (ReflectiveOperationException ignored) {
                // Fall back to local weak set
            }
        }
        return CLAIMED_OPERATIONS.contains(op);
    }

    @Nonnull
    @Override
    public MachineProcessor<CraftingOperation> getMachineProcessor() {
        return processor; 
    }

    /**
     * DynaTech removes recipe inputs before registering the operation. This opt-in lets
     * newer Slimefun cores return those inputs safely during a server shutdown.
     */
    public boolean canRefundInFlightOperation() {
        return true;
    }

    /**
     * Preserve DynaTech's actual layout when Slimefun returns an unfinished operation on shutdown.
     */
    @Nonnull
    public int[] getRefundInputSlots() {
        return getInputSlots();
    }

    /**
     * Preserve DynaTech's actual layout when Slimefun returns a finished operation on shutdown.
     */
    @Nonnull
    public int[] getRefundOutputSlots() {
        return getOutputSlots();
    }

    @Nullable 
    public abstract MachineRecipe findNextRecipe(BlockMenu menu);

    @Nonnull
    protected abstract ItemStack getProgressBar();

    protected boolean checkCraftPreconditions(Block b) {
        return true;
    }

    protected boolean onCraftFinish(BlockMenu menu, ItemStack[] ingredients) {
        return true; 
    }

    protected void addOutputs(BlockMenu menu, Block b, ItemStack[] outputs) {
        for (ItemStack output: outputs) {
            if (output != null) {
                menu.pushItem(output.clone(), getOutputSlots()); 
            }
        }
    }

    @Override
    protected void onBreak(BlockBreakEvent e, BlockMenu menu, Location l) {
        super.onBreak(e, menu, l); 

        menu.dropItems(l, getInputSlots());
        menu.dropItems(l, getOutputSlots());

        CraftingOperation currentOp = processor.getOperation(e.getBlock());
        if (currentOp != null && canRefundInFlightOperation() && markOperationRefunded(currentOp)) {
            if (currentOp.isFinished()) {
                for (ItemStack result : currentOp.getResults()) {
                    if (result != null && !result.getType().isAir()) {
                        l.getWorld().dropItemNaturally(l, result.clone());
                    }
                }
            } else {
                for (ItemStack ingredient : currentOp.getIngredients()) {
                    if (ingredient != null && !ingredient.getType().isAir()) {
                        l.getWorld().dropItemNaturally(l, ingredient.clone());
                    }
                }
            }
        }

        processor.endOperation(e.getBlock()); 
    }

    @Override
    protected void tick(BlockMenu menu, Block b) {
        CraftingOperation currentOp = processor.getOperation(b);

        if (currentOp != null) {
            if (checkCraftPreconditions(b)) {
                
                if(!currentOp.isFinished()) {
                    processor.updateProgressBar(menu, getProgressSlot(), currentOp); 
                    currentOp.addProgress(1);
                } else {
                    menu.replaceExistingItem(getProgressSlot(), new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " ")); 

                    if (!markOperationRefunded(currentOp)) {
                        return;
                    }

                    boolean isFinished = onCraftFinish(menu, currentOp.getIngredients()); 
                    if (isFinished) {
                        addOutputs(menu, b, currentOp.getResults()); 
                    }

                    processor.endOperation(b);
                }     
            }
        } else {
            MachineRecipe next = findNextRecipe(menu);

            if (next != null) {
                currentOp = new CraftingOperation(next);
                processor.startOperation(b, currentOp);

                processor.updateProgressBar(menu, getProgressSlot(), currentOp);
            }
        }
    }

    protected int getProgressSlot() {
        return 22; 
    }
}
