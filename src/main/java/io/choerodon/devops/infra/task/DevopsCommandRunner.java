package io.choerodon.devops.infra.task;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:44 2019/3/11
 * Description:
 */
@Component
public class DevopsCommandRunner implements CommandLineRunner {
    private static final String HARBOR_NAME = "harbor_default";
    private static final String CHART_NAME = "chart_default";
    private static final String HARBOR = "harbor";
    private static final String CHART = "chart";
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
            Map<String, Object> harborMap = new HashMap<>();
            harborMap.put("baseUrl", servicesHarborBaseurl);
            harborMap.put("username", servicesHarborUsername);
            harborMap.put("password", servicesHarborPassword);
            initConfig(harborMap, HARBOR_NAME, HARBOR);

            Map<String, Object> chartMap = new HashMap<>();
            chartMap.put("url", servicesHelmUrl);
            initConfig(chartMap, CHART_NAME, CHART);
        } catch (Exception e) {
            throw new CommonException("error.init.project.config", e);
        }
    }

    private void initConfig(Map<String, Object> map, String configName, String configType) {
        String json = new JSONObject(map).toJSONString();
        DevopsProjectConfigE newConfigE = new DevopsProjectConfigE(configName, json, configType);
        DevopsProjectConfigE oldConfigE = devopsProjectConfigRepository.queryByName(null, configName);
        if (oldConfigE == null) {
            devopsProjectConfigRepository.create(newConfigE);
        } else if (!json.equals(oldConfigE.getConfig())) {
            newConfigE.setId(oldConfigE.getId());
            newConfigE.setObjectVersionNumber(oldConfigE.getObjectVersionNumber());
            devopsProjectConfigRepository.updateByPrimaryKeySelective(newConfigE);
        }
    }
}
