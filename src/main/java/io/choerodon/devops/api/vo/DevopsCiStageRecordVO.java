package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:34
 */
public class DevopsCiStageRecordVO extends StageRecordVO{


    @ApiModelProperty("阶段中的任务记录信息")
    private List<DevopsCiJobRecordVO> jobRecordVOList;


    public List<DevopsCiJobRecordVO> getJobRecordVOList() {
        return jobRecordVOList;
    }

    public void setJobRecordVOList(List<DevopsCiJobRecordVO> jobRecordVOList) {
        this.jobRecordVOList = jobRecordVOList;
    }
}
