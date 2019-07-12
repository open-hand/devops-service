package io.choerodon.devops.app.service.impl

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.eventhandler.payload.GitlabUserPayload
import io.choerodon.devops.app.eventhandler.payload.OrganizationEventPayload
<<<<<<< HEAD

import io.choerodon.devops.infra.dataobject.UserAttrDTO
=======
>>>>>>> [IMP]修复后端结构
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO
import io.choerodon.devops.infra.dataobject.gitlab.UserDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.mapper.UserAttrMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 18-12-2
 * Time: 下午6:37
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(OrganizationServiceImpl)
@Stepwise
class OrganizationServiceImplSpec extends Specification {
    @Autowired
    private OrganizationServiceImpl organizationService

    @Autowired
    private UserAttrMapper userAttrMapper

    @Autowired
    private GitlabRepository gitlabRepository
    @Autowired
    private GitlabUserRepository gitlabUserRepository

    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

    def setup() {
        DependencyInjectUtil.setAttribute(gitlabRepository,"gitlabServiceClient",gitlabServiceClient)
        gitlabUserRepository.initMockService(gitlabServiceClient)

        GroupDO groupDO = new GroupDO()
        ResponseEntity<GroupDO> responseEntity = new ResponseEntity<>(groupDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createGroup(any(GroupDO.class), anyInt())).thenReturn(responseEntity)

        UserDO userDO = new UserDO()
        userDO.setId(2)
        ResponseEntity<UserDO> responseEntity1 = new ResponseEntity<>(userDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createUser(anyString(), anyInt(), any(GitlabUserPayload.class))).thenReturn(responseEntity1)
        Mockito.when(gitlabServiceClient.queryUserByUserName(anyString())).thenReturn(responseEntity1)

    }

    def "Create"() {
        given: '初始化OrganizationEventPayload'
        OrganizationEventPayload organizationEventPayload = new OrganizationEventPayload()
        organizationEventPayload.setUserId(1L)
        organizationEventPayload.setCode("payload")

        when: '调用方法'
        organizationService.create(organizationEventPayload)

        then: '无返回值'
        noExceptionThrown()
    }


    def "CleanupData"() {
        given:
        // 删除user
        List<UserAttrDTO> list = userAttrMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (UserAttrDTO e : list) {
                if (e.getIamUserId() > 1L) {
                    userAttrMapper.delete(e)
                }
            }
        }
    }
}
