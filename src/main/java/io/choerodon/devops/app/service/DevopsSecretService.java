package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.SecretReqVO;
import io.choerodon.devops.api.vo.SecretRespVO;
import io.choerodon.devops.infra.dto.DevopsSecretDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午9:45
 * Description:
 */
public interface DevopsSecretService {

    /**
     * 创建或更新密钥
     *
     * @param secretReqVO 请求体
     * @return SecretRespVO
     */
    SecretRespVO createOrUpdate(Long projectId, SecretReqVO secretReqVO);

    /**
     * 删除密钥
     *
     * @param envId    环境id
     * @param secretId 密钥id
     * @return Boolean
     */
    Boolean deleteSecret(Long projectId, Long envId, Long secretId);

    /**
     * 删除密钥,GitOps
     *
     * @param secretId 密钥Id
     */
    void deleteSecretByGitOps(Long secretId);

    /**
     * 创建密钥,GitOps
     *
     * @param secretReqVO 密钥参数
     */
    void addSecretByGitOps(SecretReqVO secretReqVO, Long userId);

    /**
     * 更新密钥,GitOps
     *
     * @param projectId   项目id
     * @param id          网络Id
     * @param secretReqVO 请求体
     */
    void updateDevopsSecretByGitOps(Long projectId, Long id, SecretReqVO secretReqVO, Long userId);

    /**
     * 分页查询secret
     *
     * @param envId        环境id
     * @param pageable     分页参数
     * @param params       查询参数
     * @param appServiceId 服务id
     * @param toDecode     是否解码值
     * @return Page
     */
    Page<SecretRespVO> pageByOption(Long envId, PageRequest pageable, String params, Long appServiceId, boolean toDecode);

    /**
     * 根据密钥id查询密钥
     *
     * @param secretId 密钥id
     * @param toDecode 是否解码值
     * @return SecretRespVO
     */
    SecretRespVO querySecret(Long secretId, boolean toDecode);

    /**
     * 校验名字合法性
     *
     * @param envId 环境id
     * @param name  密钥名
     * @return true表示通过
     */
    boolean checkName(Long envId, String name);

    /**
     * 判断名字唯一性
     *
     * @param envId 环境id
     * @param name  密钥名
     * @return true表示通过
     */
    boolean isNameUnique(Long envId, String name);

    SecretReqVO dtoToReqVo(DevopsSecretDTO devopsSecretDTO);

    SecretRespVO dtoToRespVo(DevopsSecretDTO devopsSecretDTO);

    DevopsSecretDTO baseQuery(Long secretId);

    DevopsSecretDTO baseCreate(DevopsSecretDTO devopsSecretDTO);

    void baseUpdate(DevopsSecretDTO devopsSecretDTO);

    void baseDelete(Long secretId);

    void baseDeleteSecretByEnvId(Long envId);

    void baseCheckName(String name, Long envId);

    DevopsSecretDTO baseQueryByEnvIdAndName(Long envId, String name);

    Page<DevopsSecretDTO> basePageByOption(Long envId, PageRequest pageable, String params, Long appServiceId);

    List<DevopsSecretDTO> baseListByEnv(Long envId);
}
