package io.choerodon.devops.domain.application.valueobject;

/**
 * Created by Sheep on 2019/3/14.
 */
public class ImagePullSecret {
    String name;

    public ImagePullSecret() {
    }

    public ImagePullSecret(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
