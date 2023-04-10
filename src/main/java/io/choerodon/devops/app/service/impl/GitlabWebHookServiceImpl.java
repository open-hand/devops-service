package io.choerodon.devops.app.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.DevopsMergeRequestVO;
import io.choerodon.devops.api.vo.JobWebHookVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.api.vo.PushWebHookVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.CustomContextUtil;
import io.choerodon.devops.infra.util.FastjsonParserConfigProvider;

@Service
public class GitlabWebHookServiceImpl implements GitlabWebHookService {

    private static final String DELETE_COMMIT = "0000000000000000000000000000000000000000";

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabWebHookServiceImpl.class);

    private DevopsMergeRequestService devopsMergeRequestService;
    private DevopsGitService devopsGitService;
    private DevopsGitlabCommitService devopsGitlabCommitService;
    private DevopsGitlabPipelineService devopsGitlabPipelineService;
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;
    private BaseServiceClientOperator baseServiceClientOperator;


    public GitlabWebHookServiceImpl(DevopsMergeRequestService devopsMergeRequestService,
                                    DevopsGitService devopsGitService,
                                    DevopsGitlabCommitService devopsGitlabCommitService,
                                    DevopsGitlabPipelineService devopsGitlabPipelineService,
                                    DevopsCiPipelineRecordService devopsCiPipelineRecordService,
                                    BaseServiceClientOperator baseServiceClientOperator) {
        this.devopsMergeRequestService = devopsMergeRequestService;
        this.devopsGitService = devopsGitService;
        this.devopsGitlabCommitService = devopsGitlabCommitService;
        this.devopsGitlabPipelineService = devopsGitlabPipelineService;
        this.devopsCiPipelineRecordService = devopsCiPipelineRecordService;
        this.baseServiceClientOperator = baseServiceClientOperator;
    }

    @Override
    public void forwardingEventToPortal(String body, String token) {
        JsonObject returnData = new JsonParser().parse(body).getAsJsonObject();
        String kind = returnData.get("object_kind").getAsString();
        switch (kind) {
            case "merge_request":
                DevopsMergeRequestVO devopsMergeRequestVO = JSONArray.parseObject(body, DevopsMergeRequestVO.class, FastjsonParserConfigProvider.getParserConfig());
                setUserContext(devopsMergeRequestVO.getUser().getUsername());
                devopsMergeRequestService.create(devopsMergeRequestVO, token);
                break;
            case "push":
                PushWebHookVO pushWebHookVO = JSONArray.parseObject(body, PushWebHookVO.class, FastjsonParserConfigProvider.getParserConfig());
                setUserContext(pushWebHookVO.getUserUserName());
                devopsGitService.branchSync(pushWebHookVO, token);
                break;
            case "pipeline":
                PipelineWebHookVO pipelineWebHookVO = JSONArray.parseObject(body, PipelineWebHookVO.class, FastjsonParserConfigProvider.getParserConfig());
                devopsGitlabPipelineService.create(pipelineWebHookVO, token);
                // 保存ci流水线执行记录
                devopsCiPipelineRecordService.create(pipelineWebHookVO, token);
                break;
            case "build":
                JobWebHookVO jobWebHookVO = JSONArray.parseObject(body, JobWebHookVO.class, FastjsonParserConfigProvider.getParserConfig());
                devopsGitlabPipelineService.updateStages(jobWebHookVO, token);
                break;
            case "tag_push":
                PushWebHookVO tagPushWebHookVO = JSONArray.parseObject(body, PushWebHookVO.class, FastjsonParserConfigProvider.getParserConfig());
                String afterCommitSha = tagPushWebHookVO.getAfter();
                //表示删除tag
                if (DELETE_COMMIT.equals(afterCommitSha)) {
                    devopsGitlabCommitService.deleteTag(tagPushWebHookVO, token);
                } else {
                    setUserContext(tagPushWebHookVO.getUserUserName());
                    devopsGitlabCommitService.create(tagPushWebHookVO, token, GitOpsConstants.TAG_PUSH);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void gitOpsWebHook(String body, String token) {
        JsonObject returnData = new JsonParser().parse(body).getAsJsonObject();
        String kind = returnData.get("object_kind").getAsString();
        if ("push".equals(kind)) {
            PushWebHookVO pushWebHookVO = JSONArray.parseObject(body, PushWebHookVO.class, FastjsonParserConfigProvider.getParserConfig());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(pushWebHookVO.toString());
            }
            // 不处理删除环境库的分支的情况
            if (GitOpsConstants.NO_COMMIT_SHA.equals(pushWebHookVO.getAfter())) {
                LOGGER.debug("GitOps PushWebHook is ignored due to branch deleting. Ref: {}", pushWebHookVO.getRef());
                return;
            }
            // 只处理master分支的commit, 不处理其他分支的commit
            if (!GitOpsConstants.MASTER_REF.equals(pushWebHookVO.getRef())) {
                LOGGER.debug("GitOps PushWebHook of ref {} is ignored because the ref is not master.", pushWebHookVO.getRef());
                return;
            }
            devopsGitService.fileResourceSyncSaga(pushWebHookVO, token);
        }
    }

    private void setUserContext(String loginName) {
        try {
            IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByLoginName(loginName);
            if (iamUserDTO != null) {
                CustomContextUtil.setUserContext(iamUserDTO.getId());
            }
        } catch (Exception ex) {
            LOGGER.info("Failed to query user by login name {}", loginName);
        }
    }
}
