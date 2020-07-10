package io.choerodon.devops.api.vo;

import java.util.List;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:34
 */
public class DevopsCiStageRecordVO extends StageRecordVO{

    private Long durationSeconds;
    private List<DevopsCiJobRecordVO> jobRecordVOList;


    public List<DevopsCiJobRecordVO> getJobRecordVOList() {
        return jobRecordVOList;
    }

    public void setJobRecordVOList(List<DevopsCiJobRecordVO> jobRecordVOList) {
        this.jobRecordVOList = jobRecordVOList;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}
