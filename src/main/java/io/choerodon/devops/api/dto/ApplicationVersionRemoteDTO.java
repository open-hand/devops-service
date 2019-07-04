package io.choerodon.devops.api.dto;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:43 2019/7/3
 * Description:
 */
public class ApplicationVersionRemoteDTO {
    private String values;
    private String image;
    private String readMeValue;
    private String repository;
    private String version;

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getReadMeValue() {
        return readMeValue;
    }

    public void setReadMeValue(String readMeValue) {
        this.readMeValue = readMeValue;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
