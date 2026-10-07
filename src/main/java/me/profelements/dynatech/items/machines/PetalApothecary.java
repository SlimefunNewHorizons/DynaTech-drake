package me.profelements.dynatech.items.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.profelements.dynatech.registries.RecipeTypes;
import me.profelements.dynatech.registries.Registries;
import me.profelements.dynatech.utils.Recipe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class PetalApothecary extends SlimefunItem {

    protected static final HashMap<BlockPosition, List<ItemStack>> RECIPE_ITEMS = new HashMap<>();

    public PetalApothecary(ItemGroup itemGroup, SlimefunItemStack item) {
        super(itemGroup, item);

        addItemHandler(onUse(), new BlockTicker() {

            @Override
            public boolean isSynchronized() {
                return true;
            }

            @Override
            public void tick(Block arg0, SlimefunItem arg1, Config arg2) {
                tickBlock(arg0);
            }

        });
    }

    private static BlockUseHandler onUse() {
        return new BlockUseHandler() {

            /**
             * El clic derecho ya no responde nada.
             *
             * Aqui habia trazas de desarrollo que se enviaban al jugador en cada clic: el nivel
             * del caldero, el tamano interno del mapa de recetas y la lista de items, en ingles y
             * sin formato. Quien usaba el Petal Apothecary recibia todo eso por chat y parecia que
             * la maquina estaba rota.
             */
            @Override
            public void onRightClick(PlayerRightClickEvent event) {
                // Sin cuerpo a proposito: la maquina trabaja en su tick, no al hacer clic.
            }
        };

    }

    private void tickBlock(Block block) {

        if (!(block.getBlockData() instanceof Levelled lvl)) {
            return;
        }

        int levelAfterRecipeConsume = lvl.getLevel() - 1;

        List<ItemStack> maybeRecipeContents = getMaybeRecipes(block);

        Optional<Recipe> maybeRecipe = Registries.RECIPES.getEntries().stream().filter((recipe) -> {
            boolean sameLength = recipe.getInput().length == maybeRecipeContents.size();
            boolean recipeTypeEqual = recipe.getRecipeType().equals(RecipeTypes.PETAL_APOTHECARY);
            boolean containsItems = Arrays.stream(recipe.getInput()).allMatch((itemStack) -> {
                return maybeRecipeContents.contains(itemStack);
            });

            return sameLength && recipeTypeEqual && containsItems;
        }).findFirst();

        if (maybeRecipe.isPresent()) {
            Recipe recipe = maybeRecipe.get();

            Arrays.stream(recipe.getOutput()).forEach((item) -> {
                block.getWorld().dropItemNaturally(block.getLocation().add(0, 1, 0), item);
            });

            if (levelAfterRecipeConsume >= lvl.getMinimumLevel()) {
                lvl.setLevel(levelAfterRecipeConsume);
                block.setBlockData(lvl);
            } else {
                block.setType(Material.CAULDRON);
            }

            RECIPE_ITEMS.put(new BlockPosition(block), new ArrayList<>());
        }
    }

    private List<ItemStack> getMaybeRecipes(Block block) {
        BlockPosition pos = new BlockPosition(block);
        Collection<Item> items = block.getWorld().getNearbyEntitiesByType(Item.class, block.getLocation(), 1.d);
        List<ItemStack> itemList = RECIPE_ITEMS.getOrDefault(pos, new ArrayList<>());

        Optional<Item> maybeRecipeItem = items.stream().filter((item) -> {
            return Registries.RECIPES.getEntries().stream().anyMatch((recipe) -> {
                boolean recipeTypeEqual = recipe.getRecipeType().equals(RecipeTypes.PETAL_APOTHECARY);
                boolean containsItems = Arrays.stream(recipe.getInput()).anyMatch((itemStack) -> {
                    return itemStack != null && itemStack.isSimilar(item.getItemStack());
                });

                return recipeTypeEqual && containsItems;
            });
        }).findFirst();

        if (maybeRecipeItem.isPresent())

        {
            Item itemToRemove = maybeRecipeItem.get();

            itemList.add(itemToRemove.getItemStack());
            RECIPE_ITEMS.put(pos, itemList);

            itemToRemove.setPickupDelay(10000000);
            itemToRemove.remove();
        }
        return itemList;
    }
}
