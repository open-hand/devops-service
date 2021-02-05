package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.maven.Server;

/**
 * Created by wangxiang on 2021/2/5
 */
public class DownloadMavenJarVO {
    private String downloaJar;
    private Server server;

    public String getDownloaJar() {
        return downloaJar;
    }

    public void setDownloaJar(String downloaJar) {
        this.downloaJar = downloaJar;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
