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
    BRANCH_DELETE(20),

    /**
     * 分支同步
     */
    BRANCH_SYNC(30),

    /**
     * 标记List
     */
    TAG_LIST(10),

    /**
     * 标记创建
     */
    TAG_CREATE(20),

    /**
     * 标记修改
     */
    TAG_UPDATE(20),

    /**
     * 标记删除
     */
    TAG_DELETE(20),

    /**
     * merge request List
     */
    MERGE_REQUEST_LIST(10),

    /**
     * merge request 创建
     */
    MERGE_REQUEST_CREATE(20),

    /**
     * 持续集成 连接详情
     */
    CICD_DETAIL(10),

    /**
     * 持续集成 操作
     */
    CICD_OPERATION(20),

    /**
     * 代码质量查看
     */
    SONAR_LIST(10),
    /**
     * ci 流水线 连接详情
     */
    CI_PIPELINE_DETAIL(10),
    /**
     * ci 流水线 权限执行
     */
    CI_PIPELINE_NEW_PERFORM(20),
    /**
     * ci 流水线 启用/停用
     */
    CI_PIPELINE_STATUS_UPDATE(20),
    /**
     * ci 流水线 删除
     */
    CI_PIPELINE_DELETE(20),
    /**
     * ci 流水线 更新
     */
    CI_PIPELINE_UPDATE(20),
    /**
     * ci 流水线 创建
     */
    CI_PIPELINE_CREATE(20),
    /**
     * ci 流水线 重试
     */
    CI_PIPELINE_RETRY(20),
    /**
     * ci 流水线 任务重试
     */
    CI_PIPELINE_RETRY_TASK(20),

    /********CICD*********/
    /**
     * cicd流水线 创建
     */
    CICD_PIPELINE_CREATE(20),
    /**
     * cicd 流水线 重试
     */
    CICD_PIPELINE_RETRY(20),
    /**
     * cicd 流水线 任务重试
     */
    CICD_PIPELINE_RETRY_TASK(20),

    /**
     * cicd 流水线 启用/停用
     */
    CICD_PIPELINE_STATUS_UPDATE(20),
    /**
     * cicd 流水线 删除
     */
    CICD_PIPELINE_DELETE(20),
    /**
     * cicd 流水线 更新
     */
    CICD_PIPELINE_UPDATE(20),
    /**
     * cicd 流水线 取消
     */
    CICD_PIPELINE_CANCEL(20),

    /**
     * cicd流水线 全新执行
     */
    CICD_PIPELINE_NEW_PERFORM(20),

    /********CICD*********/

    /**
     * ci 流水线 取消
     */
    CI_PIPELINE_CANCEL(20);


    private Integer accesslevel;

    AppServiceEvent(Integer accesslevel) {
        this.accesslevel = accesslevel;
    }

    public Integer getAccessLevel() {
        return accesslevel;
    }

}
