package io.choerodon.devops.api.dto;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:05 2019/5/8
 * Description:
 */
public class SonarUserDTO {
    private String token;
    private String url;

    public SonarUserDTO(String token, String url) {
        this.token = token;
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
