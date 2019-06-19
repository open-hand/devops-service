package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.dto.OrgCertificationDTO;
import io.choerodon.devops.api.dto.ProjectDTO;
import org.springframework.web.multipart.MultipartFile;

public interface DevopsOrgCertificationService {

    void insert(Long organizationId, MultipartFile key, MultipartFile cert, OrgCertificationDTO orgCertificationDTO);

    void update(Long certId, OrgCertificationDTO orgCertificationDTO);

    void checkName(Long organizationId, String name);

    List<ProjectDTO> listCertProjects(Long certId);

    void deleteCert(Long certId);

    PageInfo<ProjectDTO> listProjects(Long organizationId, Long clusterId, PageRequest pageRequest,
                                      String[] params);

    PageInfo<OrgCertificationDTO> pageCerts(Long organizationId, PageRequest pageRequest,
                                        String params);

    OrgCertificationDTO getCert(Long certId);

}
