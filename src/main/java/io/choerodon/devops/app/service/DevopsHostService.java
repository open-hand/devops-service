package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.host.DevopsDockerInstanceVO;
import io.choerodon.devops.api.vo.host.DevopsJavaInstanceVO;
import io.choerodon.devops.api.vo.host.ResourceUsageInfoVO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zmf
 * @since 2020/9/15
 */
public interface DevopsHostService {
    /**
     * 创建主机
     *
     * @param projectId                 项目id
     * @param devopsHostCreateRequestVO 主机相关数据
     * @return 创建后的主机
     */
    DevopsHostVO createHost(Long projectId, DevopsHostCreateRequestVO devopsHostCreateRequestVO);

    /**
     * 获得agent安装命令
     *
     * @param projectId     项目id
     * @param devopsHostDTO 主机配置dto
     * @return 安装命令
     */
    String getInstallString(Long projectId, DevopsHostDTO devopsHostDTO);

    /**
     * 获得agent升级命令
     *
     * @param projectId     项目id
     * @param devopsHostDTO 主机配置dto
     * @return agent升级命令
     */
    String getUpgradeString(Long projectId, DevopsHostDTO devopsHostDTO);

    /**
     * 更新主机
     *
     * @param projectId                 项目id
     * @param hostId                    主机id
     * @param devopsHostUpdateRequestVO 主机更新相关数据
     * @return 更新后的主机
     */
    void updateHost(Long projectId, Long hostId, DevopsHostUpdateRequestVO devopsHostUpdateRequestVO);

    /**
     * 查询主机
     *
     * @param projectId 项目id
     * @param hostId    主机id
     */
    DevopsHostVO queryHost(Long projectId, Long hostId);

    /**
     * 删除主机
     *
     * @param projectId 项目id
     * @param hostId    主机id
     */
    void deleteHost(Long projectId, Long hostId);

    /**
     * 测试主机连接情况
     *
     * @param projectId                  项目id
     * @param devopsHostConnectionTestVO 主机连接信息
     * @return 连接结果
     * @deprecated
     */
    DevopsHostConnectionTestResultVO testConnection(Long projectId, DevopsHostConnectionTestVO devopsHostConnectionTestVO);

    /**
     * 测试多个主机连接情况
     *
     * @param projectId 项目id
     * @param hostIds   主机ids
     * @return 所有连接失败的主机id
     * @deprecated
     */
    Set<Object> multiTestConnection(Long projectId, Set<Long> hostIds);

    /**
     * 通过id测试部署主机连接情况
     *
     * @param projectId 项目id
     * @param hostId    主机id
     * @return true表示连接成功
     * @deprecated
     */
    Boolean testConnectionByIdForDeployHost(Long projectId, Long hostId);

    /**
     * 校验名称
     *
     * @param projectId 项目id
     * @param name      主机名称
     * @return true表示
     */
    boolean isNameUnique(Long projectId, String name);

    /**
     * ip + sshPort 是否在项目下唯一
     *
     * @param projectId 项目id
     * @param ip        主机ip
     * @param sshPort   ssh端口
     * @return true表示唯一
     */
    boolean isSshIpPortUnique(Long projectId, String ip, Integer sshPort);

    /**
     * 校验主机id与实例id是否匹配
     *
     * @param hostId     主机id
     * @param instanceId 实例id
     * @return true表示匹配
     */
    boolean hostIdInstanceIdMatch(Long hostId, Long instanceId);

    boolean hostIdDockerInstanceMatch(Long hostId, Long instanceId);

    /**
     * 分页查询主机
     *
     * @param projectId       项目id
     * @param pageRequest     分页参数
     * @param withUpdaterInfo 是否需要更新者信息
     * @param searchParam     查询参数
     * @param hostStatus
     * @return 一页主机数据
     */
    Page<DevopsHostVO> pageByOptions(Long projectId, PageRequest pageRequest, boolean withUpdaterInfo, @Nullable String searchParam, @Nullable String hostStatus, @Nullable Boolean doPage);

    /**
     * 能否删除主机
     *
     * @param projectId 项目id
     * @param hostId    主机id
     * @return true表示能删除
     */
    boolean checkHostDelete(Long projectId, Long hostId);

    /**
     * 查询主机信息
     *
     * @param hostId 主机id
     * @return
     */
    DevopsHostDTO baseQuery(Long hostId);

    /**
     * 查询主机下所有java进程信息
     *
     * @param projectId
     * @param hostId
     * @return 集合中的object对象为JavaProcessInfoVO
     */
    List<DevopsJavaInstanceVO> listJavaProcessInfo(Long projectId, Long hostId);

    /**
     * 查询docker进程信息接口
     *
     * @param projectId
     * @param hostId
     * @return 集合中的object对象为DockerProcessInfoVO
     */
    List<DevopsDockerInstanceVO> listDockerProcessInfo(Long projectId, Long hostId);

    /**
     * 删除docker进程
     *
     * @param projectId
     * @param hostId
     * @param instanceId
     */
    void deleteDockerProcess(Long projectId, Long hostId, Long instanceId);

    /**
     * 停止docker进程
     *
     * @param projectId
     * @param hostId
     * @param instanceId
     */
    void stopDockerProcess(Long projectId, Long hostId, Long instanceId);

    /**
     * 重启docker进程
     *
     * @param projectId
     * @param hostId
     * @param instanceId
     */
    void restartDockerProcess(Long projectId, Long hostId, Long instanceId);

    /**
     * 启动docker进程
     *
     * @param projectId
     * @param hostId
     * @param instanceId
     */
    void startDockerProcess(Long projectId, Long hostId, Long instanceId);

    /**
     * 下载创建主机脚本
     *
     * @param projectId
     * @param hostId
     * @param token
     */
    String downloadCreateHostFile(Long projectId, Long hostId, String token);

    ResourceUsageInfoVO queryResourceUsageInfo(Long projectId, Long hostId);

    String queryShell(Long projectId, Long hostId, Boolean queryForAutoUpdate,String previousAgentVersion);

    String queryUninstallShell(Long projectId, Long hostId);

    /**
     * 主机连接
     *
     * @param projectId
     * @param hostId
     * @param devopsHostConnectionVO
     */
    void connectHost(Long projectId, Long hostId, DevopsHostConnectionVO devopsHostConnectionVO);

    /**
     * 主机连接状态查询
     *
     * @param projectId
     * @param hostId
     */
    Map<Object, Object> queryConnectHost(Long projectId, Long hostId);

    /**
     * 主机连接测试
     *
     * @param projectId
     * @param hostId
     * @param devopsHostConnectionVO
     */
    Map<String, String> testConnectHost(Long projectId, Long hostId, DevopsHostConnectionVO devopsHostConnectionVO);

    /**
     * 主机断开连接
     *
     * @return
     */
    String disconnectionHost();

    /**
     * 分页查询主机下有权限的用户
     *
     * @param projectId
     * @param pageable
     * @param params
     * @param hostId
     * @return
     */
    Page<DevopsHostUserPermissionVO> pageUserPermissionByHostId(Long projectId, PageRequest pageable, String params, Long hostId);

    /**
     * 删除主机下该用户的权限
     */
    DevopsHostUserPermissionDeleteResultVO deletePermissionOfUser(Long projectId, Long hostId, Long userId);

    /**
     * 分页查询项目下所有与该环境未分配权限的项目成员
     *
     * @param projectId
     * @param hostId
     * @param selectedIamUserId
     * @param pageable
     * @param params
     * @return
     */
    Page<DevopsUserVO> pageNonRelatedMembers(Long projectId, Long hostId, Long selectedIamUserId, PageRequest pageable, String params);

    /**
     * 查询项目下所有与该环境未分配权限的项目成员
     */
    List<IamUserDTO> listNonRelatedMembers(Long projectId, Long hostId, Long selectedIamUserId, String params);

    /**
     * 主机下为用户批量分配权限
     */
    void batchUpdateHostUserPermission(Long projectId, DevopsHostUserPermissionUpdateVO devopsHostUserPermissionUpdateVO);

    /**
     * 校验主机是否可用，必须同时满足下列条件才可用
     * 1. 主机存在
     * 2. 主机已连接
     * 3. 拥有主机权限
     *
     * @param hostId
     */
    DevopsHostDTO checkHostAvailable(Long hostId);

    List<DevopsHostDTO> listByIds(Set<Long> ids);

    void restartAgent(Long projectId, Long hostId);
}
