dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.github.lukesky19:SkyLib:1.4.0.0")

    compileOnly(project(":Core"))
    compileOnly(project(":Commands"))
    compileOnly(project(":Configuration"))
    compileOnly(project(":Database"))
    compileOnly(project(":GUI"))
    compileOnly(project(":Hook"))
    compileOnly(project(":DataHandler"))
    compileOnly(project(":Listener"))
    compileOnly(project(":Placeholder"))
    compileOnly(project(":Processor"))
    compileOnly(project(":Prestige"))
    compileOnly(project(":Task"))
}