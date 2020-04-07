package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.GitOpsConstants.*;

import java.util.Arrays;
import java.util.List;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import io.choerodon.devops.infra.dto.gitlab.ci.GitlabCi;

/**
 * @author zmf
 * @since 20-4-2
 */
public class GitlabCiUtil {
    private GitlabCiUtil() {
    }

    /**
     * 获取用于序列化{@link GitlabCi} 的{@link Yaml}对象
     *
     * @return 专用配置的序列化对象
     */
    private static Yaml getYamlObject() {
        Representer representer = new SkipNullAndUnwrapMapRepresenter();
        representer.setPropertyUtils(new FieldOrderPropertyUtil());
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);
        options.setPrettyFlow(true);
        return new Yaml(representer, options);
    }

    /**
     * 将GitlabCi对象转为Yaml格式字符串
     *
     * @param gitlabCi 配置的GitlabCi对象
     * @return yaml格式字符串
     */
    public static String gitlabCi2yaml(GitlabCi gitlabCi) {
        return getYamlObject().dump(gitlabCi);
    }

    /**
     * 将shell脚本（或者yaml文件）的每一行都以 # 号注释掉
     *
     * @param fileContent shell脚本（或者yaml文件）
     * @return 注释后的shell脚本（或者yaml文件）
     */
    public static String commentLines(String fileContent) {
        String[] lines = splitLinesToArray(fileContent);
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            stringBuilder.append(COMMENT_STRING).append(line).append(NEW_LINE);
        }
        return stringBuilder.toString();
    }

    /**
     * 将shell脚本（或者yaml文件）中的注释的行删除
     *
     * @param fileContent shell脚本（或者yaml文件）
     * @return 将注释删除后的shell脚本（或者yaml文件）
     */
    public static String deleteCommentedLines(String fileContent) {
        String[] lines = splitLinesToArray(fileContent);
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            if (line != null && !line.trim().startsWith(COMMENT_STRING)) {
                stringBuilder.append(line).append(NEW_LINE);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 将字符串按行分割为字符串数组
     *
     * @param string 字符串
     * @return 数组
     */
    public static String[] splitLinesToArray(String string) {
        return string.split(NEWLINE_REGEX);
    }

    /**
     * 将字符串按行分割为字符串列表
     *
     * @param string 字符串
     * @return 列表
     */
    public static List<String> splitLinesToList(String string) {
        return Arrays.asList(splitLinesToArray(string));
    }
}
