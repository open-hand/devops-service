package io.choerodon.devops.infra.enums;

/**
 * @author scp
 * @date 2020/6/11
 * @description
 */
public enum AppServiceEvent {

    /**
     * 分支list
     */
    BRANCH_LIST(10),

    /**
     * 分支创建
     */
    BRANCH_CREATE(20),

    /**
     * 非保护分支删除
     */
    BRANCH_DELETE(20);


    private Integer accesslevel;

    AppServiceEvent(Integer accesslevel) {
        this.accesslevel = accesslevel;
    }

    public Integer getAccessLevel() {
        return accesslevel;
    }

}
