package io.choerodon.devops.api.validator;

import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsIngressPathVO;
import io.choerodon.devops.api.vo.DevopsIngressVO;
import io.choerodon.devops.infra.exception.GitOpsExplainException;

/**
 * Created by Zenger on 2018/4/26.
 */
public class DevopsIngressValidator {

    //ingress name
    private static final String NAME_PATTERN = "[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*";
    // ingress subdomain
    private static final String SUB_PATH_PATTERN = "^/(\\S)*$";
    /**
     * Host的正则
     */
    private static final Pattern HOST_PATTERN = Pattern.compile("^(\\*\\.)?[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$");

    /**
     * 子域名正则, Annotation的Key的一部分，可参考(https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/#syntax-and-character-set)
     */
    private static final Pattern SUB_DOMAIN_PATTERN = Pattern.compile("^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$");

    /**
     * Annotation的name正则, Annotation的Key的一部分，可参考(https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/#syntax-and-character-set)
     */
    private static final Pattern ANNOTATION_NAME_PATTERN = Pattern.compile("^([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9]$");

    private DevopsIngressValidator() {
    }

    /**
     * 参数校验
     */
    public static void checkIngressName(String name) {
        if (!Pattern.matches(NAME_PATTERN, name)) {
            throw new CommonException("error.ingress.name.notMatch");
        }
    }

    /**
     * 参数校验
     */
    public static void checkPath(String path) {
        if (!Pattern.matches(SUB_PATH_PATTERN, path)) {
            throw new CommonException("error.ingress.subPath.notMatch");
        }
    }

    public static void checkHost(String host) {
        if (StringUtils.isEmpty(host)
                || !HOST_PATTERN.matcher(host).matches()) {
            throw new CommonException("error.ingress.host.format");
        }
    }

    public static void checkVOForBatchDeployment(DevopsIngressVO devopsIngressVO) {
        checkIngressName(devopsIngressVO.getName());
        checkHost(devopsIngressVO.getDomain());
        if (devopsIngressVO.getEnvId() == null) {
            throw new CommonException("error.env.id.null");
        }
        if (CollectionUtils.isEmpty(devopsIngressVO.getPathList())) {
            throw new CommonException("error.ingress.path.empty");
        }
        checkAnnotations(devopsIngressVO.getAnnotations());
    }

    public static void checkPathVO(DevopsIngressPathVO path) {
        if (StringUtils.isEmpty(path.getServiceName())) {
            throw new CommonException("error.service.name.in.path.null");
        }
        checkPath(path.getPath());
        if (!checkPort(path.getServicePort())) {
            throw new CommonException("error.port.illegal");
        }
    }

    private static Boolean checkPort(Long port) {
        return port >= 0 && port <= 65535;
    }

    /**
     * 校验Annotation字段是否合法, 这里不校验Annotations所有键值对的总长度，由后续处理逻辑校验
     * 校验规则可参考(https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/#syntax-and-character-set)
     *
     * @param annotations 注解map，可为空
     */
    public static void checkAnnotations(@Nullable Map<String, String> annotations) {
        if (CollectionUtils.isEmpty(annotations)) {
            return;
        }

        annotations.forEach((key, value) -> {
            if (StringUtils.isEmpty(key)) {
                throw new CommonException("error.ingress.annotation.key.empty");
            }
            if (StringUtils.isEmpty(value)) {
                throw new CommonException("error.ingress.annotation.value.empty");
            }
            String[] parts = key.split("/");
            if (parts.length > 2) {
                throw new CommonException("error.ingress.annotation.key.too.many.slashes", key);
            } else if (parts.length == 2) {
                // 有两段的情况
                if (parts[0].length() > 253) {
                    throw new CommonException("error.ingress.annotation.key.sub.domain.part.too.long", key);
                }
                if (!SUB_DOMAIN_PATTERN.matcher(parts[0]).matches()) {
                    throw new CommonException("error.ingress.annotation.key.sub.domain.part.invalid", key);
                }
                if (parts[1].length() > 63) {
                    throw new CommonException("error.ingress.annotation.key.name.part.too.long", key);
                }
                if (!ANNOTATION_NAME_PATTERN.matcher(parts[1]).matches()) {
                    throw new CommonException("error.ingress.annotation.key.name.part.invalid", key);
                }
            } else {
                // 只有一段的情况
                if (key.length() > 63) {
                    throw new CommonException("error.ingress.annotation.key.name.part.too.long", key);
                }
                if (!ANNOTATION_NAME_PATTERN.matcher(key).matches()) {
                    throw new CommonException("error.ingress.annotation.key.name.part.invalid", key);
                }
            }
        });
    }


    /**
     * 校验Annotation字段是否合法, 这里不校验Annotations所有键值对的总长度，由后续处理逻辑校验
     * 校验规则可参考(https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/#syntax-and-character-set)
     *
     * @param annotations 注解map，可为空
     * @param filePath    文件路径
     */
    public static void checkAnnotationsForGitOps(@Nullable Map<String, String> annotations, String filePath) {
        if (CollectionUtils.isEmpty(annotations)) {
            return;
        }

        annotations.forEach((key, value) -> {
            if (StringUtils.isEmpty(key)) {
                throw new GitOpsExplainException("error.ingress.annotation.key.empty", filePath);
            }
            if (StringUtils.isEmpty(value)) {
                throw new GitOpsExplainException("error.ingress.annotation.value.empty", filePath);
            }
            String[] parts = key.split("/");
            if (parts.length > 2) {
                throw new GitOpsExplainException("error.ingress.annotation.key.too.many.slashes", filePath, new Object[]{key});
            } else if (parts.length == 2) {
                if (parts[0].length() > 253) {
                    throw new GitOpsExplainException("error.ingress.annotation.key.sub.domain.part.too.long", filePath, new Object[]{key});
                }
                if (!SUB_DOMAIN_PATTERN.matcher(parts[0]).matches()) {
                    throw new GitOpsExplainException("error.ingress.annotation.key.sub.domain.part.invalid", filePath, new Object[]{key});
                }
                if (parts[1].length() > 63) {
                    throw new GitOpsExplainException("error.ingress.annotation.key.name.part.too.long", filePath, new Object[]{key});
                }
                if (!ANNOTATION_NAME_PATTERN.matcher(parts[1]).matches()) {
                    throw new GitOpsExplainException("error.ingress.annotation.key.name.part.invalid", filePath, new Object[]{key});
                }
            }
        });
    }
}
