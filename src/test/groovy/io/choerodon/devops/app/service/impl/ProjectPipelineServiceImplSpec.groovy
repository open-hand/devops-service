package io.choerodon.devops.app.service.impl

import io.choerodon.devops.IntegrationTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 18-12-2
 * Time: 下午8:26
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ProjectPipelineServiceImpl)
@Stepwise
class ProjectPipelineServiceImplSpec extends Specification {

    @Autowired
    private ProjectPipelineServiceImpl projectPipelineService

    def "GetStageTime"() {
        when: '调用方法'
        Long[] time = projectPipelineService.getStageTime(999999L)

        then: '校验返回值'
        time[0] == 0L
        time[1] == 0L
        time[2] == 16L
        time[3] == 39L
    }
}
