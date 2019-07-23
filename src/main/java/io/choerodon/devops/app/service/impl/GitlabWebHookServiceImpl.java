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
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.FastjsonParserConfigProvider;

@Service
public class GitlabWebHookServiceImpl implements GitlabWebHookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabWebHookServiceImpl.class);

    private DevopsMergeRequestService devopsMergeRequestService;
    private DevopsGitService devopsGitService;
    private DevopsGitlabCommitService devopsGitlabCommitService;
    private DevopsGitlabPipelineService devopsGitlabPipelineService;

    public GitlabWebHookServiceImpl(DevopsMergeRequestService devopsMergeRequestService, DevopsGitService devopsGitService, DevopsGitlabCommitService devopsGitlabCommitService,
                                    DevopsGitlabPipelineService devopsGitlabPipelineService) {
        this.devopsMergeRequestService = devopsMergeRequestService;
        this.devopsGitService = devopsGitService;
        this.devopsGitlabPipelineService = devopsGitlabPipelineService;
        this.devopsGitlabCommitService = devopsGitlabCommitService;
    }

    @Override
    public void forwardingEventToPortal(String body, String token) {
        JsonObject returnData = new JsonParser().parse(body).getAsJsonObject();
        String kind = returnData.get("object_kind").getAsString();
        switch (kind) {
            case "merge_request":
                DevopsMergeRequestVO devopsMergeRequestVO = JSONArray.parseObject(body, DevopsMergeRequestVO.class, FastjsonParserConfigProvider.getParserConfig());
                DevopsMergeRequestDTO devopsMergeRequestDTO = ConvertUtils.convertObject(devopsMergeRequestVO, DevopsMergeRequestDTO.class);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(devopsMergeRequestDTO.toString());
                }

                devopsMergeRequestService.baseCreate(devopsMergeRequestDTO);
                break;
            case "push":
                PushWebHookVO pushWebHookVO = JSONArray.parseObject(body, PushWebHookVO.class, FastjsonParserConfigProvider.getParserConfig());
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(pushWebHookVO.toString());
                }

                devopsGitService.branchSync(pushWebHookVO, token);
                devopsGitlabCommitService.create(pushWebHookVO, token);
                break;
            case "pipeline":
                PipelineWebHookVO pipelineWebHookVO = JSONArray.parseObject(body, PipelineWebHookVO.class, FastjsonParserConfigProvider.getParserConfig());

                devopsGitlabPipelineService.create(pipelineWebHookVO, token);
                break;
            case "build":
                JobWebHookVO jobWebHookVO = JSONArray.parseObject(body, JobWebHookVO.class, FastjsonParserConfigProvider.getParserConfig());
                devopsGitlabPipelineService.updateStages(jobWebHookVO);
                break;
            case "tag_push":
                PushWebHookVO tagPushWebHookVO = JSONArray.parseObject(body, PushWebHookVO.class, FastjsonParserConfigProvider.getParserConfig());
                devopsGitlabCommitService.create(tagPushWebHookVO, token);
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
            devopsGitService.fileResourceSyncSaga(pushWebHookVO, token);
        }
    }
}
