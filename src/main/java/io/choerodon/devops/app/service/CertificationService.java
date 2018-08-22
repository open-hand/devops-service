package io.choerodon.devops.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.C7nCertificationDTO;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:43
 * Description:
 */
public interface CertificationService {

    /**
     * 创建c7n证书
     *
     * @param projectId        项目id
     * @param envId            环境id
     * @param certificationDTO 证书
     * @param key              证书key
     * @param cert             证书内容
     */
    void create(Long projectId, Long envId, C7nCertificationDTO certificationDTO,
                MultipartFile key, MultipartFile cert, Boolean isGitOps);

    void deleteById(Long certId, Boolean isGitOps);

    Page<CertificationDTO> pageByEnvId(PageRequest pageRequest, Long envId, String params);

    List<CertificationDTO> getActiveByDomain(Long envId, String domain);
}
