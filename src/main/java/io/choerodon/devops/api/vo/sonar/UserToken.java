package io.choerodon.devops.api.vo.sonar;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:58 2019/9/9
 * Description:
 */
public class UserToken {
    private String login;
    private String name;
    private String createdAt;
    private String token;

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
