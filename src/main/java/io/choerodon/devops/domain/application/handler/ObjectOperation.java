package io.choerodon.devops.domain.application.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.infra.common.util.SkipNullRepresenterUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;

@Component
public class ObjectOperation<T> {

    @Autowired
    GitlabRepository gitlabRepository;

    private T t;

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public void oprerationEnvGitlabFile(String fileCode, Integer gitlabEnvProjectId, String type, Long userId) {
        SkipNullRepresenterUtil skipNullRepresenter = new SkipNullRepresenterUtil();
        String[] className = t.getClass().toString().split("\\.");
        skipNullRepresenter.addClassTag(t.getClass(), new Tag(className[className.length - 1]));
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(skipNullRepresenter, options);
        String content = yaml.dump(t);
        String path = fileCode + ".yaml";
        if (type.equals("create")) {
            gitlabRepository.createFile(gitlabEnvProjectId, path, content, "ADD FILE", TypeUtil.objToInteger(userId));
        } else {
            gitlabRepository.updateFile(gitlabEnvProjectId, path, content, "UPDATE FILE", TypeUtil.objToInteger(userId));
        }
    }
}
