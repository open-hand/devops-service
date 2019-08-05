package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by n!Ck
 * Date: 2018/10/25
 * Time: 14:46
 * Description:
 */
public class RoleAssignmentSearchVO {

    @ApiModelProperty(value = "登录名")
    private String loginName;

    @ApiModelProperty(value = "角色名")
    private String roleName;

    @ApiModelProperty(value = "用户名")
    private String realName;

    @ApiModelProperty(value = "参数")
    private String[] param;

    public RoleAssignmentSearchVO() {
        this.param = new String[]{};
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String[] getParam() {
        return param;
    }

    public void setParam(String[] param) {
        this.param = param;
    }
}
