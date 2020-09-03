package io.choerodon.devops.infra.mapper

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.infra.dto.DevopsCiContentDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class DevopsCiContentMapperSpec extends Specification {

    @Autowired
    DevopsCiContentMapper devopsCiContentMapper;

    def "QueryLatestContent"() {
        given: "构造参数"
        def ciFile = "test ci file"
        def ciContentDTO = new DevopsCiContentDTO()
        ciContentDTO.setCiPipelineId(1)
        ciContentDTO.setCiContentFile(ciFile)
        devopsCiContentMapper.insert(ciContentDTO)
        when: "查询最新的ci文件"
        String result = devopsCiContentMapper.queryLatestContent(1)
        then: "校验结果"
        result == ciFile
    }
}
