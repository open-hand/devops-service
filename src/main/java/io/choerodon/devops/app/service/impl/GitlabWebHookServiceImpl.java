package io.choerodon.devops.app.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.GitlabWebHookService;

@Service
public class GitlabWebHookServiceImpl implements GitlabWebHookService {
    private Gson gson = new Gson();
    @Override
    public void forwardingEventToPortal(String body, String token) {
        JsonObject returnData = new JsonParser().parse(body).getAsJsonObject();
        String type = returnData.get("event_type").toString();
        switch (type) {
            case "merge_request":
               break;
            case "push":
                break;
        }

        System.out.println(type);
    }
}
