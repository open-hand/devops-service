package io.choerodon.devops.api.vo.sonar;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/12/13 17:16
 */
public class User {
    private String login;
    private String name;
    private Boolean active;
    private Integer tokensCount;
    private Boolean local;
    private String externalIdentity;
    private String externalProvider;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getTokensCount() {
        return tokensCount;
    }

    public void setTokensCount(Integer tokensCount) {
        this.tokensCount = tokensCount;
    }

    public Boolean getLocal() {
        return local;
    }

    public void setLocal(Boolean local) {
        this.local = local;
    }

    public String getExternalIdentity() {
        return externalIdentity;
    }

    public void setExternalIdentity(String externalIdentity) {
        this.externalIdentity = externalIdentity;
    }

    public String getExternalProvider() {
        return externalProvider;
    }

    public void setExternalProvider(String externalProvider) {
        this.externalProvider = externalProvider;
    }
}
