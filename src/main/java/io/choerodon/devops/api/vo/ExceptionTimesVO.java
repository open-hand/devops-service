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
    @ApiModelProperty("x轴 - 日期")
    private List<String> dateList;
    @ApiModelProperty("y轴 - 异常次数")
    private List<Long> exceptionTimesList;
    @ApiModelProperty("y轴 - 停机次数")
    private List<Long> downTimeList;

    public ExceptionTimesVO() {
    }

    public ExceptionTimesVO(List<String> dateList, List<Long> exceptionTimesList, List<Long> downTimeList) {
        this.dateList = dateList;
        this.exceptionTimesList = exceptionTimesList;
        this.downTimeList = downTimeList;
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
