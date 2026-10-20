package net.cosette.miyabi.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bilan d'un chargement de configuration : fichiers lus, valeurs ajoutées au fichier, avertissements. */
public final class ConfigReport {
    private final List<String> warnings = new ArrayList<>();
    private int files = 0;
    private int added = 0;
    public void warn(String message) { warnings.add(message); }
    public void fileLoaded() { files++; }
    public void valueAdded() { added++; }
    public List<String> warnings() { return Collections.unmodifiableList(warnings); }
    public int files() { return files; }
    public int added() { return added; }
}