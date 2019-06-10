package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsDeployValueE;
import io.choerodon.devops.domain.application.repository.DevopsDeployValueRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsDeployValueDO;
import io.choerodon.devops.infra.mapper.DevopsDeployValueMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:04 2019/4/10
 * Description:
 */
@Component
public class DevopsDeployValueRepositoryImpl implements DevopsDeployValueRepository {
    private static final Gson gson = new Gson();

    @Autowired
    private DevopsDeployValueMapper valueMapper;

    @Override
    public Page<DevopsDeployValueE> listByOptions(Long projectId, Long appId, Long envId, Long userId, PageRequest pageRequest, String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        Page<DevopsDeployValueDO> devopsAutoDeployDOS = PageHelper
                .doPageAndSort(pageRequest, () -> valueMapper.listByOptions(projectId, appId, envId, userId, searchParamMap, paramMap));
        return ConvertPageHelper.convertPage(devopsAutoDeployDOS, DevopsDeployValueE.class);
    }

    @Override
    public DevopsDeployValueE createOrUpdate(DevopsDeployValueE pipelineRecordE) {
        DevopsDeployValueDO pipelineValueDO = ConvertHelper.convert(pipelineRecordE, DevopsDeployValueDO.class);
        if (pipelineValueDO.getId() == null) {
            if (valueMapper.insert(pipelineValueDO) != 1) {
                throw new CommonException("error.insert.pipeline.value");
            }
        } else {
            pipelineValueDO.setObjectVersionNumber(valueMapper.selectByPrimaryKey(pipelineValueDO).getObjectVersionNumber());
            if (valueMapper.updateByPrimaryKeySelective(pipelineValueDO) != 1) {
                throw new CommonException("error.update.pipeline.value");
            }
            pipelineValueDO.setObjectVersionNumber(null);
        }
        return ConvertHelper.convert(valueMapper.selectByPrimaryKey(pipelineValueDO), DevopsDeployValueE.class);
    }

    @Override
    public void delete(Long valueId) {
        DevopsDeployValueDO pipelineValueDO = new DevopsDeployValueDO();
        pipelineValueDO.setId(valueId);
        valueMapper.deleteByPrimaryKey(pipelineValueDO);
    }

    @Override
    public DevopsDeployValueE queryById(Long valueId) {
        DevopsDeployValueDO pipelineValueDO = new DevopsDeployValueDO();
        pipelineValueDO.setId(valueId);
        return ConvertHelper.convert(valueMapper.selectByPrimaryKey(pipelineValueDO), DevopsDeployValueE.class);
    }

    @Override
    public void checkName(Long projectId, String name) {
        DevopsDeployValueDO pipelineValueDO = new DevopsDeployValueDO();
        pipelineValueDO.setProjectId(projectId);
        pipelineValueDO.setName(name);
        if (valueMapper.select(pipelineValueDO).size() > 0) {
            throw new CommonException("error.devops.pipeline.value.name.exit");
        }
    }

    @Override
    public List<DevopsDeployValueE> queryByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        DevopsDeployValueDO valueDO = new DevopsDeployValueDO();
        valueDO.setProjectId(projectId);
        valueDO.setAppId(appId);
        valueDO.setEnvId(envId);
        return ConvertHelper.convertList(valueMapper.select(valueDO), DevopsDeployValueE.class);
    }
}
