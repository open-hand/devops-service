package io.choerodon.devops.app.eventhandler.payload;

import java.util.Date;
import java.util.List;

import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;

/**
 * @author crcokitwood
 */
public class DevOpsAppServicePayload {

    private Integer userId;
    private String path;
    private Integer gitlabProjectId;
    private Integer groupId;
    private String type;
    private Long organizationId;
    private Long appServiceId;
    private List<Long> userIds;
    private Long iamProjectId;
    private Long templateAppServiceId;
    private Long templateAppServiceVersionId;
    private Date date;
    private String errorMessage;

    private AppServiceDTO appServiceDTO;

    private List<MemberDTO> memberDTOS;

    private Boolean openAppService;

    public List<MemberDTO> getMemberDTOS() {
        return memberDTOS;
    }

    public void setMemberDTOS(List<MemberDTO> memberDTOS) {
        this.memberDTOS = memberDTOS;
    }

    public AppServiceDTO getAppServiceDTO() {
        return appServiceDTO;
    }

    public void setAppServiceDTO(AppServiceDTO appServiceDTO) {
        this.appServiceDTO = appServiceDTO;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Integer gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Long getIamProjectId() {
        return iamProjectId;
    }

    public void setIamProjectId(Long iamProjectId) {
        this.iamProjectId = iamProjectId;
    }

    public Long getTemplateAppServiceId() {
        return templateAppServiceId;
    }

    public void setTemplateAppServiceId(Long templateAppServiceId) {
        this.templateAppServiceId = templateAppServiceId;
    }

    public Long getTemplateAppServiceVersionId() {
        return templateAppServiceVersionId;
    }

    public void setTemplateAppServiceVersionId(Long templateAppServiceVersionId) {
        this.templateAppServiceVersionId = templateAppServiceVersionId;
    }

    public Date getDate() {
        return date;
    }

    public DevOpsAppServicePayload setDate(Date date) {
        this.date = date;
        return this;
    }

    public Boolean getOpenAppService() {
        return openAppService;
    }

    public void setOpenAppService(Boolean openAppService) {
        this.openAppService = openAppService;
    }
}
