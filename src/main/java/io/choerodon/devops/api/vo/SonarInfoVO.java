package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:05 2019/5/8
 * Description:
 */
public class SonarInfoVO {
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("sonar地址")
    private String url;
    public SonarInfoVO(){}
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
