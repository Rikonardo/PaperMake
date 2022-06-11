<div align="center"><h1>PaperMake - Minecraft plugin development tool</h1></div>

<div align="center"><img alt="Logo" src="logo.png"/></div>

<br>

<div align="center">
    <a href="https://github.com/Rikonardo/PaperMake/issues"><img alt="Open issues" src="https://img.shields.io/github/issues-raw/Rikonardo/PaperMake"/></a>
    <a href="https://plugins.gradle.org/plugin/com.rikonardo.papermake"><img alt="Gradle plugin" src="https://img.shields.io/gradle-plugin-portal/v/com.rikonardo.papermake"/></a>
    <a href="https://www.codefactor.io/repository/github/rikonardo/papermake"><img alt="CodeFactor" src="https://www.codefactor.io/repository/github/rikonardo/papermake/badge"/></a>
    <a href="https://discord.gg/zYRTPa3FnQ"><img alt="Discord" src="https://img.shields.io/discord/982967258013896734?color=%237289DA&label=discord&logo=discord&logoColor=%237289DA"></a>
</div>

<br>

<hr>

**PaperMake** - gradle plugin, designed to simplify development and debugging of Bukkit-based plugins. It provides ability to run development Paper server and quickly reload your plugin to see changes in-game.

## Features:
- **Development server with IDE integration ✅**
- **Gradle Shadow plugin compatibility ✅**
- **In-game plugin auto-reload on gradle build and shadowJar tasks ✅**
- **Working breakpoints in your plugin code and libraries ✅**
- **Development server console right in IDE ✅**

## Installation:
To install PaperMake, add this on top of your `build.gradle` file:

```groovy
plugins {
    id 'com.rikonardo.papermake' version '1.0.0'
}
```

*You can add `id 'com.rikonardo.papermake' version '1.0.0'` inside of your existing `plugins { }` block.*

### Kotlin DSL:
Add this on top of your `build.gradle.kts` file:

```kotlin
plugins {
    id("com.rikonardo.papermake") version "1.0.0"
}
```

*You can add `id("com.rikonardo.papermake") version "1.0.0"` inside of your existing `plugins { }` block.*

## Usage:
Run `devServer` task to start development server.

You can use next optional properties to configure environment:

| Property        | Description                                                                                                                                           |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| `pmake.version` | Minecraft version of development server. Can be any version, which has available paper release. By default, PaperMake wold use latest release.        |
| `pmake.port`    | Port for development server. Default port: `25565`. Note, that if port unavailable, PaperMake would try to use port, incremented by 1 (e.g. `25566`). |
| `pmake.server`  | Path to custom server jar. If specified, `pmake.version` property will be ignored.                                                                    |
| `pmake.gui`     | When `true`, removes default "-nogui" server arg that prevents server gui window from appearing.                                                      |
| `pmake.args`    | Additional arguments for development server. Fore example, `-o=false` will disable online-mode.                                                       |

Properties are specified with `-P` prefix. Here's an example:
```shell
./gradlew devServer -Ppmake.version=1.16.4 -Ppmake.port=25575 -Ppmake.args="-o=false -s=100"
```

You can also use this properties in IDE. For example in IntelliJ IDEA, you can specify them in "Edit Run/Debug Configurations" dialog in "Run" field after devServer task name.
