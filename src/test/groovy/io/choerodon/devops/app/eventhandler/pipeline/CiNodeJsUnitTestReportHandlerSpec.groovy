package io.choerodon.devops.app.eventhandler.pipeline

import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification
import spock.lang.Subject

@Subject(CiMavenUnitTestReportHandler)
class CiNodeJsUnitTestReportHandlerSpec extends Specification {

    def "AnalyseReport"() {
        MultipartFile file = Mock();
        CiNodeJsUnitTestReportHandler handler = new CiNodeJsUnitTestReportHandler()
        when:
        handler.analyseReport(file);
        then:
        1 * handler.analyseReport(file);
    }
}
