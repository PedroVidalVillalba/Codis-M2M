plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "m2m"

include("peer")
include("server")

project(":peer").projectDir = file("m2m/peer/")
project(":server").projectDir = file("m2m/server/")
