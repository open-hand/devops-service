package io.choerodon.devops.app.eventhandler.payload;

import io.swagger.annotations.ApiModelProperty;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:16 2019/8/2
 * Description:
 */
public class AppServiceVersionDownloadVO {
    @ApiModelProperty("应用服务版本")
    private String version;

    @ApiModelProperty("镜像地址")
    private String image;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
