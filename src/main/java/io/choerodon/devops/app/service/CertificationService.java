package io.choerodon.devops.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
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
     * @param projectId 项目id
     * @param envId     环境id
     * @param name      证书名字
     * @param type      整数类型
     * @param domains   证书域名
     * @param key       证书key
     * @param cert      证书内容
     * @return
     */
    C7nCertification create(Long projectId,
                            Long envId,
                            String name,
                            String type,
                            List<String> domains,
                            MultipartFile key,
                            MultipartFile cert);

    void deleteById(Long certId);

    Page<CertificationDTO> getByEnvid(PageRequest pageRequest,
                                      Long envId,
                                      String params);

    List<CertificationDTO> getActiveByDomain(Long envId, String domain);
}
