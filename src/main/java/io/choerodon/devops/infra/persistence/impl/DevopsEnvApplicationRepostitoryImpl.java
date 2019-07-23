package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsEnvMessageVO;
import io.choerodon.devops.domain.application.repository.DevopsEnvApplicationRepostitory;
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvApplicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Component
public class DevopsEnvApplicationRepostitoryImpl implements DevopsEnvApplicationRepostitory {

    @Autowired
    DevopsEnvApplicationMapper devopsEnvApplicationMapper;

    @Override
    public DevopsEnvApplicationE baseCreate(DevopsEnvApplicationE devopsEnvApplicationE) {
        DevopsEnvApplicationDTO devopsEnvApplicationDO = ConvertHelper.convert(devopsEnvApplicationE,DevopsEnvApplicationDTO.class);
        if(devopsEnvApplicationMapper.insert(devopsEnvApplicationDO)!= 1){
            throw new CommonException("error.insert.env.app");
        }
        return devopsEnvApplicationE;
    }

    @Override
    public List<Long> baseListAppByEnvId(Long envId) {
        return devopsEnvApplicationMapper.queryAppByEnvId(envId);
    }

    @Override
    public List<DevopsEnvMessageVO> baseListResourceByEnvAndApp(Long envId, Long appId) {
        return devopsEnvApplicationMapper.listResourceByEnvAndApp(envId,appId);
    }
}
