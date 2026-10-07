package me.profelements.dynatech.items.electric;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.profelements.dynatech.DynaTech;
import me.profelements.dynatech.items.abstracts.AbstractElectricTicker;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AntigravityBubble extends AbstractElectricTicker implements Listener {

    private final Map<Location, Set<UUID>> enabledPlayers = new HashMap<>();

	public AntigravityBubble(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
		super(itemGroup, item, recipeType, recipe);
        Bukkit.getPluginManager().registerEvents(this, DynaTech.getInstance());
	}

    @Override
    protected void onPlace(BlockPlaceEvent e, Block blockPlaced) {
        enabledPlayers.put(blockPlaced.getLocation(), new HashSet<>()); 
    }

    @Override
    protected void onBreak(BlockBreakEvent e, Location l) {
        // Tras un reinicio el mapa esta vacio aunque el bloque exista, asi que get() devolvia
        // null y romper la burbuja lanzaba NullPointerException en mitad del evento.
        Set<UUID> registrados = enabledPlayers.remove(l);
        if (registrados == null) {
            return;
        }

        for (UUID id : registrados) {
            Player player = Bukkit.getPlayer(id);
            if (player == null) {
                continue;
            }
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setFallDistance(0.f);
        }
    }

    /**
     * Al teletransportarse se retira el vuelo en el acto.
     *
     * Antes se apuntaba el UUID en una lista que el tick vaciaba entera con clear(), asi que la
     * primera burbuja en ticar borraba los teletransportes de todas las demas y el jugador se
     * llevaba el vuelo puesto a donde fuera. Resolverlo aqui no depende de que tique nadie.
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        UUID id = player.getUniqueId();

        for (Set<UUID> registrados : enabledPlayers.values()) {
            if (registrados.remove(id)) {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.setFallDistance(0.f);
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        for (Map.Entry<Location, Set<UUID>> entry : enabledPlayers.entrySet()) {
            if (entry.getKey().getChunk() == e.getChunk()) {
                Set<UUID> players = enabledPlayers.getOrDefault(entry.getKey(), new HashSet<>());
                for (Iterator<UUID> iterator = players.iterator(); iterator.hasNext();) {
                    Player p = Bukkit.getPlayer(iterator.next()); 

                     if (p != null) {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                        p.setFallDistance(0.f);

                        iterator.remove();
                    }      
                }
                break;
            }
        }
        enabledPlayers.entrySet().removeIf(entry -> entry.getKey().getChunk().equals(e.getChunk()));
    }

	@Override
	protected void tick(Block b, SlimefunItem item) {
        Collection<Entity> bubbledEntities = b.getWorld().getNearbyEntities(b.getLocation(), 16, 16, 16, Player.class::isInstance);
        for (Entity entity : bubbledEntities) {
            Player p = (Player) entity; 

            // La condicion original usaba || entre dos gamemodes distintos, asi que siempre era
            // cierta: la burbuja tambien "activaba" el vuelo de quien ya estaba en creativo.
            if (!p.getAllowFlight() && p.getGameMode() != GameMode.CREATIVE
                    && p.getGameMode() != GameMode.SPECTATOR) {
                // computeIfAbsent y no getOrDefault: este devolvia un conjunto nuevo y desechable
                // cuando la burbuja no estaba en el mapa --lo normal tras un reinicio, porque el
                // mapa vive en memoria y los bloques siguen ticando--, asi que el jugador recibia
                // el vuelo pero no quedaba registrado y nadie se lo retiraba nunca. De ahi el fly
                // permanente con solo pasar cerca de una burbuja ajena.
                enabledPlayers.computeIfAbsent(b.getLocation(), llave -> new HashSet<>())
                        .add(p.getUniqueId());
                p.setAllowFlight(true);
            }
        }

        Set<UUID> players = enabledPlayers.getOrDefault(b.getLocation(), new HashSet<>());
        for (Iterator<UUID> iterator = players.iterator(); iterator.hasNext(); ) {
            UUID id = iterator.next();
            Player p = Bukkit.getPlayer(id);

            // Si el jugador se fue del servidor tambien hay que soltar su registro, o el conjunto
            // crece sin fin y al volver conserva un vuelo que ya nadie vigila.
            if (p == null || !p.isOnline()) {
                iterator.remove();
                continue;
            }

            if (!bubbledEntities.contains(p)) {
                p.setAllowFlight(false);
                p.setFlying(false);
                p.setFallDistance(0.f);

                iterator.remove();
            }
        }
	}

    @Override
    protected boolean isSynchronized() {
        return true;
    }
}
