package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.ExceptionConstants.AppServiceCode.DEVOPS_TOKEN_INVALID;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.rdupm.NexusRepositoryVO;
import io.choerodon.devops.api.vo.rdupm.NexusUserVO;
import io.choerodon.devops.app.service.AppServiceMavenVersionService;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.AppServiceVersionService;
import io.choerodon.devops.app.service.CiPipelineMavenService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceMavenVersionDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.CiPipelineMavenDTO;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.operator.RdupmClientOperator;
import io.choerodon.devops.infra.mapper.CiPipelineMavenMapper;
import io.choerodon.devops.infra.util.*;

/**
 * @author scp
 * @date 2020/7/22
 */
@Service
public class CiPipelineMavenServiceImpl implements CiPipelineMavenService {

    private static final Logger logger = LoggerFactory.getLogger(CiPipelineMavenServiceImpl.class);

    private static final String ID = "id";
    private static final String OBJECT_VERSION_NUMBER = "objectVersionNumber";
    private static final String DEVOPS_CREATE_MAVEN_RECORD = "devops.create.maven.record";
    private static final String DEVOPS_UPDATE_MAVEN_RECORD = "devops.update.maven.record";
    private static final String DEVOPS_FAILED_TO_READ_POM_FILE = "devops.failed.to.read.pom.file";
    private static final String DEVOPS_PULL_USER_AUTH_FAIL = "devops.pull.user.auth.fail";
    private static final String DEVOPS_PULL_METADATA_FAIL = "devops.pull.metadata.fail";

    @Autowired
    private CiPipelineMavenMapper ciPipelineMavenMapper;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private AppServiceMavenVersionService appServiceMavenVersionService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private RdupmClientOperator rdupmClientOperator;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createOrUpdate(CiPipelineMavenDTO ciPipelineMavenDTO) {
        CiPipelineMavenDTO oldCiPipelineMavenDTO = queryByGitlabPipelineId(ciPipelineMavenDTO.getAppServiceId(), ciPipelineMavenDTO.getGitlabPipelineId(), ciPipelineMavenDTO.getJobName());
        if (oldCiPipelineMavenDTO == null) {
            if (ciPipelineMavenMapper.insertSelective(ciPipelineMavenDTO) != 1) {
                throw new CommonException(DEVOPS_CREATE_MAVEN_RECORD);
            }
        } else {
            //拷贝的时候忽略id与乐观锁
            BeanUtils.copyProperties(ciPipelineMavenDTO, oldCiPipelineMavenDTO, ID, OBJECT_VERSION_NUMBER);
            if (ciPipelineMavenMapper.updateByPrimaryKeySelective(oldCiPipelineMavenDTO) != 1) {
                throw new CommonException(DEVOPS_UPDATE_MAVEN_RECORD);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdateJarInfo(Long nexusRepoId,
                                      Long sequence,
                                      Long gitlabPipelineId,
                                      String jobName,
                                      String token,
                                      MultipartFile file,
                                      String mavenRepoUrl,
                                      String username,
                                      String password,
                                      String version,
                                      String groupId,
                                      String artifactId,
                                      String jarVersion,
                                      String packaging) {
        ExceptionUtil.wrapExWithCiEx(() -> {
            AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(Objects.requireNonNull(token));
            if (appServiceDTO == null) {
                throw new DevopsCiInvalidException(DEVOPS_TOKEN_INVALID);
            }
            CiPipelineMavenDTO ciPipelineMavenDTO;

            if (file != null) {
                try {
                    ciPipelineMavenDTO = MavenSettingsUtil.parsePom(new String(file.getBytes(), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    throw new DevopsCiInvalidException(DEVOPS_FAILED_TO_READ_POM_FILE);
                }
            } else {
                ciPipelineMavenDTO = new CiPipelineMavenDTO();
                ciPipelineMavenDTO.setArtifactId(artifactId);
                ciPipelineMavenDTO.setGroupId(groupId);
                ciPipelineMavenDTO.setVersion(jarVersion);
                ciPipelineMavenDTO.setArtifactType(packaging);
            }

            ciPipelineMavenDTO.setAppServiceId(Objects.requireNonNull(appServiceDTO.getId()));
            ciPipelineMavenDTO.setGitlabPipelineId(Objects.requireNonNull(gitlabPipelineId));
            ciPipelineMavenDTO.setNexusRepoId(nexusRepoId);
            ciPipelineMavenDTO.setJobName(Objects.requireNonNull(jobName));
            ciPipelineMavenDTO.setMavenRepoUrl(mavenRepoUrl);
            ciPipelineMavenDTO.setUsername(username);
            ciPipelineMavenDTO.setPassword(password);
            //填充每次跑完ci后生成的准确的版本  下载maven-metadata basic登录  用户名密码要从setting里面获取

            //如果一个job里面 有多次jar上传 会只保留最新的版本
            String jarSnapshotTimestamp;
            if (nexusRepoId != null) {
                //这个job是发布maven 的job  根据jobId sequence 查询 maven setting 获取用户名密码 仓库地址等信息
                NexusRepositoryVO c7nNexusRepoDTO = rdupmClientOperator.queryRepoWithDefaultUserInfo(appServiceDTO.getProjectId(), nexusRepoId);
                logger.debug(">>>>>>>>>>>>>>>>>3. >>>>>>>>>>>>>>>>>>>>c7nNexusRepoDTO {}", JsonHelper.marshalByJackson(c7nNexusRepoDTO));
                NexusUserVO nexusUser = c7nNexusRepoDTO.getNexusUser();
                jarSnapshotTimestamp = getCustomJarSnapshotTimestamp(c7nNexusRepoDTO.getRepoUrl(),
                        nexusUser.getNeUserId(),
                        nexusUser.getNeUserPassword(),
                        ciPipelineMavenDTO);
            } else {
                jarSnapshotTimestamp = getCustomJarSnapshotTimestamp(mavenRepoUrl, DESEncryptUtil.decode(username), DESEncryptUtil.decode(password), ciPipelineMavenDTO);
            }
            //加上小版本   0.0.1-SNAPSHOT/springboot-0.0.1-20210202.063200-1.jar
            logger.debug(">>>>>>>>>>>>>>>>>5. >>>>>>>>>>>>>>>>>>>>jarSnapshotTimestamp {}", jarSnapshotTimestamp);
            if (!StringUtils.equalsIgnoreCase(jarSnapshotTimestamp, ciPipelineMavenDTO.getVersion())) {
                ciPipelineMavenDTO.setVersion(ciPipelineMavenDTO.getVersion() + BaseConstants.Symbol.SLASH + ciPipelineMavenDTO.getArtifactId() + BaseConstants.Symbol.MIDDLE_LINE + jarSnapshotTimestamp);
            }
            createOrUpdate(ciPipelineMavenDTO);
            // 判断流水线中是否包含发布应用服务版本步骤，
            AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQueryByAppServiceIdAndVersion(appServiceDTO.getId(), version);
            if (appServiceVersionDTO != null) {
                AppServiceMavenVersionDTO appServiceMavenVersionDTO = appServiceMavenVersionService.queryByAppServiceVersionId(appServiceVersionDTO.getId());
                if (appServiceMavenVersionDTO == null) {
                    appServiceMavenVersionDTO = new AppServiceMavenVersionDTO();
                    appServiceMavenVersionDTO.setAppServiceVersionId(appServiceVersionDTO.getId());
                    appServiceMavenVersionDTO.setVersion(ciPipelineMavenDTO.getVersion());
                    appServiceMavenVersionDTO.setPassword(ciPipelineMavenDTO.getPassword());
                    appServiceMavenVersionDTO.setMavenRepoUrl(ciPipelineMavenDTO.getMavenRepoUrl());
                    appServiceMavenVersionDTO.setUsername(ciPipelineMavenDTO.getUsername());
                    appServiceMavenVersionDTO.setNexusRepoId(ciPipelineMavenDTO.getNexusRepoId());
                    appServiceMavenVersionDTO.setGroupId(ciPipelineMavenDTO.getGroupId());
                    appServiceMavenVersionDTO.setArtifactId(ciPipelineMavenDTO.getArtifactId());
                    appServiceMavenVersionService.create(appServiceMavenVersionDTO);
                } else {
                    appServiceMavenVersionDTO.setVersion(ciPipelineMavenDTO.getVersion());
                    appServiceMavenVersionDTO.setPassword(ciPipelineMavenDTO.getPassword());
                    appServiceMavenVersionDTO.setMavenRepoUrl(ciPipelineMavenDTO.getMavenRepoUrl());
                    appServiceMavenVersionDTO.setUsername(ciPipelineMavenDTO.getUsername());
                    appServiceMavenVersionDTO.setNexusRepoId(ciPipelineMavenDTO.getNexusRepoId());
                    appServiceMavenVersionDTO.setGroupId(ciPipelineMavenDTO.getGroupId());
                    appServiceMavenVersionDTO.setArtifactId(ciPipelineMavenDTO.getArtifactId());
                    appServiceMavenVersionService.baseUpdate(appServiceMavenVersionDTO);
                }
            }

        });
    }

    public String getCustomJarSnapshotTimestamp(String mavenRepoUrl, String userName, String password, CiPipelineMavenDTO ciPipelineMavenDTO) {
        if (Objects.isNull(mavenRepoUrl)) {
            return ciPipelineMavenDTO.getVersion();
        }
        try {

            String basicInfo = userName + ":" + password;
            String token = "Basic " + Base64.getEncoder().encodeToString(basicInfo.getBytes());

            mavenRepoUrl = appendWithSlash(mavenRepoUrl, ciPipelineMavenDTO.getGroupId().replaceAll("\\.", BaseConstants.Symbol.SLASH));
            mavenRepoUrl = appendWithSlash(mavenRepoUrl, ciPipelineMavenDTO.getArtifactId());
            mavenRepoUrl = appendWithSlash(mavenRepoUrl, ciPipelineMavenDTO.getVersion() + "/maven-metadata.xml");
            logger.info(">>>>>>>>>>>>>>>>>>>>>>. maven-metadata.xml url is {}", mavenRepoUrl);

            OkHttpClient client = new OkHttpClient();
            //创建一个Request
            Request request = new Request.Builder()
                    .get()
                    .header("Authorization", token)
                    .url(mavenRepoUrl)
                    .build();
            //通过client发起请求
            okhttp3.Response execute = client.newCall(request).execute();
            if (execute.isSuccessful()) {
                String metadataXml = execute.body().string();
                logger.info(">>>>>>>>>>>>>>>>>>>>>>. maven-xml url is {}", metadataXml);
                String parsedVersion = MavenSnapshotLatestVersionParser.parseVersion(metadataXml);
                return parsedVersion == null ? ciPipelineMavenDTO.getVersion() : parsedVersion;
            } else if (execute.code() == HttpStatus.NOT_FOUND.value()) {
                return ciPipelineMavenDTO.getVersion();
            } else if (execute.code() == HttpStatus.UNAUTHORIZED.value()) {
                throw new CommonException(DEVOPS_PULL_USER_AUTH_FAIL);
            } else {
                throw new CommonException(DEVOPS_PULL_METADATA_FAIL);
            }

        } catch (Exception ex) {
            if (logger.isInfoEnabled()) {
                logger.info("Ex occurred when parse JarSnapshotTimestamp for {}:{}:{}", ciPipelineMavenDTO.getGroupId(), ciPipelineMavenDTO.getArtifactId(), ciPipelineMavenDTO.getVersion());
                logger.info("The ex is:", ex);
            }
            return ciPipelineMavenDTO.getVersion();
        }
    }

    private String appendWithSlash(String source, String str) {
        if (source.endsWith("/")) {
            source = source.substring(0, source.length() - 1);
        }
        if (str.startsWith("/")) {
            str = str.substring(1, str.length());
        }
        return source + BaseConstants.Symbol.SLASH + str;
    }

    @Override
    public CiPipelineMavenDTO queryByGitlabPipelineId(Long appServiceId, Long gitlabPipelineId, String jobName) {
        Assert.notNull(appServiceId, ResourceCheckConstant.DEVOPS_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(gitlabPipelineId, PipelineCheckConstant.DEVOPS_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(jobName, ResourceCheckConstant.DEVOPS_JOB_NAME_ID_IS_NULL);

        CiPipelineMavenDTO ciPipelineMavenDTO = new CiPipelineMavenDTO();
        ciPipelineMavenDTO.setGitlabPipelineId(gitlabPipelineId);
        ciPipelineMavenDTO.setAppServiceId(appServiceId);
        ciPipelineMavenDTO.setJobName(jobName);
        return ciPipelineMavenMapper.selectOne(ciPipelineMavenDTO);
    }

    @Override
    public CiPipelineMavenDTO queryPipelineLatestImage(Long appServiceId, Long gitlabPipelineId) {
        Assert.notNull(appServiceId, ResourceCheckConstant.DEVOPS_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(gitlabPipelineId, PipelineCheckConstant.DEVOPS_GITLAB_PIPELINE_ID_IS_NULL);

        return ciPipelineMavenMapper.queryPipelineLatestMaven(appServiceId, gitlabPipelineId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAppServiceId(Long appServiceId) {
        Assert.notNull(appServiceId, ResourceCheckConstant.DEVOPS_APP_SERVICE_ID_IS_NULL);

        CiPipelineMavenDTO ciPipelineMavenDTO = new CiPipelineMavenDTO();
        ciPipelineMavenDTO.setAppServiceId(appServiceId);
        ciPipelineMavenMapper.delete(ciPipelineMavenDTO);
    }
}
