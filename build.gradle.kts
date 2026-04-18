plugins {
    `java-library`
    `maven-publish`
    jacoco
}

group = "com.github.lukesky19"
version = "2.0.0.0"

repositories {
    mavenLocal()
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
    maven("https://repo.codemc.io/repository/bentoboxworld/") {
        name = "CodeMC - BentoBox"
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
    maven("https://repo.leonardobishop.com/releases/") {
        name = "LMB Quests Repo"
    }
    maven("https://repo.nightexpressdev.com/releases") {
        name = "ExcellentCrates"
    }
    mavenCentral()
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    // SkyLib
    compileOnly("com.github.lukesky19:SkyLib:2.0.0.0")
    testImplementation("com.github.lukesky19:SkyLib:2.0.0.0")

    // Integration
    compileOnly("world.bentobox:bentobox:2.7.0-SNAPSHOT")
    testImplementation("world.bentobox:bentobox:2.7.0-SNAPSHOT")
    testImplementation("world.bentobox:level:2.8.1-SNAPSHOT")
    testImplementation("world.bentobox:bank:1.9.0-SNAPSHOT")

    compileOnly("world.bentobox:magiccobblestonegenerator:2.6.0-SNAPSHOT") {
        exclude("org.bukkit", "bukkit")
    }
    testImplementation("world.bentobox:magiccobblestonegenerator:2.6.0-SNAPSHOT") {
        exclude("org.bukkit", "bukkit")
    }

    compileOnly("me.clip:placeholderapi:2.11.7")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("dev.rosewood:rosestacker:1.5.39")
    compileOnly("su.nightexpress.excellentcrates:ExcellentCrates:6.6.1")
    compileOnly("su.nightexpress.nightcore:main:2.13.4")
    compileOnly("com.olziedev:playerauctions-api:1.32.1")
    compileOnly("com.leonardobishop:quests:3.14.2")
    compileOnly("net.luckperms:api:5.4")
    testImplementation("net.luckperms:api:5.4")

    compileOnly("com.github.lukesky19:SkyPlayTime:0.2.0.0")
    compileOnly("com.github.lukesky19:SkyShop:3.3.0.0")
    compileOnly("com.github.lukesky19:SkySellWands:1.5.1.0")
    compileOnly("com.github.lukesky19:SkyHoppers:1.2.0.0")
    compileOnly("com.github.lukesky19:SkyEnchants:0.2.0.0")

    // Test Dependencies
    testImplementation("org.xerial:sqlite-jdbc:3.51.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.14.1")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.14.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.21.0")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.108.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
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

    test {
        useJUnitPlatform()

        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)

        reports {
            xml.required = false
            csv.required = false
            html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
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
        dependsOn(publishToMavenLocal)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}