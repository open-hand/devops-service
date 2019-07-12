package io.choerodon.devops.infra.convertor;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.devops.api.vo.CertificationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.CertificationE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.infra.dto.CertificationDTO;

/**
 * Created by n!Ck
 * Date: 2018/8/21
 * Time: 10:31
 * Description:
 */
@Component
public class CertificationConvertor implements ConvertorI<CertificationE, CertificationDTO, CertificationVO> {

    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    private Gson gson = new Gson();

    @Override
    public CertificationE doToEntity(CertificationDTO dataObject) {
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
    public CertificationE dtoToEntity(CertificationVO certificationVO) {
        CertificationE certificationE = new CertificationE();
        BeanUtils.copyProperties(certificationVO, certificationE);
        certificationE.setEnvironmentE(new DevopsEnvironmentE(certificationVO.getEnvId()));
        return certificationE;
    }

    @Override
    public CertificationDTO entityToDo(CertificationE certificationE) {
        CertificationDTO certificationDTO = new CertificationDTO();
        BeanUtils.copyProperties(certificationE, certificationDTO);
        certificationDTO.setDomains(gson.toJson(certificationE.getDomains()));
        if (certificationE.getEnvironmentE() != null) {
            certificationDTO.setEnvId(certificationE.getEnvironmentE().getId());
        }
        return certificationDTO;
    }



    @Override
    public CertificationVO entityToDto(CertificationE certificationE) {
        CertificationVO certificationVO = new CertificationVO();
        BeanUtils.copyProperties(certificationE, certificationVO);
        return certificationVO;
    }

    @Override
    public CertificationVO doToDto(CertificationDTO dataObject) {
        CertificationVO certificationVO = new CertificationVO();
        BeanUtils.copyProperties(dataObject, certificationVO);
        certificationVO.setCertName(dataObject.getName());
        certificationVO.setDomains(gson.fromJson(dataObject.getDomains(), new TypeToken<List<String>>() {
        }.getType()));
        certificationVO.setCommonName(certificationVO.getDomains().get(0));
        if (dataObject.getEnvId() != null) {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(dataObject.getEnvId());
            certificationVO.setEnvName(devopsEnvironmentE.getName());
        }
        return certificationVO;
    }
}
