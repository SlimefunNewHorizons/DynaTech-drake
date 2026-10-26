package me.profelements.dynatech.items.abstracts;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;

/**
 * Unit test for Ticket #438: AbstractMachine in-flight operation refund mechanics.
 */
class AbstractMachineRefundTest {

    @BeforeAll
    static void setUp() {
        MockBukkit.mock();
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("CraftingOperation markRefunded prevents duplicate refund/payout")
    void testCraftingOperationMarkRefunded() {
        ItemStack[] inputs = new ItemStack[] { new ItemStack(Material.IRON_INGOT, 2) };
        ItemStack[] outputs = new ItemStack[] { new ItemStack(Material.GOLD_INGOT, 1) };
        CraftingOperation op = new CraftingOperation(inputs, outputs, 5);

        Assertions.assertFalse(op.isRefunded(), "Operation should not be refunded initially");
        Assertions.assertFalse(op.isFinished(), "Operation should not be finished initially");

        // First markRefunded succeeds
        Assertions.assertTrue(op.markRefunded(), "First markRefunded must return true");
        Assertions.assertTrue(op.isRefunded(), "isRefunded must be true after markRefunded");

        // Second markRefunded fails (idempotent / race protection)
        Assertions.assertFalse(op.markRefunded(), "Second markRefunded must return false to prevent duplicate refund");

        // Ingredients are intact for in-flight refund
        Assertions.assertEquals(1, op.getIngredients().length);
        Assertions.assertEquals(Material.IRON_INGOT, op.getIngredients()[0].getType());
        Assertions.assertEquals(2, op.getIngredients()[0].getAmount());
    }

    @Test
    @DisplayName("Finished CraftingOperation preserves results for refund on break/shutdown")
    void testFinishedCraftingOperationResults() {
        ItemStack[] inputs = new ItemStack[] { new ItemStack(Material.COAL, 1) };
        ItemStack[] outputs = new ItemStack[] { new ItemStack(Material.DIAMOND, 1) };
        CraftingOperation op = new CraftingOperation(inputs, outputs, 2);

        op.addProgress(2);
        Assertions.assertTrue(op.isFinished(), "Operation with progress >= totalTicks must be finished");

        Assertions.assertTrue(op.markRefunded());
        Assertions.assertEquals(Material.DIAMOND, op.getResults()[0].getType());
    }
}
