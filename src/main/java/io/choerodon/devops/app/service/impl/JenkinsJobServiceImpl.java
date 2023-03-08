package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.common.IntegerResponse;
import com.cdancy.jenkins.rest.domain.common.Response;
import com.cdancy.jenkins.rest.domain.job.*;
import com.cdancy.jenkins.rest.parsers.CustomResponseUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.jenkins.*;
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
    public void build(Long projectId, Long serverId, String folder, String name, List<PropertyVO> properties) {
        JenkinsClient jenkinsClient = jenkinsClientUtil.getClientByServerId(serverId);
        IntegerResponse build;
        if (CollectionUtils.isEmpty(properties)) {
            build = jenkinsClient.api().jobsApi().build(folder, name);
        } else {
            Map<String, List<String>> paramMap = new HashMap<>();
            properties.forEach(property -> {
                List<String> valueList = new ArrayList<>();
                valueList.add(property.getValue());
                paramMap.put(property.getKey(), valueList);
            });
            build = jenkinsClient.api().jobsApi().buildWithParameters(folder, name, paramMap);
        }
        if (!CollectionUtils.isEmpty(build.errors())) {
            throw new CommonException("devops.build.failed");
        }
    }

    @Override
    public List<PropertyVO> listProperty(Long projectId, Long serverId, String folder, String name) {
        JenkinsClient jenkinsClient = jenkinsClientUtil.getClientByServerId(serverId);
        JobInfo jobInfo = jenkinsClient.api().jobsApi().jobInfo(folder, name);
        List<PropertyVO> propertyList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(jobInfo.property())) {
            for (ParametersDefinitionProperty parametersDefinitionProperty : jobInfo.property()) {
                if ("hudson.model.ParametersDefinitionProperty".equals(parametersDefinitionProperty.clazz())) {
                    for (ParameterDefinition parameterDefinition : parametersDefinitionProperty.parameterDefinitions()) {
                        propertyList.add(new PropertyVO(parameterDefinition.name(),
                                parameterDefinition.defaultParameterValue().get("value") == null ? "" : parameterDefinition.defaultParameterValue().get("value")));
                    }
                }
            }
        }

        return propertyList;
    }

    @Override
    public List<JenkinsBuildInfo> listBuildHistory(Long projectId, Long serverId, String folder, String name) {
        JenkinsClient clientByServerId = jenkinsClientUtil.getClientByServerId(serverId);
        Response response = clientByServerId.api().c7nJobsApi().buildHistory(folder, name);

        List<JenkinsBuildInfo> jenkinsBuildInfoList = CustomResponseUtil.parse(response, new TypeReference<List<JenkinsBuildInfo>>() {
        });
        if (CollectionUtils.isEmpty(jenkinsBuildInfoList)) {
            return new ArrayList<>();
        }

        for (JenkinsBuildInfo jenkinsBuildInfo : jenkinsBuildInfoList) {
            JenkinsPendingInputAction nextPendingInputAction = jenkinsBuildInfo.getNextPendingInputAction();
            if (nextPendingInputAction != null && !CollectionUtils.isEmpty(nextPendingInputAction.getInputs())) {
                List<JenkinsInputParameterDef> inputs = nextPendingInputAction.getInputs();
                nextPendingInputAction.setPropertyList(inputs.stream().map(i -> new PropertyVO(i.getName(),
                                i.getDefinition().get("defaultVal") == null ? "" : i.getDefinition().get("defaultVal").toString()))
                        .collect(Collectors.toList()));
            }

        }
        return jenkinsBuildInfoList;
    }

    @Override
    public void stopBuild(Long projectId, Long serverId, String folder, String name, Integer buildId) {
        JenkinsClient jenkinsClient = jenkinsClientUtil.getClientByServerId(serverId);
        jenkinsClient.api().jobsApi().stop(folder, name, buildId);
    }

    @Override
    public void retryBuild(Long projectId, Long serverId, String folder, String name, Integer buildId) {
        JenkinsClient jenkinsClient = jenkinsClientUtil.getClientByServerId(serverId);
        jenkinsClient.api().jobsApi().stop(folder, name, buildId);
    }

    @Override
    public void auditPass(Long projectId, Long serverId, String folder, String name, Integer buildId, String inputId) {
        JenkinsClient jenkinsClient = jenkinsClientUtil.getClientByServerId(serverId);
        jenkinsClient.api().c7nJobsApi().inputSubmit(folder, name, buildId, inputId);
    }

    @Override
    public void auditRefuse(Long projectId, Long serverId, String folder, String name, Integer buildId, String inputId) {
        JenkinsClient jenkinsClient = jenkinsClientUtil.getClientByServerId(serverId);
        jenkinsClient.api().c7nJobsApi().abort(folder, name, buildId, inputId);
    }

    @Override
    public JenkinsBuildInfo queryBuildInfo(Long projectId, Long serverId, String folder, String name, Integer buildId) {
        JenkinsClient clientByServerId = jenkinsClientUtil.getClientByServerId(serverId);
        Response response = clientByServerId.api().c7nJobsApi().buildInfo(folder, name, buildId);
        return CustomResponseUtil.parse(response, JenkinsBuildInfo.class);
    }

    @Override
    public String queryLog(Long projectId, Long serverId, String folder, String name, Integer buildId) {
        JenkinsClient clientByServerId = jenkinsClientUtil.getClientByServerId(serverId);
        ProgressiveText progressiveText = clientByServerId.api().jobsApi().progressiveText(folder, name, buildId, 0);
        return progressiveText.text();
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
            } else if (JenkinsJobTypeEnum.WORKFLOW_MULTI_BRANCH_PROJECT.className().equals(job.clazz())) {
                JenkinsJobVO jenkinsJobVO = new JenkinsJobVO(serverId,
                        serverName,
                        JenkinsJobTypeEnum.getTypeByClassName(job.clazz()),
                        folder,
                        job.name(),
                        job.url());
                List<JenkinsJobVO> workflowJobList = new ArrayList<>();
                jenkinsJobVO.setJobs(workflowJobList);
                jenkinsJobVOList.add(jenkinsJobVO);
                listFolderJobs(jenkinsClient, serverId, serverName, folder + job.name() + "/", workflowJobList);
            } else {
                JenkinsJobVO jenkinsJobVO = new JenkinsJobVO(serverId,
                        serverName,
                        JenkinsJobTypeEnum.getTypeByClassName(job.clazz()),
                        folder,
                        job.name(),
                        job.url());
                C7nBuildInfo buildInfo = jenkinsClient.api().c7nJobsApi().lastBuild(folder, job.name());
                if (buildInfo != null) {
                    jenkinsJobVO.setStartTimeMillis(buildInfo.startTimeMillis());
                    jenkinsJobVO.setDurationMillis(buildInfo.durationTimeMillis());
                    jenkinsJobVO.setUsername(buildInfo.username());
                    jenkinsJobVO.setTriggerType(buildInfo.triggerType());
                    jenkinsJobVO.setStatus(buildInfo.status());
                }


                jenkinsJobVOList.add(jenkinsJobVO);
            }
        }
    }
}
