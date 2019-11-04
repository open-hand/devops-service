package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1Endpoints;

import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;


/**
 * 处理资源对象和文件对应关系
 *
 * @param <T>
 */
public interface HandlerObjectFileRelationsService<T> {

    /**
     * 处理目标对象和文件的关系以及其自身的数据纪录
     *
     * @param objectPath  对象的hashcode和路径的映射
     * @param beforeSync  之前解析的资源文件关系
     * @param ts          对象列表
     * @param v1Endpoints Endpoints对象列表
     * @param envId       环境id
     * @param projectId   项目id
     * @param path        环境库的本地目录
     * @param userId      用户id
     */
    void handlerRelations(Map<String, String> objectPath,
                          List<DevopsEnvFileResourceDTO> beforeSync,
                          List<T> ts,
                          List<V1Endpoints> v1Endpoints,
                          Long envId, Long projectId, String path, Long userId);

    /**
     * 获取目标对象的类型
     *
     * @return 目标对象的类型
     */
    Class<T> getTarget();
}
