package io.choerodon.devops.app.task;

import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_SONAR_NAME;

import java.io.IOException;
import java.util.*;

import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import retrofit2.Call;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.sonar.User;
import io.choerodon.devops.api.vo.sonar.UserPageObject;
import io.choerodon.devops.api.vo.sonar.UserToken;
import io.choerodon.devops.api.vo.sonar.UserTokens;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.app.service.DevopsHelmConfigService;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.DevopsHelmConfigDTO;
import io.choerodon.devops.infra.enums.ProjectConfigType;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.util.RetrofitCallExceptionParse;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:44 2019/3/11
 * Description:
 */
@ConditionalOnProperty(value = "local.test", havingValue = "false", matchIfMissing = true)
@Order(100)
@Component
public class DevopsCommandRunner implements CommandLineRunner {
    public static final String SONAR = "sonar";

    public static final String C7N_ANALYSES_USER = "c7n-analyses-user";
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private DevopsHelmConfigService devopsHelmConfigService;

    @Value("${services.helm.url}")
    private String servicesHelmUrl;
    @Value("${services.helm.userName:#{null}}")
    private String servicesHelmUserName;
    @Value("${services.helm.password:#{null}}")
    private String servicesHelmPassword;
    @Value("${services.sonarqube.url:}")
    private String sonarqubeUrl;
    @Value("${services.sonarqube.username:}")
    private String userName;
    @Value("${services.sonarqube.password:}")
    private String password;

    @Override
    public void run(String... strings) {
        try {
            DevopsHelmConfigDTO devopsHelmConfigDTO = new DevopsHelmConfigDTO();
            devopsHelmConfigDTO.setUrl(servicesHelmUrl);
            devopsHelmConfigDTO.setName(UUID.randomUUID().toString());
            // 只有helm的用户名密码都设置了, 才设置到数据库中
            if (StringUtils.hasText(servicesHelmUserName) && StringUtils.hasText(servicesHelmPassword)) {
                devopsHelmConfigDTO.setUsername(servicesHelmUserName);
                devopsHelmConfigDTO.setPassword(servicesHelmPassword);
                devopsHelmConfigDTO.setRepoPrivate(Boolean.TRUE);
            }
            initHelmConfig(devopsHelmConfigDTO);

            if (sonarqubeUrl != null && !sonarqubeUrl.isEmpty()) {
                createSonarToken();
            }
        } catch (Exception e) {
            throw new CommonException("devops.init.project.config", e);
        }
    }

    private void initHelmConfig(DevopsHelmConfigDTO devopsHelmConfigDTO) {
        devopsHelmConfigDTO.setResourceId(0L);
        devopsHelmConfigDTO.setResourceType(ResourceLevel.SITE.value());
        devopsHelmConfigDTO.setRepoDefault(true);
        DevopsHelmConfigDTO oldConfigDTO = devopsHelmConfigService.queryDefaultDevopsHelmConfigByLevel(ResourceLevel.SITE.value(),0L);
        if (oldConfigDTO == null) {
            devopsHelmConfigService.createDevopsHelmConfig(devopsHelmConfigDTO);
        } else if (Objects.equals(oldConfigDTO.getUrl(), devopsHelmConfigDTO.getUrl())) {
            if (!Objects.equals(oldConfigDTO.getUsername(), devopsHelmConfigDTO.getUsername())
                    || !Objects.equals(oldConfigDTO.getPassword(), devopsHelmConfigDTO.getPassword())) {
                devopsHelmConfigDTO.setId(oldConfigDTO.getId());
                devopsHelmConfigDTO.setObjectVersionNumber(oldConfigDTO.getObjectVersionNumber());
                devopsHelmConfigService.updateDevopsHelmConfig(devopsHelmConfigDTO);
            }
        } else {
            oldConfigDTO.setRepoDefault(false);
            devopsHelmConfigService.updateDevopsHelmConfig(oldConfigDTO);
            devopsHelmConfigService.createDevopsHelmConfig(devopsHelmConfigDTO);
        }
    }

    private void createSonarToken() {
        DevopsConfigDTO oldConfigDTO = devopsConfigService.baseQueryByName(null, DEFAULT_SONAR_NAME);
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);

        // 扫描用户如果不存在则新建
        queryOrCreateUser(sonarClient);

        Map<String, String> map = new HashMap<>();
        map.put("name", "ci-new-token");
        map.put("login", C7N_ANALYSES_USER);
        Call<ResponseBody> responseCall = sonarClient.listToken();
        UserTokens userTokens = RetrofitCallExceptionParse.executeCall(responseCall, "devops.sonar.token.get", UserTokens.class);
        Optional<UserToken> userTokenOptional = userTokens.getUserTokens().stream().filter(userToken -> "ci-token".equals(userToken.getName())).findFirst();
        UserToken userToken;
        if (userTokenOptional.isPresent()) {
            Call<Void> revokeToken = sonarClient.revokeToken(map);
            try {
                revokeToken.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Call<ResponseBody> responseCallNew = sonarClient.createToken(map);
        userToken = RetrofitCallExceptionParse.executeCall(responseCallNew, "devops.create.sonar.token", UserToken.class);
        if (oldConfigDTO == null) {
            DevopsConfigDTO newConfigDTO = new DevopsConfigDTO();
            newConfigDTO.setConfig(userToken.getToken());
            newConfigDTO.setName(DEFAULT_SONAR_NAME);
            newConfigDTO.setType(ProjectConfigType.SONAR.getType());
            devopsConfigService.baseCreate(newConfigDTO);
        } else {
            oldConfigDTO.setConfig(userToken.getToken());
            devopsConfigService.baseUpdate(oldConfigDTO);
        }
    }

    private void queryOrCreateUser(SonarClient sonarClient) {
        Map<String, String> map = new HashMap<>();
        map.put("q", C7N_ANALYSES_USER);
        Call<ResponseBody> responseBodyCall = sonarClient.getUser(map);
        UserPageObject userPageObject = RetrofitCallExceptionParse.executeCall(responseBodyCall, "devops.query.user", UserPageObject.class);
        List<User> users = userPageObject.getUsers();

        // 1. 用户不存在则先创建用户
        if (CollectionUtils.isEmpty(users)) {
            creatUser(sonarClient);
        }
        // 2. 给用户分配权限
        addUserPermission(sonarClient);


    }

    private void addUserPermission(SonarClient sonarClient) {
        Map<String, Object> map = new HashMap<>();
        map.put("login", C7N_ANALYSES_USER);
        map.put("permission", "scan");
        Call<ResponseBody> user = sonarClient.addUserPermission(map);
        RetrofitCallExceptionParse.executeCall(user, "devops.add.userPermission", Void.class);
    }

    private void creatUser(SonarClient sonarClient) {
        Map<String, Object> map = new HashMap<>();
        map.put("login", C7N_ANALYSES_USER);
        map.put("name", C7N_ANALYSES_USER);
        map.put("local", false);
        Call<ResponseBody> user = sonarClient.createUser(map);
        RetrofitCallExceptionParse.executeCall(user, "devops.create.user", Void.class);

    }
}
