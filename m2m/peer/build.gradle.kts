plugins {
    id("m2m.java-conventions")
    id("application")
    alias(libs.plugins.springboot)
    alias(libs.plugins.javafx.plugin)
}

application {
    mainClass = "m2m.peer.PeerMain"
}

javafx {
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(project(":shared"))
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/java"))
        }
        resources {
            setSrcDirs(listOf("src/resources"))
        }
    }
}
