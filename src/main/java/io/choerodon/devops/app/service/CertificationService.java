package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.dto.C7nCertificationDTO;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.devops.api.dto.OrgCertificationDTO;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import org.springframework.web.multipart.MultipartFile;

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
     * @param certificationDTO 证书
     */
    void create(Long projectId, C7nCertificationDTO certificationDTO,
                MultipartFile key, MultipartFile cert, Boolean isGitOps);

    C7nCertification getC7nCertification(String name, String type, List<String> domains,
                                         String keyContent, String certContent, String envCode);

    void deleteById(Long certId);

    void certDeleteByGitOps(Long certId);

    PageInfo<CertificationDTO> page(Long projectId, Long envId, PageRequest pageRequest, String params);

    List<CertificationDTO> getActiveByDomain(Long projectId, Long envId, String domain);

    Boolean checkCertNameUniqueInEnv(Long envId, String certName);


    /**
     * 根据证书名称查询证书
     *
     * @param envId     环境ID
     * @param certName  证书名称
     * @return CertificationDTO
     */
    CertificationDTO  queryByName(Long envId , String certName);

    Long createCertCommandE(String type, Long certId, Long userId);

    List<OrgCertificationDTO> listByProject(Long projectId);
}
