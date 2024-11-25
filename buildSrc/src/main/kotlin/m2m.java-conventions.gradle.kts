fun DependencyHandler.implementationFromCatalog(libName: String) {
    versionCatalogs
    .named("libs")
    .findLibrary(libName)
    .ifPresentOrElse(
        { implementation(it) },
        { logger.warn("Library '$libName' not found in version catalog.") }
    )
}


plugins {
    id("application")
}

group = "m2m"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementationFromCatalog("guava")
    implementationFromCatalog("postgresql")
}
