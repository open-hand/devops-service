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
import io.choerodon.devops.api.vo.CertificationDTO;
import io.choerodon.devops.domain.application.entity.CertificationE;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
<<<<<<< HEAD
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.CertificationStatus;
=======
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CertificationStatus;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.infra.dataobject.CertificationDO;
import io.choerodon.devops.infra.dataobject.CertificationFileDO;
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
    public CertificationE queryByEnvAndName(Long envId, String name) {
        CertificationDO certificationDO = new CertificationDO();
        certificationDO.setEnvId(envId);
        certificationDO.setName(name);
        return ConvertHelper.convert(devopsCertificationMapper.selectOne(certificationDO), CertificationE.class);
    }

    @Override
    public CertificationE create(CertificationE certificationE) {
        CertificationDO certificationDO = ConvertHelper.convert(certificationE, CertificationDO.class);
        devopsCertificationMapper.insert(certificationDO);
        certificationE.setId(certificationDO.getId());
        return certificationE;
    }

    @Override
    public CertificationE queryById(Long certId) {
        return ConvertHelper.convert(devopsCertificationMapper.selectByPrimaryKey(certId), CertificationE.class);
    }

    @Override
    public PageInfo<CertificationDTO> page(Long projectId, Long organizationId, Long envId, PageRequest pageRequest, String params) {
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
        PageInfo<CertificationDTO> certificationDTOPage = ConvertPageHelper.convertPageInfo(
                PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), sortResult).doSelectPageInfo(() -> devopsCertificationMapper
                        .selectCertification(projectId, organizationId, envId, searchParamMap, param)),
                CertificationDTO.class);

        // check if cert is overdue
        certificationDTOPage.getList().forEach(dto -> {
            if (CertificationStatus.ACTIVE.getStatus().equals(dto.getStatus())) {
                CertificationE certificationE = ConvertHelper.convert(dto, CertificationE.class);
                if (!certificationE.checkValidity()) {
                    dto.setStatus(CertificationStatus.OVERDUE.getStatus());
                    CertificationDO certificationDO = new CertificationDO();
                    certificationDO.setId(dto.getId());
                    certificationDO.setStatus(CertificationStatus.OVERDUE.getStatus());
                    devopsCertificationMapper.updateByPrimaryKeySelective(certificationDO);
                }
            }
        });

        return certificationDTOPage;
    }

    @Override
    public List<CertificationDTO> getActiveByDomain(Long projectId, Long clusterId, String domain) {
        return ConvertHelper.convertList(devopsCertificationMapper.getActiveByDomain(projectId, clusterId, domain),
                CertificationDTO.class);
    }

    @Override
    public void updateStatus(CertificationE certificationE) {
        CertificationDO certificationDO = devopsCertificationMapper.selectByPrimaryKey(certificationE.getId());
        certificationDO.setStatus(certificationE.getStatus());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDO);
    }

    @Override
    public void updateCommandId(CertificationE certificationE) {
        CertificationDO certificationDO = devopsCertificationMapper.selectByPrimaryKey(certificationE.getId());
        certificationDO.setCommandId(certificationE.getCommandId());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDO);
    }

    @Override
    public void updateValid(CertificationE certificationE) {
        CertificationDO certificationDO = devopsCertificationMapper.selectByPrimaryKey(certificationE.getId());
        if (certificationE.checkValidity()) {
            certificationDO.setStatus(CertificationStatus.ACTIVE.getStatus());
        } else {
            certificationDO.setStatus(CertificationStatus.OVERDUE.getStatus());
        }
        certificationDO.setValid(certificationE.getValidFrom(), certificationE.getValidUntil());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDO);
    }

    @Override
    public void updateCertFileId(CertificationE certificationE) {
        CertificationDO certificationDO = devopsCertificationMapper.selectByPrimaryKey(certificationE.getId());
        certificationDO.setCertificationFileId(certificationE.getCertificationFileId());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDO);
    }

    @Override
    public void clearValid(Long certId) {
        CertificationDO certificationDO = devopsCertificationMapper.selectByPrimaryKey(certId);
        if (certificationDO != null
                && (certificationDO.getValidFrom() != null || certificationDO.getValidUntil() != null)) {
            certificationDO.setValid(null, null);
            devopsCertificationMapper.updateByPrimaryKey(certificationDO);
        }
    }

    @Override
    public void deleteById(Long id) {
        CertificationDO certificationDO = devopsCertificationMapper.selectByPrimaryKey(id);
        if (certificationDO.getOrgCertId() == null) {
            deleteCertFile(id);
        }
        devopsCertificationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Boolean checkCertNameUniqueInEnv(Long envId, String certName) {
        return devopsCertificationMapper.select(new CertificationDO(certName, envId)).isEmpty();
    }

    @Override
    public Long storeCertFile(CertificationFileDO certificationFileDO) {
        devopsCertificationFileMapper.insert(certificationFileDO);
        return certificationFileDO.getId();
    }

    @Override
    public CertificationFileDO getCertFile(Long certId) {
        CertificationDO certificationDO = devopsCertificationMapper.selectByPrimaryKey(certId);
        return devopsCertificationFileMapper.selectByPrimaryKey(certificationDO.getCertificationFileId());
    }

    @Override
    public List<CertificationE> listByEnvId(Long envId) {
        CertificationDO certificationDO = new CertificationDO();
        certificationDO.setEnvId(envId);
        return ConvertHelper.convertList(devopsCertificationMapper.select(certificationDO), CertificationE.class);
    }

    @Override
    public void updateSkipProjectPermission(CertificationE certificationE) {
        devopsCertificationMapper.updateSkipCheckPro(certificationE.getId(), certificationE.getSkipCheckProjectPermission());
    }

    @Override
    public CertificationE queryByOrgAndName(Long orgId, String name) {
        CertificationDO certificationDO = new CertificationDO();
        certificationDO.setName(name);
        certificationDO.setOrganizationId(orgId);
        return ConvertHelper.convert(devopsCertificationMapper.selectOne(certificationDO), CertificationE.class);
    }

    @Override
    public List<CertificationE> listByOrgCertId(Long orgCertId) {
        CertificationDO certificationDO = new CertificationDO();
        certificationDO.setOrgCertId(orgCertId);
        return ConvertHelper.convertList(devopsCertificationMapper.select(certificationDO), CertificationE.class);
    }

    @Override
    public List<CertificationDTO> listByProject(Long projectId, Long organizationId) {
        return ConvertHelper.convertList(devopsCertificationMapper.listByProjectId(projectId, organizationId), CertificationDTO.class);
    }

    private void deleteCertFile(Long certId) {
        CertificationDO certificationDO = devopsCertificationMapper.selectByPrimaryKey(certId);
        if (devopsCertificationFileMapper.selectByPrimaryKey(certificationDO.getCertificationFileId()) != null) {
            devopsCertificationFileMapper.deleteByPrimaryKey(certificationDO.getCertificationFileId());
        }
    }
}
