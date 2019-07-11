package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileErrorE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileErrorRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDO;
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
    public DevopsEnvFileErrorE createOrUpdate(DevopsEnvFileErrorE devopsEnvFileErrorE) {
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
        return ConvertHelper.convert(devopsEnvFileErrorMapper.selectOne(devopsEnvFileErrorDO), DevopsEnvFileErrorE.class);
    }

    @Override
    public List<DevopsEnvFileErrorE> listByEnvId(Long envId) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO();
        devopsEnvFileErrorDO.setEnvId(envId);
        return ConvertHelper.convertList(
                devopsEnvFileErrorMapper.select(devopsEnvFileErrorDO), DevopsEnvFileErrorE.class);
    }

    @Override
    public PageInfo<DevopsEnvFileErrorE> pageByEnvId(Long envId, PageRequest pageRequest) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = new DevopsEnvFileErrorDO();
        devopsEnvFileErrorDO.setEnvId(envId);
        return ConvertPageHelper.convertPageInfo(PageHelper.startPage(
                pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(
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

    @Override
    public void create(DevopsEnvFileErrorE devopsEnvFileErrorE) {
        DevopsEnvFileErrorDO devopsEnvFileErrorDO = ConvertHelper.convert(devopsEnvFileErrorE,DevopsEnvFileErrorDO.class);
        devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDO);
    }


}
