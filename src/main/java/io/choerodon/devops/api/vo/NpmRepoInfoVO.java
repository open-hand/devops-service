package io.choerodon.devops.api.vo;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/14 15:02
 */
public class NpmRepoInfoVO {
    private String registry;
    private String username;
    private String password;
    private String email;

    public NpmRepoInfoVO(String registry, String username, String password, String email) {
        this.registry = registry;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public NpmRepoInfoVO() {
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
