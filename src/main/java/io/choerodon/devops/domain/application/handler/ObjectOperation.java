package io.choerodon.devops.domain.application.handler;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.infra.common.util.SkipNullRepresenterUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;

public class ObjectOperation<T> {

    private T type;

    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    /**
     * operate files in GitLab
     *
     * @param fileCode           file's code
     * @param gitlabEnvProjectId Environment corresponding GitLab project ID
     * @param operationType      operation type
     * @param userId             GitLab user ID
     */
    public void operationEnvGitlabFile(String fileCode, Integer gitlabEnvProjectId, String operationType, Long userId) {
        GitlabRepository gitlabRepository = ApplicationContextHelper.getSpringFactory().getBean(GitlabRepository.class);
        SkipNullRepresenterUtil skipNullRepresenter = new SkipNullRepresenterUtil();
        String[] className = this.type.getClass().toString().split("\\.");
        skipNullRepresenter.addClassTag(this.type.getClass(), new Tag(className[className.length - 1]));
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(skipNullRepresenter, options);
        String content = yaml.dump(this.type);
        String path = fileCode + ".yaml";
        if (operationType.equals("create")) {
            gitlabRepository.createFile(gitlabEnvProjectId, path, content,
                    "ADD FILE", TypeUtil.objToInteger(userId));
        } else {
            gitlabRepository.updateFile(gitlabEnvProjectId, path, content,
                    "UPDATE FILE", TypeUtil.objToInteger(userId));
        }
    }
}
