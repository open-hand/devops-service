package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.choerodon.devops.api.vo.SonarInfoVO;
import io.choerodon.devops.app.service.SonarService;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:10 2019/5/8
 * Description:
 */
@Component
public class SonarServiceImpl implements SonarService {
    @Value("${services.sonarqube.username:}")
    private String userName;

    @Value("${services.sonarqube.password:}")
    private String password;

    @Value("${services.sonarqube.url:}")
    private String url;

    @Override
    public SonarInfoVO getSonarInfo() {
        return new SonarInfoVO(userName, password, url);
    }
}
