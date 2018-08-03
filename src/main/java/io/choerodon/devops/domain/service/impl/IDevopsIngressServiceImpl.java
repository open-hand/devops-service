package io.choerodon.devops.domain.service.impl;

import io.kubernetes.client.models.V1beta1Ingress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.service.IDevopsIngressService;
import io.choerodon.devops.infra.common.util.SkipNullRepresenterUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;

/**
 * Created by Zenger on 2018/5/14.
 */
@Service
public class IDevopsIngressServiceImpl implements IDevopsIngressService {

    private static final Logger logger = LoggerFactory.getLogger(IDevopsIngressServiceImpl.class);

    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    @Override
    public void createIngress(V1beta1Ingress v1beta1Ingress, DevopsEnvironmentE devopsEnvironmentE, Long userId, String type) {
        SkipNullRepresenterUtil skipNullRepresenter = new SkipNullRepresenterUtil();
        skipNullRepresenter.addClassTag(V1beta1Ingress.class, new Tag("!V1beta1Ingress"));
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(skipNullRepresenter, options);
        String content = yaml.dump(v1beta1Ingress);
        String path = v1beta1Ingress.getMetadata().getName() + ".yaml";
        Integer projectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
        if (type.equals("create")) {
            gitlabRepository.createFile(projectId, path, content, "ADD FILE", TypeUtil.objToInteger(userId));
        } else {
            gitlabRepository.updateFile(projectId, path, content, "UPDATE FILE", TypeUtil.objToInteger(userId));
        }
    }

    @Override
    public void deleteIngress(Long ingressId, DevopsEnvironmentE devopsEnvironmentE, Long userId) {
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(devopsEnvironmentE.getId(), ingressId, "Ingress");
        gitlabRepository.deleteFile(
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                devopsEnvFileResourceE.getFilePath(),
                "DELETE FILE",
                TypeUtil.objToInteger(userId));
    }


}
