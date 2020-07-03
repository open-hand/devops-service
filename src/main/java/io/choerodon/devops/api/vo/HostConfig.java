package io.choerodon.devops.api.vo;

public class HostConfig {
    private String ip;
    private String port;
    /**
     * 账号配置  accountPassword（用户名与密码） accountKey(用户名与密钥)
     */
    private String accountConfig;
    /**
     * 用户名
     */
    private String accout;
    /**
     * 密码
     */
    private String accountPassword;

    /**
     * 秘钥
     */
    private String accountKey;

    /**
     * 部署模式  customize（自定义） image(镜像) jar(jar包)
     */
    private String mode;

    /**
     * nexus 服务
     */
    private String nexus;

    /**
     * 项目制品库
     */
    private String product;

    /**
     * groupId
     */
    private String groupId;
    private String artifactId;
    /**
     * jar包版本正则匹配
     */
    private String jarMatch;

    public HostConfig() {
    }

    private String values;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAccountConfig() {
        return accountConfig;
    }

    public void setAccountConfig(String accountConfig) {
        this.accountConfig = accountConfig;
    }

    public String getAccout() {
        return accout;
    }

    public void setAccout(String accout) {
        this.accout = accout;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getNexus() {
        return nexus;
    }

    public void setNexus(String nexus) {
        this.nexus = nexus;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getJarMatch() {
        return jarMatch;
    }

    public void setJarMatch(String jarMatch) {
        this.jarMatch = jarMatch;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }
}
