package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/5/24 15:06
 */
@ApiModel("日期查询筛选VO")
public class DateQueryVO {
    @ApiModelProperty("开始时间")
    private Date startTime;
    @ApiModelProperty("结束时间")
    private Date endTime;

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
