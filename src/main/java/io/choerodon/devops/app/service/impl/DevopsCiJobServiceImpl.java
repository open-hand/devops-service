package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.SonarQubeConfigVO;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.DevopsCiJobArtifactRecordDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.JobDTO;
import io.choerodon.devops.infra.enums.SonarAuthType;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.FileFeignClient;
import io.choerodon.devops.infra.feign.SonarClient;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.DevopsCiJobArtifactRecordMapper;
import io.choerodon.devops.infra.mapper.DevopsCiJobMapper;
import io.choerodon.devops.infra.mapper.DevopsCiMavenSettingsMapper;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:24
 */
@Service
public class DevopsCiJobServiceImpl implements DevopsCiJobService {
    private static final Pattern ARTIFACT_NAME_PATTERN = Pattern.compile("[0-9a-zA-Z._-]{6,30}");

    private static final String CREATE_JOB_FAILED = "create.job.failed";
    private static final String DELETE_JOB_FAILED = "delete.job.failed";
    private static final String ERROR_STAGE_ID_IS_NULL = "error.stage.id.is.null";
    private static final String ERROR_PIPELINE_ID_IS_NULL = "error.pipeline.id.is.null";
    private static final String ERROR_UPLOAD_ARTIFACT_TO_MINIO = "error.upload.file.to.minio";
    private static final String SONAR_KEY = "%s-%s:%s";
    private static final String SONAR = "sonar";

    /**
     * ci的上传的最大文件字节数，默认200 * 1024 * 1024
     */
    @Value("${ci.max.file.bytes:209715200}")
    private Long maxFileSize;

    private DevopsCiJobMapper devopsCiJobMapper;
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    private UserAttrService userAttrService;
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;
    private FileFeignClient fileFeignClient;
    private DevopsCiJobArtifactRecordMapper devopsCiJobArtifactRecordMapper;

    public DevopsCiJobServiceImpl(DevopsCiJobMapper devopsCiJobMapper,
                                  GitlabServiceClientOperator gitlabServiceClientOperator,
                                  UserAttrService userAttrService,
                                  FileFeignClient fileFeignClient,
                                  DevopsCiJobArtifactRecordMapper devopsCiJobArtifactRecordMapper,
                                  DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper) {
        this.devopsCiJobMapper = devopsCiJobMapper;
        this.gitlabServiceClientOperator = gitlabServiceClientOperator;
        this.userAttrService = userAttrService;
        this.devopsCiMavenSettingsMapper = devopsCiMavenSettingsMapper;
        this.devopsCiJobArtifactRecordMapper = devopsCiJobArtifactRecordMapper;
        this.fileFeignClient = fileFeignClient;
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
        return true;
    }

    @Override
    public String queryTrace(Long gitlabProjectId, Long jobId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId().longValue());
        return gitlabServiceClientOperator.queryTrace(gitlabProjectId.intValue(), jobId.intValue(), userAttrDTO.getGitlabUserId().intValue());
    }

    @Override
    public JobDTO retryJob(Long gitlabProjectId, Long jobId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(GitUserNameUtil.getUserId().longValue());
        return gitlabServiceClientOperator.retryJob(gitlabProjectId.intValue(), jobId.intValue(), userAttrDTO.getGitlabUserId().intValue());
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long ciPipelineId) {
        if (ciPipelineId == null) {
            throw new CommonException(ERROR_PIPELINE_ID_IS_NULL);
        }
        DevopsCiJobDTO devopsCiJobDTO = new DevopsCiJobDTO();
        devopsCiJobDTO.setCiPipelineId(ciPipelineId);
        devopsCiJobMapper.delete(devopsCiJobDTO);
    }

    @Override
    public String queryMavenSettings(Long projectId, Long jobId, Long sequence) {
        return devopsCiMavenSettingsMapper.queryMavenSettings(jobId, sequence);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void uploadArtifact(String token, String commit, Long ciPipelineId, Long ciJobId, String artifactName, MultipartFile file) {
        // 这个方法暂时用不到的字段留待后用

        if (!ARTIFACT_NAME_PATTERN.matcher(artifactName).matches()) {
            throw new DevopsCiInvalidException("error.artifact.name.invalid", artifactName);
        }
        if (file.getSize() > maxFileSize) {
            throw new DevopsCiInvalidException("error.artifact.too.big", file.getSize(), maxFileSize);
        }
        try {
            // 存到文件服务器的文件名
            String fileName = String.format(GitOpsConstants.CI_JOB_ARTIFACT_NAME_TEMPLATE, ciPipelineId, artifactName);
            ResponseEntity<String> response = fileFeignClient.uploadFile(GitOpsConstants.DEV_OPS_CI_ARTIFACT_FILE_BUCKET, fileName, file);
            if (response == null) {
                throw new DevopsCiInvalidException(ERROR_UPLOAD_ARTIFACT_TO_MINIO);
            }

            // 插入纪录到数据库
            String artifactUrl = response.getBody();
            DevopsCiJobArtifactRecordDTO devopsCiJobArtifactRecordDTO = new DevopsCiJobArtifactRecordDTO(ciPipelineId, artifactName, artifactUrl);
            MapperUtil.resultJudgedInsert(devopsCiJobArtifactRecordMapper, devopsCiJobArtifactRecordDTO, "error.insert.artifact.record");
        } catch (CommonException e) {
            throw new DevopsCiInvalidException(e.getCode(), e.getCause());
        } catch (DevopsCiInvalidException e) {
            throw e;
        } catch (Exception e) {
            throw new DevopsCiInvalidException(ERROR_UPLOAD_ARTIFACT_TO_MINIO, e);
        }
    }
}
