package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-26
 */
@ApiModel("构建包信息")
public class DevopsCiArtifactVO {
    @ApiModelProperty("构建包名称")
    private String artifactName;
    @ApiModelProperty("构建包下载地址")
    private String artifactUrl;

    public DevopsCiArtifactVO() {
    }

    public DevopsCiArtifactVO(String artifactName, String artifactUrl) {
        this.artifactName = artifactName;
        this.artifactUrl = artifactUrl;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getArtifactUrl() {
        return artifactUrl;
    }

    public void setArtifactUrl(String artifactUrl) {
        this.artifactUrl = artifactUrl;
    }
}
