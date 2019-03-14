package io.choerodon.devops.infra.task;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.ProjectConfigDTO;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.infra.common.util.enums.ProjectConfigType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:44 2019/3/11
 * Description:
 */
@Component
public class DevopsCommandRunner implements CommandLineRunner {
    private static final String HARBOR_NAME = "harbor_default";
    private static final String CHART_NAME = "chart_default";
    @Autowired
    DevopsProjectConfigRepository devopsProjectConfigRepository;
    @Value("${services.helm.url}")
    private String servicesHelmUrl;
    @Value("${services.harbor.baseUrl}")
    private String servicesHarborBaseurl;
    @Value("${services.harbor.username}")
    private String servicesHarborUsername;
    @Value("${services.harbor.password}")
    private String servicesHarborPassword;

    @Override
    public void run(String... strings) {
        try {
            ProjectConfigDTO harborConfig = new ProjectConfigDTO();
            harborConfig.setUrl(servicesHarborBaseurl);
            harborConfig.setUserName(servicesHarborUsername);
            harborConfig.setPassword(servicesHarborPassword);
            initConfig(harborConfig, HARBOR_NAME, ProjectConfigType.HARBOR.toString());

            ProjectConfigDTO chartConfig = new ProjectConfigDTO();
            chartConfig.setUrl(servicesHarborBaseurl);
            initConfig(chartConfig, CHART_NAME, ProjectConfigType.CHART.toString());
        } catch (Exception e) {
            throw new CommonException("error.init.project.config", e);
        }
    }

    private void initConfig(ProjectConfigDTO configDTO, String configName, String configType) {
        DevopsProjectConfigE newConfigE = new DevopsProjectConfigE(configName, configDTO, configType);
        DevopsProjectConfigE oldConfigE = devopsProjectConfigRepository.queryByName(null, configName);
        if (oldConfigE == null) {
            devopsProjectConfigRepository.create(newConfigE);
        } else if (!configDTO.equals(oldConfigE.getConfig())) {
            newConfigE.setId(oldConfigE.getId());
            newConfigE.setObjectVersionNumber(oldConfigE.getObjectVersionNumber());
            devopsProjectConfigRepository.updateByPrimaryKeySelective(newConfigE);
        }
    }
}
