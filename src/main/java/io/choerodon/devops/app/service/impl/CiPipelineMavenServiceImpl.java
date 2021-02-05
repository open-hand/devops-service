package io.choerodon.devops.app.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.Response;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiConfigTemplateVO;
import io.choerodon.devops.api.vo.CiConfigVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.CiPipelineMavenService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.CiPipelineMavenDTO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.maven.Profile;
import io.choerodon.devops.infra.dto.maven.Repository;
import io.choerodon.devops.infra.dto.maven.Server;
import io.choerodon.devops.infra.dto.maven.Settings;
import io.choerodon.devops.infra.dto.repo.C7nNexusRepoDTO;
import io.choerodon.devops.infra.enums.CiJobScriptTypeEnum;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.feign.NexusClient;
import io.choerodon.devops.infra.feign.RdupmClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.CiPipelineMavenMapper;
import io.choerodon.devops.infra.mapper.DevopsCiJobMapper;
import io.choerodon.devops.infra.mapper.DevopsCiMavenSettingsMapper;
import io.choerodon.devops.infra.util.*;

/**
 * @author scp
 * @date 2020/7/22
 */
@Service
public class CiPipelineMavenServiceImpl implements CiPipelineMavenService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String ID = "id";
    private final String OBJECT_VERSION_NUMBER = "objectVersionNumber";

    @Autowired
    private CiPipelineMavenMapper ciPipelineMavenMapper;
    @Autowired
    private AppServiceService appServiceService;

    @Autowired
    private DevopsCiMavenSettingsMapper devopsCiMavenSettingsMapper;

    @Autowired
    private DevopsCiJobMapper devopsCiJobMapper;

    @Autowired
    private RdupmClient rdupmClient;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createOrUpdate(CiPipelineMavenDTO ciPipelineMavenDTO) {
        CiPipelineMavenDTO oldCiPipelineMavenDTO = queryByGitlabPipelineId(ciPipelineMavenDTO.getGitlabPipelineId(), ciPipelineMavenDTO.getJobName());
        if (oldCiPipelineMavenDTO == null) {
            if (ciPipelineMavenMapper.insertSelective(ciPipelineMavenDTO) != 1) {
                throw new CommonException("error.create.maven.record");
            }
        } else {
            //拷贝的时候忽略id与乐观锁
            BeanUtils.copyProperties(ciPipelineMavenDTO, oldCiPipelineMavenDTO, ID, OBJECT_VERSION_NUMBER);
            if (ciPipelineMavenMapper.updateByPrimaryKeySelective(oldCiPipelineMavenDTO) != 1) {
                throw new CommonException("error.update.maven.record");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createOrUpdate(Long nexusRepoId, Long jobId, Long sequence, Long gitlabPipelineId, String jobName, String token, MultipartFile file) {
        ExceptionUtil.wrapExWithCiEx(() -> {
            AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(Objects.requireNonNull(token));
            if (appServiceDTO == null) {
                throw new DevopsCiInvalidException("error.token.invalid");
            }
            CiPipelineMavenDTO ciPipelineMavenDTO;

            try {
                ciPipelineMavenDTO = MavenSettingsUtil.parsePom(new String(file.getBytes(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new DevopsCiInvalidException("error.failed.to.read.pom.file");
            }
            ciPipelineMavenDTO.setGitlabPipelineId(Objects.requireNonNull(gitlabPipelineId));
            ciPipelineMavenDTO.setNexusRepoId(Objects.requireNonNull(nexusRepoId));
            ciPipelineMavenDTO.setJobName(Objects.requireNonNull(jobName));
            //填充每次跑完ci后生成的准确的版本  下载maven-metadata basic登录  用户名密码要从setting里面获取
            //根据jobId 拿到JOb  判断job的类型是 maven_deploy 才请求maven
            DevopsCiJobDTO devopsCiJobDTO = devopsCiJobMapper.selectByPrimaryKey(jobId);
            if (Objects.isNull(devopsCiJobDTO)) {
                throw new DevopsCiInvalidException("error.ci.job.not.exist");
            }
            CiConfigVO ciConfigVO = JsonHelper.unmarshalByJackson(devopsCiJobDTO.getMetadata(), CiConfigVO.class);
            List<CiConfigTemplateVO> ciConfigVOConfig = ciConfigVO.getConfig();
            // seq 与 type确定一个job内唯一的构建步骤CiConfigTemplateVO
            List<CiConfigTemplateVO> ciConfigTemplateVOS = ciConfigVOConfig.stream().filter(ciConfigTemplateVO -> StringUtils.equalsIgnoreCase(ciConfigTemplateVO.getType(), CiJobScriptTypeEnum.MAVEN_DEPLOY.getType())
                    && ciConfigTemplateVO.getSequence().longValue() == sequence.longValue()).collect(Collectors.toList());
            //如果一个job里面 有多次jar上传 会只保留最新的版本
            if (!CollectionUtils.isEmpty(ciConfigTemplateVOS)) {
                //这个job是发布maven 的job  根据jobId sequence 查询 maven setting 获取用户名密码 仓库地址等信息
                String queryMavenSettings = devopsCiMavenSettingsMapper.queryMavenSettings(jobId, sequence);
                // 将maven的setting文件转换为java对象
                Settings settings = (Settings) XMLUtil.convertXmlFileToObject(Settings.class, queryMavenSettings);
                //通过仓库的id 筛选出匹配的server节点和Profiles 节点
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(appServiceDTO.getProjectId());
                C7nNexusRepoDTO c7nNexusRepoDTO = rdupmClient.getMavenRepo(projectDTO.getOrganizationId(), projectDTO.getId(), nexusRepoId).getBody();

                // baseUrl=http://xxx/repository/zmf-test-mixed/ =>http://xx:17145/
                String baseUrl = null;
                Server server = null;
                String neRepositoryName = null;

                if (!Objects.isNull(c7nNexusRepoDTO)) {
                    neRepositoryName = c7nNexusRepoDTO.getNeRepositoryName();
                    String[] temp = c7nNexusRepoDTO.getUrl().split(BaseConstants.Symbol.SLASH);
                    String repo = temp[temp.length - 1];
                    if (c7nNexusRepoDTO.getUrl().endsWith("/")) {
                        c7nNexusRepoDTO.setUrl(c7nNexusRepoDTO.getUrl().substring(0, c7nNexusRepoDTO.getUrl().length() - 1));
                    }
                    baseUrl = c7nNexusRepoDTO.getUrl().replace(repo, "").replace(temp[temp.length - 2] + BaseConstants.Symbol.SLASH, "");
                    String finalNeRepositoryName = neRepositoryName;
                    server = settings.getServers().stream().filter(server1 -> StringUtils.equalsIgnoreCase(server1.getId(), finalNeRepositoryName)).collect(Collectors.toList()).get(0);
                }
                // 下载mate_date获取时间戳 0.0.1-20210203.012553-2
                String jarSnapshotTimestamp = getJarSnapshotTimestamp(baseUrl, neRepositoryName, server.getUsername(), server.getPassword(), ciPipelineMavenDTO);
                //
                //加上小版本   0.0.1-SNAPSHOT/springboot-0.0.1-20210202.063200-1.jar
                if (!StringUtils.equalsIgnoreCase(jarSnapshotTimestamp, ciPipelineMavenDTO.getVersion())) {
                    ciPipelineMavenDTO.setVersion(ciPipelineMavenDTO.getVersion() + BaseConstants.Symbol.SLASH + ciPipelineMavenDTO.getArtifactId() + BaseConstants.Symbol.MIDDLE_LINE + jarSnapshotTimestamp);
                }
            }
            createOrUpdate(ciPipelineMavenDTO);
        });
    }

    private String getJarSnapshotTimestamp(String nexusUrl, String repositoryName, String userName, String password, CiPipelineMavenDTO ciPipelineMavenDTO) {
        if (Objects.isNull(nexusUrl) || Objects.isNull(repositoryName)) {
            return ciPipelineMavenDTO.getVersion();
        }
        try {
            // 这个用scalar客户端是为了返回Callable<String>，另外一个方法的client用的Gson解析响应值，会导致响应解析出错
            // 另外这里不用nexus的list API是因为这个API返回的是乱序的
            NexusClient nexusClient2 = RetrofitHandler.getScalarNexusClient(nexusUrl, userName, password);
            Response<String> metadataXml = nexusClient2.componentMetadata(repositoryName, ciPipelineMavenDTO.getGroupId().replaceAll("\\.", BaseConstants.Symbol.SLASH), ciPipelineMavenDTO.getVersion()).execute();
            // 如果请求返回404，maven-metadata.xml不存在，说明没有多个版本
            if (metadataXml.code() == HttpStatus.NOT_FOUND.value()) {
                return ciPipelineMavenDTO.getVersion();
            }
            if (metadataXml.code() == HttpStatus.UNAUTHORIZED.value()) {
                throw new CommonException("error.pull.user.auth.fail");
            }
            String parsedVersion = MavenSnapshotLatestVersionParser.parseVersion(metadataXml.body());
            return parsedVersion == null ? ciPipelineMavenDTO.getVersion() : parsedVersion;
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ex occurred when parse JarSnapshotTimestamp for {}:{}", ciPipelineMavenDTO.getGroupId(), ciPipelineMavenDTO.getArtifactId(), ciPipelineMavenDTO.getVersion());
                logger.debug("The ex is:", ex);
            }
            return ciPipelineMavenDTO.getVersion();
        }
    }

    @Override
    public CiPipelineMavenDTO queryByGitlabPipelineId(Long gitlabPipelineId, String jobName) {
        CiPipelineMavenDTO ciPipelineMavenDTO = new CiPipelineMavenDTO();
        ciPipelineMavenDTO.setGitlabPipelineId(gitlabPipelineId);
        ciPipelineMavenDTO.setJobName(jobName);
        return ciPipelineMavenMapper.selectOne(ciPipelineMavenDTO);
    }
}
