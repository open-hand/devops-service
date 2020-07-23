package io.choerodon.devops.app.task;

import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_CHART_NAME;
import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_SONAR_NAME;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import retrofit2.Call;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.sonar.UserToken;
import io.choerodon.devops.api.vo.sonar.UserTokens;
import io.choerodon.devops.app.service.AppServiceVersionService;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.enums.ProjectConfigType;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.DevopsConfigMapper;
import io.choerodon.devops.infra.util.RetrofitCallExceptionParse;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:44 2019/3/11
 * Description:
 */
@Component
public class DevopsCommandRunner implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCommandRunner.class);

    public static final String SONAR = "sonar";

    private final Gson gson = new Gson();

    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private DevopsConfigMapper devopsConfigMapper;

    @Value("${services.helm.url}")
    private String servicesHelmUrl;
    @Value("${services.helm.userName:#{null}}")
    private String servicesHelmUserName;
    @Value("${services.helm.password:#{null}}")
    private String servicesHelmPassword;
    @Value("${services.harbor.update:true}")
    private Boolean servicesHarborUpdate;
    @Value("${services.sonarqube.url:}")
    private String sonarqubeUrl;
    @Value("${services.sonarqube.username:}")
    private String userName;
    @Value("${services.sonarqube.password:}")
    private String password;

    @Override
    public void run(String... strings) {
        if (servicesHarborUpdate) {
            try {
                ConfigVO chartConfig = new ConfigVO();
                chartConfig.setUrl(servicesHelmUrl);
                // 只有helm的用户名密码都设置了, 才设置到数据库中
                if (!StringUtils.isEmpty(servicesHelmUserName) && !StringUtils.isEmpty(servicesHelmPassword)) {
                    chartConfig.setUserName(servicesHelmUserName);
                    chartConfig.setPassword(servicesHelmPassword);
                    chartConfig.setPrivate(Boolean.TRUE);
                }
                initConfig(chartConfig, DEFAULT_CHART_NAME, ProjectConfigType.CHART.getType());

                if (sonarqubeUrl != null && !sonarqubeUrl.isEmpty()) {
                    createSonarToken();
                }
            } catch (Exception e) {
                throw new CommonException("error.init.project.config", e);
            }
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
            // 存在判断是否已经生成服务版本，无服务版本，直接覆盖更新；有服务版本，将原config对应的resourceId设置为null,新建config
            if (appServiceVersionService.isVersionUseConfig(oldConfigDTO.getId(), oldConfigDTO.getType())) {
                // 将原有配置的name, app_service, project_id, organization_id 字段置为null
                devopsConfigMapper.updateConfigFieldsNull(oldConfigDTO.getId());
                newConfigDTO.setId(null);
                devopsConfigService.baseCreate(newConfigDTO);
            } else {
                newConfigDTO.setId(oldConfigDTO.getId());
                newConfigDTO.setObjectVersionNumber(oldConfigDTO.getObjectVersionNumber());
                devopsConfigService.baseUpdate(newConfigDTO);
            }
        }
    }

    private void createSonarToken() {
        DevopsConfigDTO oldConfigDTO = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
        if (oldConfigDTO == null) {
            try {
                SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
                Map<String, String> map = new HashMap<>();
                map.put("name", "ci-token");
                map.put("login", "admin");
                Call<ResponseBody> responseCall = sonarClient.listToken();
                UserTokens userTokens = RetrofitCallExceptionParse.executeCall(responseCall, "error.sonar.token.get", UserTokens.class);
                Optional<UserToken> userTokenOptional = userTokens.getUserTokens().stream().filter(userToken -> "ci-token".equals(userToken.getName())).findFirst();
                if (userTokenOptional.isPresent()) {
                    map.put("name", "ci-token-new");
                }
                Call<ResponseBody> responseCallNew = sonarClient.createToken(map);
                UserToken userToken = RetrofitCallExceptionParse.executeCall(responseCallNew, "error.create.sonar.token", UserToken.class);
                DevopsConfigDTO newConfigDTO = new DevopsConfigDTO();
                newConfigDTO.setConfig(userToken.getToken());
                newConfigDTO.setName(DEFAULT_SONAR_NAME);
                newConfigDTO.setType(ProjectConfigType.SONAR.getType());
                devopsConfigService.baseCreate(newConfigDTO);

            } catch (Exception e) {
                LOGGER.error("======创建SonarQube token失败======={}", e.getMessage());
            }
        }
    }
}
