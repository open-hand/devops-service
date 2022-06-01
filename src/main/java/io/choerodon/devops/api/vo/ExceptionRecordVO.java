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
    @ApiModelProperty("持续时长 - 分钟")
    private String durationMinute;
    @ApiModelProperty("发生时间")
    private Date startTime;
    @ApiModelProperty("结束时间")
    private Date endTime;
    @ApiModelProperty("发生日期")
    private Date date;

    public ExceptionRecordVO(Long duration, String durationMinute, Date startTime, Date endTime, Date date) {
        this.duration = duration;
        this.durationMinute = durationMinute;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
    }

    public String getDurationMinute() {
        return durationMinute;
    }

    public void setDurationMinute(String durationMinute) {
        this.durationMinute = durationMinute;
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
