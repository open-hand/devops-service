package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.domain.application.event.HarborPayload;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.config.RetrofitHandler;
import io.choerodon.devops.infra.dataobject.harbor.Project;
import io.choerodon.devops.infra.dataobject.harbor.ProjectDetail;
import io.choerodon.devops.infra.dataobject.harbor.Role;
import io.choerodon.devops.infra.dataobject.harbor.User;
import io.choerodon.devops.infra.feign.HarborClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;

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

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;


    @Override
    public void createHarbor(HarborPayload harborPayload) {
        //创建harbor仓库
        try {
            ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
            configurationProperties.setType("harbor");
            Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
            HarborClient harborClient = retrofit.create(HarborClient.class);
            Response<Void> result = null;
            LOGGER.info(harborConfigurationProperties.getParams());
            if (harborConfigurationProperties.getParams() == null || harborConfigurationProperties.getParams().equals("")) {
                result = harborClient.insertProject(new Project(harborPayload.getProjectCode(), 1)).execute();
            } else {
                Map<String, String> params = new HashMap<>();
                params = gson.fromJson(harborConfigurationProperties.getParams(), params.getClass());
                result = harborClient.insertProject(params, new Project(harborPayload.getProjectCode(), 1)).execute();
            }
            if (result.raw().code() != 201) {
                throw new CommonException(result.message());
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }

    }
}
