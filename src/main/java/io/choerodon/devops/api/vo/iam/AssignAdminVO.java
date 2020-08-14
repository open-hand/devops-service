package io.choerodon.devops.api.vo.iam;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 批量分配用户为admin时发送的saga载体
 *
 * @author zmf
 * @since 19-12-24
 */
public class AssignAdminVO {
    @Encrypt
    @ApiModelProperty("分配为admin的用户id")
    private List<Long> adminUserIds;

    public List<Long> getAdminUserIds() {
        return adminUserIds;
    }

    public void setAdminUserIds(List<Long> adminUserIds) {
        this.adminUserIds = adminUserIds;
    }

    @Override
    public String toString() {
        return "AssignAdminVO{" +
                "adminUserIds=" + adminUserIds +
                '}';
    }
}
