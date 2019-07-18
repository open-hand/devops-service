package io.choerodon.devops.app.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.choerodon.devops.infra.util.FastjsonParserConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.DevopsGitService;
import io.choerodon.devops.app.service.DevopsGitlabCommitService;
import io.choerodon.devops.app.service.DevopsGitlabPipelineService;
import io.choerodon.devops.app.service.GitlabWebHookService;
import io.choerodon.devops.api.vo.iam.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.repository.DevopsMergeRequestRepository;

@Service
public class GitlabWebHookServiceImpl implements GitlabWebHookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabWebHookServiceImpl.class);

    private DevopsMergeRequestRepository devopsMergeRequestRepository;
    private DevopsGitService devopsGitService;
    private DevopsGitlabCommitService devopsGitlabCommitService;
    private DevopsGitlabPipelineService devopsGitlabPipelineService;

    public GitlabWebHookServiceImpl(DevopsMergeRequestRepository devopsMergeRequestRepository, DevopsGitService devopsGitService, DevopsGitlabCommitService devopsGitlabCommitService,
                                    DevopsGitlabPipelineService devopsGitlabPipelineService) {
        this.devopsMergeRequestRepository = devopsMergeRequestRepository;
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
                DevopsMergeRequestE devopsMergeRequestE = ConvertHelper.convert(devopsMergeRequestVO,
                        DevopsMergeRequestE.class);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(devopsMergeRequestE.toString());
                }

                devopsMergeRequestRepository.saveDevopsMergeRequest(devopsMergeRequestE);
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
                PipelineWebHookDTO pipelineWebHookDTO = JSONArray.parseObject(body, PipelineWebHookDTO.class, FastjsonParserConfigProvider.getParserConfig());

                devopsGitlabPipelineService.create(pipelineWebHookDTO, token);
                break;
            case "build":
                JobWebHookDTO jobWebHookDTO = JSONArray.parseObject(body, JobWebHookDTO.class, FastjsonParserConfigProvider.getParserConfig());
                devopsGitlabPipelineService.updateStages(jobWebHookDTO);
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
