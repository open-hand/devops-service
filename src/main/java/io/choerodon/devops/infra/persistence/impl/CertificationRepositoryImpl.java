package io.choerodon.devops.infra.persistence.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.devops.domain.application.entity.CertificationE;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.CertificationDO;
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;

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

    private Gson gson = new Gson();

    @Autowired
    private EnvListener envListener;
    @Autowired
    private EnvUtil envUtil;

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
    public Page<CertificationDTO> page(Long projectId, PageRequest pageRequest, String params) {
        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());
        if (pageRequest.getSort() != null) {
            Map<String, String> map = new HashMap<>();
            map.put("envName", "de.name");
            map.put("envCode", "de.code");
            map.put("certName", "dc.`name`");
            map.put("commonName", "dc.domains");
            pageRequest.resetOrder("dc", map);
        }
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String param = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        Page<CertificationDTO> certificationDTOPage = ConvertPageHelper.convertPage(
                PageHelper.doPageAndSort(pageRequest,
                        () -> devopsCertificationMapper.selectCertification(projectId, searchParamMap, param)),
                CertificationDTO.class);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        certificationDTOPage.getContent().parallelStream()
                .forEach(certificationDTO ->
                        certificationDTO.setEnvConnected(
                                connectedEnvList.contains(certificationDTO.getEnvId())
                                        && updatedEnvList.contains(certificationDTO.getEnvId())));
        return certificationDTOPage;
    }

    @Override
    public List<CertificationDTO> getActiveByDomain(Long envId, String domain) {
        return ConvertHelper.convertList(devopsCertificationMapper.getActiveByDomain(envId, domain),
                CertificationDTO.class);
    }

    @Override
    public void updateStatus(CertificationE certificationE) {
        CertificationDO certificationDO = devopsCertificationMapper.selectByPrimaryKey(certificationE.getId());
        certificationDO.setStatus(certificationE.getStatus());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDO);
    }

    @Override
    public void deleteById(Long id) {
        devopsCertificationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Boolean checkCertNameUniqueInEnv(Long envId, String certName) {
        return devopsCertificationMapper.select(new CertificationDO(certName, envId)).isEmpty();
    }
}
