package io.choerodon.devops.infra.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.util.StringUtils
import spock.lang.Specification

import io.choerodon.devops.infra.dto.gitlab.ci.GitlabCi

/**
 *
 * @author zmf
 * @since 20-4-2
 *
 */
class GitlabCiUtilSpec extends Specification {
    def "GitlabCi2yaml"() {
        given: "准备测试数据"
        String json = "{\"include\":[{\"local\":\"/url.yaml\"},{\"remote\":\"https://sdf.com/ci.yaml\"}],\"image\":\"registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.9.1\",\"stages\":[\"build\",\"release\"],\"build backend\":{\"stage\":\"build\",\"only\":{\"ref\":[\"master\"]},\"script\":[\"ls -al\",\"rm -rf ./*\"],\"except\":{\"ref\":[\"master\"]},\"cache\":{\"key\":{\"files\":[\"app.jar\"]},\"paths\":[\"./name.jar\",\"./app.jar\"],\"untracked\":true}},\"release backend\":{\"stage\":\"build\",\"only\":{\"ref\":[\"master\"]},\"script\":[\"ls -al\",\"rm -rf ./*\"],\"except\":{\"ref\":[\"master\"]},\"cache\":{\"key\":{\"files\":[\"app.jar\"]},\"paths\":[\"./name.jar\",\"./app.jar\"],\"untracked\":true}}}"
        GitlabCi gitlabCi = new ObjectMapper().readValue(json, GitlabCi)

        when: "调用转为yaml的方法"
        String yaml = GitlabCiUtil.gitlabCi2yaml(gitlabCi)
        println(yaml)

        then: "校验结果"
        !StringUtils.isEmpty(yaml)
        noExceptionThrown()
    }

//    def "GitlabCi2yaml2"() {
//        given: "准备测试数据"
//        GitlabCi gitlabCi = new GitlabCi()
//        gitlabCi.setImage("registry.cn-hangzhou.aliyuncs.com/choerodon-tools/golang-ci:0.8.0")
//
//    }
}
