package io.choerodon.devops.infra.task;

import io.choerodon.core.exception.CommonException;
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
            System.out.println(servicesHelmUrl);
        } catch (Exception e) {
            throw new CommonException("error.init.project.config", e);
        }
    }
}
