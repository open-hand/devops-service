package io.choerodon.devops.app.task;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.sonar.UserToken;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.enums.ProjectConfigType;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.util.RetrofitCallExceptionParse;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:44 2019/3/11
 * Description:
 */
@Component
public class DevopsCommandRunner implements CommandLineRunner {
    public static final Logger LOGGER = LoggerFactory.getLogger(DevopsCommandRunner.class);

    private static final String HARBOR_NAME = "harbor_default";
    private static final String CHART_NAME = "chart_default";
    private static final String SONAR_NAME = "sonar_default";
    private static final String SONAR = "sonar";

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
    @Value("${services.sonarqube.url:}")
    private String sonarqubeUrl;
    @Value("${services.gateway.url}")
    private String gatewayUrl;
    @Value("${services.sonarqube.username:}")
    private String userName;
    @Value("${services.sonarqube.password:}")
    private String password;

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
            if (sonarqubeUrl != null && !sonarqubeUrl.isEmpty()) {
                createSonarToken();
            }
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
        } else if (!gson.toJson(configDTO).equals(oldConfigDTO.getConfig())) {
            newConfigDTO.setId(oldConfigDTO.getId());
            newConfigDTO.setObjectVersionNumber(oldConfigDTO.getObjectVersionNumber());
            devopsConfigService.baseUpdate(newConfigDTO);
        }
    }

    private void createSonarToken() {
        DevopsConfigDTO oldConfigDTO = devopsConfigService.baseQueryByName(null, SONAR_NAME);
        if (oldConfigDTO == null) {
            try {
                SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
                Map<String, String> map = new HashMap<>();
                map.put("name", "token");
                map.put("login", "admin");
                Call<ResponseBody> responseCall = sonarClient.createToken(map);
                UserToken userToken = RetrofitCallExceptionParse.executeCall(responseCall, "error.create.sonar.token", UserToken.class);
                DevopsConfigDTO newConfigDTO = new DevopsConfigDTO();
                newConfigDTO.setConfig(userToken.getToken());
                newConfigDTO.setName(SONAR_NAME);
                newConfigDTO.setType(ProjectConfigType.SONAR.getType());
                devopsConfigService.baseCreate(newConfigDTO);
            } catch (Exception e) {
                LOGGER.error("======创建SonarQube token失败======={}", e.getMessage());
            }
        }
    }
}
