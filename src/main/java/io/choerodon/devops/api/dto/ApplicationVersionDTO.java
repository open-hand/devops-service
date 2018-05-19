package io.choerodon.devops.api.dto;

import java.io.File;

/**
 * Created by younger on 2018/4/14.
 */
public class ApplicationVersionDTO {

    private String image;
    private String token;
    private String version;
    private File file;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
