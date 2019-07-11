package io.choerodon.devops.infra.persistence.impl


import io.choerodon.devops.IntegrationTestConfiguration

import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.feign.IamServiceClient
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class IamRepositoryImplSpec extends Specification {

    IamRepository iamRepository

    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)


    def "QueryOrganization"() {
        given:
        iamRepository = new IamRepositoryImpl(iamServiceClient)
        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        ResponseEntity<OrganizationDO> organization = new ResponseEntity<>(organizationDO,HttpStatus.OK)
        Mockito.doReturn(organization).when(iamServiceClient).queryOrganization()

        when:
        def newOrganization = iamRepository.queryOrganization()

        then:
        newOrganization.getId()==1L
    }
}
