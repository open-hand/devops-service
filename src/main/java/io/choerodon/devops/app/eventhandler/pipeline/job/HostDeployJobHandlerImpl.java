package io.choerodon.devops.app.eventhandler.pipeline.job;

import static io.choerodon.devops.infra.constant.ExceptionConstants.CiHostDeployCode.DEVOPS_HOST_DEPLOY_INFO_CREATE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.pipeline.DevopsCiHostDeployInfoVO;
import io.choerodon.devops.app.service.DevopsHostAppService;
import io.choerodon.devops.infra.dto.DevopsCiApiTestInfoDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsCiApiTestInfoMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class HostDeployJobHandlerImpl extends AbstractJobHandler {
    @Autowired
    DevopsCiApiTestInfoMapper devopsCiApiTestInfoMapper;

    @Autowired
    private DevopsHostAppService devopsHostAppService;

    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.HOST_DEPLOY;
    }

    @Override
    protected void checkConfigInfo(Long projectId, DevopsCiJobVO devopsCiJobVO) {
        DevopsCiHostDeployInfoVO devopsCiHostDeployInfoVO = devopsCiJobVO.getDevopsCiHostDeployInfoVO();
        devopsHostAppService.checkNameAndCodeUniqueAndThrow(projectId,
                devopsCiHostDeployInfoVO.getAppId(),
                devopsCiHostDeployInfoVO.getAppName(),
                devopsCiHostDeployInfoVO.getAppCode());
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {

        return null;
    }

    @Override
    protected Long saveConfig(DevopsCiJobVO devopsCiJobVO) {
        DevopsCiApiTestInfoDTO devopsCiApiTestInfoDTO = ConvertUtils.convertObject(devopsCiJobVO.getDevopsCiHostDeployInfoVO(), DevopsCiApiTestInfoDTO.class);
        devopsCiApiTestInfoDTO.setId(null);
        MapperUtil.resultJudgedInsert(devopsCiApiTestInfoMapper, devopsCiApiTestInfoDTO, DEVOPS_HOST_DEPLOY_INFO_CREATE);
        return devopsCiApiTestInfoDTO.getId();
    }
}
