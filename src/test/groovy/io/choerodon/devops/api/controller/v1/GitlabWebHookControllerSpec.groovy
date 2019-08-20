package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.asgard.saga.feign.SagaClient
import io.choerodon.asgard.saga.feign.SagaClientCallback
import io.choerodon.asgard.saga.producer.TransactionalProducer
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.service.AgentCommandService
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.app.service.DevopsGitlabPipelineService
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.infra.dto.*
import io.choerodon.devops.infra.dto.gitlab.CommitDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.handler.ClusterConnectionHandler
import io.choerodon.devops.infra.mapper.*
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.Matchers.anyString
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 *
 * @author zmf
 *
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(GitlabWebHookController)
@Stepwise
class GitlabWebHookControllerSpec extends Specification {
    private static final String BASE_URL = "/webhook"
    private static final String token = "b5eff928-e0cd-4279-abcf-7908e0922dc6"

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper
    @Autowired
    private DevopsBranchMapper devopsBranchMapper
    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private DevopsEnvCommitMapper devopsEnvCommitMapper

    @Autowired
    private DevopsGitlabPipelineService devopsGitlabPipelineService
    @Autowired
    private IamService iamRepository
    @Autowired
    private DevopsGitService devopsGitRepository
    @Autowired
    private AgentCommandService deployService
    @Autowired
    @Qualifier("mockClusterConnectionHandler")
    private ClusterConnectionHandler mockEnvUtil
    @Autowired
    private DevopsGitService devopsGitService
    
    private BaseServiceClient iamServiceClient = Mockito.mock(BaseServiceClient)
    private GitlabServiceClient mockGitlabServiceClient = Mockito.mock(GitlabServiceClient)
//    private CommandSender mockCommandSender = Mockito.mock(CommandSender)

    @Shared
    private AppServiceDTO applicationDO = new AppServiceDTO()
    @Shared
    private DevopsEnvironmentDTO devopsEnvironmentDO = new DevopsEnvironmentDTO()
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false

    def setup() {
        if (isToInit) {
            // dependency injection
            DependencyInjectUtil.setAttribute(iamRepository, "baseServiceClient", iamServiceClient)
            DependencyInjectUtil.setAttribute(devopsGitRepository, "gitlabServiceClient", mockGitlabServiceClient)
            DependencyInjectUtil.setAttribute(deployService, "commandSender", mockCommandSender)
            DependencyInjectUtil.setAttribute(deployService, "envUtil", mockEnvUtil)

            // do preparation
            applicationDO.setToken(token)
            applicationDO.setProjectId(1L)
            applicationDO.setGitlabProjectId(1)
            applicationMapper.insert(applicationDO)

            devopsEnvironmentDO.setToken(token)
            devopsEnvironmentDO.setName("env-test")
            devopsEnvironmentDO.setCode("env-test-webhook")
            devopsEnvironmentMapper.insert(devopsEnvironmentDO)

            // mock baseList user
            IamUserDTO userDO = new IamUserDTO()
            userDO.setId(1L)
            PageInfo<IamUserDTO> page = new PageInfo<>(Collections.singletonList(userDO) as List<IamUserDTO>)
            ResponseEntity<PageInfo<IamUserDTO>> responseEntity = new ResponseEntity<>(page, HttpStatus.OK)
            Mockito.when(iamServiceClient.listUsersByEmail(Mockito.anyLong(), anyInt(), anyInt(), anyString())).thenReturn(responseEntity)

            // mock get commit
            CommitDTO commit = new CommitDTO()
            commit.setMessage("message")
            commit.setAuthorName("zzz")
            commit.setId("asdfsdfasdfsdfsdfadf")
            commit.setAuthorEmail("1332efas@163.mail.com")
            commit.setUrl("http://www.baidu.com")
            commit.setCommittedDate(new Date())
            ResponseEntity<CommitDTO> res = new ResponseEntity<>(commit, HttpStatus.OK)
            Mockito.when(mockGitlabServiceClient.queryCommit(anyInt(), anyString(), anyInt())).thenReturn(res)

            // mock env util
            Mockito.when(mockEnvUtil.getConnectedEnvList()).thenReturn(Arrays.asList(1L))
        }
    }

    def cleanup() {
        if (isToClean) {
            // reset dependency injection
            DependencyInjectUtil.restoreDefaultDependency(iamRepository, "baseServiceClient")
            DependencyInjectUtil.restoreDefaultDependency(devopsGitRepository, "gitlabServiceClient")
            DependencyInjectUtil.restoreDefaultDependency(deployService, "commandSender")
            DependencyInjectUtil.restoreDefaultDependency(deployService, "envUtil")

            applicationMapper.delete(applicationDO)
            devopsEnvironmentMapper.delete(devopsEnvironmentDO)
            devopsGitlabPipelineMapper.selectAll().forEach { devopsGitlabPipelineMapper.delete(it) }
            devopsGitlabCommitMapper.selectAll().forEach { devopsGitlabCommitMapper.delete(it) }
            devopsBranchMapper.selectAll().forEach { devopsBranchMapper.delete(it) }
            devopsMergeRequestMapper.selectAll().forEach { devopsMergeRequestMapper.delete(it) }
            devopsEnvCommitMapper.selectAll().forEach { devopsEnvCommitMapper.delete(it) }
        }
    }

    def "forward push webhook"() {
        given: "准备数据"
        isToInit = false
        String body = "{\"object_kind\":\"push\",\"event_name\":\"push\",\"before\":\"0000000000000000000000000000000000000000\",\"after\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"ref\":\"refs/heads/feature-C7NCD-1756\",\"checkout_sha\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":null,\"user_id\":10256,\"user_name\":\"zmf\",\"user_username\":\"20610\",\"user_email\":\"mofang.zheng@test.com\",\"user_avatar\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\",\"project_id\":237,\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"commits\":[{\"id\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":\"[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\\n\",\"timestamp\":\"2018-12-25T10:33:28+08:00\",\"url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service/commit/c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"author\":{\"name\":\"zmf\",\"email\":\"1984654893@qq.com\"},\"added\":[],\"modified\":[\"src/main/java/io/choerodon/devops/app/service/impl/DevopsEnvPodServiceImpl.java\",\"src/test/groovy/io/choerodon/devops/api/controller/v1/DevopsEnvPodControllerSpec.groovy\"],\"removed\":[]}],\"total_commits_count\":1,\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"visibility_level\":0}  }"

        def requestEntity = createEntity(body)

        def validation = new DevopsGitlabCommitDTO()
        validation.setCommitSha("c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b")

        when: "调用方法"
        def entity = restTemplate.postForEntity(BASE_URL, requestEntity, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        devopsGitlabCommitMapper.selectOne(validation) != null

        when: "准备没有commit的push数据并调用方法"
        body = "{\"object_kind\":\"push\",\"event_name\":\"push\",\"before\":\"0000000000000000000000000000000000000000\",\"after\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"ref\":\"refs/heads/feature-C7NCD-1757\",\"checkout_sha\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":null,\"user_id\":10256,\"user_name\":\"zmf\",\"user_username\":\"20610\",\"user_email\":\"mofang.zheng@test.com\",\"user_avatar\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\",\"project_id\":237,\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"commits\":[],\"total_commits_count\":0,\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"visibility_level\":0}}"

        requestEntity = createEntity(body)

        validation = new DevopsGitlabCommitDTO()
        validation.setCommitSha("asdfsdfasdfsdfsdfadf")

        entity = restTemplate.postForEntity(BASE_URL, requestEntity, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        devopsGitlabCommitMapper.selectOne(validation) != null

        when: "准备更新分支commit"
        body = "{\"object_kind\":\"push\",\"event_name\":\"push\",\"before\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"after\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e86\",\"ref\":\"refs/heads/feature-C7NCD-1757\",\"checkout_sha\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":null,\"user_id\":10256,\"user_name\":\"zmf\",\"user_username\":\"20610\",\"user_email\":\"mofang.zheng@test.com\",\"user_avatar\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\",\"project_id\":237,\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"commits\":[],\"total_commits_count\":0,\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"visibility_level\":0}}"

        requestEntity = createEntity(body)

        def branchValidation = new DevopsBranchDTO()
        branchValidation.setLastCommit("c10c5ec88b6e1a8a48cf213dd88058b3e9741e86")

        entity = restTemplate.postForEntity(BASE_URL, requestEntity, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        devopsBranchMapper.selectOne(branchValidation) != null

        when: "准备删除分支"
        body = "{\"object_kind\":\"push\",\"event_name\":\"push\",\"before\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"after\":\"0000000000000000000000000000000000000000\",\"ref\":\"refs/heads/feature-C7NCD-1757\",\"checkout_sha\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":null,\"user_id\":10256,\"user_name\":\"zmf\",\"user_username\":\"20610\",\"user_email\":\"mofang.zheng@test.com\",\"user_avatar\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\",\"project_id\":237,\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"commits\":[],\"total_commits_count\":0,\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"visibility_level\":0}}"

        requestEntity = createEntity(body)

        branchValidation = new DevopsBranchDTO()
        branchValidation.setBranchName("feature-C7NCD-1757")

        entity = restTemplate.postForEntity(BASE_URL, requestEntity, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        devopsBranchMapper.selectOne(branchValidation).getDeleted() != null
        devopsBranchMapper.selectOne(branchValidation).getDeleted()

        when: "准备unknown的webhook"
        body = "{\"object_kind\":\"unknown\",\"event_name\":\"push\"}"
        requestEntity = createEntity(body)
        entity = restTemplate.postForEntity(BASE_URL, requestEntity, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
    }

    // webhook转发
    def "ForwardGitlabWebHook Pipeline"() {
        given: "准备PileLine数据"
        String body = "{\"object_kind\":\"pipeline\",\"object_attributes\":{\"id\":54192,\"ref\":\"master\",\"tag\":false,\"sha\":\"b77fceb9aab97e2a8dbacb8ef69686368fe8f7a1\",\"before_sha\":\"bae263786ff7009c1bfabe65baf5e575f68041cd\",\"status\":\"failed\",\"detailed_status\":\"failed\",\"stages\":[\"mvn-package\",\"docker-build\",\"external\"],\"created_at\":\"2018-12-25 10:55:26 UTC\",\"finished_at\":\"2018-12-25 11:04:56 UTC\",\"duration\":317},\"user\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"},\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null},\"commit\":{\"id\":\"b77fceb9aab97e2a8dbacb8ef69686368fe8f7a1\",\"message\":\"Merge branch 'feature-C7NCD-1756' into 'master'\\n\\n[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\\n\\nSee merge request choerodon-c7ncd/devops-service!302\",\"timestamp\":\"2018-12-25T02:55:23Z\",\"url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service/commit/b77fceb9aab97e2a8dbacb8ef69686368fe8f7a1\",\"author\":{\"name\":\"zmf\",\"email\":\"mofang.zheng@test.com\"}},\"builds\":[{\"id\":101344,\"stage\":\"docker-build\",\"name\":\"docker-build\",\"status\":\"success\",\"created_at\":\"2018-12-25 10:55:26 UTC\",\"started_at\":\"2018-12-25 11:04:35 UTC\",\"finished_at\":\"2018-12-25 11:04:56 UTC\",\"when\":\"on_success\",\"manual\":false,\"user\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"},\"runner\":{\"id\":1,\"description\":\"choerodon-runner\",\"active\":true,\"is_shared\":true},\"artifacts_file\":{\"filename\":null,\"size\":0}},{\"id\":101343,\"stage\":\"mvn-package\",\"name\":\"maven-test-build\",\"status\":\"success\",\"created_at\":\"2018-12-25 10:55:26 UTC\",\"started_at\":\"2018-12-25 10:59:30 UTC\",\"finished_at\":\"2018-12-25 11:04:26 UTC\",\"when\":\"on_success\",\"manual\":false,\"user\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"},\"runner\":{\"id\":1,\"description\":\"choerodon-runner\",\"active\":true,\"is_shared\":true},\"artifacts_file\":{\"filename\":null,\"size\":1671812}}]}"

        def requestEntity = createEntity(body)

        when: "调用方法"
        def entity = restTemplate.postForEntity(BASE_URL, requestEntity, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
    }

    def "webhook forword build"() {
        given: "准备build请求数据"
        def body = "{\"object_kind\":\"build\",\"ref\":\"master\",\"tag\":false,\"before_sha\":\"bae263786ff7009c1bfabe65baf5e575f68041cd\",\"sha\":\"b77fceb9aab97e2a8dbacb8ef69686368fe8f7a1\",\"build_id\":101344,\"build_name\":\"docker-build\",\"build_stage\":\"docker-build\",\"build_status\":\"created\",\"build_started_at\":null,\"build_finished_at\":null,\"build_duration\":null,\"build_allow_failure\":false,\"project_id\":237,\"project_name\":\"Choerodon-Choerodon持续交付 / devops-service\",\"user\":{\"id\":10256,\"name\":\"zmf\",\"email\":\"mofang.zheng@test.com\"},\"commit\":{\"id\":54192,\"sha\":\"b77fceb9aab97e2a8dbacb8ef69686368fe8f7a1\",\"message\":\"Merge branch 'feature-C7NCD-1756' into 'master'\\n\\n[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\\n\\nSee merge request choerodon-c7ncd/devops-service!302\",\"author_name\":\"zmf\",\"author_email\":\"mofang.zheng@test.com\",\"author_url\":\"https://code.choerodon.com.cn/20610\",\"status\":\"created\",\"duration\":null,\"started_at\":null,\"finished_at\":null},\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"visibility_level\":0}  }"

        def requestEntity = createEntity(body)

        when: "调用方法"
        def entity = restTemplate.postForEntity(BASE_URL, requestEntity, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
    }

    def "webhook forword merge_request"() {
        given: "准备merge request请求数据"
        String body = "{\"object_kind\":\"merge_request\",\"event_type\":\"merge_request\",\"user\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"},\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"object_attributes\":{\"assignee_id\":10256,\"author_id\":10256,\"created_at\":\"2018-12-25 10:34:24 +0800\",\"description\":\"\",\"head_pipeline_id\":54187,\"id\":19459,\"iid\":302,\"last_edited_at\":null,\"last_edited_by_id\":null,\"merge_commit_sha\":null,\"merge_error\":null,\"merge_params\":{\"force_remove_source_branch\":\"1\"},\"merge_status\":\"unchecked\",\"merge_user_id\":null,\"merge_when_pipeline_succeeds\":false,\"milestone_id\":null,\"source_branch\":\"feature-C7NCD-1756\",\"source_project_id\":237,\"state\":\"opened\",\"target_branch\":\"master\",\"target_project_id\":237,\"time_estimate\":0,\"title\":\"[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\",\"updated_at\":\"2018-12-25 10:34:25 +0800\",\"updated_by_id\":null,\"url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service/merge_requests/302\",\"source\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"target\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"last_commit\":{\"id\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":\"[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\\n\",\"timestamp\":\"2018-12-25T10:33:28+08:00\",\"url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service/commit/c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"author\":{\"name\":\"zmf\",\"email\":\"1984654893@qq.com\"}},\"work_in_progress\":false,\"total_time_spent\":0,\"human_total_time_spent\":null,\"human_time_estimate\":null,\"action\":\"open\"},\"labels\":[],\"changes\":{\"head_pipeline_id\":{\"previous\":null,\"current\":54187},\"updated_at\":{\"previous\":\"2018-12-25 10:34:24 +0800\",\"current\":\"2018-12-25 10:34:25 +0800\"},\"assignee\":{\"previous\":null,\"current\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"}},\"total_time_spent\":{\"previous\":null,\"current\":0}},\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\"},\"assignee\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"}}"
        def entity = createEntity(body)

        def validation = new DevopsMergeRequestDTO()
        validation.setSourceBranch("feature-C7NCD-1756")
        validation.setTargetBranch("master")

        when: "调用方法"
        restTemplate.postForEntity(BASE_URL, entity, Object)

        then: "校验结果"
        devopsMergeRequestMapper.selectOne(validation) != null
        devopsMergeRequestMapper.selectOne(validation).getState() == "opened"


        when: "更新merge request"
        body = "{\"object_kind\":\"merge_request\",\"event_type\":\"merge_request\",\"user\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"},\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"object_attributes\":{\"assignee_id\":10256,\"author_id\":10256,\"created_at\":\"2018-12-25 10:34:24 UTC\",\"description\":\"\",\"head_pipeline_id\":54187,\"id\":19459,\"iid\":302,\"last_edited_at\":null,\"last_edited_by_id\":null,\"merge_commit_sha\":\"b77fceb9aab97e2a8dbacb8ef69686368fe8f7a1\",\"merge_error\":null,\"merge_params\":{\"force_remove_source_branch\":\"1\",\"should_remove_source_branch\":true,\"commit_message\":\"Merge branch 'feature-C7NCD-1756' into 'master'\\n\\n[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\\n\\nSee merge request choerodon-c7ncd/devops-service!302\",\"squash\":false},\"merge_status\":\"can_be_merged\",\"merge_user_id\":10256,\"merge_when_pipeline_succeeds\":true,\"milestone_id\":null,\"source_branch\":\"feature-C7NCD-1756\",\"source_project_id\":237,\"state\":\"merged\",\"target_branch\":\"master\",\"target_project_id\":237,\"time_estimate\":0,\"title\":\"[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\",\"updated_at\":\"2018-12-25 10:55:26 UTC\",\"updated_by_id\":null,\"url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service/merge_requests/302\",\"source\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"target\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"last_commit\":{\"id\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":\"[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\\n\",\"timestamp\":\"2018-12-25T10:33:28+08:00\",\"url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service/commit/c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"author\":{\"name\":\"zmf\",\"email\":\"1984654893@qq.com\"}},\"work_in_progress\":false,\"total_time_spent\":0,\"human_total_time_spent\":null,\"human_time_estimate\":null,\"action\":\"merge\"},\"labels\":[],\"changes\":{\"state\":{\"previous\":\"locked\",\"current\":\"merged\"},\"updated_at\":{\"previous\":\"2018-12-25 10:55:25 UTC\",\"current\":\"2018-12-25 10:55:26 UTC\"},\"assignee\":{\"previous\":null,\"current\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"}},\"total_time_spent\":{\"previous\":null,\"current\":0}},\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\"},\"assignee\":{\"name\":\"zmf\",\"username\":\"20610\",\"avatar_url\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\"}}"
        entity = createEntity(body)

        restTemplate.postForEntity(BASE_URL, entity, Object)

        then: "校验结果"
        devopsMergeRequestMapper.selectOne(validation) != null
        devopsMergeRequestMapper.selectOne(validation).getState() == "merged"
    }

    def "Forward push tag"() {
        given: "准备数据"
        String body = "{\"object_kind\":\"tag_push\",\"event_name\":\"tag_push\",\"before\":\"0000000000000000000000000000000000000000\",\"after\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8c\",\"ref\":\"refs/heads/feature-C7NCD-1756\",\"checkout_sha\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":null,\"user_id\":10256,\"user_name\":\"zmf\",\"user_username\":\"20610\",\"user_email\":\"mofang.zheng@test.com\",\"user_avatar\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\",\"project_id\":237,\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"commits\":[],\"total_commits_count\":0,\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"visibility_level\":0}}"

        def requestEntity = createEntity(body)

        def validation = new DevopsGitlabCommitDTO()
        validation.setCommitSha("c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b")

        when: "调用方法"
        def entity = restTemplate.postForEntity(BASE_URL, requestEntity, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        devopsGitlabCommitMapper.selectOne(validation) != null
    }

    def "forward gitops webhook"() {
        given: "准备数据"
        String body = "{\"object_kind\":\"push\",\"event_name\":\"push\",\"before\":\"0000000000000000000000000000000000000000\",\"after\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"ref\":\"refs/heads/feature-C7NCD-1756\",\"checkout_sha\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":null,\"user_id\":10256,\"user_name\":\"郑膜坊\",\"user_username\":\"20610\",\"user_email\":\"mofang.zheng@test.com\",\"user_avatar\":\"https://code.choerodon.com.cn/uploads/-/system/user/avatar/10256/avatar.png\",\"project_id\":237,\"project\":{\"id\":237,\"name\":\"devops-service\",\"description\":\"\",\"web_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"avatar_url\":null,\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"namespace\":\"Choerodon-Choerodon持续交付\",\"visibility_level\":0,\"path_with_namespace\":\"choerodon-c7ncd/devops-service\",\"default_branch\":\"master\",\"ci_config_path\":null,\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\"},\"commits\":[{\"id\":\"c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"message\":\"[IMP] 多容器返回在同一个Pod中的时候将不可用的容器排列在靠前\\n\",\"timestamp\":\"2018-12-25T10:33:28+08:00\",\"url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service/commit/c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b\",\"author\":{\"name\":\"zmf\",\"email\":\"1984654893@qq.com\"},\"added\":[],\"modified\":[\"src/main/java/io/choerodon/devops/app/service/impl/DevopsEnvPodServiceImpl.java\",\"src/test/groovy/io/choerodon/devops/api/controller/v1/DevopsEnvPodControllerSpec.groovy\"],\"removed\":[]}],\"total_commits_count\":1,\"repository\":{\"name\":\"devops-service\",\"url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"description\":\"\",\"homepage\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service\",\"git_http_url\":\"https://code.choerodon.com.cn/choerodon-c7ncd/devops-service.git\",\"git_ssh_url\":\"git@code.choerodon.com.cn:choerodon-c7ncd/devops-service.git\",\"visibility_level\":0}  }"
        def url = BASE_URL + "/git_ops"

        def request = createEntity(body)
        def validation = new DevopsEnvCommitDTO()
        validation.setCommitSha("c10c5ec88b6e1a8a48cf213dd88058b3e9741e8b")

        when: "调用方法"
        def entity = restTemplate.postForEntity(url, request, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        devopsEnvCommitMapper.selectOne(validation) != null
    }



    // 查询自动化测试应用实例状态
    def "get test status"() {
        given: "准备数据"
        def url = BASE_URL + "/get_test_status"
        Map<Long, List<String>> request = new HashMap<>()
        request.put(1L, Arrays.asList("name", "test"))

        when: "调用方法"
        def entity = restTemplate.postForEntity(url, request, Object)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
    }

    // 清理现场
    def "clean stage"() {
        given: "设置标志位"
        isToClean = true
    }

    private static HttpEntity<Object> createEntity(Object body) {
        HttpHeaders headers = new HttpHeaders()
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json")
        headers.add("X-Gitlab-Token", token)

        return new HttpEntity<Object>(body, headers)
    }
}
