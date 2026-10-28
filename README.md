<div align="center">

  <img src="https://raw.githubusercontent.com/SlimefunNewHorizons/DynaTech-drake/main/banner.svg" alt="DynaTech-drake Banner" width="920" />

# ⚡ DynaTech-Drake

**Hydroponics Automation, Automated Cooking, Ore Washers and Quantum Storage for Slimefun4**

<p>
  <a href="https://github.com/SlimefunNewHorizons/DynaTech-drake"><img src="https://img.shields.io/badge/GitHub-DynaTech--Drake-181717?style=for-the-badge&logo=github" alt="GitHub"/></a>
  <img src="https://img.shields.io/badge/Java-21_FFM_Panama-F89820?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21 FFM"/>
  <img src="https://img.shields.io/badge/Rust-FFM_Accelerated-FF4500?style=for-the-badge&logo=rust&logoColor=white" alt="Rust Native"/>
  <img src="https://img.shields.io/badge/Paper-1.21.11-00A6FB?style=for-the-badge&logo=minecraft&logoColor=white" alt="Paper 1.21.11"/>
</p>

</div>

> ### 🏰 Join the Official DrakesCraft Community!
> 
> * 🎮 **Server IP**: `mc.drakescraft.cl` *(Java 1.21.11 & Bedrock)*
> * 💬 **Official Discord**: [discord.gg/drakescraft](https://discord.gg/rv3vtXZTk7)
> * 🌐 **Web & Guide**: [web.drakescraft.cl](https://web.drakescraft.cl) — 🛒 **Store**: [web.drakescraft.cl/store](https://web.drakescraft.cl/store.html)
> 
> *Play with this addon and 80+ more optimized expansions live on our technical survival network!*

---

---

## ⚡ What is DynaTech-Drake?

`DynaTech-drake` is a Slimefun4 addon focused on **farming automation, cooking, ore processing and wireless logistics** for DrakesCraft.

---

## 🧰 Featured Machines and Systems

### 1. 🌿 Hydroponics & Automated Farming
- **Hydroponics Chamber**: Grows seeds, vegetables and Slimefun plants fully automatically, consuming energy and water.
- **Electric Harvester**: Automatically harvests mature crops within a configurable radius.

### 2. 🍲 Auto-Kitchen & Automated Cooking
- **Auto-Kitchen**: Automatically prepares complex ExoticGarden and Slimefun meals with no manual intervention.

### 3. 💎 Ore Washer & Material Press
- **Automatic Ore Washer**: Processes gravel and sifted ore to extract metal dusts (Iron, Gold, Copper, Lead, Silver, Magnesium).
- **Material Press**: Compresses dusts into ingots and compact blocks.

### 4. 📦 Quantum Storage & Wireless Charging
- **Quantum Storage**: Stores millions of units of a single item type in one block.
- **Wireless Charger**: Automatically recharges the electric armor and tools of nearby players.

---

## ⚡ Native Rust Acceleration (Zero-Risk Hybrid Model)

`DynaTech-drake` includes the Panama FFM bridge **`RustNativeBridge`** to delegate hydroponic growth and ore processing calculations to the native `Slimefun-Rust` engine (`slimefun_ffi`):
- 🚀 **Nanosecond Tick Processing**: No CPU overhead or Garbage Collector pauses.
- 🛡️ **Full Preservation, No Reset (SQLite 0-Reset)**: 1:1 interface with the `stored-blocks.db` database.

---

## 🛠️ Build and Installation

```bash
# Build the JAR package with Maven
mvn clean package
```

Place the built `DynaTech-drake-v1.5.0.jar` in the `plugins/` folder of your Paper/Purpur 1.21.11 Minecraft server.

Dependencies are resolved from the Drake Maven repository, <https://maven.drakescraft.cl> (GitHub Pages of
[`SlimefunNewHorizons/maven-repo`](https://github.com/SlimefunNewHorizons/maven-repo)):

| Line | Core | Java |
|---|---|---|
| 1.21.11 (production) | `slimefun-core 11.0-Drake-1.21.11-SNAPSHOT` | 21 |
| Universal 26.x (Paper 26.2) | `slimefun-core 11.0-Universal-26.x-SNAPSHOT` + `dough-core 1.3.1-Universal-26.x-SNAPSHOT` | 25 |

To use an already built jar on the 26.x line, port it with `scripts/portar_addons.py <input> <output>` from `ai-hub`
(it relocates the package from the Dallas fork to the universal one and raises `api-version` to 1.21).

The 1.21.11 line requires `slimefun-core` build 4 or later (it uses `getRefundInputSlots`).

---

<div align="center">

**DrakesCraft Labs** · Maintained by [**JackStar6677-1**](https://github.com/JackStar6677-1)

</div>

## What it adds to the game

A slimefun addon that add random stuff I want
Registers around **97 items** in the Slimefun guide.

Everything is crafted and researched from the normal guide (`/sf guide`), like any other Slimefun
content: no special command is needed to get started.

## Compatibility

| | |
|---|---|
| Server | Paper / Purpur **1.21.11** |
| Java | **21** |
| Requires | [Slimefun4-Drake](https://github.com/SlimefunNewHorizons/Slimefun4-Drake) |
| Side | Server-side only — players install nothing |
| Version | ${project.version} |

## Installation

1. Download the `.jar` of the latest version.
2. Drop it in the server's `plugins/` folder, next to Slimefun.
3. Restart the server. The items appear in the guide on their own.

> This addon is ported to the DrakesCraft Slimefun fork. It may fail to load with the original
> Slimefun, because the class namespace changes.

## Credits
- ProfElements

Port and maintenance by **DrakesCraft Labs**. Original authorship belongs to those listed above; details are in [docs/UPSTREAM_ATTRIBUTION.md](https://raw.githubusercontent.com/SlimefunNewHorizons/DynaTech-drake/main/docs/UPSTREAM_ATTRIBUTION.md).

License **MIT**.

---

## 📄 License & Upstream Attribution

This project is a sovereign fork maintained by [**JackStar6677-1**](https://github.com/JackStar6677-1) under [**DrakesCraft Labs**](https://github.com/SlimefunNewHorizons).

- **Original Project:** Created by the upstream authors and the open-source community.
- **DrakesCraft Optimizations:** Modernized for Paper/Purpur 1.21.11+, Java 21, high concurrency, asynchronous safety, and exploit/duplication prevention.
- **License:** Distributed under the original **GNU General Public License v3.0 (GPLv3)** (or original upstream license). See the [LICENSE](LICENSE) file for complete terms.
