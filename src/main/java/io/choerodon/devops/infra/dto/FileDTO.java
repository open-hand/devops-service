package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;

public class FileDTO {

    @ApiModelProperty(value = "文件服务器地址")
    private String endPoint;
    @ApiModelProperty(value = "原始文件名")
    private String originFileName;
    @ApiModelProperty(value = "新文件名")
    private String fileName;

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getOriginFileName() {
        return originFileName;
    }

    public void setOriginFileName(String originFileName) {
        this.originFileName = originFileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
