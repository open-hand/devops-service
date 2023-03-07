package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CdApiTestConfigForSagaVO;
import io.choerodon.devops.api.vo.CdApiTestConfigVO;
import io.choerodon.devops.api.vo.DevopsCdJobVO;
import io.choerodon.devops.app.service.DevopsCdJobService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCdAuditDTO;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.enums.JobTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsCdAuditMapper;
import io.choerodon.devops.infra.mapper.DevopsCdJobMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCdJobServiceImpl implements DevopsCdJobService {
    public static final Logger LOGGER = LoggerFactory.getLogger(DevopsCdJobServiceImpl.class);
    private static final String DEVOPS_UPDATE_JOB_FAILED = "devops.update.job.failed";
    private static final String DEVOPS_INSERT_CD_JOB = "devops.insert.cd.job";

    @Autowired
    private DevopsCdJobMapper devopsCdJobMapper;
    @Autowired
    private DevopsCdAuditMapper devopsCdAuditMapper;


    @Override
    public DevopsCdJobDTO create(DevopsCdJobDTO devopsCdJobDTO) {
        if (devopsCdJobMapper.insert(devopsCdJobDTO) != 1) {
            throw new CommonException(DEVOPS_INSERT_CD_JOB);
        }
        return devopsCdJobMapper.selectByPrimaryKey(devopsCdJobDTO.getId());
    }

    @Override
    public List<DevopsCdJobDTO> listByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setPipelineId(pipelineId);
        return devopsCdJobMapper.select(devopsCdJobDTO);
    }

    @Override
    public List<DevopsCdJobDTO> listByType(JobTypeEnum jobTypeEnum) {
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setType(jobTypeEnum.value());
        return devopsCdJobMapper.select(devopsCdJobDTO);
    }

    @Override
    public void deleteByStageId(Long stageId) {
        Assert.notNull(stageId, PipelineCheckConstant.DEVOPS_STAGE_ID_IS_NULL);

        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setStageId(stageId);
        List<DevopsCdJobDTO> cdJobDTOS = devopsCdJobMapper.select(devopsCdJobDTO);
        if (!CollectionUtils.isEmpty(cdJobDTOS)) {
            cdJobDTOS.forEach(cdJobDTO -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
                devopsCdAuditDTO.setCdJobId(cdJobDTO.getId());
                devopsCdAuditMapper.delete(devopsCdAuditDTO);
            });
            devopsCdJobMapper.delete(devopsCdJobDTO);
        }
    }

    @Override
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);
        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setPipelineId(pipelineId);
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobMapper.select(devopsCdJobDTO);
        if (!CollectionUtils.isEmpty(devopsCdJobDTOS)) {
            devopsCdJobDTOS.forEach(cdJobDTO -> {
                DevopsCdAuditDTO devopsCdAuditDTO = new DevopsCdAuditDTO();
                devopsCdAuditDTO.setCdJobId(cdJobDTO.getId());
                devopsCdAuditMapper.delete(devopsCdAuditDTO);
            });
            devopsCdJobMapper.delete(devopsCdJobDTO);
        }
    }

    @Override
    public DevopsCdJobDTO queryById(Long stageId) {
        Assert.notNull(stageId, PipelineCheckConstant.DEVOPS_STAGE_ID_IS_NULL);
        return devopsCdJobMapper.selectByPrimaryKey(stageId);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsCdJobDTO devopsCdJobDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCdJobMapper, devopsCdJobDTO, DEVOPS_UPDATE_JOB_FAILED);
    }

    @Override
    @Transactional
    public void baseDelete(Long id) {
        devopsCdJobMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<DevopsCdJobDTO> listByProjectIdAndType(Long projectId, JobTypeEnum typeEnum) {
        Assert.notNull(projectId, ResourceCheckConstant.DEVOPS_PROJECT_ID_IS_NULL);
        Assert.notNull(projectId, ResourceCheckConstant.DEVOPS_JOB_TYPE_IS_NULL);

        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setProjectId(projectId);
        devopsCdJobDTO.setType(typeEnum.value());
        return devopsCdJobMapper.select(devopsCdJobDTO);
    }

    @Override
    public List<DevopsCdJobVO> listByIdsWithNames(Set<Long> jobIds) {
        return devopsCdJobMapper.listByIdsWithNames(jobIds);
    }

    @Override
    public List<CdApiTestConfigForSagaVO> listCdApiTestConfig() {
        List<CdApiTestConfigForSagaVO> cdApiTestConfigForSagaVOArrayList = new ArrayList<>();
        List<DevopsCdJobDTO> devopsCdJobDTOList = devopsCdJobMapper.listByType(JobTypeEnum.CD_API_TEST.value());
        if (!CollectionUtils.isEmpty(devopsCdJobDTOList)) {
            devopsCdJobDTOList.forEach(devopsCdJobDTO -> {
                CdApiTestConfigVO cdApiTestConfigVO = JsonHelper.unmarshalByJackson(devopsCdJobDTO.getMetadata(), CdApiTestConfigVO.class);
                CdApiTestConfigForSagaVO cdApiTestConfigForSagaVO = ConvertUtils.convertObject(cdApiTestConfigVO, CdApiTestConfigForSagaVO.class);
                cdApiTestConfigForSagaVO.setDevopsCdJobId(devopsCdJobDTO.getId());
                cdApiTestConfigForSagaVOArrayList.add(cdApiTestConfigForSagaVO);
            });
        }
        return cdApiTestConfigForSagaVOArrayList;
    }

    @Override
    public List<DevopsCdJobDTO> listByStageId(Long stageId) {
        Assert.notNull(stageId, PipelineCheckConstant.DEVOPS_STAGE_ID_IS_NULL);

        DevopsCdJobDTO devopsCdJobDTO = new DevopsCdJobDTO();
        devopsCdJobDTO.setStageId(stageId);

        return devopsCdJobMapper.select(devopsCdJobDTO);
    }
}
