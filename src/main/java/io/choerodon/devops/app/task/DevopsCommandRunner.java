package io.choerodon.devops.app.task;

import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ProjectConfigVO;
import io.choerodon.devops.app.service.DevopsProjectConfigService;
import io.choerodon.devops.infra.dto.DevopsProjectConfigDTO;
import io.choerodon.devops.infra.enums.ProjectConfigType;
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

    private Gson gson = new Gson();

    @Autowired
    private DevopsProjectConfigService devopsProjectConfigService;
    @Value("${services.helm.url}")
    private String servicesHelmUrl;
    @Value("${services.harbor.baseUrl}")
    private String servicesHarborBaseUrl;
    @Value("${services.harbor.username}")
    private String servicesHarborUsername;
    @Value("${services.harbor.password}")
    private String servicesHarborPassword;

    @Override
    public void run(String... strings) {
        try {
            ProjectConfigVO harborConfig = new ProjectConfigVO();
            harborConfig.setUrl(servicesHarborBaseUrl);
            harborConfig.setUserName(servicesHarborUsername);
            harborConfig.setPassword(servicesHarborPassword);
            initConfig(harborConfig, HARBOR_NAME, ProjectConfigType.HARBOR.getType());

            ProjectConfigVO chartConfig = new ProjectConfigVO();
            chartConfig.setUrl(servicesHelmUrl);
            initConfig(chartConfig, CHART_NAME, ProjectConfigType.CHART.getType());
        } catch (Exception e) {
            throw new CommonException("error.init.project.config", e);
        }
    }

    private void initConfig(ProjectConfigVO configDTO, String configName, String configType) {
        DevopsProjectConfigDTO newConfigDTO = new DevopsProjectConfigDTO();
        newConfigDTO.setConfig(gson.toJson(configDTO));
        newConfigDTO.setName(configName);
        newConfigDTO.setType(configType);
        DevopsProjectConfigDTO oldConfigDTO = devopsProjectConfigService.baseQueryByName(null, configName);
        if (oldConfigDTO == null) {
            devopsProjectConfigService.baseCreate(newConfigDTO);
        } else if (!configDTO.equals(gson.fromJson(oldConfigDTO.getConfig(), ProjectConfigVO.class))) {
            newConfigDTO.setId(oldConfigDTO.getId());
            newConfigDTO.setObjectVersionNumber(oldConfigDTO.getObjectVersionNumber());
            devopsProjectConfigService.baseUpdate(newConfigDTO);
        }
    }
}
