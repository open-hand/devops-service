package io.choerodon.devops.infra.persistence.impl;

import com.google.gson.Gson;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsAutoDeployRecordE;
import io.choerodon.devops.domain.application.repository.DevopsAutoDeployRecordRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsAutoDeployRecordDO;
import io.choerodon.devops.infra.mapper.DevopsAutoDeployRecordMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:12 2019/2/27
 * Description:
 */
@Component
public class DevopsAutoDeployRecorlRepositoryImpl implements DevopsAutoDeployRecordRepository {
    private Gson gson = new Gson();

    @Autowired
    private DevopsAutoDeployRecordMapper devopsAutoDeployRecordMapper;

    @Override
    public Page<DevopsAutoDeployRecordE> listByOptions(Long projectId, Long appId, Long envId, String taskName, Boolean doPage, PageRequest pageRequest, String params) {
        Page<DevopsAutoDeployRecordDO> devopsAutoDeployRecordDOS = new Page<>();
        String param = null;

        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("searchParam", null);
        mapParams.put("param", null);
        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            mapParams.put("searchParam", TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)));
            mapParams.put("param", TypeUtil.cast(maps.get(TypeUtil.PARAM)));
        }
        //是否需要分页
        if (doPage != null && !doPage) {
            devopsAutoDeployRecordDOS.setContent(devopsAutoDeployRecordMapper.list(projectId, appId, envId, taskName,
                    (Map<String, Object>) mapParams.get("searchParam"),
                    mapParams.get("param").toString()));
        } else {
            devopsAutoDeployRecordDOS = PageHelper
                    .doPageAndSort(pageRequest, () -> devopsAutoDeployRecordMapper.list(projectId, appId, envId, taskName,
                            (Map<String, Object>) mapParams.get("searchParam"),
                            mapParams.get("param").toString()));
        }
        return ConvertPageHelper.convertPage(devopsAutoDeployRecordDOS, DevopsAutoDeployRecordE.class);
    }

    @Override
    public void updateStatus(Long autoDeployId, String status) {
        devopsAutoDeployRecordMapper.banchUpdateStatus(autoDeployId, status);
    }

    @Override
    public DevopsAutoDeployRecordE createOrUpdate(DevopsAutoDeployRecordE devopsAutoDeployRecordE) {
        DevopsAutoDeployRecordDO devopsAutoDeployRecordDO = ConvertHelper.convert(devopsAutoDeployRecordE, DevopsAutoDeployRecordDO.class);
        if (devopsAutoDeployRecordDO.getId() == null) {
            if (devopsAutoDeployRecordMapper.insert(devopsAutoDeployRecordDO) != 1) {
                throw new CommonException("error.auto.deploy.record.create.error");
            }
        } else {
            devopsAutoDeployRecordDO.setObjectVersionNumber(devopsAutoDeployRecordMapper.selectByPrimaryKey(devopsAutoDeployRecordDO).getObjectVersionNumber());
            if (devopsAutoDeployRecordMapper.updateByPrimaryKeySelective(devopsAutoDeployRecordDO) != 1) {
                throw new CommonException("error.auto.deploy.record.update.error");
            }
        }
        devopsAutoDeployRecordDO.setObjectVersionNumber(null);
        return ConvertHelper.convert(devopsAutoDeployRecordMapper.selectOne(devopsAutoDeployRecordDO), DevopsAutoDeployRecordE.class);

    }

    @Override
    public void updateInstanceId(Long instanceId) {

    }
}
