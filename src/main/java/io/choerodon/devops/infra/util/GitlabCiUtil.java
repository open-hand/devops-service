package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.GitOpsConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;
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
        String[] lines = simpleSplitLinesToArray(fileContent);
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
        String[] lines = simpleSplitLinesToArray(fileContent);
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            if (isUnCommented(line)) {
                stringBuilder.append(line).append(NEW_LINE);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 这一行是否未被注释
     *
     * @param line 行
     * @return true表示未被注释，反之，被注释了
     */
    private static boolean isUnCommented(String line) {
        return line != null && !line.trim().startsWith(COMMENT_STRING);
    }

    /**
     * 过滤行，过滤注释和空的
     *
     * @param lineList        行列表
     * @param filterCommented 是否过滤注释的
     * @param filterEmpty     是否过滤空的
     * @return 过滤后的列表
     */
    public static List<String> filterLines(List<String> lineList, boolean filterCommented, boolean filterEmpty) {
        return lineList.stream()
                .filter(l -> !filterCommented || GitlabCiUtil.isUnCommented(l))
                .filter(l -> !filterEmpty || (l != null && !StringUtils.isEmpty(l.trim())))
                .collect(Collectors.toList());
    }

    /**
     * 将字符串按行分割为字符串数组
     *
     * @param string 字符串
     * @return 数组
     */
    private static String[] simpleSplitLinesToArray(String string) {
        return string.split(NEWLINE_REGEX);
    }

    /**
     * 将字符串按行分割为字符串列表
     *
     * @param string 字符串
     * @return 列表
     */
    public static List<String> simpleSplitLinesToList(String string) {
        return Arrays.asList(simpleSplitLinesToArray(string));
    }

    /**
     * 将shell按行分割 (支持 \ 符号进行多行连接)
     *
     * @param shellContent shell脚本内容
     * @return 分割后的shell脚本内容
     */
    public static List<String> splitLinesForShell(String shellContent) {
        String[] lines = simpleSplitLinesToArray(shellContent);
        List<String> result = new ArrayList<>();
        StringBuilder multiLineShellCommand = new StringBuilder();
        // 这里之所以比 simpleSplitLinesToList 复杂是因为对多行命令的支持
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                String line = lines[i];
                String trimmedLine = line.trim();
                // 以 \ 结尾且不以 \\ 结尾
                if (trimmedLine.endsWith("\\") && !trimmedLine.endsWith("\\\\")) {
                    multiLineShellCommand.append(line).append(NEW_LINE);
                } else {
                    if (multiLineShellCommand.length() > 0) {
                        multiLineShellCommand.append(line);
                        result.add(multiLineShellCommand.toString());
                        // 清空
                        multiLineShellCommand.delete(0, multiLineShellCommand.length());
                    } else {
                        result.add(line);
                    }
                }
            } else {
                result.add(lines[i]);
            }
        }
        return result;
    }

    /**
     * 获取用于CI的sonar的命令
     *
     * @param sonarUrl sonar服务地址
     * @param token    认证的token
     * @return sonar命令
     */
    public static String renderSonarCommand(String sonarUrl, String token) {
        return SONAR_TOKEN_TEMPLATE.replace(SONAR_URL_ANCHOR, sonarUrl).replace(SONAR_TOKEN_ANCHOR, token);
    }

    /**
     * 获取用于CI的sonar的命令
     *
     * @param sonarUrl      sonar服务地址
     * @param sonarUsername sonar用户名
     * @param sonarPassword sonar用户密码
     * @return sonar命令
     */
    public static String renderSonarCommand(String sonarUrl, String sonarUsername, String sonarPassword) {
        return SONAR_USER_PASSWORD_TEMPLATE
                .replace(SONAR_URL_ANCHOR, sonarUrl)
                .replace(SONAR_TOKEN_ANCHOR, sonarUsername)
                .replace(SONAR_PASSWORD_ANCHOR, sonarPassword);
    }
}
