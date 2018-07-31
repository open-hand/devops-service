package io.choerodon.devops.domain.service.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import com.esotericsoftware.yamlbeans.YamlWriter;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Service;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.service.DeployService;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;

/**
 * Created by younger on 2018/4/18.
 */
@Service
public class DeployServiceImpl implements DeployService {

    private ObjectMapper mapper = new ObjectMapper();
    Yaml yaml = new Yaml();
    JSON json = new JSON();
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;


    @Value("${services.helm.url}")
    private String helmUrl;

    @Override
    @Async
    public void deploy(ApplicationE applicationE, ApplicationVersionE applicationVersionE, ApplicationInstanceE applicationInstanceE, DevopsEnvironmentE devopsEnvironmentE, String values, String type, Long userId) {
        C7nHelmRelease C7nHelmRelease = new C7nHelmRelease();
        C7nHelmRelease.setApiVersion("choerodon.io:v1alpha1");
        C7nHelmRelease.setKind("C7NHelmRelease");
        C7nHelmRelease.getMetadata().setName(applicationInstanceE.getCode());
        C7nHelmRelease.getSpec().setRepoUrl(helmUrl+applicationVersionE.getRepository());
        C7nHelmRelease.getSpec().setChartName(applicationE.getCode());
        C7nHelmRelease.getSpec().setChartVersion(applicationVersionE.getVersion());
        C7nHelmRelease.getSpec().setValues(FileUtil.getChangeYaml(null,values));
            String path = applicationInstanceE.getCode()+".yaml";
        YamlWriter writer;
        try {

            writer = new YamlWriter(new FileWriter(path));
            writer.getConfig().setClassTag("C7nHelmRelease", C7nHelmRelease.class);
            writer.write(C7nHelmRelease);
            writer.close();
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }
        String content = FileUtil.getFileContent(new File(path));
        FileUtil.deleteFile(path);
            Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            if(type.equals("create")) {
                gitlabRepository.createFile(projectId,path,content,"ADD FILE",TypeUtil.objToInteger(userId));
            }else {
                gitlabRepository.updateFile(projectId,path,content,"UPDATE FILE",TypeUtil.objToInteger(userId));
            }

    }

}
