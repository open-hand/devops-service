package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:05 2019/5/8
 * Description:
 */
public class SonarInfoVO {
    private String userName;
    private String password;
    private String url;

    public SonarInfoVO(String userName, String password, String url) {
        this.userName = userName;
        this.password = password;
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
