package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.ClusterDetailResourceVO;
import io.choerodon.devops.api.vo.GeneralResourceVO;
import io.choerodon.devops.api.vo.HostDetailResourceVO;
import java.util.List;

/**
 * 用户资源查询接口
 *
 * @author xingxingwu.hand-china.com 2021/07/13 16:55
 */
public interface UserResourceService {

    /**
     * 查询上下文用户在指定组织下的资源概览
     *
     * @param organizationId 组织ID
     * @return 资源概览
     */
    GeneralResourceVO queryGeneral(Long organizationId);

    /**
     * 查询上下文用户在指定组织下的主机资源
     *
     * @param organizationId 组织ID
     * @return 主机资源列表
     */
    List<HostDetailResourceVO> queryHostResource(Long organizationId);

    /**
     * 查询上下文用户在指定组织下的集群资源
     *
     * @param organizationId 组织ID
     * @return 主机资源列表
     */
    List<ClusterDetailResourceVO> queryClusterResource(Long organizationId);
}
