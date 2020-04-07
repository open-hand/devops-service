package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.SonarContentsVO;
import io.choerodon.devops.api.vo.SonarQubeConfigVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.SonarAuthType;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.DevopsCiJobMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:24
 */
@Service
public class DevopsCiJobServiceImpl implements DevopsCiJobService {
    private static final String CREATE_JOB_FAILED = "create.job.failed";
    private static final String DELETE_JOB_FAILED = "delete.job.failed";
    private static final String ERROR_STAGE_ID_IS_NULL = "error.stage.id.is.null";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";
    private static final String SONAR_KEY = "%s-%s:%s";
    private static final String SONAR = "sonar";

    private DevopsCiJobMapper devopsCiJobMapper;
    private AppServiceService appServiceService;
    private BaseServiceClientOperator baseServiceClientOperator;

    public DevopsCiJobServiceImpl(DevopsCiJobMapper devopsCiJobMapper,
                                  AppServiceService appServiceService,
                                  BaseServiceClientOperator baseServiceClientOperator) {
        this.devopsCiJobMapper = devopsCiJobMapper;
        this.appServiceService = appServiceService;
        this.baseServiceClientOperator = baseServiceClientOperator;
    }

    @Override
    @Transactional
    public DevopsCiJobDTO create(DevopsCiJobDTO devopsCiJobDTO) {
        if (devopsCiJobMapper.insertSelective(devopsCiJobDTO) != 1) {
            throw new CommonException(CREATE_JOB_FAILED);
        }
        return devopsCiJobMapper.selectByPrimaryKey(devopsCiJobDTO.getId());
    }

    @Override
    @Transactional
    public void deleteByStageId(Long stageId) {
        if (stageId == null) {
            throw new CommonException(ERROR_STAGE_ID_IS_NULL);
        }
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiStageId(stageId);
        devopsCiJobMapper.delete(devopsCiJobDTO);
    }

    @Override
    public List<DevopsCiJobDTO> listByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        return devopsCiJobMapper.select(devopsCiJobDTO);
    }

    @Override
    public Boolean sonarConnect(Long projectId, SonarQubeConfigVO sonarQubeConfigVO) {
        if (Objects.isNull(sonarQubeConfigVO)) {
            return false;
        }
        if (Objects.isNull(sonarQubeConfigVO.getSonarUrl())) {
            return false;
        }
        if (SonarAuthType.USERNAME_PWD.value().equals(sonarQubeConfigVO.getAuthType())) {
            SonarClient sonarClient = RetrofitHandler.getSonarClient(
                    sonarQubeConfigVO.getSonarUrl(),
                    SONAR,
                    sonarQubeConfigVO.getUsername(),
                    sonarQubeConfigVO.getPassword());
            try {
                sonarClient.getUser().execute();
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        if (SonarAuthType.TOKEN.value().equals(sonarQubeConfigVO.getAuthType())) {
            SonarClient sonarClient = RetrofitHandler.getSonarClient(
                    sonarQubeConfigVO.getSonarUrl(),
                    SONAR,
                    sonarQubeConfigVO.getToken());
            try {
                sonarClient.getUser().execute();
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return true;
    }
}
