package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈应用监控 - 异常次数图展示VO〉
 *
 * @author wanghao
 * @since 2022/5/24 10:57
 */
@ApiModel("应用监控 - 异常持续时长图展示VO")
public class ExceptionDurationVO {
    @ApiModelProperty("异常总时长")
    private Long exceptionTotalDuration;
    @ApiModelProperty("停机总时长")
    private Long downTimeTotalDuration;
    @ApiModelProperty("异常时长")
    private List<ExceptionRecordVO> exceptionDurationList;
    @ApiModelProperty("停机时长")
    private List<ExceptionRecordVO> downTimeDurationList;

    public ExceptionDurationVO() {
    }

    public ExceptionDurationVO(List<ExceptionRecordVO> exceptionDurationList, List<ExceptionRecordVO> downTimeDurationList) {
        this.exceptionDurationList = exceptionDurationList;
        this.downTimeDurationList = downTimeDurationList;
    }

    public ExceptionDurationVO(Long exceptionTotalDuration, Long downTimeTotalDuration, List<ExceptionRecordVO> exceptionDurationList, List<ExceptionRecordVO> downTimeDurationList) {
        this.exceptionTotalDuration = exceptionTotalDuration;
        this.downTimeTotalDuration = downTimeTotalDuration;
        this.exceptionDurationList = exceptionDurationList;
        this.downTimeDurationList = downTimeDurationList;
    }

    public Long getExceptionTotalDuration() {
        return exceptionTotalDuration;
    }

    public void setExceptionTotalDuration(Long exceptionTotalDuration) {
        this.exceptionTotalDuration = exceptionTotalDuration;
    }

    public Long getDownTimeTotalDuration() {
        return downTimeTotalDuration;
    }

    public void setDownTimeTotalDuration(Long downTimeTotalDuration) {
        this.downTimeTotalDuration = downTimeTotalDuration;
    }

    public List<ExceptionRecordVO> getExceptionDurationList() {
        return exceptionDurationList;
    }

    public void setExceptionDurationList(List<ExceptionRecordVO> exceptionDurationList) {
        this.exceptionDurationList = exceptionDurationList;
    }

    public List<ExceptionRecordVO> getDownTimeDurationList() {
        return downTimeDurationList;
    }

    public void setDownTimeDurationList(List<ExceptionRecordVO> downTimeDurationList) {
        this.downTimeDurationList = downTimeDurationList;
    }
}
