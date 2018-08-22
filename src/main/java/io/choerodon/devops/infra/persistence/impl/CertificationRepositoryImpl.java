package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
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
    public void create(CertificationE certificationE) {
        devopsCertificationMapper.insert(ConvertHelper.convert(certificationE, CertificationDO.class));
    }

    @Override
    public void deleteById(Long certId) {
        devopsCertificationMapper.deleteByPrimaryKey(certId);
    }

    @Override
    public CertificationE queryByEnvAndName(Long envId,
                                            String name) {
        CertificationDO certificationDO = new CertificationDO();
        certificationDO.setEnvId(envId);
        certificationDO.setName(name);
        return ConvertHelper.convert(devopsCertificationMapper.selectOne(certificationDO), CertificationE.class);
    }

    @Override
    public Page<CertificationDTO> getCertification(
            Long envId,
            PageRequest pageRequest,
            String params) {
        List<CertificationDTO> certificationDTOS = new ArrayList<>();
        Map<String, Object> maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        // domains name
        Page<CertificationDO> certificationDOPage = PageHelper.doPageAndSort(pageRequest,
                () -> devopsCertificationMapper.selectCertification(envId));
// TODO
        Page<CertificationDTO> certificationDTOPage = ConvertPageHelper.convertPage(certificationDOPage, CertificationDTO.class);
        List<Long> connectedEnvList = envUtil.getConnectedEnvList(envListener);
        List<Long> updatedEnvList = envUtil.getUpdatedEnvList(envListener);
        certificationDTOPage.parallelStream().forEach(certificationDTO -> {
            if (connectedEnvList.contains(certificationDTO.getEnvId()) &&
                    updatedEnvList.contains(certificationDTO.getEnvId())) {
                certificationDTO.setEnvConnected(true);
            }
        });
        return certificationDTOPage;
    }

    @Override
    public List<CertificationDTO> getActiveByDomain(Long envId, String domain) {
        return ConvertHelper.convertList(devopsCertificationMapper.getActiveByDomain(envId, domain),
                CertificationDTO.class);
    }
}
