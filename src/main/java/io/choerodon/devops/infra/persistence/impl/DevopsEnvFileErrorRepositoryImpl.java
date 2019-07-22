package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileErrorRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevopsEnvFileErrorRepositoryImpl implements DevopsEnvFileErrorRepository {

    @Autowired
    DevopsEnvFileErrorMapper devopsEnvFileErrorMapper;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DevopsEnvFileErrorE baseCreateOrUpdate(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDO = new DevopsEnvFileErrorDTO();
        devopsEnvFileErrorDO.setFilePath(devopsEnvFileErrorE.getFilePath());
        devopsEnvFileErrorDO.setEnvId(devopsEnvFileErrorE.getEnvId());
        DevopsEnvFileErrorDTO newDevopsEnvFileErrorDO = devopsEnvFileErrorMapper.selectOne(devopsEnvFileErrorDO);
        if (newDevopsEnvFileErrorDO != null) {
            newDevopsEnvFileErrorDO.setCommit(devopsEnvFileErrorE.getCommit());
            newDevopsEnvFileErrorDO.setError(devopsEnvFileErrorE.getError());
            if (devopsEnvFileErrorMapper.updateByPrimaryKeySelective(newDevopsEnvFileErrorDO) != 1) {
                throw new CommonException("error.env.error.file.update");
            }
        } else {
            if (devopsEnvFileErrorMapper.insert(ConvertHelper.convert(devopsEnvFileErrorE, DevopsEnvFileErrorDTO.class)) != 1) {
                throw new CommonException("error.env.error.file.create");
            }
        }
        return ConvertHelper.convert(devopsEnvFileErrorMapper.selectOne(devopsEnvFileErrorDO), DevopsEnvFileErrorE.class);
    }

    @Override
    public List<DevopsEnvFileErrorE> baseListByEnvId(Long envId) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDO = new DevopsEnvFileErrorDTO();
        devopsEnvFileErrorDO.setEnvId(envId);
        return ConvertHelper.convertList(
                devopsEnvFileErrorMapper.select(devopsEnvFileErrorDO), DevopsEnvFileErrorE.class);
    }

    @Override
    public PageInfo<DevopsEnvFileErrorE> basePageByEnvId(Long envId, PageRequest pageRequest) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDO = new DevopsEnvFileErrorDTO();
        devopsEnvFileErrorDO.setEnvId(envId);
        return ConvertPageHelper.convertPageInfo(PageHelper.startPage(
                pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
                () -> devopsEnvFileErrorMapper.select(devopsEnvFileErrorDO)), DevopsEnvFileErrorE.class);
    }


    @Override
    public void baseDelete(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDO = ConvertHelper
                .convert(devopsEnvFileErrorE, DevopsEnvFileErrorDTO.class);
        devopsEnvFileErrorMapper.delete(devopsEnvFileErrorDO);
    }

    @Override
    public DevopsEnvFileErrorE baseQueryByEnvIdAndFilePath(Long envId, String filePath) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDO = new DevopsEnvFileErrorDTO();
        devopsEnvFileErrorDO.setEnvId(envId);
        devopsEnvFileErrorDO.setFilePath(filePath);
        return ConvertHelper.convert(devopsEnvFileErrorMapper.selectOne(devopsEnvFileErrorDO), DevopsEnvFileErrorE.class);
    }

    @Override
    public void baseCreate(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDO = ConvertHelper.convert(devopsEnvFileErrorE,DevopsEnvFileErrorDTO.class);
        devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDO);
    }


}
