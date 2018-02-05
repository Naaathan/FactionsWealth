# FactionsWealth
Calculate the wealth of a faction from their claimed land. Configurable messages and values.
**Please note: only FactionsUUID is currently supported with this plugin, more Factions versions will be supported in the near future.**

#### Commands
##### Recalculation
* `/f recalculate <faction>` Recalculates `<faction>`'s value. (Permission: `factions.recalculate`)
* `/f recalculatewealth [start/stop]` Starts/stops a server-wide faction recalculation task. (Permission: `factions.recalculatewealth`)
##### Wealth
* `/f top/wealth [page=1]` Shows page `[page]` of faction wealth rankings with a detailed description in tooltips. (Permission: `factions.wealth`)

#### Setting up your project
After cloning the repository, create a folder in your project's base directory named `lib`. In this folder, the following external plugin JAR files are required:
* FactionsUUID (Named `Factions.jar`).

**When new versions of dependencies are added, the names of their JAR files may change in the project's** `pom.xml` **file.**
