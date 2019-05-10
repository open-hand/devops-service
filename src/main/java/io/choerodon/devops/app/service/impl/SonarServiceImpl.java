package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.api.dto.SonarUserDTO;
import io.choerodon.devops.app.service.SonarService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:10 2019/5/8
 * Description:
 */
@Component
public class SonarServiceImpl implements SonarService {
    @Value("${sonar.username}")
    private String userName;

    @Value("${sonar.password}")
    private String password;

    @Override
    public SonarUserDTO getAdminUser() {
        return new SonarUserDTO(userName, password);
    }
}
