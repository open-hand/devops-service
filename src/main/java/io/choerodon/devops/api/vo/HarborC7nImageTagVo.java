package io.choerodon.devops.api.vo;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author scp
 * @date 2020/7/11
 * @description
 */
public class HarborC7nImageTagVo {
    @ApiModelProperty("TAG名称")
    @SerializedName("name")
    private String tagName;

    @ApiModelProperty("最新push时间")
    @SerializedName("push_time")
    private String pushTime;

    @ApiModelProperty("pull命令")
    private String pullCmd;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getPushTime() {
        return pushTime;
    }

    public void setPushTime(String pushTime) {
        this.pushTime = pushTime;
    }

    public String getPullCmd() {
        return pullCmd;
    }

    public void setPullCmd(String pullCmd) {
        this.pullCmd = pullCmd;
    }
}
