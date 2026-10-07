package me.profelements.dynatech.items.tools;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.attributes.Rechargeable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemDropHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.profelements.dynatech.DynaTech;
import me.profelements.dynatech.registries.Items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;

public class AngelGem extends SlimefunItem implements Rechargeable, NotPlaceable, Listener {

    private final ItemSetting<Double> maxFlightSpeed = new ItemSetting<>(this, "max-flight-speed", 0.15d);
    private final ItemSetting<Boolean> hasMaxFlightSpeed = new ItemSetting<>(this, "has-max-flight-speed", true);

    // Double y no Float a proposito: SnakeYAML lee todo decimal de Items.yml como Double, asi que
    // un ItemSetting<Float> nunca valida y Slimefun cae siempre al valor por defecto avisando
    // "Expected Float but found Double" en cada arranque. Con Double la clave si es configurable.
    private final ItemSetting<Double> energyCapacity = new ItemSetting<>(this, "energy-capacity", 10240.0d);
    private final ItemSetting<Double> energyDrainRate = new ItemSetting<>(this, "energy-drain-per-second", 16.0d);

    public static final Set<UUID> enabledFlightUsers = Collections.synchronizedSet(new HashSet<>());
    public static final Map<UUID, Long> fallImmunity = new ConcurrentHashMap<>();

    private float flySpeed = 0.1f;

    public AngelGem(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        addItemSetting(maxFlightSpeed, hasMaxFlightSpeed, energyCapacity, energyDrainRate);

        addItemHandler(onRightClick(), onItemDrop());

        Bukkit.getPluginManager().registerEvents(this, DynaTech.getInstance());

        startEnergyDrainTask();
    }

    @Override
    public float getMaxItemCharge(ItemStack item) {
        return energyCapacity.getValue().floatValue();
    }

    public float getEnergyConsumption() {
        return energyDrainRate.getValue().floatValue();
    }

    private void startEnergyDrainTask() {
        Bukkit.getScheduler().runTaskTimer(DynaTech.getInstance(), () -> {
            if (enabledFlightUsers.isEmpty()) {
                return;
            }

            synchronized (enabledFlightUsers) {
                Iterator<UUID> it = enabledFlightUsers.iterator();
                while (it.hasNext()) {
                    UUID uuid = it.next();
                    Player p = Bukkit.getPlayer(uuid);

                    if (p == null || !p.isOnline()) {
                        it.remove();
                        continue;
                    }

                    if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR
                            || !p.isFlying()) {
                        continue;
                    }

                    ItemStack gem = findAngelGem(p);
                    if (gem == null) {
                        it.remove();
                        p.setFlying(false);
                        if (!hasPermanentFlightPermission(p)) {
                            p.setAllowFlight(false);
                        }
                        p.setFallDistance(0.0f);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 160, 0));
                        fallImmunity.put(uuid, System.currentTimeMillis() + 10000L);
                        p.sendMessage(ChatColor.RED + "⚡ ¡La Gema Angelical no está en tu inventario! Vuelo suspendido.");
                        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
                        continue;
                    }

                    float cost = getEnergyConsumption();
                    if (removeItemCharge(gem, cost)) {
                        float remaining = getItemCharge(gem);
                        if (remaining <= cost * 3.0f) {
                            p.sendActionBar(Component.text("⚡ BATERÍA CRÍTICA: " + (int) remaining + " J restantes",
                                    NamedTextColor.RED));
                        }
                        continue;
                    }

                    it.remove();
                    p.setFlying(false);
                    if (!hasPermanentFlightPermission(p)) {
                        p.setAllowFlight(false);
                    }
                    p.setFallDistance(0.0f);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 160, 0));
                    fallImmunity.put(uuid, System.currentTimeMillis() + 10000L);
                    p.sendMessage(ChatColor.RED + "⚡ ¡La Gema Angelical se ha quedado sin energía! Vuelo desactivado (Caída Lenta activa por 8s).");
                    p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);
                }
            }
        }, 20L, 20L);
    }

    private boolean hasPermanentFlightPermission(Player p) {
        return p.hasPermission("cmi.command.fly")
                || p.hasPermission("essentials.fly")
                || p.hasPermission("drakescraft.vip.fly");
    }

    private ItemStack findAngelGem(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Items.ANGEL_GEM.stack().getType()
                    && SlimefunUtils.isItemSimilar(item, Items.ANGEL_GEM.stack(), false, false)) {
                return item;
            }
        }

        return null;
    }

    private ItemDropHandler onItemDrop() {
        return (e, p, itemEntity) -> {
            ItemStack item = itemEntity.getItemStack();
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE && item.getType() == Items.ANGEL_GEM.stack().getType()
                    && SlimefunUtils.isItemSimilar(item, Items.ANGEL_GEM.stack(), false, false)) {
                enabledFlightUsers.remove(e.getPlayer().getUniqueId());
                e.getPlayer().setFlying(false);
                if (!hasPermanentFlightPermission(e.getPlayer())) {
                    e.getPlayer().setAllowFlight(false);
                }
                e.getPlayer().setFallDistance(0.0f);
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 160, 0));
                fallImmunity.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + 10000L);
            } else {
                return false;
            }
            e.getPlayer().setFlySpeed(0.1f);
            return true;
        };
    }

    private ItemUseHandler onRightClick() {
        return e -> {
            Player p = e.getPlayer();

            if (p.isSneaking()) {
                enabledFlightUsers.remove(p.getUniqueId());
                p.setFlying(false);
                if (!hasPermanentFlightPermission(p)) {
                    p.setAllowFlight(false);
                }
                p.setFallDistance(0.0f);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 160, 0));
                fallImmunity.put(p.getUniqueId(), System.currentTimeMillis() + 10000L);
                p.sendMessage(ChatColor.YELLOW + "⚡ Vuelo desactivado (Caída segura activa por 8s).");
                e.getItem().setItemMeta(updateLore(e.getItem(), p));
                e.cancel();
                return;
            }

            if (!enabledFlightUsers.contains(p.getUniqueId()) && !p.getAllowFlight()) {
                float charge = getItemCharge(e.getItem());
                if (charge < getEnergyConsumption()) {
                    p.sendMessage(ChatColor.RED + "⚡ ¡La Gema Angelical no tiene suficiente energía! Cárgala en un Banco de Carga de Slimefun.");
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                    e.cancel();
                    return;
                }

                enabledFlightUsers.add(p.getUniqueId());
                p.setAllowFlight(true);
                setFlySpeed(0.10f);
                p.setFlySpeed(getFlySpeed());
                p.sendMessage(ChatColor.GREEN + "⚡ Vuelo angelical activado. (" + (int) charge
                        + " J disponibles - Consumo: " + (int) getEnergyConsumption() + " J/s)");
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
            } else {
                if (hasMaxFlightSpeed.getValue()) {
                    if (getFlySpeed() < maxFlightSpeed.getValue()) {
                        if (getFlySpeed() + 0.05f > maxFlightSpeed.getValue()) {
                            setFlySpeed(maxFlightSpeed.getValue().floatValue());
                        } else {
                            setFlySpeed(getFlySpeed() + 0.05f);
                        }
                    } else {
                        setFlySpeed(0.10f);
                    }
                } else {
                    if (getFlySpeed() < 0.25f) {
                        setFlySpeed(getFlySpeed() + 0.05f);
                    } else {
                        setFlySpeed(0.10f);
                    }
                }
                p.sendMessage(ChatColor.AQUA + "⚡ Velocidad de vuelo ajustada a: " + getFlySpeed());
            }

            p.setFlySpeed(getFlySpeed());
            e.getItem().setItemMeta(updateLore(e.getItem(), p));
            e.cancel();
        };
    }

    @EventHandler
    public void getItemClicked(InventoryClickEvent e) {
        List<HumanEntity> views = e.getViewers();
        if (isItem(e.getCursor()) || isItem(e.getCurrentItem())) {
            for (HumanEntity he : views) {
                if (he instanceof Player p) {
                    if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                        continue;
                    }
                    enabledFlightUsers.remove(p.getUniqueId());
                    p.setFlying(false);
                    if (!hasPermanentFlightPermission(p)) {
                        p.setAllowFlight(false);
                    }
                    p.setFallDistance(0.0f);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 160, 0));
                    fallImmunity.put(p.getUniqueId(), System.currentTimeMillis() + 10000L);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        enabledFlightUsers.remove(e.getPlayer().getUniqueId());
        fallImmunity.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            UUID uuid = p.getUniqueId();
            if (enabledFlightUsers.contains(uuid) || fallImmunity.getOrDefault(uuid, 0L) > System.currentTimeMillis()) {
                e.setCancelled(true);
                p.setFallDistance(0.0f);
                return;
            }

            ItemStack gem = findAngelGem(p);
            if (gem != null) {
                float charge = getItemCharge(gem);
                if (charge >= 50.0f) {
                    removeItemCharge(gem, 50.0f);
                    e.setCancelled(true);
                    p.setFallDistance(0.0f);
                    p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
                    p.sendActionBar(Component.text("⚡ Gema Angelical absorbió el daño de caída (-50 J)", NamedTextColor.AQUA));
                }
            }
        }
    }

    protected ItemMeta updateLore(ItemStack item, Player p) {
        ItemMeta im = item.getItemMeta();

        if (im == null || !im.hasLore()) {
            return im;
        }

        List<String> lore = im.getLore();
        boolean active = p != null && p.getAllowFlight();

        for (int line = 0; line < lore.size(); line++) {
            String str = lore.get(line);
            if (str.contains("Flight: ") || str.contains("Vuelo: ")) {
                lore.set(line, ChatColor.GRAY + (str.contains("Vuelo") ? "Vuelo: " : "Flight: ")
                        + (active ? ChatColor.GREEN + "Activado" : ChatColor.RED + "Desactivado"));
            }
            if (str.contains("Flight Speed: ") || str.contains("Velocidad: ")) {
                lore.set(line, ChatColor.GRAY + (str.contains("Velocidad") ? "Velocidad: " : "Flight Speed: ")
                        + ChatColor.YELLOW + getFlySpeed());
            }
            // Consumo y capacidad salen de Items.yml (ticket 471), no del texto fijo.
            if (str.contains("Consumo: ")) {
                lore.set(line, consumoLore());
            }
            if (str.contains("Capacidad: ")) {
                lore.set(line, capacidadLore());
            }
        }

        im.setLore(lore);
        return im;
    }

    public String consumoLore() {
        int drain = Math.max(1, (int) getEnergyConsumption());
        int seconds = (int) (energyCapacity.getValue() / drain);
        String dur = seconds >= 120 ? "~" + (seconds / 60) + " min por carga" : "~" + seconds + "s por carga";
        return ChatColor.GRAY + "Consumo: " + ChatColor.RED + drain + " J/s " + ChatColor.DARK_GRAY + "("
                + ChatColor.GRAY + dur + ChatColor.DARK_GRAY + ")";
    }

    public String capacidadLore() {
        return ChatColor.GRAY + "Capacidad: " + ChatColor.YELLOW + (int) energyCapacity.getValue().doubleValue() + " J";
    }

    public float getFlySpeed() {
        return flySpeed;
    }

    public void setFlySpeed(float newFlySpeed) {
        Preconditions.checkArgument(newFlySpeed > 0, "Must be greater then 0");

        BigDecimal bd = new BigDecimal(Float.toString(newFlySpeed));
        bd = bd.setScale(2, RoundingMode.DOWN);
        flySpeed = bd.floatValue();
    }

}
