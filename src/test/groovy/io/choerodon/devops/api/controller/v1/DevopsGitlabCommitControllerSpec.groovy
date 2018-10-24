package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsGitlabCommitDTO
import io.choerodon.devops.domain.application.entity.iam.UserE
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.DevopsGitlabCommitDO
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.DevopsGitlabCommitMapper
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
class DevopsGitlabCommitControllerSpec extends Specification {

    private static flag = 0


    @Autowired
    private TestRestTemplate restTemplate
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

            DevopsGitlabCommitDO devopsGitlabCommitDO = new DevopsGitlabCommitDO()
            devopsGitlabCommitDO.setAppId(1L)
            devopsGitlabCommitDO.setUserId(1L)
            devopsGitlabCommitDO.setCommitSha("test")
            devopsGitlabCommitDO.setCommitContent("test")
            devopsGitlabCommitDO.setRef("test")
            devopsGitlabCommitDO.setCommitDate(new Date())
            devopsGitlabCommitMapper.insert(devopsGitlabCommitDO)
            flag = 1
        }

    }

    def "GetCommits"() {
        given:
        List<UserE> userES = new ArrayList<>()
        UserE userE = new UserE()
        userE.setLoginName("test")
        userE.setId(1L)
        userES.add(userE)
        1 * iamRepository.listUsersByIds(_) >> userES


        when:
        def devopsGitlabCommit = restTemplate.postForObject("/v1/projects/1/apps/commits?start_date=2015/10/12&end_date=3018/10/18", [1], DevopsGitlabCommitDTO.class)

        then:
        devopsGitlabCommit != null
    }

    def "GetRecordCommits"() {
        given:
        List<UserE> userES = new ArrayList<>()
        UserE userE = new UserE()
        userE.setLoginName("test")
        userE.setId(1L)
        userES.add(userE)
        1 * iamRepository.listUsersByIds(_) >> userES


        when:
        def pages = restTemplate.postForObject("/v1/projects/1/apps/commits/record?page=0&size=5&start_date=2015/10/12&end_date=3018/10/18", [1], Page.class)

        then:

        pages.size() == 1
        applicationMapper.deleteByPrimaryKey(1L)
        devopsGitlabCommitMapper.deleteByPrimaryKey(1L)

    }
}
