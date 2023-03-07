package io.choerodon.devops.api.vo;

/**
 * Created by Sheep on 2019/5/6.
 */
public class SonarContentVO {

    private String key;
    private String value;
    private String rate;
    private String url;




    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
