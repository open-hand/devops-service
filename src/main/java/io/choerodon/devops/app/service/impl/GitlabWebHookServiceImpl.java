package io.choerodon.devops.app.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.api.dto.PushWebHookDTO;
import io.choerodon.devops.app.service.GitlabWebHookService;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
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
                saveDevopsMergeRequest(devopsMergeRequestDTO);
                break;
            case "push":
                devopsGitService.branchSync(JSONArray.parseObject(body, PushWebHookDTO.class), token);
                break;
            default:
                break;
        }
    }

    private void saveDevopsMergeRequest(DevopsMergeRequestDTO devopsMergeRequestDTO) {
        DevopsMergeRequestE devopsMergeRequestE = ConvertHelper.convert(devopsMergeRequestDTO,
                DevopsMergeRequestE.class);
        Long projectId = devopsMergeRequestDTO.getProject().getId();
        Long gitlabMergeRequestId = devopsMergeRequestDTO.getObjectAttributes().getIid();
        Long mergeRequestId = devopsMergeRequestRepository.queryByAppIdAndGitlabId(projectId, gitlabMergeRequestId).getId();
        if (mergeRequestId == null) {
            Integer index = devopsMergeRequestRepository.create(devopsMergeRequestE);
            if (index == 0) {
                throw new CommonException("error.save.merge.request");
            }
        } else {
            devopsMergeRequestE.setId(mergeRequestId);
            Integer temp = devopsMergeRequestRepository.update(devopsMergeRequestE);
            if (temp == 0) {
                throw new CommonException("error.update.merge.request");
            }
        }
    }
}
