plugins {
    id("m2m.java-conventions")
    id("application")
    alias(libs.plugins.springboot)
}

application {
    mainClass = "m2m.server.ServerMain"
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.postgresql)
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
