# Gradle Maven Settings Plugin

> [!CAUTION]
> **Work in Progress**: This plugin is currently under active development. Features and APIs are subject to change and may not be fully stable.

[![WIP](https://img.shields.io/badge/status-WIP-orange.svg)](https://github.com/joaojunceira/gradle-mvn-settings)

A Gradle plugin that allows you to load repositories, authentication credentials, and proxies directly from your Maven `settings.xml` file. This acts as a bridge between your existing Maven configuration and your Gradle build, making it easier to transition or work in a mixed environment.

## Features

-   **Repositories**: Automatically adds repositories defined in your active Maven profiles to your Gradle build.
-   **Authentication**: Reads username and password from `<servers>` configuration, including support for encrypted passwords (using `settings-security.xml`).
-   **Mirrors**: Respects mirror configurations to redirect repository requests.
-   **Proxies**: Detects and logs active proxy configurations (can be extended to configure system properties).

## Usage

Apply the plugin in your `build.gradle.kts` (Kotlin) or `build.gradle` (Groovy):

```kotlin
plugins {
    id("io.github.joaojunceira.gradle-mvn-settings") version "0.0.1" // Replace with actual version
}
```

### Configuration

By default, the plugin looks for `settings.xml` in `~/.m2/settings.xml`. You can configure the paths using the `mavenSettings` extension:

```kotlin
mavenSettings {
    // Optional: Path to user settings file
    userSettingsFile = file("/path/to/custom/settings.xml")
    
    // Optional: Path to global settings file
    globalSettingsFile = file("/usr/local/maven/conf/settings.xml")
    
    // Optional: Path to security settings (for encrypted passwords)
    securitySettingsFile = file("~/.m2/settings-security.xml")
}
```

## How it works

1.  **Parses Settings**: Loads your user and global `settings.xml` files.
2.  **Identifies Active Profiles**: Determines which profiles are active (currently uses `<activeProfiles>` from settings).
3.  **Configures Repositories**: Iterates through repositories in active profiles.
4.  **Applies Mirrors**: Checks if any mirrors match the repository ID.
5.  **Injects Credentials**: Looks up credentials in `<servers>` matching the repository (or mirror) ID. Decrypts passwords if necessary using `settings-security.xml`.
6.  **Registers in Gradle**: Adds the fully configured repository to `project.repositories`.

## Requirements

-   Gradle 7.x or higher
-   Java 11 or higher
