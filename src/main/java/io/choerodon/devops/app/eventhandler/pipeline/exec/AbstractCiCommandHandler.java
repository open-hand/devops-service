package io.choerodon.devops.app.eventhandler.pipeline.exec;

import static io.choerodon.devops.infra.constant.ExceptionConstants.AppServiceCode.DEVOPS_TOKEN_INVALID;
import static io.choerodon.devops.infra.constant.PipelineConstants.DEVOPS_CI_JOB_RECORD_QUERY;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.api.vo.pipeline.CiResponseVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.util.CustomContextUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/4 15:50
 */
public abstract class AbstractCiCommandHandler {

    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private DevopsCiJobRecordService devopsCiJobRecordService;

    public abstract CiCommandTypeEnum getType();

    CiResponseVO executeCommand(String token, Long gitlabPipelineId, Long gitlabJobId, Long configId) {
        CiResponseVO ciResponseVO = new CiResponseVO();
        StringBuilder log = new StringBuilder();
        Map<String, Object> content = new HashMap<>();
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
        if (appServiceDTO == null) {
            throw new DevopsCiInvalidException(DEVOPS_TOKEN_INVALID);
        }
        // 查询任务记录设置用户上下文
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByAppServiceIdAndGitlabJobId(appServiceDTO.getId(), gitlabJobId);
        if (devopsCiJobRecordDTO == null) {
            throw new DevopsCiInvalidException(DEVOPS_CI_JOB_RECORD_QUERY);
        }
        CustomContextUtil.setUserContext(devopsCiJobRecordDTO.getTriggerUserId());
        try {
            execute(appServiceDTO, gitlabPipelineId, gitlabJobId, configId, log, content);
        } catch (Exception e) {
            ciResponseVO.setFailed(true);
            log.append(e.getMessage());
        }
        ciResponseVO.setMessage(log.toString());
        ciResponseVO.setContent(content);
        return ciResponseVO;
    }

    protected abstract void execute(AppServiceDTO appServiceDTO, Long gitlabPipelineId, Long gitlabJobId, Long configId, StringBuilder log, Map<String, Object> content);

    private void appendMessage(CiResponseVO ciResponseVO, String message) {
        if (ciResponseVO.getMessage() == null) {
            ciResponseVO.setMessage(message);
        }
        ciResponseVO.setMessage(ciResponseVO.getMessage() + message);
    }
}
