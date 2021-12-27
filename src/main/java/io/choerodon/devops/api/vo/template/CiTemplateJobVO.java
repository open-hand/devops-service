package io.choerodon.devops.api.vo.template;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.DevopsCiStepVO;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 17:12:44
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CiTemplateJobVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "任务名称", required = true)
    private String name;
    @ApiModelProperty(value = "任务分组id", required = true)
    private Long groupId;
    @ApiModelProperty(value = "层级", required = true)
    private String sourceType;
    @ApiModelProperty(value = "层级Id", required = true)
    private Long sourceId;
    @ApiModelProperty(value = "是否上传到共享目录", required = true)
    private Long toUpload;
    @ApiModelProperty(value = "是否下载到共享目录", required = true)
    private Long toDownload;
    @ApiModelProperty(value = "任务镜像", required = true)
    private String image;

    private String type;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    private Boolean builtIn;

    private Long relateStageId;
    private CiTemplateJobGroupDTO ciTemplateJobGroupDTO;

    @ApiModelProperty("创建者信息")
    private IamUserDTO creatorInfo;

//    @ApiModelProperty(value = "任务模板下面的步骤模板")
//    private List<CiTemplateStepVO> ciTemplateStepVOS;

    @ApiModelProperty("任务中的步骤列表")
    private List<CiTemplateStepVO> devopsCiStepVOList;


    @ApiModelProperty("所属任务分组名称")
    private String groupName;

    @ApiModelProperty(value = "含有步骤数")
    private Long stepNumber;

    @ApiModelProperty("创建者Id")
    private Long createdBy;

    private Date creationDate;

    private String script;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }


    public List<CiTemplateStepVO> getDevopsCiStepVOList() {
        return devopsCiStepVOList;
    }

    public void setDevopsCiStepVOList(List<CiTemplateStepVO> devopsCiStepVOList) {
        this.devopsCiStepVOList = devopsCiStepVOList;
    }

    public Long getRelateStageId() {
        return relateStageId;
    }

    public void setRelateStageId(Long relateStageId) {
        this.relateStageId = relateStageId;
    }

    public CiTemplateJobGroupDTO getCiTemplateJobGroupDTO() {
        return ciTemplateJobGroupDTO;
    }

    public void setCiTemplateJobGroupDTO(CiTemplateJobGroupDTO ciTemplateJobGroupDTO) {
        this.ciTemplateJobGroupDTO = ciTemplateJobGroupDTO;
    }

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getToUpload() {
        return toUpload;
    }

    public void setToUpload(Long toUpload) {
        this.toUpload = toUpload;
    }

    public Long getToDownload() {
        return toDownload;
    }

    public void setToDownload(Long toDownload) {
        this.toDownload = toDownload;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public IamUserDTO getCreatorInfo() {
        return creatorInfo;
    }

    public void setCreatorInfo(IamUserDTO creatorInfo) {
        this.creatorInfo = creatorInfo;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Long stepNumber) {
        this.stepNumber = stepNumber;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
