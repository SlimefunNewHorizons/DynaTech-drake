package me.profelements.dynatech.items.tools;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unit tests for AngelGem + AntigravityBubble flight state isolation (Tickets #536 / #537).
 */
class AngelGemFlightTest {

    private static ServerMock server;
    private static PluginMock mockPlugin;

    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        mockPlugin = MockBukkit.createMockPlugin("DynaTechTest");
    }

    @AfterEach
    void cleanState() {
        AngelGem.enabledFlightUsers.clear();
        AngelGem.fallImmunity.clear();
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("vueloProtegido distinguishes active AngelGem / fly perm from AntigravityBubble allowFlight")
    void testVueloProtegidoIsolation() {
        PlayerMock p = server.addPlayer("StoneAgeKing");
        p.setAllowFlight(true);

        Assertions.assertFalse(AngelGem.vueloProtegido(p),
                "AllowFlight alone from AntigravityBubble must not mark flight as protected");

        AngelGem.enabledFlightUsers.add(p.getUniqueId());
        Assertions.assertTrue(AngelGem.vueloProtegido(p),
                "Active AngelGem user must be protected from AntigravityBubble flight removal");

        AngelGem.enabledFlightUsers.remove(p.getUniqueId());
        p.addAttachment(mockPlugin, "drakescraft.vip.fly", true);
        Assertions.assertTrue(AngelGem.vueloProtegido(p),
                "Permanent fly permission must also be protected from AntigravityBubble flight removal");
    }

    @Test
    @DisplayName("Flight speed is isolated per player and cleared on quit")
    void testPerPlayerSpeedIsolation() {
        Map<UUID, Float> velocidades = new ConcurrentHashMap<>();
        PlayerMock p1 = server.addPlayer("PilotOne");
        PlayerMock p2 = server.addPlayer("PilotTwo");

        AngelGem.ajustarVelocidadJugador(velocidades, p1, 0.20f);
        Assertions.assertEquals(0.20f, AngelGem.obtenerVelocidadJugador(velocidades, p1), 0.001f);
        Assertions.assertEquals(0.20f, p1.getFlySpeed(), 0.001f);
        Assertions.assertEquals(0.10f, AngelGem.obtenerVelocidadJugador(velocidades, p2), 0.001f,
                "Second player speed must remain default 0.10f");

        velocidades.remove(p1.getUniqueId());
        Assertions.assertEquals(0.10f, AngelGem.obtenerVelocidadJugador(velocidades, p1), 0.001f);
    }

    @Test
    @DisplayName("actualizarLineasLore reflects AngelGem state in universal English")
    void testLoreReflectsGemState() {
        List<String> lore = new ArrayList<>(Arrays.asList(
                "Drain: 16 J/s",
                "Capacity: 10240 J",
                "Flight: <enabled>",
                "Flight Speed: <speed>"
        ));

        AngelGem.actualizarLineasLore(lore, false, 0.10f, "Drain: 16 J/s", "Capacity: 10240 J");
        Assertions.assertTrue(lore.get(2).contains("Disabled"));
        Assertions.assertTrue(lore.get(3).contains("0.1"));

        AngelGem.actualizarLineasLore(lore, true, 0.15f, "Drain: 16 J/s", "Capacity: 10240 J");
        Assertions.assertTrue(lore.get(2).contains("Enabled"));
        Assertions.assertTrue(lore.get(3).contains("0.15"));
    }

    @Test
    @DisplayName("AntigravityBubble chunk unload revokes multiple bubbles in same chunk without revoking active AngelGem")
    void testChunkUnloadMultiBubbleAndAngelGemProtection() {
        org.bukkit.World world = server.addSimpleWorld("bubble_world");
        PlayerMock bubbleOnlyPlayer = server.addPlayer("BubbleUser");
        PlayerMock gemPlayer = server.addPlayer("GemUser");
        bubbleOnlyPlayer.setAllowFlight(true);
        bubbleOnlyPlayer.setFlying(true);
        gemPlayer.setAllowFlight(true);
        gemPlayer.setFlying(true);
        AngelGem.enabledFlightUsers.add(gemPlayer.getUniqueId());

        Map<org.bukkit.Location, java.util.Set<UUID>> tracked = new java.util.HashMap<>();
        org.bukkit.Location b1 = new org.bukkit.Location(world, 32, 70, 48); // chunk (2, 3)
        org.bukkit.Location b2 = new org.bukkit.Location(world, 40, 80, 55); // same chunk (2, 3)
        org.bukkit.Location otherChunk = new org.bukkit.Location(world, 100, 70, 100); // chunk (6, 6)

        tracked.put(b1, new java.util.HashSet<>(List.of(gemPlayer.getUniqueId())));
        tracked.put(b2, new java.util.HashSet<>(List.of(bubbleOnlyPlayer.getUniqueId())));
        tracked.put(otherChunk, new java.util.HashSet<>());

        me.profelements.dynatech.items.electric.AntigravityBubble.revocarVueloEnChunk(tracked, world, 2, 3);

        Assertions.assertFalse(bubbleOnlyPlayer.getAllowFlight(),
                "Second bubble in unloaded chunk must revoke bubble-only flight (no premature break)");
        Assertions.assertTrue(gemPlayer.getAllowFlight(),
                "Active AngelGem user must retain flight when AntigravityBubble chunk unloads");
        Assertions.assertEquals(1, tracked.size(), "Only bubbles from unloaded chunk (2, 3) should be removed");
        Assertions.assertTrue(tracked.containsKey(otherChunk));
    }
}
