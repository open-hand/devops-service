package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.host.ResourceUsageInfoVO;
import io.choerodon.devops.infra.dto.DevopsDockerInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsHostDTO;
import io.choerodon.devops.infra.enums.DevopsHostStatus;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

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
    String createHost(Long projectId, DevopsHostCreateRequestVO devopsHostCreateRequestVO);

    /**
     * 获得agent安装命令
     *
     * @param projectId         项目id
     * @param devopsHostDTO     主机配置dto
     * @return 安装命令
     */
    String getInstallString(Long projectId, DevopsHostDTO devopsHostDTO);

    /**
     * 批量设置主机状态为处理中
     *
     * @param projectId 项目id
     * @param hostIds   主机id数据
     * @return 返回要校准的主机数据
     */
    Set<Long> batchSetStatusOperating(Long projectId, Set<Long> hostIds);

    /**
     * 异步批量校准主机状态
     *
     * @param projectId 项目id
     * @param hostIds   主机id
     * @param userId    当前用户id
     */
    void asyncBatchCorrectStatus(Long projectId, Set<Long> hostIds, Long userId);

    /**
     * 异步批量校准主机状态
     *
     * @param hostIds 主机id
     */
    String asyncBatchCorrectStatusWithProgress(Long projectId, Set<Long> hostIds);

    /**
     * 异步批量更新超时的主机为失败
     *
     * @param projectId 项目id
     * @param hostIds   主机id
     */
    void asyncBatchSetTimeoutHostFailed(Long projectId, Set<Long> hostIds);

    /**
     * 校正一个主机的状态
     *
     * @param projectId 项目id
     * @param hostId    主机id
     * @param updaterId 更新者的id
     */
    void correctStatus(Long projectId, Long hostId, Long updaterId);

    /**
     * 校正一个主机的状态
     *
     * @param projectId 项目id
     * @param hostId    主机id
     */
    void correctStatus(Long projectId, String correctKey, Long hostId);

    /**
     * 更新主机
     *
     * @param projectId                 项目id
     * @param hostId                    主机id
     * @param devopsHostUpdateRequestVO 主机更新相关数据
     * @return 更新后的主机
     */
    DevopsHostVO updateHost(Long projectId, Long hostId, DevopsHostUpdateRequestVO devopsHostUpdateRequestVO);

    /**
     * 查询主机
     * @deprecated
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
     * @deprecated
     * @param projectId                  项目id
     * @param devopsHostConnectionTestVO 主机连接信息
     * @return 连接结果
     */
    DevopsHostConnectionTestResultVO testConnection(Long projectId, DevopsHostConnectionTestVO devopsHostConnectionTestVO);

    /**
     * 测试多个主机连接情况
     * @deprecated
     *
     * @param projectId 项目id
     * @param hostIds   主机ids
     * @return 所有连接失败的主机id
     */
    Set<Object> multiTestConnection(Long projectId, Set<Long> hostIds);

    /**
     * 通过id测试部署主机连接情况
     * @deprecated
     * @param projectId 项目id
     * @param hostId    主机id
     * @return true表示连接成功
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
     * 分页查询主机
     *
     * @param projectId       项目id
     * @param pageRequest     分页参数
     * @param withUpdaterInfo 是否需要更新者信息
     * @param options         查询参数
     * @return 一页主机数据
     */
    Page<DevopsHostVO> pageByOptions(Long projectId, PageRequest pageRequest, boolean withUpdaterInfo, @Nullable String options);

    /**
     * 能否删除主机
     *
     * @param projectId 项目id
     * @param hostId    主机id
     * @return true表示能删除
     */
    boolean checkHostDelete(Long projectId, Long hostId);

    CheckingProgressVO getCheckingProgress(Long projectId, String correctKey);

    Page<DevopsHostVO> pagingWithCheckingStatus(Long projectId, PageRequest pageRequest, String correctKey, String searchParam);

    /**
     * 查询主机信息
     * @param hostId 主机id
     * @return
     */
    DevopsHostDTO baseQuery(Long hostId);


    /**
     * 更新主机状态
     * @param hostId
     * @param status
     */
    void baseUpdateHostStatus(Long hostId, DevopsHostStatus status);

    /**
     * 查询主机下所有java进程信息
     * @param projectId
     * @param hostId
     * @return 集合中的object对象为JavaProcessInfoVO
     */
    List<Object> listJavaProcessInfo(Long projectId, Long hostId);

    /**
     * 查询docker进程信息接口
     * @param projectId
     * @param hostId
     * @return 集合中的object对象为DockerProcessInfoVO
     */
    List<DevopsDockerInstanceDTO> listDockerProcessInfo(Long projectId, Long hostId);

    /**
     * 删除java进程
     * @param projectId
     * @param hostId
     * @param instanceId
     */
    void deleteJavaProcess(Long projectId, Long hostId, Long instanceId);

    /**
     * 删除docker进程
     * @param projectId
     * @param hostId
     * @param instanceId
     */
    void deleteDockerProcess(Long projectId, Long hostId, Long instanceId);

    /**
     * 停止docker进程
     * @param projectId
     * @param hostId
     * @param instanceId
     */
    void stopDockerProcess(Long projectId, Long hostId, Long instanceId);

    /**
     * 重启docker进程
     * @param projectId
     * @param hostId
     * @param instanceId
     */
    void restartDockerProcess(Long projectId, Long hostId, Long instanceId);

    /**
     * 启动docker进程
     * @param projectId
     * @param hostId
     * @param instanceId
     */
    void startDockerProcess(Long projectId, Long hostId, Long instanceId);

    /**
     * 下载创建主机脚本
     * @param projectId
     * @param hostId
     * @param token
     * @param res
     */
    String downloadCreateHostFile(Long projectId, Long hostId, String token, HttpServletResponse res);

    ResourceUsageInfoVO queryResourceUsageInfo(Long projectId, Long hostId);
}
