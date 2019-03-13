package io.choerodon.devops.app.service.impl;

import com.google.gson.Gson;
import io.choerodon.devops.api.dto.ProjectConfigDTO;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.app.service.ProjectConfigHarborService;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.event.HarborPayload;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.config.RetrofitHandler;
import io.choerodon.devops.infra.dataobject.harbor.Project;
import io.choerodon.devops.infra.feign.HarborClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@Component
public class ProjectConfigHarborServiceImpl implements ProjectConfigHarborService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectConfigHarborServiceImpl.class);

    @Autowired
    IamRepository iamRepository;

    //创建harbor仓库
    @Override
    public void createHarbor(ProjectConfigDTO config, Long projectId) {
        try {
            ConfigurationProperties configurationProperties = new ConfigurationProperties(config);
            configurationProperties.setType("harbor");
            Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
            HarborClient harborClient = retrofit.create(HarborClient.class);
            Response<Object> result = null;

            ProjectE projectE = iamRepository.queryIamProject(projectId);

            result = harborClient.insertProject(new Project(
                    projectE.getOrganization().getCode() + "-" + projectE.getCode(), 1)).execute();

            okhttp3.Response raw = result.raw();

            if (raw.code() != 201 || raw.code() != 409) {
                LOGGER.error("The request url is {}", raw.request().url());
                LOGGER.error("create harbor project error {}", result.errorBody());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
