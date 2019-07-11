package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ProjectConfigDTO;
import io.choerodon.devops.app.service.ProjectConfigHarborService;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.dto.harbor.Project;
import io.choerodon.devops.infra.feign.HarborClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;

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
            Response<Void> result = null;

            ProjectVO projectE = iamRepository.queryIamProject(projectId);
            result = harborClient.insertProject(new Project(
                    projectE.getOrganization().getCode() + "-" + projectE.getCode(), 1)).execute();

            if (result.raw().code() != 201) {
                throw new CommonException(result.errorBody().string());
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }
}
