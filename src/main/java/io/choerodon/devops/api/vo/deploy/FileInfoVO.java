package io.choerodon.devops.api.vo.deploy;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/30 20:19
 */
public class FileInfoVO {
    @ApiModelProperty("文件名")
    private String fileName;
    @ApiModelProperty("jar地址")
    private String jarFileUrl;
    @ApiModelProperty("文件地址")
    private String uploadUrl;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getJarFileUrl() {
        return jarFileUrl;
    }

    public void setJarFileUrl(String jarFileUrl) {
        this.jarFileUrl = jarFileUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
}
