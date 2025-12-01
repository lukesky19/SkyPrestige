dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.github.lukesky19:SkyLib:1.4.0.0")

    // Modules
    compileOnly(project(":Core"))
    compileOnly(project(":Data"))
    compileOnly(project(":Hook"))
    compileOnly(project(":Island"))
    compileOnly(project(":Leaderboard"))

    // Hooks
    compileOnly("world.bentobox:bentobox:2.7.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.7")
}