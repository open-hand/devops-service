package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsCertificationProRelE;
import io.choerodon.devops.domain.application.repository.DevopsCertificationProRelRepository;
import io.choerodon.devops.infra.dataobject.DevopsCertificationProRelDO;
import io.choerodon.devops.infra.mapper.DevopsCertificationProRelMapper;

@Service
public class DevopsCertificationProRelRepositoryImpl implements DevopsCertificationProRelRepository {

    @Autowired
    private DevopsCertificationProRelMapper devopsCertificationProRelMapper;

    @Override
    public void insert(DevopsCertificationProRelE devopsCertificationProRelE) {
        DevopsCertificationProRelDO devopsCertificationProRelDO = ConvertHelper.convert(devopsCertificationProRelE, DevopsCertificationProRelDO.class);
        if (devopsCertificationProRelMapper.insert(devopsCertificationProRelDO) != 1) {
            throw new CommonException("error.devops.cert.project.rel.add.error");
        }
    }

    @Override
    public List<DevopsCertificationProRelE> listByCertId(Long certId) {
        DevopsCertificationProRelDO devopsCertificationProRelDO = new DevopsCertificationProRelDO();
        devopsCertificationProRelDO.setCertId(certId);
        return ConvertHelper.convertList(devopsCertificationProRelMapper.select(devopsCertificationProRelDO), DevopsCertificationProRelE.class);
    }

    @Override
    public void delete(DevopsCertificationProRelE devopsCertificationProRelE) {
        DevopsCertificationProRelDO devopsCertificationProRelDO = ConvertHelper.convert(devopsCertificationProRelE, DevopsCertificationProRelDO.class);
        devopsCertificationProRelMapper.delete(devopsCertificationProRelDO);
    }

    @Override
    public void deleteByCertId(Long certId) {
        DevopsCertificationProRelDO devopsCertificationProRelDO = new DevopsCertificationProRelDO();
        devopsCertificationProRelDO.setCertId(certId);
        devopsCertificationProRelMapper.delete(devopsCertificationProRelDO);
    }
}
