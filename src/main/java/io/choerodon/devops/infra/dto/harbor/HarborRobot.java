package io.choerodon.devops.infra.dto.harbor;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 制品库-harbor机器人账户表
 *
 * @author mofei.li@hand-china.com 2020-05-28 15:29:06
 */
@ApiModel("制品库-harbor机器人账户表")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HarborRobot extends AuditDomain {

    public static final String FIELD_ROBOT_ID = "robotId";
    public static final String FIELD_HARBOR_ROBOT_ID = "harborRobotId";
    public static final String FIELD_PROJECT_ID = "projectId";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ACTION = "action";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_ENABLE_FLAG = "enableFlag";
    public static final String FIELD_TOKEN = "token";
    public static final String FIELD_END_DATE = "endDate";
    public static final String FIELD_ORGANIZATION_ID = "organizationId";
    public static final String FIELD_CREATION_DATE = "creationDate";
    public static final String FIELD_CREATED_BY = "createdBy";
    public static final String FIELD_LAST_UPDATED_BY = "lastUpdatedBy";
    public static final String FIELD_LAST_UPDATE_DATE = "lastUpdateDate";
    public static final String FIELD_LAST_UPDATE_LOGIN = "lastUpdateLogin";

    //
    // 业务方法(按public protected private顺序排列)
    // ------------------------------------------------------------------------------

    //
    // 数据库字段
    // ------------------------------------------------------------------------------


    @ApiModelProperty("表ID，主键，供其他表做外键")
    @Id
    @GeneratedValue
    private Long robotId;
    @ApiModelProperty(value = "harbor机器人账户ID",required = true)
    @NotNull
    private Long harborRobotId;
    @ApiModelProperty(value = "猪齿鱼项目id",required = true)
    @NotNull
    private Long projectId;
    @ApiModelProperty(value = "账户名称",required = true)
    @NotBlank
    private String name;
    @ApiModelProperty(value = "功能，pull/push",required = true)
    @NotBlank
    private String action;
   @ApiModelProperty(value = "机器人账户描述，拉取/推送")    
    private String description;
    @ApiModelProperty(value = "是否启用，Y启用/N禁用",required = true)
    @NotBlank
    private String enableFlag;
    @ApiModelProperty(value = "机器人账户token",required = true)
    @NotBlank
    private String token;
    @ApiModelProperty(value = "账户到期时间",required = true)
    @NotNull
    private Date endDate;
    @ApiModelProperty(value = "组织id",required = true)
    @NotNull
    private Long organizationId;

	//
    // 非数据库字段
    // ------------------------------------------------------------------------------

	@Transient
	@ApiModelProperty(value = "harbor项目id")
	private Long harborProjectId;

	//
    // getter/setter
    // ------------------------------------------------------------------------------


    public Long getRobotId() {
        return robotId;
    }

    public void setRobotId(Long robotId) {
        this.robotId = robotId;
    }

    public Long getHarborRobotId() {
        return harborRobotId;
    }

    public void setHarborRobotId(Long harborRobotId) {
        this.harborRobotId = harborRobotId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = enableFlag;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getHarborProjectId() {
        return harborProjectId;
    }

    public void setHarborProjectId(Long harborProjectId) {
        this.harborProjectId = harborProjectId;
    }
}
