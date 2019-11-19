package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.gitops.YamlConvertToResourceHandler;

/**
 * 将资源对象从文本形式转为对象形式并对其进行校验
 *
 * @param <T> 对象类型
 */
public abstract class ConvertK8sObjectService<T> {
    private final Class<T> targetClass;

    public ConvertK8sObjectService(Class<T> targetClass) {
        this.targetClass = Objects.requireNonNull(targetClass);
    }

    /**
     * 将JSON格式的资源转为Java对象，并添加对象的hashcode和文件路径的对应关系
     *
     * @param jsonString json
     * @param filePath   资源所在文件路径
     * @param objectPath 用于存放对象hashcode和文件路径的对应关系
     * @param envId      环境id
     * @return 对象
     */
    public T serializableObject(String jsonString, String filePath, Map<String, String> objectPath, Long envId) {
        YamlConvertToResourceHandler<T> yamlConvertToResourceHandler
                = new YamlConvertToResourceHandler<>(targetClass);
        return yamlConvertToResourceHandler
                .serializable(jsonString, filePath, objectPath);
    }

    /**
     * 校验资源的对象的参数
     *
     * @param t          资源对象
     * @param objectPath 用于存放对象hashcode和文件路径的对应关系
     */
    public void checkParameters(T t, Map<String, String> objectPath) {
    }

    /**
     * 校验资源是否已经有定义了的
     * 一般，先校验数据库中是否已经存在，然后校验之前解析的list中是否已存在
     *
     * @param ts               当前已解析的所有资源
     * @param envId            环境id
     * @param beforeSyncDelete 之前删除的文件的文件资源对应关系
     * @param objectPath       对象hashcode和对象文件的映射
     * @param t                待检验的资源
     */
    public void checkIfExist(List<T> ts, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, T t) {
    }

    /**
     * 获取这个处理类所对应的资源类型
     *
     * @return 资源类型, 如 C7NHelmRelease
     */
    public abstract ResourceType getType();
}
