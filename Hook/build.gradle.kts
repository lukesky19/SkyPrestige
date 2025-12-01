dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.github.lukesky19:SkyLib:1.4.0.0")

    // Modules
    compileOnly(project(":Core"))
    compileOnly(project(":Configuration"))
    compileOnly(project(":Data"))

    // Hooks
    compileOnly("world.bentobox:bentobox:2.7.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.7")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("world.bentobox:bskyblock:1.18.1-SNAPSHOT")
    compileOnly("world.bentobox:magiccobblestonegenerator:2.6.0-SNAPSHOT") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("dev.rosewood:rosestacker:1.5.32")
    compileOnly("com.github.lukesky19:SkyPlayTime:1.0.0.0")
    compileOnly("com.github.lukesky19:SkySellWands:1.4.0.0")
    compileOnly("com.olziedev:playerauctions-api:1.32.1")
}