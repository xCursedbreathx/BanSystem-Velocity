/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2022 kennytv (https://github.com/kennytv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.cursedbreath.bansystemreworked.config;

import com.google.common.collect.Sets;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Further modified version of the <a href="https://github.com/PSandro/SimpleConfig">SimpleConfig</a>SimpleConfig project of PSandro.
 *
 * @author PSandro on 26.01.19
 * @author kennytv
 */
public final class Config extends ConfigSection {
    private final Yaml yaml = createYaml();
    private final File file;
    private final Set<String> unsupportedFields;
    private Map<String, String[]> comments = new HashMap<>();
    private String header;

    public Config(final File file, final String... unsupportedFields) {
        super(null, "");
        this.file = file;
        this.unsupportedFields = unsupportedFields.length == 0 ? Collections.emptySet() : Sets.newHashSet(unsupportedFields);
    }

    public void load() throws IOException {
        final String data = new String(Files.readAllBytes(this.file.toPath()), StandardCharsets.UTF_8);
        final Map<String, Object> map = yaml.load(data);
        this.values = map != null ? map : new LinkedHashMap<>();
        this.comments = ConfigSerializer.deserializeComments(data);

        final String[] header = comments.remove(".header");
        if (header != null) {
            this.header = String.join("\n", header);
        }

        final boolean removedFields = values.keySet().removeIf(key -> {
            final String[] split = key.split("\\.");
            String splitKey = "";
            for (final String s : split) {
                splitKey += s;
                if (!unsupportedFields.contains(splitKey)) {
                    splitKey += ".";
                    continue;
                }

                // Unsupported field
                comments.remove(key);
                return true;
            }
            return false;
        });
        if (removedFields) {
            save();
        }
    }

    public void save() throws IOException {
        saveTo(file);
    }

    public void saveTo(final File file) throws IOException {
        final byte[] bytes = toString().getBytes(StandardCharsets.UTF_8);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        Files.write(file.toPath(), bytes);
    }

    public boolean addMissingFields(final Map<String, Object> fields, final Map<String, String[]> comments) {
        // Note: Only scans for the first two levels
        boolean changed = false;
        for (final Map.Entry<String, Object> entry : fields.entrySet()) {
            final Object o = values.get(entry.getKey());
            if (o != null) {
                final Object o2 = entry.getValue();
                if (!(o instanceof Map) || !(o2 instanceof Map)) continue;

                final Map<String, Object> deepMap = (Map<String, Object>) o2;
                for (final Map.Entry<String, Object> deepEntry : ((Map<String, Object>) o2).entrySet()) {
                    if (deepMap.containsKey(deepEntry.getKey())) continue;
                    deepMap.put(deepEntry.getKey(), deepEntry.getValue());
                    changed = true;
                }
                continue;
            }

            values.put(entry.getKey(), entry.getValue());
            changed = true;
        }

        this.comments = new HashMap<>(comments);
        return changed;
    }

    private static Yaml createYaml() {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(false);
        options.setIndent(2);
        options.setWidth(10_000); // be sneaky because autobreak on saving looks disgusting
        return new Yaml(options);
    }

    public void clear() {
        this.values.clear();
        this.comments.clear();
        this.header = null;
    }

    public Map<String, String[]> getComments() {
        return comments;
    }

    public Set<String> getUnsupportedFields() {
        return unsupportedFields;
    }

    @Override
    public Config getRoot() {
        return this;
    }

    @Override
    public String toString() {
        return ConfigSerializer.serialize(this.header, this.values, this.comments, this.yaml);
    }
}