package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.OrgCertificationVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import org.springframework.web.multipart.MultipartFile;

public interface DevopsOrgCertificationService {

    void create(Long organizationId, MultipartFile key, MultipartFile cert, OrgCertificationVO orgCertificationVO);

    void update(Long certId, OrgCertificationVO orgCertificationVO);

    void checkName(Long organizationId, String name);

    List<ProjectReqVO> listCertProjects(Long certId);

    void deleteCert(Long certId);

    PageInfo<ProjectReqVO> pageProjects(Long organizationId, Long clusterId, PageRequest pageRequest,
                                        String[] params);

    PageInfo<OrgCertificationVO> pageCerts(Long organizationId, PageRequest pageRequest,
                                           String params);

    OrgCertificationVO queryCert(Long certId);

}
