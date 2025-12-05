plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

group = "com.github.lukesky19"
version = "1.1.0.0"

subprojects {
    apply(plugin = "java")

    group = "com.github.lukesky19"
    version = "1.1.0.0"

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
}

dependencies {
    implementation(project(":Commands"))
    implementation(project(":Configuration"))
    implementation(project(":Core"))
    implementation(project(":Data"))
    implementation(project(":Database"))
    implementation(project(":GUI"))
    implementation(project(":Hook"))
    implementation(project(":DataHandler"))
    implementation(project(":Listener"))
    implementation(project(":Placeholder"))
    implementation(project(":Plugin"))
    implementation(project(":Prestige"))
    implementation(project(":Processor"))
    implementation(project(":Task"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }

        archiveClassifier.set("")

        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }
}