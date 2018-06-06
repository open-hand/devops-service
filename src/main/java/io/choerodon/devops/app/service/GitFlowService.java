package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.domain.application.entity.gitlab.GitFlowE;
import io.choerodon.devops.domain.application.event.GitFlowFinishPayload;
import io.choerodon.devops.domain.application.event.GitFlowStartPayload;
import io.choerodon.devops.infra.dataobject.gitlab.TagsDO;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 11:40
 * Description:
 */
public interface GitFlowService {
    /**
     * 获取项目的所有tag
     *
     * @param projectId     项目ID
     * @param applicationId 服务ID
     * @param page          页数
     * @param size          每页个数
     * @return tag列表
     */
    TagsDO getTags(Long projectId, Long applicationId, Integer page, Integer size);

    /**
     * 刷新合并请求合并状态
     *
     * @param applicationId 应用ID
     * @param branchName    分支名称
     * @return 合并请求状态
     */
    String updateMRStatus(Long applicationId, String branchName);


    /**
     * 获取分支，不包括结束中分支
     *
     * @param projectId     项目Id
     * @param applicationId 服务ID
     * @return List
     */
    List<GitFlowE> getBranches(Long projectId, Long applicationId);

    /**
     * 获取release版本
     *
     * @param applicationId 服务ID
     * @return 发布版本号
     */
    String getReleaseNumber(Long applicationId);

    /**
     * 获取hotfix版本
     *
     * @param applicationId 服务ID
     * @return 发布版本号
     */
    String getHotfixNumber(Long applicationId);

    /**
     * 开始 GitFlow
     *
     * @param applicationId 服务ID
     * @param branchName    GitFlow 对象
     */
    void startGitFlow(Long applicationId, String branchName);

    /**
     * 分支开始事件处理
     *
     * @param gitFlowStartPayload 分支开始事件消息
     */
    void gitFlowStart(GitFlowStartPayload gitFlowStartPayload);

    /**
     * 结束 GitFlow-feature
     *
     * @param applicationId 应用ID
     * @param branchName    分支名称
     */
    void finishGitFlowFeature(Long applicationId, String branchName);

    /**
     * 结束 GitFlow
     *
     * @param applicationId 应用ID
     * @param branchName    分支名称
     */
    void finishGitFlow(Long applicationId, String branchName);

    /**
     * 分支结束事件处理
     *
     * @param gitFlowFinishPayload 分支结束事件消息
     */
    void gitFlowFinish(GitFlowFinishPayload gitFlowFinishPayload);
}
