package io.choerodon.devops.infra.util

import io.choerodon.devops.IntegrationTestConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class HumpToUnderlineUtilSpec extends Specification {

    @Unroll
    def "ToUnderLine"() {
        expect: "测试驼峰转下划线"
        HumpToUnderlineUtil.toUnderLine(camelCase) == underline

        where: ""
        camelCase << ["", null, "ciPipeline", "cdPipeline"]
        underline << ["", null, "ci_pipeline", "cd_pipeline"]
    }
}
