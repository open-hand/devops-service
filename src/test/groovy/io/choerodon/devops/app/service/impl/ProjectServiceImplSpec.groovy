package io.choerodon.devops.app.service.impl

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.app.eventhandler.payload.ProjectPayload
import io.choerodon.devops.infra.dto.DevopsProjectDTO
import io.choerodon.devops.infra.mapper.DevopsProjectMapper
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
 * Time: 下午7:30
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(ProjectServiceImpl)
@Stepwise
class ProjectServiceImplSpec extends Specification {

    @Autowired
    private ProjectServiceImpl projectService

    @Autowired
    private DevopsProjectMapper devopsProjectMapper


//    def "CleanupData"() {
//        given:
//        // 删除project
//        List<DevopsProjectDTO> list = devopsProjectMapper.selectAll()
//        if (list != null && !list.isEmpty()) {
//            for (DevopsProjectDTO e : list) {
//                if (e.getIamProjectId() > 1L) {
//                    devopsProjectMapper.delete(e)
//                }
//            }
//        }
//    }
}
