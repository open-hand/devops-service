package io.choerodon.devops.api.vo;


import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/13 16:10
 */
public class CiAuditResultVO {
    @ApiModelProperty("是否是会签")
    private Boolean countersigned;
    @ApiModelProperty("审核通过人员")
    private List<String> passedUserNameList = new ArrayList<>();
    @ApiModelProperty("审核拒绝人员")
    private List<String> refusedUserNameList = new ArrayList<>();
    @ApiModelProperty("未审核人员")
    private List<String> notAuditUserNameList = new ArrayList<>();
    @ApiModelProperty("是否审核通过")
    private Boolean success;

    public Boolean getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Boolean countersigned) {
        this.countersigned = countersigned;
    }

    public List<String> getPassedUserNameList() {
        return passedUserNameList;
    }

    public void setPassedUserNameList(List<String> passedUserNameList) {
        this.passedUserNameList = passedUserNameList;
    }

    public List<String> getRefusedUserNameList() {
        return refusedUserNameList;
    }

    public void setRefusedUserNameList(List<String> refusedUserNameList) {
        this.refusedUserNameList = refusedUserNameList;
    }

    public List<String> getNotAuditUserNameList() {
        return notAuditUserNameList;
    }

    public void setNotAuditUserNameList(List<String> notAuditUserNameList) {
        this.notAuditUserNameList = notAuditUserNameList;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
