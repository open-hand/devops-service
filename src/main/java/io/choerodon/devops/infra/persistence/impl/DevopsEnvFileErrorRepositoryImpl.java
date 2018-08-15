package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileErrorE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileErrorRepository;
import io.choerodon.devops.infra.dataobject.DevopsEnvFileErrorDO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Component
public class DevopsEnvFileErrorRepositoryImpl implements DevopsEnvFileErrorRepository {

    @Autowired
    DevopsEnvFileErrorMapper devopsEnvFileErrorMapper;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO();
        devopsEnvFileErrorDO.setFilePath(devopsEnvFileErrorE.getFilePath());
        devopsEnvFileErrorDO.setEnvId(devopsEnvFileErrorE.getEnvId());
        DevopsEnvFileErrorDO newDevopsEnvFileErrorDO = devopsEnvFileErrorMapper.selectOne(devopsEnvFileErrorDO);
        if (newDevopsEnvFileErrorDO != null) {
            newDevopsEnvFileErrorDO.setCommit(devopsEnvFileErrorE.getCommit());
            newDevopsEnvFileErrorDO.setError(devopsEnvFileErrorE.getError());
            if (devopsEnvFileErrorMapper.updateByPrimaryKeySelective(newDevopsEnvFileErrorDO) != 1) {
                throw new CommonException("error.env.error.file.update");
            }
        } else {
            if (devopsEnvFileErrorMapper.insert(ConvertHelper.convert(devopsEnvFileErrorE, DevopsEnvFileErrorDO.class)) != 1) {
                throw new CommonException("error.env.error.file.create");
            }
        }
    }

    @Override
    public List<DevopsEnvFileErrorE> listByEnvId(Long envId) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO();
        devopsEnvFileErrorDO.setEnvId(envId);
        return ConvertHelper.convertList(
                devopsEnvFileErrorMapper.select(devopsEnvFileErrorDO), DevopsEnvFileErrorE.class);
    }

    @Override
    public Page<DevopsEnvFileErrorE> pageByEnvId(Long envId, PageRequest pageRequest) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO();
        devopsEnvFileErrorDO.setEnvId(envId);
        return ConvertPageHelper.convertPage(PageHelper.doPage(
                pageRequest.getPage(), pageRequest.getSize(),
                () -> devopsEnvFileErrorMapper.select(devopsEnvFileErrorDO)), DevopsEnvFileErrorE.class);
    }


    @Override
    public void delete(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = ConvertHelper
                .convert(devopsEnvFileErrorE, DevopsEnvFileErrorDO.class);
        devopsEnvFileErrorMapper.delete(devopsEnvFileErrorDO);
    }

    @Override
    public DevopsEnvFileErrorE queryByEnvIdAndFilePath(Long envId, String filePath) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO();
        devopsEnvFileErrorDO.setEnvId(envId);
        devopsEnvFileErrorDO.setFilePath(filePath);
        return ConvertHelper.convert(devopsEnvFileErrorMapper.selectOne(devopsEnvFileErrorDO), DevopsEnvFileErrorE.class);
    }


}
