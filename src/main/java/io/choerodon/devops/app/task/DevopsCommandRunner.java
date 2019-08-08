package io.choerodon.devops.app.task;

import com.google.gson.Gson;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.enums.ProjectConfigType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:44 2019/3/11
 * Description:
 */
//@Component
public class DevopsCommandRunner implements CommandLineRunner {
    private static final String HARBOR_NAME = "harbor_default";
    private static final String CHART_NAME = "chart_default";

    private Gson gson = new Gson();

    @Autowired
    private DevopsConfigService devopsConfigService;
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
            ConfigVO harborConfig = new ConfigVO();
            harborConfig.setUrl(servicesHarborBaseUrl);
            harborConfig.setUserName(servicesHarborUsername);
            harborConfig.setPassword(servicesHarborPassword);
            initConfig(harborConfig, HARBOR_NAME, ProjectConfigType.HARBOR.getType());

            ConfigVO chartConfig = new ConfigVO();
            chartConfig.setUrl(servicesHelmUrl);
            initConfig(chartConfig, CHART_NAME, ProjectConfigType.CHART.getType());
        } catch (Exception e) {
            throw new CommonException("error.init.project.config", e);
        }
    }

    private void initConfig(ConfigVO configDTO, String configName, String configType) {
        DevopsConfigDTO newConfigDTO = new DevopsConfigDTO();
        newConfigDTO.setConfig(gson.toJson(configDTO));
        newConfigDTO.setName(configName);
        newConfigDTO.setType(configType);
        DevopsConfigDTO oldConfigDTO = devopsConfigService.baseQueryByName(null, configName);
        if (oldConfigDTO == null) {
            devopsConfigService.baseCreate(newConfigDTO);
        } else if (!configDTO.equals(gson.fromJson(oldConfigDTO.getConfig(), ConfigVO.class))) {
            newConfigDTO.setId(oldConfigDTO.getId());
            newConfigDTO.setObjectVersionNumber(oldConfigDTO.getObjectVersionNumber());
            devopsConfigService.baseUpdate(newConfigDTO);
        }
    }
}
