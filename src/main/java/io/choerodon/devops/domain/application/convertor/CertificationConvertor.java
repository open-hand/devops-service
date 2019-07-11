package io.choerodon.devops.domain.application.convertor;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.CertificationDTO;
import io.choerodon.devops.api.vo.iam.entity.CertificationE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.infra.dataobject.CertificationDO;

/**
 * Created by n!Ck
 * Date: 2018/8/21
 * Time: 10:31
 * Description:
 */
@Component
public class CertificationConvertor implements ConvertorI<CertificationE, CertificationDO, CertificationDTO> {

    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    private Gson gson = new Gson();

    @Override
    public CertificationE doToEntity(CertificationDO dataObject) {
        CertificationE certificationE = new CertificationE();
        BeanUtils.copyProperties(dataObject, certificationE);
        if (dataObject.getEnvId() != null) {
            certificationE.setEnvironmentE(new DevopsEnvironmentE(dataObject.getEnvId()));
        }
        certificationE.setDomains(gson.fromJson(dataObject.getDomains(), new TypeToken<List<String>>() {
        }.getType()));
        return certificationE;
    }

    @Override
    public CertificationE dtoToEntity(CertificationDTO certificationDTO) {
        CertificationE certificationE = new CertificationE();
        BeanUtils.copyProperties(certificationDTO, certificationE);
        certificationE.setEnvironmentE(new DevopsEnvironmentE(certificationDTO.getEnvId()));
        return certificationE;
    }

    @Override
    public CertificationDO entityToDo(CertificationE certificationE) {
        CertificationDO certificationDO = new CertificationDO();
        BeanUtils.copyProperties(certificationE, certificationDO);
        certificationDO.setDomains(gson.toJson(certificationE.getDomains()));
        if (certificationE.getEnvironmentE() != null) {
            certificationDO.setEnvId(certificationE.getEnvironmentE().getId());
        }
        return certificationDO;
    }



    @Override
    public CertificationDTO entityToDto(CertificationE certificationE) {
        CertificationDTO certificationDTO = new CertificationDTO();
        BeanUtils.copyProperties(certificationE, certificationDTO);
        return certificationDTO;
    }

    @Override
    public CertificationDTO doToDto(CertificationDO dataObject) {
        CertificationDTO certificationDTO = new CertificationDTO();
        BeanUtils.copyProperties(dataObject, certificationDTO);
        certificationDTO.setCertName(dataObject.getName());
        certificationDTO.setDomains(gson.fromJson(dataObject.getDomains(), new TypeToken<List<String>>() {
        }.getType()));
        certificationDTO.setCommonName(certificationDTO.getDomains().get(0));
        if (dataObject.getEnvId() != null) {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(dataObject.getEnvId());
            certificationDTO.setEnvName(devopsEnvironmentE.getName());
        }
        return certificationDTO;
    }
}
