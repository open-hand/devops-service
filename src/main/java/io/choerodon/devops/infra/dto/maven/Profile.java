package io.choerodon.devops.infra.dto.maven;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author zmf
 * @since 20-4-15
 */
public class Profile {
    private String id;
    private Activation activation;
    private List<Repository> repositories;

    private List<PluginRepository> pluginRepositories;

    public Profile() {
    }

    public Profile(String id, Activation activation, List<Repository> repositories, List<PluginRepository> pluginRepositories) {
        this.id = id;
        this.activation = activation;
        this.repositories = repositories;
        this.pluginRepositories = pluginRepositories;
    }

    public Profile(String id, Activation activation, List<Repository> repositories) {
        this.id = id;
        this.activation = activation;
        this.repositories = repositories;
    }

    public Activation getActivation() {
        return activation;
    }

    public void setActivation(Activation activation) {
        this.activation = activation;
    }

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    @XmlElement(name = "pluginRepository")
    @XmlElementWrapper(name = "pluginRepositories")
    public List<PluginRepository> getPluginRepositories() {
        return pluginRepositories;
    }

    public void setPluginRepositories(List<PluginRepository> pluginRepositories) {
        this.pluginRepositories = pluginRepositories;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
