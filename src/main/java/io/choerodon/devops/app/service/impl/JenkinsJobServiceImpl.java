package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.job.C7nBuildInfo;
import com.cdancy.jenkins.rest.domain.job.Job;
import com.cdancy.jenkins.rest.domain.job.JobList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.jenkins.JenkinsJobVO;
import io.choerodon.devops.app.DevopsJenkinsServerService;
import io.choerodon.devops.app.service.JenkinsJobService;
import io.choerodon.devops.infra.dto.DevopsJenkinsServerDTO;
import io.choerodon.devops.infra.enums.DevopsJenkinsServerStatusEnum;
import io.choerodon.devops.infra.enums.jenkins.JenkinsJobTypeEnum;
import io.choerodon.devops.infra.util.JenkinsClientUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/3 9:24
 */
@Service
public class JenkinsJobServiceImpl implements JenkinsJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsJobServiceImpl.class);

    @Autowired
    private JenkinsClientUtil jenkinsClientUtil;
    @Autowired
    private DevopsJenkinsServerService devopsJenkinsServerService;

    @Override
    public List<JenkinsJobVO> listAll(Long projectId) {
        List<JenkinsJobVO> jenkinsJobVOList = new ArrayList<>();
        List<DevopsJenkinsServerDTO> devopsJenkinsServerDTOS = devopsJenkinsServerService.listByProjectId(projectId);
//        DevopsJenkinsServerDTO devopsJenkinsServerDTO = devopsJenkinsServerService.queryById(serverId);

        for (DevopsJenkinsServerDTO devopsJenkinsServerDTO : devopsJenkinsServerDTOS) {
//            if (DevopsJenkinsServerStatusEnum.DISABLE.getStatus().equals(devopsJenkinsServerDTO.getStatus())) {
//                throw new CommonException("devops.jenkins.server.is.disable");
//            }
            if (DevopsJenkinsServerStatusEnum.ENABLED.getStatus().equals(devopsJenkinsServerDTO.getStatus())) {
                Long serverId = devopsJenkinsServerDTO.getId();
                String serverName = devopsJenkinsServerDTO.getName();
                JenkinsClient jenkinsClient = jenkinsClientUtil.getClientByServerId(serverId);
                try {
                    listFolderJobs(jenkinsClient, serverId, serverName, "/", jenkinsJobVOList);
                } catch (Exception e) {
                    LOGGER.error("Query jenkins jobs failed, server name: {}", serverName, e);
                }
            }
        }

        return jenkinsJobVOList;
    }

    @Override
    public void build(Long projectId, Long serverId, String folder, String name, Map<String, String> params) {
        JenkinsClient jenkinsClient = jenkinsClientUtil.getClientByServerId(serverId);
        if (CollectionUtils.isEmpty(params)) {
            jenkinsClient.api().jobsApi().build(folder, name);
        } else {
            Map<String, List<String>> paramMap = new HashMap<>();
            params.forEach((k, v) -> {
                List<String> valueList = new ArrayList<>();
                valueList.add(v);
                paramMap.put(k, valueList);
            });
            jenkinsClient.api().jobsApi().buildWithParameters(folder, name, paramMap);
        }
    }

    private void listFolderJobs(JenkinsClient jenkinsClient, Long serverId, String serverName, String folder, List<JenkinsJobVO> jenkinsJobVOList) {

        JobList jobList = jenkinsClient.api().jobsApi().jobList(folder);
        if (CollectionUtils.isEmpty(jobList.jobs())) {
            return;
        }
        for (Job job : jobList.jobs()) {
            if (JenkinsJobTypeEnum.FOLDER.className().equals(job.clazz())
                    || JenkinsJobTypeEnum.ORGANIZATION_FOLDER.className().equals(job.clazz())) {
                listFolderJobs(jenkinsClient, serverId, serverName, folder + job.name() + "/", jenkinsJobVOList);
            } else {
                JenkinsJobVO jenkinsJobVO = new JenkinsJobVO(serverId,
                        serverName,
                        JenkinsJobTypeEnum.getTypeByClassName(job.clazz()),
                        folder,
                        job.name(),
                        job.url());
                C7nBuildInfo buildInfo = jenkinsClient.api().c7nJobsApi().lastBuild(folder, job.name());
                jenkinsJobVO.setStartTimeMillis(buildInfo.startTimeMillis());
                jenkinsJobVO.setDurationMillis(buildInfo.durationTimeMillis());
                jenkinsJobVO.setUsername(buildInfo.username());
                jenkinsJobVO.setTriggerType(buildInfo.triggerType());

                jenkinsJobVOList.add(jenkinsJobVO);
            }
        }
    }
}
