package io.mvgrd;

import io.mvgrd.crypto.MavenPasswordDecryptor;
import io.mvgrd.extension.MavenSettingsExtension;
import io.mvgrd.parser.MavenSettingsLoader;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.authentication.http.BasicAuthentication;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Plugin to load Maven settings into Gradle.
 */
public class GradleMvnSettingsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // Register the extension
        MavenSettingsExtension extension = project.getExtensions().create("mavenSettings",
                MavenSettingsExtension.class);

        project.afterEvaluate(p -> {
            try {
                loadAndApplySettings(p, extension);
            } catch (Exception e) {
                p.getLogger().error("Failed to load Maven settings", e);
            }
        });
    }

    private void loadAndApplySettings(Project project, MavenSettingsExtension extension) throws Exception {
        File userSettings = extension.getUserSettingsFile().isPresent()
                ? extension.getUserSettingsFile().getAsFile().get()
                : new File(System.getProperty("user.home"), ".m2/settings.xml");

        File globalSettings = extension.getGlobalSettingsFile().isPresent()
                ? extension.getGlobalSettingsFile().getAsFile().get()
                : null;

        File securitySettings = extension.getSecuritySettingsFile().isPresent()
                ? extension.getSecuritySettingsFile().getAsFile().get()
                : new File(System.getProperty("user.home"), ".m2/settings-security.xml");

        MavenSettingsLoader loader = new MavenSettingsLoader();
        Settings settings = loader.loadSettings(userSettings, globalSettings);

        MavenPasswordDecryptor decryptor = new MavenPasswordDecryptor(securitySettings);

        // 1. Proxies
        configureProxies(settings, project);

        // 2. Repositories from Profiles
        configureRepositories(settings, project, decryptor);
    }

    private void configureProxies(Settings settings, Project project) {
        // Simple proxy logging for now
        org.apache.maven.settings.Proxy proxy = settings.getActiveProxy();
        if (proxy != null) {
            project.getLogger().info("Found active Maven proxy: " + proxy.getHost() + ":" + proxy.getPort());
            // In a real implementation, we would set system properties here if not present:
            // System.setProperty("http.proxyHost", proxy.getHost());
            // System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
            // etc.
        }
    }

    private void configureRepositories(Settings settings, Project project, MavenPasswordDecryptor decryptor) {
        List<String> activeProfileIds = settings.getActiveProfiles();
        Map<String, Profile> profiles = settings.getProfilesAsMap();

        RepositoryHandler gradleRepos = project.getRepositories();

        for (String profileId : activeProfileIds) {
            Profile profile = profiles.get(profileId);
            if (profile != null) {
                for (Repository mavenRepo : profile.getRepositories()) {
                    addRepository(gradleRepos, mavenRepo, settings, decryptor);
                }
            }
        }
    }

    private void addRepository(RepositoryHandler gradleRepos, Repository mavenRepo, Settings settings,
            MavenPasswordDecryptor decryptor) {
        String url = mavenRepo.getUrl();
        String id = mavenRepo.getId();

        // Check for Mirrors
        Mirror mirror = getMirror(settings, id);
        if (mirror != null) {
            url = mirror.getUrl();
            id = mirror.getId();
        }

        // Check for Auth (Server)
        Server server = settings.getServer(id);

        String finalUrl = url;
        String finalId = id;

        gradleRepos.maven(repo -> {
            repo.setName(finalId);
            repo.setUrl(finalUrl);

            if (server != null) {
                repo.getCredentials().setUsername(server.getUsername());
                if (server.getPassword() != null) {
                    try {
                        repo.getCredentials().setPassword(decryptor.decrypt(server.getPassword()));
                    } catch (Exception e) {
                        repo.getCredentials().setPassword(server.getPassword());
                    }
                }
                repo.authentication(auth -> auth.create("basic", BasicAuthentication.class));
            }
        });
    }

    private Mirror getMirror(Settings settings, String repoId) {
        for (Mirror mirror : settings.getMirrors()) {
            if (matches(mirror.getMirrorOf(), repoId)) {
                return mirror;
            }
        }
        return null;
    }

    private boolean matches(String mirrorOf, String repoId) {
        if (mirrorOf == null)
            return false;
        if ("*".equals(mirrorOf))
            return true;

        // Handle "external:*" which is common
        if ("external:*".equals(mirrorOf)) {
            // Basic assumption: most http/https repos are external except localhost/file
            // This is a simplification.
            return true;
        }

        // Split by comma
        String[] patterns = mirrorOf.split(",");
        for (String pattern : patterns) {
            if (pattern.trim().equals(repoId))
                return true;
            if (pattern.trim().equals("*"))
                return true;
            // Handle !repoId logic for exclusions (simplified handling here)
            if (pattern.trim().startsWith("!") && pattern.trim().substring(1).equals(repoId)) {
                return false;
            }
        }
        return false;
    }
}
