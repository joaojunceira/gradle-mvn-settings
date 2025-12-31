package io.mvgrd.parser;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;

import java.io.File;

public class MavenSettingsLoader {

    public Settings loadSettings(File userSettingsFile, File globalSettingsFile) throws SettingsBuildingException {
        SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
        SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();

        if (userSettingsFile != null && userSettingsFile.exists()) {
            request.setUserSettingsFile(userSettingsFile);
        }

        if (globalSettingsFile != null && globalSettingsFile.exists()) {
            request.setGlobalSettingsFile(globalSettingsFile);
        }

        // We can add system properties if needed
        // request.setSystemProperties(System.getProperties());

        SettingsBuildingResult result = settingsBuilder.build(request);
        return result.getEffectiveSettings();
    }
}
