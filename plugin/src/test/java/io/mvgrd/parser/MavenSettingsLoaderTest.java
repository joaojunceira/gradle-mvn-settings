package io.mvgrd.parser;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MavenSettingsLoaderTest {

    @TempDir
    Path tempDir;

    private File createSettingsFile(String name, String content) throws IOException {
        File file = tempDir.resolve(name).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    @Test
    void loadSettings_withUserSettingsOnly() throws Exception {
        // Arrange
        String content = "<settings><localRepository>/tmp/user-repo</localRepository></settings>";
        File userSettings = createSettingsFile("user-settings.xml", content);
        MavenSettingsLoader loader = new MavenSettingsLoader();

        // Act
        Settings settings = loader.loadSettings(userSettings, null);

        // Assert
        assertNotNull(settings);
        // Note: Paths in settings are usually normalized, but let's check the string
        // value
        assertEquals("/tmp/user-repo", settings.getLocalRepository());
    }

    @Test
    void loadSettings_withGlobalSettingsOnly() throws Exception {
        // Arrange
        String content = "<settings><localRepository>/tmp/global-repo</localRepository></settings>";
        File globalSettings = createSettingsFile("global-settings.xml", content);
        MavenSettingsLoader loader = new MavenSettingsLoader();

        // Act
        Settings settings = loader.loadSettings(null, globalSettings);

        // Assert
        assertNotNull(settings);
        assertEquals("/tmp/global-repo", settings.getLocalRepository());
    }

    @Test
    void loadSettings_withBothFiles_UserPrecedence() throws Exception {
        // Arrange
        File userSettings = createSettingsFile("user.xml",
                "<settings><localRepository>/tmp/user-repo</localRepository></settings>");
        File globalSettings = createSettingsFile("global.xml",
                "<settings><localRepository>/tmp/global-repo</localRepository></settings>");
        MavenSettingsLoader loader = new MavenSettingsLoader();

        // Act
        Settings settings = loader.loadSettings(userSettings, globalSettings);

        // Assert
        assertNotNull(settings);
        // User settings should override global settings
        assertEquals("/tmp/user-repo", settings.getLocalRepository());
    }

    @Test
    void loadSettings_withNonExistentFiles() throws Exception {
        // Arrange
        File userSettings = new File(tempDir.toFile(), "non-existent-user.xml");
        File globalSettings = new File(tempDir.toFile(), "non-existent-global.xml");
        MavenSettingsLoader loader = new MavenSettingsLoader();

        // Act
        Settings settings = loader.loadSettings(userSettings, globalSettings);

        // Assert
        assertNotNull(settings);
        // We do not check settings.getLocalRepository() because it may be null if no
        // system properties are set
    }

    @Test
    void loadSettings_withNullFiles() throws Exception {
        // Arrange
        MavenSettingsLoader loader = new MavenSettingsLoader();

        // Act
        Settings settings = loader.loadSettings(null, null);

        // Assert
        assertNotNull(settings);
    }

    @Test
    void loadSettings_withInvalidXml() throws IOException {
        // Arrange
        File invalidFile = createSettingsFile("invalid.xml", "<settings><unclosedTag>");
        MavenSettingsLoader loader = new MavenSettingsLoader();

        // Act & Assert
        assertThrows(SettingsBuildingException.class, () -> {
            loader.loadSettings(invalidFile, null);
        });
    }
}
