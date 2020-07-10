package io.choerodon.devops.api.vo;


import java.util.Date;
import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:18
 */
public class CiCdPipelineRecordVO {
    //ci 执行记录的id
    private Long ciRecordId;
    //cd 执行记录的id
    private Long cdRecordId;
    // cicd 执行记录创建时间
    private Date createdDate;
    //gitlab的流水线id
    private Long gitlabPipelineId;
    //cd流水线记录的状态
    private String cdStatus;
    //ci的流水线记录状态
    private String ciStatus;
    //cicd 流水线的状态
    private String status;
    private String gitlabTriggerRef;
    private CustomCommitVO commit;

    private CiCdPipelineVO ciCdPipelineVO;

    // ci和cd阶段记录的集合
    private List<StageRecordVO> stageRecordVOS;

    private DevopsCiPipelineRecordVO ciPipelineRecordVO;
    private DevopsCdPipelineRecordVO cdPipelineRecordVO;

    public CiCdPipelineVO getCiCdPipelineVO() {
        return ciCdPipelineVO;
    }

    public void setCiCdPipelineVO(CiCdPipelineVO ciCdPipelineVO) {
        this.ciCdPipelineVO = ciCdPipelineVO;
    }

    public DevopsCiPipelineRecordVO getCiPipelineRecordVO() {
        return ciPipelineRecordVO;
    }

    public void setCiPipelineRecordVO(DevopsCiPipelineRecordVO ciPipelineRecordVO) {
        this.ciPipelineRecordVO = ciPipelineRecordVO;
    }

    public DevopsCdPipelineRecordVO getCdPipelineRecordVO() {
        return cdPipelineRecordVO;
    }

    public void setCdPipelineRecordVO(DevopsCdPipelineRecordVO cdPipelineRecordVO) {
        this.cdPipelineRecordVO = cdPipelineRecordVO;
    }

    public Long getCiRecordId() {
        return ciRecordId;
    }

    public void setCiRecordId(Long ciRecordId) {
        this.ciRecordId = ciRecordId;
    }

    public Long getCdRecordId() {
        return cdRecordId;
    }

    public void setCdRecordId(Long cdRecordId) {
        this.cdRecordId = cdRecordId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public String getCdStatus() {
        return cdStatus;
    }

    public void setCdStatus(String cdStatus) {
        this.cdStatus = cdStatus;
    }

    public String getCiStatus() {
        return ciStatus;
    }

    public void setCiStatus(String ciStatus) {
        this.ciStatus = ciStatus;
    }

    public List<StageRecordVO> getStageRecordVOS() {
        return stageRecordVOS;
    }

    public void setStageRecordVOS(List<StageRecordVO> stageRecordVOS) {
        this.stageRecordVOS = stageRecordVOS;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGitlabTriggerRef() {
        return gitlabTriggerRef;
    }

    public void setGitlabTriggerRef(String gitlabTriggerRef) {
        this.gitlabTriggerRef = gitlabTriggerRef;
    }

    public CustomCommitVO getCommit() {
        return commit;
    }

    public void setCommit(CustomCommitVO commit) {
        this.commit = commit;
    }
}
