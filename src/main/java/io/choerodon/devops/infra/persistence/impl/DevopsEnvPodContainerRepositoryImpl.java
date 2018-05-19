package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.repository.DevopsEnvPodContainerRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsEnvPodContainerDO;
import io.choerodon.devops.infra.mapper.DevopsEnvPodContainerMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/5/16
 * Time: 11:55
 * Description:
 */
@Component
public class DevopsEnvPodContainerRepositoryImpl implements DevopsEnvPodContainerRepository {
    private Gson gson = new Gson();
    @Autowired
    private DevopsEnvPodContainerMapper containerMapper;

    @Override
    public void insert(DevopsEnvPodContainerDO containerDO) {
        containerMapper.insert(containerDO);
    }

    @Override
    public void update(DevopsEnvPodContainerDO containerDO) {
        containerMapper.updateByPrimaryKey(containerDO);
    }

    @Override
    public void delete(Long id) {
        containerMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void deleteByPodId(Long podId) {
        containerMapper.delete(new DevopsEnvPodContainerDO(podId));
    }


    @Override
    public DevopsEnvPodContainerDO get(Long id) {
        return containerMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsEnvPodContainerDO get(DevopsEnvPodContainerDO container) {
        List<DevopsEnvPodContainerDO> containerDO = containerMapper.select(container);
        return containerDO.isEmpty() ? new DevopsEnvPodContainerDO() : containerDO.get(0);
    }

    @Override
    public List<DevopsEnvPodContainerDO> list(DevopsEnvPodContainerDO container) {
        return containerMapper.select(container);
    }

    @Override
    public Page<DevopsEnvPodContainerDO> page(Long podId, PageRequest pageRequest, String param) {
        Map<String, Object> searchParamMap = gson.fromJson(param, Map.class);
        return PageHelper.doPageAndSort(pageRequest, () ->
                containerMapper.listContainer(podId,
                        TypeUtil.cast(searchParamMap.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(searchParamMap.get(TypeUtil.PARAM))));
    }
}
