package io.choerodon.devops.infra.persistence.impl;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsProjectConfigE;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsProjectConfigDO;
import io.choerodon.devops.infra.mapper.DevopsProjectConfigMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@Component
public class DevopsProjectProjectConfigRepositoryImpl implements DevopsProjectConfigRepository {

    @Autowired
    DevopsProjectConfigMapper configMapper;

    private Gson gson = new Gson();

    @Override
    public DevopsProjectConfigE create(DevopsProjectConfigE devopsProjectConfigE) {
        DevopsProjectConfigDO paramDO = ConvertHelper.convert(devopsProjectConfigE, DevopsProjectConfigDO.class);

        DevopsProjectConfigDO checkParamDO = new DevopsProjectConfigDO();
        checkParamDO.setName(paramDO.getName());
        checkParamDO.setProjectId(paramDO.getProjectId());
        DevopsProjectConfigDO checkedDO = configMapper.selectOne(checkParamDO);

        if (ObjectUtils.isEmpty(checkedDO)) {
            if (configMapper.insert(paramDO) != 1) {
                throw new CommonException("error.devops.project.config.insert");
            }
        } else {
            throw new CommonException("error.devops.project.config.projectId.name.already.exist");
        }
        return ConvertHelper.convert(configMapper.selectOne(paramDO), DevopsProjectConfigE.class);
    }

    @Override
    public DevopsProjectConfigE updateByPrimaryKeySelective(DevopsProjectConfigE devopsProjectConfigE) {
        DevopsProjectConfigDO paramDO = ConvertHelper.convert(devopsProjectConfigE, DevopsProjectConfigDO.class);
        if (configMapper.updateByPrimaryKeySelective(paramDO) != 1) {
            throw new CommonException("error.devops.project.config.update");
        }
        return ConvertHelper.convert(configMapper.selectByPrimaryKey(paramDO), DevopsProjectConfigE.class);
    }

    @Override
    public DevopsProjectConfigE queryByPrimaryKey(Long id) {
        DevopsProjectConfigDO paramDO = new DevopsProjectConfigDO();
        paramDO.setId(id);
        return ConvertHelper.convert(configMapper.selectByPrimaryKey(paramDO), DevopsProjectConfigE.class);
    }

    @Override
    public Page<DevopsProjectConfigE> listByOptions(Long projectId, PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("searchParam", null);
        mapParams.put("param", null);

        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            mapParams.put("searchParam", TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)));
            mapParams.put("param", TypeUtil.cast(maps.get(TypeUtil.PARAM)));
        }
        Page<DevopsProjectConfigDO> configDOPage = PageHelper
                .doPageAndSort(pageRequest, () -> configMapper.list(projectId,
                        (Map<String,Object>)mapParams.get("searchParam"),
                        (String) mapParams.get("param"), checkSortIsEmpty(pageRequest)));
        return ConvertPageHelper.convertPage(configDOPage, DevopsProjectConfigE.class);
    }

    private String checkSortIsEmpty(PageRequest pageRequest) {
        String index = "";
        if (pageRequest.getSort() == null) {
            index = "true";
        }
        return index;
    }

    @Override
    public void delete(Long id) {
        DevopsProjectConfigDO paramDO = new DevopsProjectConfigDO();
        paramDO.setId(id);
        if (configMapper.deleteByPrimaryKey(paramDO) != 1) {
            throw new CommonException("error.devops.project.config.delete");
        }
    }
}
