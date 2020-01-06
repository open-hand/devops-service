package io.choerodon.devops.infra.mapper

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.infra.dto.UserAttrDTO

/**
 *
 * @author zmf
 * @since 19-12-30
 *
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
@Subject(UserAttrMapper)
class UserAttrMapperSpec extends Specification {
    private static final Long IAM_USER_ID = 100L

    @Autowired
    private UserAttrMapper userAttrMapper
    private boolean isToInit;
    private boolean isToCleanup;

    def setup() {
        if (!isToInit) {
            return
        }
    }

    def cleanup() {
        if (!isToCleanup) {
            return
        }

        userAttrMapper.deleteByPrimaryKey(IAM_USER_ID)
    }

    def "UpdateIsGitlabAdmin"() {
        given: "准备数据"
        isToInit = false
        isToCleanup = true
        UserAttrDTO userAttrDTO = new UserAttrDTO()
        userAttrDTO.setIamUserId(IAM_USER_ID)
        userAttrDTO.setGitlabUserId(100)

        when: "插入数据"
        userAttrMapper.insertSelective(userAttrDTO)

        then: "校验默认值"
        !userAttrMapper.selectByPrimaryKey(IAM_USER_ID).getGitlabAdmin()

        when: "更新管理员字段"
        userAttrMapper.updateIsGitlabAdmin(IAM_USER_ID, Boolean.TRUE)

        then: "校验更新结果"
        userAttrMapper.selectByPrimaryKey(IAM_USER_ID).getGitlabAdmin()

        when: "更新管理员字段"
        userAttrMapper.updateIsGitlabAdmin(IAM_USER_ID, Boolean.FALSE)

        then: "校验更新结果"
        !userAttrMapper.selectByPrimaryKey(IAM_USER_ID).getGitlabAdmin()
    }
}
