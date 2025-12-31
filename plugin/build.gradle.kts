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
    implementation("org.apache.maven:maven-settings-builder:3.9.6")
    
    // Plexus Cipher and Sec Dispatcher for decryption
    implementation("org.codehaus.plexus:plexus-cipher:2.0")
    implementation("org.codehaus.plexus:plexus-sec-dispatcher:2.0")
    
    // Sisu Inject / Guice might be needed by Maven components, but let's see if transitive deps cover it.
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.12.1")
        }

        // Create a new test suite
        val functionalTest by registering(JvmTestSuite::class) {
            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(project())
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) } 
                }
            }
        }
    }
}

gradlePlugin {
    // Define the plugin
    val greeting by plugins.creating {
        id = "io.mvgrd.greeting"
        implementationClass = "io.mvgrd.GradleMvnSettingsPlugin"
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
    dependsOn(testing.suites.named("functionalTest"))
}
