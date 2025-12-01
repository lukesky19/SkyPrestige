rootProject.name = "SkyPrestige"

include("Commands")
include("Configuration")
include("Core")
include("Data")
include("Database")
include("GUI")
include("Hook")
include("Island")
include("Leaderboard")
include("Listener")
include("Multiplier")
include("Placeholder")
include("Plugin")
include("Prestige")
include("Processor")
include("Protection")
include("Task")
include("Vault")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

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
}