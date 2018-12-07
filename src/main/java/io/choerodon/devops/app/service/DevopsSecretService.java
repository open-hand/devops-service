package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.SecretRepDTO;
import io.choerodon.devops.api.dto.SecretReqDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
    Page<SecretRepDTO> listByOption(Long envId, PageRequest pageRequest, String params);

    /**
     * 校验名字唯一性
     *
     * @param envId 环境id
     * @param name  密钥名
     */
    void checkName(Long envId, String name);
}
