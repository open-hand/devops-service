package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsCertificationProRelRepository;
import io.choerodon.devops.infra.dto.DevopsCertificationProRelationshipDTO;
import io.choerodon.devops.infra.mapper.DevopsCertificationProRelMapper;

@Service
public class DevopsCertificationProRelRepositoryImpl implements DevopsCertificationProRelRepository {

    @Autowired
    private DevopsCertificationProRelMapper devopsCertificationProRelMapper;

    @Override
    public void baseInsertRelationship(DevopsCertificationProRelE devopsCertificationProRelE) {
        DevopsCertificationProRelationshipDTO devopsCertificationProRelDO = ConvertHelper.convert(devopsCertificationProRelE, DevopsCertificationProRelationshipDTO.class);
        if (devopsCertificationProRelMapper.insert(devopsCertificationProRelDO) != 1) {
            throw new CommonException("error.devops.cert.project.rel.add.error");
        }
    }

    @Override
    public List<DevopsCertificationProRelE> baseListByCertificationId(Long certificationId) {
        DevopsCertificationProRelationshipDTO devopsCertificationProRelDO = new DevopsCertificationProRelationshipDTO();
        devopsCertificationProRelDO.setCertId(certificationId);
        return ConvertHelper.convertList(devopsCertificationProRelMapper.select(devopsCertificationProRelDO), DevopsCertificationProRelE.class);
    }

    @Override
    public void baseDelete(DevopsCertificationProRelE devopsCertificationProRelE) {
        DevopsCertificationProRelationshipDTO devopsCertificationProRelDO = ConvertHelper.convert(devopsCertificationProRelE, DevopsCertificationProRelationshipDTO.class);
        devopsCertificationProRelMapper.delete(devopsCertificationProRelDO);
    }

    @Override
    public void baseDeleteByCertificationId(Long certificationId) {
        DevopsCertificationProRelationshipDTO devopsCertificationProRelDO = new DevopsCertificationProRelationshipDTO();
        devopsCertificationProRelDO.setCertId(certificationId);
        devopsCertificationProRelMapper.delete(devopsCertificationProRelDO);
    }
}
