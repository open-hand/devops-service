package io.choerodon.devops.api.vo.sonar;

import java.util.List;

/**
 * Created by Sheep on 2019/9/29.
 */
public class UserTokens {
    private String login;
    private List<UserToken> userTokens;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<UserToken> getUserTokens() {
        return userTokens;
    }

    public void setUserTokens(List<UserToken> userTokens) {
        this.userTokens = userTokens;
    }
}
