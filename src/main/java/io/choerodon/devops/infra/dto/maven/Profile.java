package io.choerodon.devops.infra.dto.maven;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author zmf
 * @since 20-4-15
 */
public class Profile {
    private Activation activation;
    private List<Repository> repositories;

    public Profile() {
    }

    public Profile(Activation activation, List<Repository> repositories) {
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
}
