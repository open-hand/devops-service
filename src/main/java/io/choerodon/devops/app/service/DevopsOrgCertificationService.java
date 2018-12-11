package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.OrgCertificationDTO;
import io.choerodon.devops.api.dto.ProjectDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsOrgCertificationService {

    void insert(Long organizationId, OrgCertificationDTO orgCertificationDTO);

    void update(Long certId, OrgCertificationDTO orgCertificationDTO);

    void checkName(Long organizationId, String name);

    List<ProjectDTO> listCertProjects(Long certId);

    void deleteCert(Long certId);

    Page<ProjectDTO> listProjects(Long organizationId, Long clusterId, PageRequest pageRequest,
                                  String[] params);

    Page<OrgCertificationDTO> pageCerts(Long organizationId, PageRequest pageRequest,
                                        String params);

    OrgCertificationDTO getCert(Long certId);

}
