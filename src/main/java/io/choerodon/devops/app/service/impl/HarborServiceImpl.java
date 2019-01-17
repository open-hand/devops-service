package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.domain.application.event.HarborPayload;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dataobject.harbor.Project;
import io.choerodon.devops.infra.feign.HarborClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Response;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:37
 * Description:
 */
@Component
public class HarborServiceImpl implements HarborService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HarborServiceImpl.class);
    private static final Gson gson = new Gson();
    private HarborClient harborClient;

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;

    public HarborServiceImpl(HarborClient harborClient) {
        this.harborClient = harborClient;
    }

    @Override
    public void createHarbor(HarborPayload harborPayload) {
        //创建harbor仓库
        try {
            Response<Object> result = null;
            System.out.println("testtest" + harborConfigurationProperties.getParams());
            if (harborConfigurationProperties.getParams() == null || harborConfigurationProperties.getParams().equals("")) {
                result = harborClient.insertProject(new Project(harborPayload.getProjectCode(), 1)).execute();
            } else {
                Map<String, String> params = new HashMap<>();
                params = gson.fromJson(harborConfigurationProperties.getParams(), params.getClass());
                result = harborClient.insertProject(params, new Project(harborPayload.getProjectCode(), 1)).execute();
            }
            if (result.raw().code() != 201 || result.raw().code() != 409) {
                LOGGER.error("The request url is ", result.raw().request().url().toString());
                LOGGER.error("create harbor project error {}", result.errorBody());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

    }
}
