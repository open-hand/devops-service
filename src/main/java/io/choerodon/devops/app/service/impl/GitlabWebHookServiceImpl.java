package io.choerodon.devops.app.service.impl;

import com.alibaba.fastjson.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.DevopsMergeRequestDTO;
import io.choerodon.devops.app.service.GitlabWebHookService;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.repository.DevopsMergeRequestRepository;

@Service
public class GitlabWebHookServiceImpl implements GitlabWebHookService {

    @Autowired
    DevopsMergeRequestRepository devopsMergeRequestRepository;

    @Override
    public void forwardingEventToPortal(String body, String token) {
        DevopsMergeRequestDTO devopsMergeRequestDTO =JSONArray.parseObject(body, DevopsMergeRequestDTO.class);
        String type=devopsMergeRequestDTO.getObjectKind();
        switch (type) {
            case "merge_request":
                saveDevopsMergeRequest(devopsMergeRequestDTO);
                break;
            case "pipeline":
                break;
            case "push":
                break;
            case "issues":
                break;
            case "job":
                break;
            default:
                break;
        }
    }

    private void saveDevopsMergeRequest(DevopsMergeRequestDTO devopsMergeRequestDTO) {
        Long projectId = devopsMergeRequestDTO.getProject().getId();
        Long gitlabMergeRequestId =devopsMergeRequestDTO.getObjectAttributes().getIid();
        if (devopsMergeRequestRepository.queryByAppIdAndGitlabId(projectId,gitlabMergeRequestId) == 0) {
            DevopsMergeRequestE devopsMergeRequestE = ConvertHelper.convert(devopsMergeRequestDTO,
                    DevopsMergeRequestE.class);
            Integer index = devopsMergeRequestRepository.create(devopsMergeRequestE);
            if (index == 0) {
                throw new CommonException("error.save.merge.request");
            }
        }
    }
}
