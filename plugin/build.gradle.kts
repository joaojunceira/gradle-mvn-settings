plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Maven Settings Builder for parsing settings.xml
    implementation(libs.maven.settings.builder)
    
    // Plexus Cipher and Sec Dispatcher for decryption
    implementation(libs.plexus.cipher)
    implementation(libs.plexus.sec.dispatcher)
    
    // Sisu Inject / Guice might be needed by Maven components, but let's see if transitive deps cover it.
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter(libs.versions.junit.jupiter)
        }
    }
}

gradlePlugin {
    // Define the plugin
    plugins.create("mvnSettings") {
        id = "io.mvgrd"
        implementationClass = "io.mvgrd.GradleMvnSettingsPlugin"
        displayName = "Gradle Maven Settings Plugin"
        description = "A Gradle plugin to load repositories and authentication from Maven settings.xml"
    }
}
