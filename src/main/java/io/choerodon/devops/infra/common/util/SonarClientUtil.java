package io.choerodon.devops.infra.common.util;

import retrofit2.Retrofit;

import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.RetrofitHandler;
import io.choerodon.devops.infra.feign.SonarClient;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  20:00 2019/5/22
 * Description:
 */
public class SonarClientUtil {

    public SonarClient getSonarClient(String sonarqubeUrl, String sonar) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(sonarqubeUrl);
        configurationProperties.setType(sonar);
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        return retrofit.create(SonarClient.class);
    }
}
