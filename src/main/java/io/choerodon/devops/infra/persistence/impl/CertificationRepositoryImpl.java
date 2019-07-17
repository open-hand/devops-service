package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.devops.api.vo.CertificationVO;
import io.choerodon.devops.api.vo.iam.entity.CertificationE;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
<<<<<<< HEAD
<<<<<<< HEAD
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.CertificationStatus;
=======
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CertificationStatus;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.infra.dataobject.CertificationDO;
import io.choerodon.devops.infra.dataobject.CertificationFileDO;
=======

import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.CertificationStatus;

import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.CertificationFileDTO;

>>>>>>> [IMP]修复后端结构
import io.choerodon.devops.infra.mapper.DevopsCertificationFileMapper;
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 19:28
 * Description:
 */
@Service
public class CertificationRepositoryImpl implements CertificationRepository {

    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsCertificationFileMapper devopsCertificationFileMapper;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;

    private Gson gson = new Gson();

    @Override
    public CertificationE baseQueryByEnvAndName(Long envId, String name) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setEnvId(envId);
        certificationDTO.setName(name);
        return ConvertHelper.convert(devopsCertificationMapper.selectOne(certificationDTO), CertificationE.class);
    }

    @Override
    public CertificationE baseCreate(CertificationE certificationE) {
        CertificationDTO certificationDTO = ConvertHelper.convert(certificationE, CertificationDTO.class);
        devopsCertificationMapper.insert(certificationDTO);
        certificationE.setId(certificationDTO.getId());
        return certificationE;
    }

    @Override
    public CertificationE baseQueryById(Long certId) {
        return ConvertHelper.convert(devopsCertificationMapper.selectByPrimaryKey(certId), CertificationE.class);
    }

    @Override
    public PageInfo<CertificationVO> basePage(Long projectId, Long organizationId, Long envId, PageRequest pageRequest, String params) {
        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());

        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("envName")) {
                            property = "de.name";
                        } else if (property.equals("envCode")) {
                            property = "de.code";
                        } else if (property.equals("certName")) {
                            property = "dc.`name`";
                        }else if (property.equals("commonName")) {
                            property = "dc.domains";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String param = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        PageInfo<CertificationVO> certificationDTOPage = ConvertPageHelper.convertPageInfo(
                PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), sortResult).doSelectPageInfo(() -> devopsCertificationMapper
                        .selectCertification(projectId, organizationId, envId, searchParamMap, param)),
                CertificationVO.class);

        // check if cert is overdue
        certificationDTOPage.getList().forEach(dto -> {
            if (CertificationStatus.ACTIVE.getStatus().equals(dto.getStatus())) {
                CertificationE certificationE = ConvertHelper.convert(dto, CertificationE.class);
                if (!certificationE.checkValidity()) {
                    dto.setStatus(CertificationStatus.OVERDUE.getStatus());
                    CertificationDTO certificationDTO = new CertificationDTO();
                    certificationDTO.setId(dto.getId());
                    certificationDTO.setStatus(CertificationStatus.OVERDUE.getStatus());
                    devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
                }
            }
        });

        return certificationDTOPage;
    }

    @Override
    public List<CertificationVO> baseGetActiveByDomain(Long projectId, Long clusterId, String domain) {
        return ConvertHelper.convertList(devopsCertificationMapper.getActiveByDomain(projectId, clusterId, domain),
                CertificationVO.class);
    }

    @Override
    public void baseUpdateStatus(CertificationE certificationE) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certificationE.getId());
        certificationDTO.setStatus(certificationE.getStatus());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    @Override
    public void baseUpdateCommandId(CertificationE certificationE) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certificationE.getId());
        certificationDTO.setCommandId(certificationE.getCommandId());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    @Override
    public void baseUpdateValidField(CertificationE certificationE) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certificationE.getId());
        if (certificationE.checkValidity()) {
            certificationDTO.setStatus(CertificationStatus.ACTIVE.getStatus());
        } else {
            certificationDTO.setStatus(CertificationStatus.OVERDUE.getStatus());
        }
        certificationDTO.setValid(certificationE.getValidFrom(), certificationE.getValidUntil());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    @Override
    public void baseUpdateCertFileId(CertificationE certificationE) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certificationE.getId());
        certificationDTO.setCertificationFileId(certificationE.getCertificationFileId());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    @Override
    public void baseClearValidField(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
        if (certificationDTO != null
                && (certificationDTO.getValidFrom() != null || certificationDTO.getValidUntil() != null)) {
            certificationDTO.setValid(null, null);
            devopsCertificationMapper.updateByPrimaryKey(certificationDTO);
        }
    }

    @Override
    public void baseDeleteById(Long id) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(id);
        if (certificationDTO.getOrgCertId() == null) {
            deleteCertFile(id);
        }
        devopsCertificationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Boolean baseCheckCertNameUniqueInEnv(Long envId, String certName) {
        return devopsCertificationMapper.select(new CertificationDTO(certName, envId)).isEmpty();
    }

    @Override
    public Long baseStoreCertFile(CertificationFileDTO certificationFileDTO) {
        devopsCertificationFileMapper.insert(certificationFileDTO);
        return certificationFileDTO.getId();
    }

    @Override
    public CertificationFileDTO baseGetCertFile(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
        return devopsCertificationFileMapper.selectByPrimaryKey(certificationDTO.getCertificationFileId());
    }

    @Override
    public List<CertificationE> baseListByEnvId(Long envId) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setEnvId(envId);
        return ConvertHelper.convertList(devopsCertificationMapper.select(certificationDTO), CertificationE.class);
    }

    @Override
    public void baseUpdateSkipProjectPermission(CertificationE certificationE) {
        devopsCertificationMapper.updateSkipCheckPro(certificationE.getId(), certificationE.getSkipCheckProjectPermission());
    }

    @Override
    public CertificationE baseQueryByOrgAndName(Long orgId, String name) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setName(name);
        certificationDTO.setOrganizationId(orgId);
        return ConvertHelper.convert(devopsCertificationMapper.selectOne(certificationDTO), CertificationE.class);
    }

    @Override
    public List<CertificationE> baseListByOrgCertId(Long orgCertId) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setOrgCertId(orgCertId);
        return ConvertHelper.convertList(devopsCertificationMapper.select(certificationDTO), CertificationE.class);
    }

    @Override
    public List<CertificationVO> baseListByProject(Long projectId, Long organizationId) {
        return ConvertHelper.convertList(devopsCertificationMapper.listByProjectId(projectId, organizationId), CertificationVO.class);
    }

    private void deleteCertFile(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
        if (devopsCertificationFileMapper.selectByPrimaryKey(certificationDTO.getCertificationFileId()) != null) {
            devopsCertificationFileMapper.deleteByPrimaryKey(certificationDTO.getCertificationFileId());
        }
    }
}
