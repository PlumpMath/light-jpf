/*
 *    Copyright 2017 Luke Sosnicki
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ljpf.repository;

import com.google.common.collect.Maps;
import ljpf.PluginDescriptor;
import ljpf.PluginException;
import ljpf.PluginRepository;
import ljpf.PluginRepositoryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Created by souzen on 25.03.2017.
 */
public class BasePluginRepository implements PluginRepository {

    private static final Logger LOG = LoggerFactory.getLogger(BasePluginRepository.class.getSimpleName());

    private final Map<String, PluginRepositoryEntry> repositoryEntryMap;

    public BasePluginRepository() {
        repositoryEntryMap = Maps.newConcurrentMap();
    }

    @Override
    public boolean containsPlugin(String id) {
        return repositoryEntryMap.containsKey(id);
    }

    @Override
    public PluginRepositoryEntry getPlugin(String id) {
        return repositoryEntryMap.get(id);
    }

    @Override
    public Collection<PluginRepositoryEntry> getPlugins() {
        return Collections.unmodifiableCollection(repositoryEntryMap.values());
    }

    public void addEntry(final PluginRepositoryEntry entry) {
        if (containsPlugin(entry.getDescriptor().getId()))
            throw new PluginException(entry.getDescriptor().getId(), "Duplicate plugin with same id in repository");

        repositoryEntryMap.put(entry.getDescriptor().getId(), entry);
        LOG.debug("Plugin added: {} {}", entry.getDescriptor().getId(), entry.getClasspath().getPath());
    }

    protected Properties parseDescriptorFile(File file) {
        Properties properties = new Properties();

        try {
            final InputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);

        } catch (IOException e) {
            LOG.warn("Could not parse plugin descriptor {}", file.getAbsolutePath());
        }

        return properties;
    }

    protected Properties parseDescriptorFile(JarFile jar, JarEntry jarEntry) {
        Properties properties = new Properties();

        try {
            final InputStream inputStream = jar.getInputStream(jarEntry);
            properties.load(inputStream);

        } catch (IOException e) {
            LOG.warn("Could not parse plugin descriptor {}", jar.getName());
        }

        return properties;
    }

    protected boolean matchesDescriptorExtension(String filename) {
        return Pattern.matches(".*" + PluginDescriptor.FILE_EXTENSION, filename);
    }
}
