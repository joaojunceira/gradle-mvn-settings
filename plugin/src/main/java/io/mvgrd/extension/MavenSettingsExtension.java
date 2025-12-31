package io.mvgrd.extension;

import org.gradle.api.file.RegularFileProperty;

/**
 * Extension for configuring the Maven Settings plugin.
 */
public abstract class MavenSettingsExtension {

    /**
     * Path to the global settings.xml file.
     * Defaults to ~/.m2/settings.xml if not specified.
     */
    public abstract RegularFileProperty getGlobalSettingsFile();

    /**
     * Path to the user settings.xml file.
     * Defaults to ~/.m2/settings.xml if not specified.
     */
    public abstract RegularFileProperty getUserSettingsFile();

    /**
     * Path to the settings-security.xml file.
     * Defaults to ~/.m2/settings-security.xml if not specified.
     */
    public abstract RegularFileProperty getSecuritySettingsFile();

}
