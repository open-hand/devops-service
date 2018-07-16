package io.choerodon.devops.app.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.api.dto.PushWebHookDTO;
import io.choerodon.devops.app.service.GitlabWebHookService;
import io.choerodon.devops.domain.application.repository.DevopsMergeRequestRepository;

@Service
public class GitlabWebHookServiceImpl implements GitlabWebHookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabWebHookServiceImpl.class);

    @Autowired
    private DevopsMergeRequestRepository devopsMergeRequestRepository;
    @Autowired
    private DevopsGitServiceImpl devopsGitService;

    @Override
    public void forwardingEventToPortal(String body, String token) {
        LOGGER.info(body);
        JsonObject returnData = new JsonParser().parse(body).getAsJsonObject();
        String kind = returnData.get("object_kind").getAsString();
        switch (kind) {
            case "merge_request":
                DevopsMergeRequestDTO devopsMergeRequestDTO = JSONArray.parseObject(body, DevopsMergeRequestDTO.class);
                devopsMergeRequestRepository.saveDevopsMergeRequest(devopsMergeRequestDTO);
                break;
            case "push":
                devopsGitService.branchSync(JSONArray.parseObject(body, PushWebHookDTO.class), token);
                break;
            default:
                break;
        }
    }
}
