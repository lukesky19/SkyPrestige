dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.github.lukesky19:SkyLib:1.4.0.0")

    // Hooks
    compileOnly("world.bentobox:bentobox:2.7.0-SNAPSHOT")
    compileOnly("dev.rosewood:rosestacker:1.5.32")
    compileOnly("com.github.lukesky19:SkyPlayTime:1.0.0.0")

    compileOnly(project(":Core"))
    compileOnly(project(":Configuration"))
    compileOnly(project(":Data"))
    compileOnly(project(":Database"))
    compileOnly(project(":GUI"))
    compileOnly(project(":Hook"))
    compileOnly(project(":Island"))
    compileOnly(project(":Multiplier"))
    compileOnly(project(":Processor"))
    compileOnly(project(":Prestige"))
    compileOnly(project(":Protection"))
}