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
        p.setAllowFlight(true); // Granted by AntigravityBubble alone

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
    @DisplayName("actualizarLineasLore reflects AngelGem state rather than raw allowFlight")
    void testLoreReflectsGemState() {
        List<String> lore = new ArrayList<>(Arrays.asList(
                "Consumo: 16 J/s",
                "Capacidad: 10240 J",
                "Flight: <enabled>",
                "Flight Speed: <speed>"
        ));

        AngelGem.actualizarLineasLore(lore, false, 0.10f, "Consumo: 16 J/s", "Capacidad: 10240 J");
        Assertions.assertTrue(lore.get(2).contains("Desactivado"));
        Assertions.assertTrue(lore.get(3).contains("0.1"));

        AngelGem.actualizarLineasLore(lore, true, 0.15f, "Consumo: 16 J/s", "Capacidad: 10240 J");
        Assertions.assertTrue(lore.get(2).contains("Activado"));
        Assertions.assertTrue(lore.get(3).contains("0.15"));
    }
}
