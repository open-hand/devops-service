package io.choerodon.devops.app.service.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.GitlabWebHookService;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.repository.DevopsMergeRequestRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;

@Service
public class GitlabWebHookServiceImpl implements GitlabWebHookService {

    private static final String KEY = "object_attributes";

    @Autowired
    DevopsMergeRequestRepository devopsMergeRequestRepository;

    @Override
    public void forwardingEventToPortal(String body, String token) {
        JsonObject jsonObject = new JsonParser().parse(body).getAsJsonObject();
        String type = jsonObject.get("object_kind").getAsString();
        switch (type) {
            case "merge_request":
                getDevopsMergeRequestE(jsonObject);
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

    private void getDevopsMergeRequestE(JsonObject jsonObject) {
        DevopsMergeRequestE devopsMergeRequestE = new DevopsMergeRequestE();
        Long appId = jsonObject.getAsJsonObject("project").get("id").getAsLong();
        devopsMergeRequestE.setApplicationId(TypeUtil.objToLong(appId));
        Long gitlabMergeRequestId = jsonObject.getAsJsonObject(KEY).get("id").getAsLong();
        if (devopsMergeRequestRepository.queryByAppIdAndGitlabId(appId,gitlabMergeRequestId) == 0) {
            devopsMergeRequestE.setGitlabMergeRequestId(TypeUtil.objToLong(gitlabMergeRequestId));
            String sourceBranch = jsonObject.getAsJsonObject(KEY).get("source_branch").getAsString();
            devopsMergeRequestE.setSourceBranch(sourceBranch);
            String targetBranch = jsonObject.getAsJsonObject(KEY).get("target_branch").getAsString();
            devopsMergeRequestE.setTargetBranch(targetBranch);
            Integer authorId = jsonObject.getAsJsonObject(KEY).get("author_id").getAsInt();
            devopsMergeRequestE.setAuthorId(TypeUtil.objToLong(authorId));
            if (jsonObject.getAsJsonObject(KEY).get("assignee_id").toString().equals("")) {
                Integer assigneeId = jsonObject.getAsJsonObject(KEY)
                        .get("assignee_id").getAsInt();
                devopsMergeRequestE.setAssigneeId(TypeUtil.objToLong(assigneeId));
            }
            String state = jsonObject.getAsJsonObject(KEY).get("state").getAsString();
            devopsMergeRequestE.setState(state);
            Integer index = devopsMergeRequestRepository.create(devopsMergeRequestE);
            if (index == 0) {
                throw new CommonException("error.save.merge.request");
            }
        }
    }
}
