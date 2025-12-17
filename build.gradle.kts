plugins {
    java
}

group = "com.github.lukesky19"
version = "1.1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "codemc"
    }
    maven("https://repo.rosewooddev.io/repository/public/") {
        name = "RoseWood"
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        name = "PlaceholderAPI Repo"
    }
    maven("https://repo.olziedev.com/") {
        name = "PlayerAuctions Repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.lukesky19:SkyLib:1.4.0.0")

    // Hooks
    compileOnly("world.bentobox:bentobox:2.7.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.7")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("world.bentobox:magiccobblestonegenerator:2.6.0-SNAPSHOT") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("dev.rosewood:rosestacker:1.5.32")
    compileOnly("com.github.lukesky19:SkyPlayTime:1.0.0.0")
    compileOnly("com.github.lukesky19:SkySellWands:1.4.0.0")
    compileOnly("com.olziedev:playerauctions-api:1.32.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    javadoc {
        source = sourceSets["main"].allJava
        classpath = files() + configurations["compileClasspath"]

        (options as StandardJavadocDocletOptions).apply {
            tags("apiNote:a:API Note:")
            addStringOption("sourcepath", "")
        }
    }

    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }

        archiveClassifier.set("")
    }

    build {
        dependsOn(javadoc)
    }
}