package io.choerodon.devops.api.vo;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:29 2019/4/24
 * Description:
 */
public class CheckAuditVO {
    private Integer isCountersigned;
    private String userName;

    public Integer getIsCountersigned() {
        return isCountersigned;
    }

    public void setIsCountersigned(Integer isCountersigned) {
        this.isCountersigned = isCountersigned;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
