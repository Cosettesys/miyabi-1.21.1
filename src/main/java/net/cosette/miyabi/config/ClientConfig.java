package net.cosette.miyabi.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import java.nio.file.Path;
/**
 * Configuration côté client : config/miyabi/miyabi-client.toml, à côté de miyabi.toml.
 * Rechargeable avec /miyabiclient reload (et avec /miyabi config reload en solo).
 */
public final class ClientConfig {
    private ClientConfig() {}
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("miyabi").resolve("miyabi-client.toml");
    }
    public static synchronized ConfigReport reload() {
        ConfigReport report = new ConfigReport();
        Path file = file();
        try (CommentedFileConfig config = ConfigIO.open(file)) {
            ConfigIO.getInt(config, "config_version", 1,
                    "Réglages côté client de Miyabi (affichage). Ce fichier ne concerne que ta machine.\nRecharger sans redémarrer : /miyabiclient reload", 1, 1, report);
            ConfigBinder.bind(config, ClientSettings.class, report);
            if (config.contains("incantation")) {
                ConfigIO.sectionComment(config, "incantation", "Affichage de l'incantation pendant le cast d'un sort.");
            }
            config.save();
            report.fileLoaded();
        } catch (Exception e) {
            report.warn(file.getFileName() + " illisible, valeurs actuelles conservées : " + e.getMessage());
        }
        for (String warning : report.warnings()) {
            LOGGER.warn("[Miyabi client config] {}", warning);
        }
        return report;
    }
}