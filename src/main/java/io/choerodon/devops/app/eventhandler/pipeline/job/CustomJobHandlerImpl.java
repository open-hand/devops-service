package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.List;

import org.springframework.stereotype.Service;

import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/8 9:40
 */
@Service
public class CustomJobHandlerImpl extends AbstractJobHandler {
    @Override
    public CiJobTypeEnum getType() {
        return CiJobTypeEnum.CUSTOM;
    }

    @Override
    public List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO) {
        return null;
    }
}
