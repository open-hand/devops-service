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
@ApiModel("应用监控 - 异常次数图展示VO")
public class ExceptionTimesVO {
    @ApiModelProperty("异常总次数")
    private Long exceptionTotalTimes;
    @ApiModelProperty("停机总次数")
    private Long downTimeTotalTimes;
    @ApiModelProperty("x轴 - 日期")
    private List<String> dateList;
    @ApiModelProperty("y轴 - 异常次数")
    private List<Long> exceptionTimesList;
    @ApiModelProperty("y轴 - 停机次数")
    private List<Long> downTimeList;

    public ExceptionTimesVO() {
    }

    public ExceptionTimesVO(Long exceptionTotalTimes, Long downTimeTotalTimes, List<String> dateList, List<Long> exceptionTimesList, List<Long> downTimeList) {
        this.exceptionTotalTimes = exceptionTotalTimes;
        this.downTimeTotalTimes = downTimeTotalTimes;
        this.dateList = dateList;
        this.exceptionTimesList = exceptionTimesList;
        this.downTimeList = downTimeList;
    }

    public Long getExceptionTotalTimes() {
        return exceptionTotalTimes;
    }

    public void setExceptionTotalTimes(Long exceptionTotalTimes) {
        this.exceptionTotalTimes = exceptionTotalTimes;
    }

    public Long getDownTimeTotalTimes() {
        return downTimeTotalTimes;
    }

    public void setDownTimeTotalTimes(Long downTimeTotalTimes) {
        this.downTimeTotalTimes = downTimeTotalTimes;
    }

    public List<String> getDateList() {
        return dateList;
    }

    public void setDateList(List<String> dateList) {
        this.dateList = dateList;
    }

    public List<Long> getExceptionTimesList() {
        return exceptionTimesList;
    }

    public void setExceptionTimesList(List<Long> exceptionTimesList) {
        this.exceptionTimesList = exceptionTimesList;
    }

    public List<Long> getDownTimeList() {
        return downTimeList;
    }

    public void setDownTimeList(List<Long> downTimeList) {
        this.downTimeList = downTimeList;
    }
}
