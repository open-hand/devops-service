package io.choerodon.devops.domain.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.websocket.Msg;
import io.choerodon.websocket.helper.CommandSender;

/**
 * Created by younger on 2018/4/18.
 */
@Service
public class DeployServiceImpl implements DeployService {

    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private CommandSender commandSender;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    @Value("${services.helm.url}")
    private String helmUrl;

    @Override
    public void deploy(ApplicationE applicationE, ApplicationVersionE applicationVersionE, ApplicationInstanceE applicationInstanceE, DevopsEnvironmentE devopsEnvironmentE, String values, String type, Long userId) {
        C7nHelmRelease c7nHelmRelease = new C7nHelmRelease();
        c7nHelmRelease.getMetadata().setName(applicationInstanceE.getCode());
        c7nHelmRelease.getMetadata().setCreationTimestamp(new Date());
        c7nHelmRelease.getSpec().setRepoUrl(helmUrl + applicationVersionE.getRepository());
        c7nHelmRelease.getSpec().setChartName(applicationE.getCode());
        c7nHelmRelease.getSpec().setChartVersion(applicationVersionE.getVersion());
        c7nHelmRelease.getSpec().setValues(FileUtil.jsonToYaml(FileUtil.yamlStringtoJson(values)));
        Representer representer = new Representer();
        representer.addClassTag(C7nHelmRelease.class, new Tag("!C7NHelmRelease"));
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(representer, options);
        String content = yaml.dump(c7nHelmRelease);
        String path = applicationInstanceE.getCode() + ".yaml";
        Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
        if (type.equals("create")) {
            gitlabRepository.createFile(projectId, path, content, "ADD FILE", TypeUtil.objToInteger(userId));
        } else {
            gitlabRepository.updateFile(projectId, path, content, "UPDATE FILE", TypeUtil.objToInteger(userId));
        }

    }

    @Override
    public void delete(ApplicationInstanceE applicationInstanceE, DevopsEnvironmentE devopsEnvironmentE, Long userId) {
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(devopsEnvironmentE.getId(), applicationInstanceE.getId(), "C7NHelmRelease");
        gitlabRepository.deleteFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), devopsEnvFileResourceE.getFilePath(), "DELETE FILE", TypeUtil.objToInteger(userId));
    }


    @Async
    @Override
    public void sendCommand(DevopsEnvironmentE devopsEnvironmentE) {
        Msg msg = new Msg();
        msg.setKey("env:" + devopsEnvironmentE.getCode() + ".envId:" + devopsEnvironmentE.getId());
        msg.setType("git_ops_sync");
        msg.setPayload("");
        commandSender.sendMsg(msg);
    }

}
