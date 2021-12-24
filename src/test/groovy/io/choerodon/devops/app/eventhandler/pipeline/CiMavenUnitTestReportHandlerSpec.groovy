package io.choerodon.devops.app.eventhandler.pipeline


import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification
import spock.lang.Subject

@Subject(CiMavenUnitTestReportHandler)
class CiMavenUnitTestReportHandlerSpec extends Specification {

    def "AnalyseReport"() {
        CiMavenUnitTestReportHandler ciMavenUnitTestReportHandler = new CiMavenUnitTestReportHandler();
        MultipartFile file = Mock();
        when:
        ciMavenUnitTestReportHandler.analyseReport(file);
        then:
        1 * ciMavenUnitTestReportHandler.analyseReport(file)
    }
}
