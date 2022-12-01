package io.choerodon.devops.app.eventhandler.pipeline.exec;

import static io.choerodon.devops.infra.constant.ExceptionConstants.AppServiceCode.DEVOPS_TOKEN_INVALID;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.api.vo.pipeline.CiResponseVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.util.CustomContextUtil;
import io.choerodon.devops.infra.util.LogUtil;

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
    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    public abstract CiCommandTypeEnum getType();

    public CiResponseVO executeCommand(String token, Long gitlabPipelineId, Long gitlabJobId, Long configId) {
        CiResponseVO ciResponseVO = new CiResponseVO();
        StringBuilder log = new StringBuilder();
        Map<String, Object> content = new HashMap<>();
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
        if (appServiceDTO == null) {
            throw new DevopsCiInvalidException(DEVOPS_TOKEN_INVALID);
        }
        Long appServiceId = appServiceDTO.getId();

        DevopsCiPipelineRecordDTO devopsCiPipelineRecordDTO = devopsCiPipelineRecordService.queryByAppServiceIdAndGitlabPipelineId(appServiceId, gitlabPipelineId);
        Long ciPipelineRecordId = devopsCiPipelineRecordDTO.getId();
        Long ciPipelineId = devopsCiPipelineRecordDTO.getCiPipelineId();

        Integer gitlabProjectId = appServiceDTO.getGitlabProjectId();

        // 查询任务记录设置用户上下文
        DevopsCiJobRecordDTO devopsCiJobRecordDTO = devopsCiJobRecordService.queryByAppServiceIdAndGitlabJobId(appServiceId, gitlabJobId);
        if (devopsCiJobRecordDTO == null) {
            devopsCiJobRecordDTO = devopsCiJobRecordService.syncJobRecord(gitlabJobId, appServiceId, ciPipelineRecordId, ciPipelineId, gitlabProjectId);
        }
        CustomContextUtil.setUserContext(devopsCiJobRecordDTO.getTriggerUserId());
        try {
            execute(appServiceDTO, gitlabPipelineId, gitlabJobId, configId, log, content);
        } catch (Exception e) {
            ciResponseVO.setFailed(true);
            log.append(LogUtil.readContentOfThrowable(e));
        }
        ciResponseVO.setMessage(log.toString());
        ciResponseVO.setContent(content);
        return ciResponseVO;
    }

    public abstract void execute(AppServiceDTO appServiceDTO, Long gitlabPipelineId, Long gitlabJobId, Long configId, StringBuilder log, Map<String, Object> content);
}
