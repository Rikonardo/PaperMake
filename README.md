<div align="center"><h1>PaperMake - Minecraft plugin development tool</h1></div>

<div align="center"><img alt="Logo" src="logo.png"/></div>

<br>

<div align="center">
    <a href="https://github.com/Rikonardo/PaperMake/issues"><img alt="Open issues" src="https://img.shields.io/github/issues-raw/Rikonardo/PaperMake"/></a>
    <a href="https://plugins.gradle.org/plugin/com.rikonardo.papermake"><img alt="Gradle plugin" src="https://img.shields.io/gradle-plugin-portal/v/com.rikonardo.papermake"/></a>
    <a href="https://www.codefactor.io/repository/github/rikonardo/papermake"><img alt="CodeFactor" src="https://www.codefactor.io/repository/github/rikonardo/papermake/badge"/></a>
    <a href="https://discord.gg/zYRTPa3FnQ"><img alt="Discord" src="https://img.shields.io/discord/982967258013896734?color=%237289DA&label=discord&logo=discord&logoColor=%237289DA"></a>
</div>

<hr>

**PaperMake** is a gradle plugin, designed to simplify development and debugging of Bukkit-based plugins. It provides ability to run development Paper server and quickly reload your plugin to see changes in-game.

## Features
- **Development server with IDE integration ✅**
- **Gradle Shadow plugin compatibility ✅**
- **In-game plugin auto-reload on Gradle's "build", "shadowJar", and "reobfJar" tasks ✅**
- **Working breakpoints in your plugin code and libraries ✅**
- **Development server console right in IDE ✅**
- **Useful in-game commands ✅**

## Installation
To install PaperMake, add this on top of your `build.gradle` file, to the end of `plugins { }` block:

```groovy
plugins {
    id 'com.rikonardo.papermake' version '1.0.4'
}
```

### Kotlin DSL
Add this on top of your `build.gradle.kts` file, to the end of `plugins { }` block:

```kotlin
plugins {
    id("com.rikonardo.papermake") version "1.0.4"
}
```

### Notice
PaperMake should always be specified after java/kotlin and shadow (if you are using it) plugins. Here is an example of correct plugins order:

```groovy
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '...'
    id 'com.rikonardo.papermake' version '...'
}
```

## Usage
Run `devServer` task (from `papermake` category) to start development server.

You can use next optional properties to configure environment:

| Property         | Description                                                                                                                                                 |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `pmake.version`  | Minecraft version of development server. Can be any version, which has available paper release. By default, PaperMake would use latest release.             |
| `pmake.mojmap`   | If `true`, downloads Paper version that uses Mojang's deobfuscation mappings.                                                                               |
| `pmake.noverify` | If `true`, checksum verification of the downloaded Paper server is not performed.                                                                           |
| `pmake.port`     | Port for development server. Default port: `25565`. Note, that if port unavailable, PaperMake would try to use port, incremented by 1 (e.g. `25566`).       |
| `pmake.dir`      | Path to the directory where dev server will be launched, can be relative to project directory. By default, server runs in `build/papermake/run`.            |
| `pmake.server`   | Path to custom server jar, can be relative to run directory. If specified, `pmake.version`, `pmake.mojmap` and `pmake.noverify` properties will be ignored. |
| `pmake.gui`      | When `true`, removes default "-nogui" server arg that prevents server gui window from appearing.                                                            |
| `pmake.autoop`   | When `true`, *all* players that join the server will be OPed.                                                                                               |
| `pmake.args`     | Additional arguments for development server. Fore example, `-o=false` will disable online-mode.                                                             |

Properties are specified with `-P` prefix. Here's an example:
```shell
./gradlew devServer -Ppmake.version=1.16.4 -Ppmake.port=25575 -Ppmake.args="-o=false -s=100"
```

You can also use this properties in IDE. For example in IntelliJ IDEA, you can specify them in "Edit Run/Debug Configurations" dialog in "Run" field after devServer task name.

When development server is running, you can use `build` or `shadowJar` (when using shadow plugin) tasks to build and automatically reload your plugin.

## In-game commands
PaperMake provides some useful in-game commands to simplify development.

| Command                                | Description                                               |
|----------------------------------------|-----------------------------------------------------------|
| `/pmake`                               | Show help message.                                        |
| `/pmake info`                          | Display development environment information.              |
| `/pmake reload`                        | Reload developed plugin without rebuilding it.            |
| `/pmake console <command>`             | Run command as console without leaving game.              |
| `/pmake plugin load <plugin jar name>` | Load external plugin from "plugins" directory in runtime. |
| `/pmake plugin unload <plugin name>`   | Unload external plugin completely (with class unloading). |
| `/pmake plugin enable <plugin name>`   | Enable disabled plugin.                                   |
| `/pmake plugin disable <plugin name>`  | Disable plugin without unloading.                         |
| `/pmake plugin reload <plugin name>`   | Reload plugin without unloading, just disable and enable. |

## Additional server configuration
You can go into server directory (`build/papermake/run` by default) and edit server configuration files.

## Installing other Minecraft plugins
Additional Minecraft plugins can be placed into `plugins` folder inside of server directory (`build/papermake/run` by default). You can use `/pmake plugin load` in-game command to load them without restarting development server. They will load as usual and PaperMake will not reload them when reloading your plugin.
