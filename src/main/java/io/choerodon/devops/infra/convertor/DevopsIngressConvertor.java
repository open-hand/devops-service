package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsIngressDTO;
import io.choerodon.devops.api.vo.iam.entity.CertificationE;
import io.choerodon.devops.api.vo.iam.entity.DevopsIngressE;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.infra.dto.DevopsIngressDO;

/**
 * Created by younger on 2018/4/28.
 */
@Component
public class DevopsIngressConvertor implements ConvertorI<DevopsIngressE, DevopsIngressDO, DevopsIngressDTO> {

    @Autowired
    private CertificationRepository certificationRepository;

    @Override
    public DevopsIngressE doToEntity(DevopsIngressDO dataObject) {
        DevopsIngressE devopsIngressE = new DevopsIngressE();
        BeanUtils.copyProperties(dataObject, devopsIngressE);
        Long certId = dataObject.getCertId();
        if (certId != null) {
            CertificationE certificationE = certificationRepository.queryById(certId);
            if (certificationE != null) {
                devopsIngressE.setCertName(certificationE.getName());
                devopsIngressE.setCertStatus(certificationE.getStatus());
            } else {
                devopsIngressE.setCertId(null);
            }
        }
        return devopsIngressE;
    }

    @Override
    public DevopsIngressDO entityToDo(DevopsIngressE entity) {
        DevopsIngressDO devopsIngressDO = new DevopsIngressDO();
        BeanUtils.copyProperties(entity, devopsIngressDO);
        return devopsIngressDO;
    }
}
