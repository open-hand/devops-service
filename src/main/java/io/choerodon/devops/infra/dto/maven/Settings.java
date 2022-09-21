package io.choerodon.devops.infra.dto.maven;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author zmf
 * @since 20-4-15
 */
@XmlRootElement(name = "settings")
public class Settings {
    private List<Server> servers;
    private List<Profile> profiles;
    private List<Proxy> proxies;

    public Settings() {
    }

    public Settings(List<Server> servers, List<Profile> profiles, List<Proxy> proxies) {
        this.servers = servers;
        this.profiles = profiles;
        this.proxies = proxies;
    }

    @XmlElementWrapper(name = "servers")
    @XmlElement(name = "server")
    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    @XmlElementWrapper(name = "profiles")
    @XmlElement(name = "profile")
    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    @XmlElementWrapper(name = "proxies")
    @XmlElement(name = "proxy")
    public List<Proxy> getProxies() {
        return proxies;
    }

    public void setProxies(List<Proxy> proxies) {
        this.proxies = proxies;
    }
}
