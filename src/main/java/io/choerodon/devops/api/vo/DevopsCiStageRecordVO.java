package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:34
 */
public class DevopsCiStageRecordVO {

    private String name;
    private Long sequence;
    private String status;
    private List<DevopsCiJobRecordVO> jobRecordVOList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public List<DevopsCiJobRecordVO> getJobRecordVOList() {
        return jobRecordVOList;
    }

    public void setJobRecordVOList(List<DevopsCiJobRecordVO> jobRecordVOList) {
        this.jobRecordVOList = jobRecordVOList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
