package io.choerodon.devops.infra.persistence.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  10:12 2019/2/27
 * Description:
 */
@Component
public class DevopsAutoDeployRecorlRepositoryImpl implements DevopsAutoDeployRecordRepository {

    @Autowired
    private DevopsAutoDeployRecordMapper devopsAutoDeployRecordMapper;

    @Override
    public Page<DevopsAutoDeployRecordE> listByOptions(Long projectId, Long userId,Long appId, Long envId, String taskName, Boolean doPage, PageRequest pageRequest, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);

        Page<DevopsAutoDeployRecordDO> devopsAutoDeployRecordDOS = PageHelper
                .doPageAndSort(pageRequest, () -> devopsAutoDeployRecordMapper.list(projectId,userId, appId, envId, taskName,
                        (Map<String, Object>) mapParams.get(TypeUtil.SEARCH_PARAM),
                        mapParams.get(TypeUtil.PARAM).toString()));

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
}
