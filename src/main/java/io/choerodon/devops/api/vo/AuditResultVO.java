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
public class AuditResultVO {
    private Integer countersigned;
    @ApiModelProperty("已审核人员")
    private List<String> auditedUserNameList = new ArrayList<>();
    @ApiModelProperty("未审核人员")
    private List<String> notAuditUserNameList = new ArrayList<>();

    public Integer getCountersigned() {
        return countersigned;
    }

    public void setCountersigned(Integer countersigned) {
        this.countersigned = countersigned;
    }

    public List<String> getAuditedUserNameList() {
        return auditedUserNameList;
    }

    public void setAuditedUserNameList(List<String> auditedUserNameList) {
        this.auditedUserNameList = auditedUserNameList;
    }

    public List<String> getNotAuditUserNameList() {
        return notAuditUserNameList;
    }

    public void setNotAuditUserNameList(List<String> notAuditUserNameList) {
        this.notAuditUserNameList = notAuditUserNameList;
    }
}
