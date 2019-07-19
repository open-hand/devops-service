package io.choerodon.devops.app.service;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.SecretRepDTO;
import io.choerodon.devops.api.vo.SecretReqDTO;
import io.choerodon.devops.infra.dto.DevopsSecretDTO;

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
     * @param secretReqDTO 请求体
     * @return SecretRepDTO
     */
    SecretRepDTO createOrUpdate(SecretReqDTO secretReqDTO);

    /**
     * 删除密钥
     *
     * @param envId    环境id
     * @param secretId 密钥id
     * @return Boolean
     */
    Boolean deleteSecret(Long envId, Long secretId);

    /**
     * 删除密钥,GitOps
     *
     * @param secretId 密钥Id
     */
    void deleteSecretByGitOps(Long secretId);

    /**
     * 创建密钥,GitOps
     *
     * @param secretReqDTO 密钥参数
     */
    void addSecretByGitOps(SecretReqDTO secretReqDTO, Long userId);

    /**
     * 更新密钥,GitOps
     *
     * @param projectId    项目id
     * @param id           网络Id
     * @param secretReqDTO 请求体
     */
    void updateDevopsSecretByGitOps(Long projectId, Long id, SecretReqDTO secretReqDTO, Long userId);

    /**
     * 分页查询secret
     *
     * @param envId       环境id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return Page
     */
    PageInfo<SecretRepDTO> listByOption(Long envId, PageRequest pageRequest, String params, Long appId);

    /**
     * 根据密钥id查询密钥
     *
     * @param secretId 密钥id
     * @return SecretRepDTO
     */
    SecretRepDTO querySecret(Long secretId);

    /**
     * 校验名字唯一性
     *
     * @param envId 环境id
     * @param name  密钥名
     */
    void checkName(Long envId, String name);


    DevopsSecretDTO baseQuery(Long secretId);


}
