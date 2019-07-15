package io.choerodon.devops.infra.persistence.impl;


import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.DevopsDeployValueE;
import io.choerodon.devops.domain.application.repository.DevopsDeployValueRepository;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;
import io.choerodon.devops.infra.mapper.DevopsDeployValueMapper;


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
    public PageInfo<DevopsDeployValueE> basePageByOptions(Long projectId, Long appId, Long envId, Long userId, PageRequest pageRequest, String params) {
        Map maps = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        PageInfo<DevopsDeployValueDTO> devopsAutoDeployDOS = PageHelper
                .startPage(pageRequest.getPage(), pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> valueMapper.listByOptions(projectId, appId, envId, userId, searchParamMap, paramMap));
        return ConvertPageHelper.convertPageInfo(devopsAutoDeployDOS, DevopsDeployValueE.class);
    }

    @Override
    public DevopsDeployValueE baseCreateOrUpdate(DevopsDeployValueE pipelineRecordE) {
        DevopsDeployValueDTO pipelineValueDO = ConvertHelper.convert(pipelineRecordE, DevopsDeployValueDTO.class);
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
    public void baseDelete(Long valueId) {
        DevopsDeployValueDTO pipelineValueDO = new DevopsDeployValueDTO();
        pipelineValueDO.setId(valueId);
        valueMapper.deleteByPrimaryKey(pipelineValueDO);
    }

    @Override
    public DevopsDeployValueE baseQueryById(Long valueId) {
        DevopsDeployValueDTO pipelineValueDO = new DevopsDeployValueDTO();
        pipelineValueDO.setId(valueId);
        return ConvertHelper.convert(valueMapper.selectByPrimaryKey(pipelineValueDO), DevopsDeployValueE.class);
    }

    @Override
    public void baseCheckName(Long projectId, String name) {
        DevopsDeployValueDTO pipelineValueDO = new DevopsDeployValueDTO();
        pipelineValueDO.setProjectId(projectId);
        pipelineValueDO.setName(name);
        if (valueMapper.select(pipelineValueDO).size() > 0) {
            throw new CommonException("error.devops.pipeline.value.name.exit");
        }
    }

    @Override
    public List<DevopsDeployValueE> baseQueryByAppIdAndEnvId(Long projectId, Long appId, Long envId) {
        DevopsDeployValueDTO valueDO = new DevopsDeployValueDTO();
        valueDO.setProjectId(projectId);
        valueDO.setAppId(appId);
        valueDO.setEnvId(envId);
        return ConvertHelper.convertList(valueMapper.select(valueDO), DevopsDeployValueE.class);
    }
}
