package io.choerodon.devops.app.eventhandler.pipeline.job;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.CiJobWebHookVO;
import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.app.service.CiJobConfigFileRelService;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.infra.dto.CiJobConfigFileRelDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobRecordDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.CiJob;
import io.choerodon.devops.infra.enums.CiCommandTypeEnum;
import io.choerodon.devops.infra.enums.CiJobTypeEnum;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/3 9:03
 */
public abstract class AbstractJobHandler {
    @Autowired
    @Lazy
    private DevopsCiJobService devopsCiJobService;

    @Autowired
    private CiJobConfigFileRelService ciJobConfigFileRelService;

    public abstract CiJobTypeEnum getType();

    /**
     * 把配置转换为gitlab-ci配置（maven,sonarqube）
     *
     * @param organizationId 组织id
     * @param projectId      项目id
     * @param devopsCiJobDTO 生成脚本
     * @return 生成的脚本列表
     */
    public abstract List<String> buildScript(Long organizationId, Long projectId, DevopsCiJobDTO devopsCiJobDTO);

    /**
     * 校验任务配置信息
     *
     * @param projectId
     * @param devopsCiJobVO
     */
    protected void checkConfigInfo(Long projectId, DevopsCiJobVO devopsCiJobVO) {

    }

    @Transactional(rollbackFor = Exception.class)
    public DevopsCiJobDTO saveJobInfo(Long projectId, Long ciPipelineId, Long ciStageId, DevopsCiJobVO devopsCiJobVO) {
        DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
        // 校验任务配置
        checkConfigInfo(projectId, devopsCiJobVO);
        // 保存任务配置
        Long configId = saveConfig(ciPipelineId, devopsCiJobVO);

        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        devopsCiJobDTO.setCiStageId(ciStageId);
        devopsCiJobDTO.setConfigId(configId);

        devopsCiJobService.create(devopsCiJobDTO);

        // 保存文件配置
        if (!CollectionUtils.isEmpty(devopsCiJobVO.getConfigFileRelList())) {
            devopsCiJobVO.getConfigFileRelList().forEach(configFileRelVO -> ciJobConfigFileRelService.baseCreate(new CiJobConfigFileRelDTO(devopsCiJobDTO.getId(),
                    configFileRelVO.getConfigFileId(),
                    configFileRelVO.getConfigFilePath())));

        }

        return devopsCiJobDTO;
    }

    /**
     * 初始化ci任务记录时，需要记录当前的任务配置信息
     *
     * @param devopsCiJobDTO
     * @param job
     */
    public void fillJobAdditionalInfo(DevopsCiJobDTO devopsCiJobDTO, CiJobWebHookVO job) {

    }

    /**
     * 保存任务配置，实现类如果需要存储任务配置则重写
     *
     * @param ciPipelineId
     * @param devopsCiJobVO
     * @return
     */
    protected Long saveConfig(Long ciPipelineId, DevopsCiJobVO devopsCiJobVO) {
        // do nothong
        return null;
    }

    /**
     * 查询流水线详情时，给包含任务配置的任务填充信息
     *
     * @param devopsCiJobVO
     */
    public void fillJobConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        // do nothing
    }

    /**
     * 查询任务模板时，给包含任务配置的任务填充信息
     *
     * @param devopsCiJobVO
     */
    public void fillJobTemplateConfigInfo(DevopsCiJobVO devopsCiJobVO) {
        // do nothing
    }

    /**
     * 初始化流水线记录时需要额外保存的信息
     *
     * @param devopsCiJobRecordDTO
     * @param gitlabPipelineId
     * @param ciJobWebHookVO
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAdditionalRecordInfo(Long ciPipelineId, DevopsCiJobRecordDTO devopsCiJobRecordDTO, Long gitlabPipelineId, CiJobWebHookVO ciJobWebHookVO) {

    }

    /**
     * 初始化流水线记录时需要额外保存的信息
     *
     * @param devopsCiJobRecordDTO
     * @param gitlabPipelineId
     * @param existDevopsCiJobDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAdditionalRecordInfo(Long ciPipelineId, DevopsCiJobRecordDTO devopsCiJobRecordDTO, Long gitlabPipelineId, DevopsCiJobDTO existDevopsCiJobDTO) {

    }

    /**
     * 添加job额外的配置信息，比如部署任务是否可编辑
     *
     * @param devopsCiJobVO
     */
    public void fillJobAdditionalInfo(DevopsCiJobVO devopsCiJobVO) {

    }

    public void setCiJobConfig(DevopsCiJobDTO job, CiJob ciJob) {

    }

    /**
     * 复制流水线时，cd的部分数据需要删除
     *
     * @param devopsCiJobVO
     */
    public void deleteCdInfo(DevopsCiJobVO devopsCiJobVO) {

    }

    public boolean isComplete(DevopsCiJobVO devopsCiJobVO) {
        if (StringUtils.isBlank(devopsCiJobVO.getTriggerType())) {
            return false;
        }

        return isConfigComplete(devopsCiJobVO);
    }

    protected Boolean isConfigComplete(DevopsCiJobVO devopsCiJobVO) {
        return CiCommandTypeEnum.fromTypeName(devopsCiJobVO.getType()) == null ? Boolean.TRUE : Boolean.FALSE;
    }
}
