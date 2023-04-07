package io.choerodon.devops.infra.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cdancy.jenkins.rest.JenkinsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsJenkinsServerService;
import io.choerodon.devops.infra.dto.DevopsJenkinsServerDTO;
import io.choerodon.devops.infra.dto.JenkinsClientWrapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/3 9:49
 */
@Component
public class JenkinsClientUtil {

    private final Map<Long, JenkinsClientWrapper> jenkinsClientMap = new ConcurrentHashMap<>();
    @Autowired
    private DevopsJenkinsServerService devopsJenkinsServerService;

    public JenkinsClient getClientByServerId(Long serverId) {
//        JenkinsClientWrapper jenkinsClientWrapper = jenkinsClientMap.get(serverId);
        DevopsJenkinsServerDTO devopsJenkinsServerDTO = devopsJenkinsServerService.queryById(serverId);
//        if (jenkinsClientWrapper == null || jenkinsClientWrapper.getVersionId() < devopsJenkinsServerDTO.getObjectVersionNumber()) {
//            synchronized (this) {
//                JenkinsClient jenkinsClient = JenkinsClient.builder()
//                        .endPoint(devopsJenkinsServerDTO.getUrl())
//                        .credentials(String.format("%s:%s", devopsJenkinsServerDTO.getUsername(), devopsJenkinsServerDTO.getPassword()))
//                        .build();
//                jenkinsClientWrapper = new JenkinsClientWrapper(devopsJenkinsServerDTO.getObjectVersionNumber(), jenkinsClient);
//                jenkinsClientMap.put(serverId, jenkinsClientWrapper);
//            }
//        }
//        return jenkinsClientWrapper.getJenkinsClient();

        return JenkinsClient.builder()
                .endPoint(devopsJenkinsServerDTO.getUrl())
                .credentials(String.format("%s:%s", devopsJenkinsServerDTO.getUsername(), devopsJenkinsServerDTO.getPassword()))
                .build();
    }

}
