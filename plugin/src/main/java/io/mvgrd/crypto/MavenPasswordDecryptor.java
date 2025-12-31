package io.mvgrd.crypto;

import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import java.io.File;

// We need to implement a SecDispatcher that can load from a specific file.
// The standard DefaultSecDispatcher often relies on Plexus container injection which we don't have fully here.
// Let's try to manually construct a basic decryptor if possible, or use the library's helpers.

/**
 * Helper to decrypt passwords.
 * Note: Accessing Plexus components without a container can be tricky.
 * We might need to manually configure the DefaultSecDispatcher.
 */
public class MavenPasswordDecryptor {

    private final DefaultSecDispatcher secDispatcher;

    public MavenPasswordDecryptor(File securitySettingsFile) {
        org.sonatype.plexus.components.cipher.DefaultPlexusCipher cipher = new org.sonatype.plexus.components.cipher.DefaultPlexusCipher();
        this.secDispatcher = new DefaultSecDispatcher(cipher);

        if (securitySettingsFile != null) {
            System.setProperty(DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION,
                    securitySettingsFile.getAbsolutePath());
        }
    }

    public String decrypt(String encryptedPassword) {
        try {
            return secDispatcher.decrypt(encryptedPassword);
        } catch (Exception e) {
            // Fallback or log warning? For now runtime exception to fail fast if config is
            // wrong.
            throw new RuntimeException("Failed to decrypt password", e);
        }
    }
}
