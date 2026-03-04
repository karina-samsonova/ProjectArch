plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "com.samsonova"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "ProjectArch"
        id="com.samsonova.projectarch"
    }
}

dependencies {
    intellijPlatform {
        local("/Applications/Android Studio.app/Contents")
    }
}