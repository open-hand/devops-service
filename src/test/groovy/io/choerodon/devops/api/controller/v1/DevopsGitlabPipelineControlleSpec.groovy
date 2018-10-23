package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.PipelineFrequencyDTO
import io.choerodon.devops.api.dto.PipelineTimeDTO
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.iam.UserE
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper
import io.choerodon.devops.infra.mapper.DevopsGitlabPipelineMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsGitlabPipelineControlleSpec extends Specification {


    private static flag = 0


    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper
    @Autowired
    private DevopsGitlabCommitMapper devopsGitlabCommitMapper

    @Autowired
    private ApplicationMapper applicationMapper

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    void setup() {
        if (flag == 0) {

            ApplicationDO applicationDO = new ApplicationDO()
            applicationDO.setId(1L)
            applicationDO.setProjectId(1L)
            applicationDO.setActive(true)
            applicationDO.setCode("test")
            applicationDO.setName("test")
            applicationDO.setGitlabProjectId(1)
            applicationMapper.insert(applicationDO)


            DevopsGitlabPipelineDO devopsGitlabPipelineDO = new DevopsGitlabPipelineDO()
            devopsGitlabPipelineDO.setAppId(1L)
            devopsGitlabPipelineDO.setPipelineId(1L)
            devopsGitlabPipelineDO.setPipelineCreateUserId(1L)
            devopsGitlabPipelineDO.setCommitId(1L)
            devopsGitlabPipelineDO.setPipelineCreationDate(new Date())
            devopsGitlabPipelineDO.setStatus("passed")
            devopsGitlabPipelineMapper.insert(devopsGitlabPipelineDO);

            flag = 1
        }

    }

    def "ListPipelineTime"() {
        given:

        when:
        def pipelineTimeDTO = restTemplate.getForObject("/v1/projects/1/pipeline/time?appId=1&startTime=2015/10/12&endTime=3018/10/18", PipelineTimeDTO.class)

        then:
        pipelineTimeDTO.getRefs().size() != 0
    }

    def "ListPipelineFrequency"() {
        given:

        when:
        def pipelineFrequencyDTO = restTemplate.getForObject("/v1/projects/1/pipeline/frequency?appId=1&startTime=2015/10/12&endTime=3018/10/18", PipelineFrequencyDTO.class)

        then:
        pipelineFrequencyDTO.getPipelineFrequencys().size() != 0
    }

    def "PagePipeline"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")
        ProjectE projectE = initProj(1L, "testProject", organization)
        UserE userE = new UserE()
        userE.setLoginName("test")
        userE.setId(1L)
        userE.setRealName("test")
        userE.setImageUrl("test")
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        2 * iamRepository.queryUserByUserId(_) >> userE


        when:
        def pages = restTemplate.getForObject("/v1/projects/384/pipeline/page?appId=1&startTime=2015/10/13&endTime=3018/10/19&page=0&size=10", Page.class)

        then:
        pages.size() == 1
        applicationMapper.deleteByPrimaryKey(1L)
        devopsGitlabPipelineMapper.deleteByPrimaryKey(1L)

    }


    private static Organization initOrg(Long id, String code) {
        Organization organization = new Organization()
        organization.setId(id)
        organization.setCode(code)
        organization
    }

    private static ProjectE initProj(Long id, String code, Organization organization) {
        ProjectE projectE = new ProjectE()
        projectE.setId(id)
        projectE.setCode(code)
        projectE.setOrganization(organization)
        projectE
    }
}
