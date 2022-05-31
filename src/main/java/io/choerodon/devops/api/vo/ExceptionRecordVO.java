package io.choerodon.devops.api.vo;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/5/24 16:04
 */
public class ExceptionRecordVO {
    @ApiModelProperty("持续时长")
    private Long duration;
    @ApiModelProperty("发生时间")
    private Date startTime;
    @ApiModelProperty("结束时间")
    private Date endTime;
    @ApiModelProperty("发生日期")
    private Date date;

    public ExceptionRecordVO() {
    }

    public ExceptionRecordVO(Long duration, Date startTime, Date endTime, Date date) {
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

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
