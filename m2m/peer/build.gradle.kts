plugins {
    id("m2m.java-conventions")
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

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/java/m2m/peer"))
        }
        resources {
            setSrcDirs(listOf("src/resources"))
        }
    }
}
