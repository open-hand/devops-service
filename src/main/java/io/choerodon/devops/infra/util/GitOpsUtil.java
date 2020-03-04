package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.GitOpsConstants.*;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.C7NHelmReleaseMetadataType;
import io.choerodon.devops.infra.enums.EnvironmentType;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;

/**
 * GitOps解析相关的工具类
 *
 * @author zmf
 * @since 11/4/19
 */
public class GitOpsUtil {
    private GitOpsUtil() {
    }

    /**
     * 获取系统环境的code
     *
     * @param clusterCode 集群code
     * @return 系统环境所对应的GitLab项目code
     */
    public static String getSystemEnvCode(String clusterCode) {
        return String.format(GitOpsConstants.SYSTEM_ENV_GITLAB_PROJECT_CODE_FORMAT, clusterCode, GitOpsConstants.SYSTEM_NAMESPACE);
    }

    /**
     * 获取环境对应的kubernetes的namespace
     *
     * @param envCode 环境的code
     * @param envType 环境的type
     * @return namespace
     */
    public static String getEnvNamespace(String envCode, String envType) {
        return EnvironmentType.SYSTEM.getValue().equals(envType) ? GitOpsConstants.SYSTEM_NAMESPACE : envCode;
    }

    /**
     * 获取存储本地环境库的路径
     * 加入envId是为了防止环境删除后又创建同名的环境导致的脏数据
     *
     * @param orgCode     组织code
     * @param projectCode 项目code
     * @param clusterCode 集群code
     * @param envCode     环境code
     * @param envId       环境id
     * @return 存储环境库的本地路径
     */
    public static String getLocalPathToStoreEnv(
            String orgCode, String projectCode, String clusterCode, String envCode, Long envId) {
        //本地路径
        return String.format(LOCAL_ENV_PATH,
                Objects.requireNonNull(orgCode),
                Objects.requireNonNull(projectCode),
                Objects.requireNonNull(clusterCode),
                Objects.requireNonNull(envCode),
                Objects.requireNonNull(envId));
    }

    /**
     * 通过环境类型获取组后缀
     *
     * @param type 环境类型
     * @return 组后缀
     */
    public static String getGroupSuffixByEnvType(EnvironmentType type) {
        if (EnvironmentType.SYSTEM == type) {
            return CLUSTER_ENV_GROUP_SUFFIX;
        } else if (EnvironmentType.USER == type) {
            return ENV_GROUP_SUFFIX;
        } else {
            throw new CommonException("error.environment.type.not.supported", type);
        }
    }

    /**
     * 通过组织name和项目name获取gitlab项目组的name
     *
     * @param orgName     组织name
     * @param projectName 项目name
     * @param groupSuffix 组后缀,参考 {@link GitOpsConstants}
     * @return group name
     */
    public static String renderGroupName(String orgName, String projectName, String groupSuffix) {
        // name: orgName-projectName + suffix
        return String.format(GITLAB_GROUP_NAME_FORMAT, orgName, projectName, groupSuffix);
    }

    /**
     * 通过组织code和项目code获取gitlab项目组的path
     *
     * @param orgCode     组织code
     * @param projectCode 项目code
     * @param groupSuffix 组后缀,参考 {@link GitOpsConstants}
     * @return path
     */
    public static String renderGroupPath(String orgCode, String projectCode, String groupSuffix) {
        // path: orgName-projectCode + suffix
        return String.format(GITLAB_GROUP_NAME_FORMAT, orgCode, projectCode, groupSuffix);
    }

    /**
     * 实例是否是集群组件的实例
     *
     * @param envType        环境类型
     * @param c7nHelmRelease release数据
     * @return true则是，反之，不是
     */
    public static boolean isClusterComponent(String envType, C7nHelmRelease c7nHelmRelease) {
        return EnvironmentType.SYSTEM.getValue().equals(envType) && C7NHelmReleaseMetadataType.CLUSTER_COMPONENT.getType().equals(c7nHelmRelease.getMetadata().getType());
    }

    /**
     * 根据资源名称对资源进行分拣处理，从所有涉及的资源分拣出哪些是新增的，更新的和删除的
     *
     * @param beforeResourceNames 此处操作前数据库的所有资源名称，分类之后这个列表中存放的是待删除的资源
     * @param all                 所有涉及的资源
     * @param add                 放置新增的资源的容器，分类之后将需要更新的资源放入此处，建议传入时为空
     * @param update              放置更新的资源的容器，分类之后将需要更新的资源放入此处，建议传入时为空
     * @param getName             获取资源的名称的逻辑
     */
    public static <T> void pickCUDResource(List<String> beforeResourceNames,
                                           List<T> all,
                                           List<T> add,
                                           List<T> update,
                                           Function<T, String> getName) {
        for (T obj : all) {
            if (beforeResourceNames.contains(getName.apply(obj))) {
                update.add(obj);
                beforeResourceNames.remove(getName.apply(obj));
            } else {
                add.add(obj);
            }
        }
    }

    /**
     * 是否被当前的操作删除了
     *
     * @param beforeSyncDelete 当前操作删除的文件及其对应关系
     * @param resourceId       待判断的资源id
     * @param resourceType     资源类型
     * @return true表示被删除了，反之，没有被删除
     */
    public static boolean isDeletedByCurrentOperation(List<DevopsEnvFileResourceDTO> beforeSyncDelete,
                                                      Long resourceId, ResourceType resourceType) {
        return beforeSyncDelete.stream().anyMatch(
                envFileResourceDTO -> envFileResourceDTO.getResourceType().equals(resourceType.getType())
                        && envFileResourceDTO.getResourceId().equals(resourceId));
    }

    /**
     * 通过对象的某一个key的比较，来确认list中是否包含某个对象
     *
     * @param beforeList        已有的list
     * @param toBeJudged        待判断的对象
     * @param propertyExtractor 提取对象的用于比较的数据的提取器
     * @param <T>               泛型参数
     * @return true表示包含
     */
    public static <T> boolean isContainedByList(
            List<T> beforeList, T toBeJudged, Function<T, Object> propertyExtractor) {
        return beforeList.stream().anyMatch(before -> identifyByProperty(before, toBeJudged, propertyExtractor));
    }

    /**
     * 通过对象的某一个key比较对象是否相等
     *
     * @param one               一个对象
     * @param another           另一个对象
     * @param propertyExtractor 提取对象的用于比较的数据的提取器
     * @param <T>               泛型参数
     * @return true表示相等
     */
    public static <T> boolean identifyByProperty(T one, T another, Function<T, Object> propertyExtractor) {
        return Objects.equals(propertyExtractor.apply(one), propertyExtractor.apply(another));
    }

    /**
     * 校验资源未在数据库中定义，已定义则抛异常
     *
     * @param devopsEnvFileResourceDTO 资源所对应的数据纪录
     * @param filePath                 文件路径
     * @param resourceName             资源名称
     */
    public static void checkNotExistInDb(@Nullable DevopsEnvFileResourceDTO devopsEnvFileResourceDTO,
                                         String filePath, String resourceName) {
        if (devopsEnvFileResourceDTO != null &&
                !devopsEnvFileResourceDTO.getFilePath().equals(filePath)) {
            throwExistEx(filePath, resourceName);
        }
    }

    /**
     * 抛出资源对象存在的异常
     *
     * @param filePath     资源所在文件路径
     * @param resourceName 资源名称
     */
    public static void throwExistEx(String filePath, String resourceName) {
        throw new GitOpsExplainException(
                GitOpsObjectError.OBJECT_EXIST.getError(), filePath, resourceName);
    }

    /**
     * 是否需要重试GitOps流程
     * 当环境总览第一阶段为空，第一阶段的commit不是最新commit, 第一阶段和第二阶段commit不一致时，可以重新触发gitOps
     *
     * @param sagaSyncCommitId      环境的saga_sync_commit id
     * @param sagaSyncCommitSha     环境的saga_sync_commit对应的sha值
     * @param devopsSyncCommitId    环境的devops_sync_commit id
     * @param remoteLatestCommitSha 环境远程的GitLab的最新提交的sha值
     * @return true说明需要重试, false反之
     */
    public static boolean isToRetryGitOps(Long sagaSyncCommitId,
                                          String sagaSyncCommitSha,
                                          Long devopsSyncCommitId,
                                          String remoteLatestCommitSha) {
        return sagaSyncCommitId == null
                || !Objects.equals(sagaSyncCommitId, devopsSyncCommitId)
                || !Objects.equals(sagaSyncCommitSha, remoteLatestCommitSha);
    }

    /**
     * 返回用户的语言类型
     *
     * @return Locale
     */
    public static Locale locale() {
        CustomUserDetails details = DetailsHelper.getUserDetails();
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (details != null && "en_US".equals(details.getLanguage())) {
            locale = Locale.US;
        }
        return locale;
    }
}
