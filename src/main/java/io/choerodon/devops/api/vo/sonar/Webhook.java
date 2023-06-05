package io.choerodon.devops.api.vo.sonar;

/**
 * Created by Sheep on 2019/5/6.
 */
public class Webhook {

    private String key;

    private String name;

    private String url;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
