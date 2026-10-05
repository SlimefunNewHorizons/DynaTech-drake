<div align="center">

  <img src="https://raw.githubusercontent.com/SlimefunNewHorizons/DynaTech-drake/main/banner.svg" alt="DynaTech-drake Banner" width="920" />

# ⚡ DynaTech-Drake

**Automatización de Hidroponía, Cocina Automatizada, Lavadores de Minerales y Almacenamiento Cuántico para Slimefun4**

<p>
  <a href="https://github.com/SlimefunNewHorizons/DynaTech-drake"><img src="https://img.shields.io/badge/GitHub-DynaTech--Drake-181717?style=for-the-badge&logo=github" alt="GitHub"/></a>
  <img src="https://img.shields.io/badge/Java-21_FFM_Panama-F89820?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21 FFM"/>
  <img src="https://img.shields.io/badge/Rust-FFM_Accelerated-FF4500?style=for-the-badge&logo=rust&logoColor=white" alt="Rust Native"/>
  <img src="https://img.shields.io/badge/Paper-1.21.11-00A6FB?style=for-the-badge&logo=minecraft&logoColor=white" alt="Paper 1.21.11"/>
</p>

</div>

> ### 🏰 ¡Únete a la Comunidad Oficial de DrakesCraft!
> 
> * 🎮 **IP del Servidor**: `mc.drakescraft.cl` *(Java 1.21.11 & Bedrock)*
> * 💬 **Discord Oficial**: [discord.gg/drakescraft](https://discord.gg/rv3vtXZTk7)
> * 🌐 **Web & Guía**: [web.drakescraft.cl](https://web.drakescraft.cl) — 🛒 **Tienda**: [web.drakescraft.cl/store](https://web.drakescraft.cl/store.html)
> 
> *¡Juega con este addon y más de 80 expansiones optimizadas en vivo en nuestra network de supervivencia técnica!*

---

---

## ⚡ ¿Qué es DynaTech-Drake?

`DynaTech-drake` es un addon de Slimefun4 enfocado en la **automatización agrícola, de cocina, procesamiento de minerales y logística inalámbrica** para DrakesCraft.

---

## 🧰 Máquinas y Sistemas Destacados

### 1. 🌿 Hidroponía & Agricultura Automática
- **Cámara Hidropónica (Hydroponics Chamber)**: Cultiva semillas, vegetales y plantas de Slimefun de forma 100% automatizada consumiendo energía y agua.
- **Cosechadora Eléctrica**: Cosecha automáticamente cultivos maduros dentro de un radio configurable.

### 2. 🍲 Auto-Kitchen & Cocina Automatizada
- **Cocina Automática (Auto-Kitchen)**: Prepara automáticamente comidas complejas de ExoticGarden y Slimefun sin intervención manual.

### 3. 💎 Lavador de Minerales & Prensa de Materiales
- **Lavador de Minerales Automático (Ore Washer)**: Procesa grava y mineral tamizado para extraer polvos metálicos (Hierro, Oro, Cobre, Plomo, Plata, Magnesio).
- **Prensa de Materiales (Material Press)**: Comprime polvos en lingotes y bloques compactos.

### 4. 📦 Almacenamiento Cuántico & Carga Inalámbrica
- **Almacenador Cuántico (Quantum Storage)**: Almacena millones de unidades de un solo tipo de ítem en un único bloque.
- **Cargador Inalámbrico (Wireless Charger)**: Recarga automáticamente la armadura y herramientas eléctricas de los jugadores cercanos.

---

## ⚡ Aceleración Nativa en Rust (Modelo Híbrido Cero-Riesgo)

`DynaTech-drake` incluye el puente Panama FFM **`RustNativeBridge`** para delegar el cálculo de crecimiento hidropónico y procesamiento de minerales al motor nativo `Slimefun-Rust` (`slimefun_ffi`):
- 🚀 **Procesamiento de Ticks en Nanosegundos**: Sin sobrecarga de CPU ni pausas de Garbage Collector.
- 🛡️ **Preservación Total sin Reset (SQLite 0-Reset)**: Interfaz 1:1 con la base de datos `stored-blocks.db`.

---

## 🛠️ Compilación e Instalación

```bash
# Compilar paquete JAR con Maven
mvn clean package
```

Ubica el archivo compilado `DynaTech-drake-v1.5.0.jar` en la carpeta `plugins/` de tu servidor Minecraft Paper/Purpur 1.21.11.

---

<div align="center">

**DrakesCraft Labs** · Mantenido por [**JackStar6677-1**](https://github.com/JackStar6677-1)

</div>

## Qué añade al juego

A slimefun addon that add random stuff I want
Registra alrededor de **97 objetos** en la guía de Slimefun.

Todo se fabrica y se investiga desde la guía normal (`/sf guide`), como cualquier otro contenido
de Slimefun: no hace falta ningún comando especial para empezar.

## Compatibilidad

| | |
|---|---|
| Servidor | Paper / Purpur **1.21.11** |
| Java | **21** |
| Requiere | [Slimefun4-Drake](https://github.com/SlimefunNewHorizons/Slimefun4-Drake) |
| Lado | Solo servidor — quien juega no instala nada |
| Versión | ${project.version} |

## Instalación

1. Descarga el `.jar` de la última versión.
2. Déjalo en la carpeta `plugins/` del servidor, junto a Slimefun.
3. Reinicia el servidor. Los objetos aparecen solos en la guía.

> Este addon está portado al fork de Slimefun de DrakesCraft. Con el Slimefun original puede no
> cargar, porque cambia el espacio de nombres de las clases.

## Créditos
- ProfElements

Port y mantenimiento por **DrakesCraft Labs**. La autoría original es de quien figura arriba; el detalle está en [docs/UPSTREAM_ATTRIBUTION.md](https://raw.githubusercontent.com/SlimefunNewHorizons/DynaTech-drake/main/docs/UPSTREAM_ATTRIBUTION.md).

Licencia **MIT**.

---

## 📄 License & Upstream Attribution

This project is a sovereign fork maintained by [**JackStar6677-1**](https://github.com/JackStar6677-1) under [**DrakesCraft Labs**](https://github.com/SlimefunNewHorizons).

- **Original Project:** Created by the upstream authors and the open-source community.
- **DrakesCraft Optimizations:** Modernized for Paper/Purpur 1.21.11+, Java 21, high concurrency, asynchronous safety, and exploit/duplication prevention.
- **License:** Distributed under the original **GNU General Public License v3.0 (GPLv3)** (or original upstream license). See the [LICENSE](LICENSE) file for complete terms.
