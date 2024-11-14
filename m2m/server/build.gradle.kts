plugins {
    id("m2m.java-conventions")
    alias(libs.plugins.springboot)
}

application {
    mainClass = "m2m.server.ServerMain"
}

dependencies {
    implementation(libs.postgresql)
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/java/m2m/server"))
        }
        resources {
            setSrcDirs(listOf("src/resources"))
        }
    }
}
