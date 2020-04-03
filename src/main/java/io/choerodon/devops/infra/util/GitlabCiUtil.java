package io.choerodon.devops.infra.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import io.choerodon.devops.infra.dto.gitlab.ci.GitlabCi;

/**
 * @author zmf
 * @since 20-4-2
 */
public class GitlabCiUtil {
    private static final String GITLAB_CI_YAML_TAG = "!!io.choerodon.devops.infra.dto.gitlab.ci.GitlabCi";

    private GitlabCiUtil() {
    }

    public static Yaml getYamlObject() {
        Representer representer = new SkipNullAndUnwrapMapRepresenter();
        representer.setPropertyUtils(new FieldOrderPropertyUtil());
//        Representer representer = new SkipNullRepresenterUtil();
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);
        options.setPrettyFlow(true);
        return new Yaml(representer, options);
    }

    public static String gitlabCi2yaml(GitlabCi gitlabCi) {
//        return getYamlObject().dump(gitlabCi).replaceAll(GITLAB_CI_YAML_TAG, "---");
        return getYamlObject().dump(gitlabCi);
    }
}
