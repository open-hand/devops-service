package io.choerodon.devops.app.service.impl


import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsDeployGroupAppConfigVO
import io.choerodon.devops.api.vo.DevopsDeployGroupContainerConfigVO
import io.choerodon.devops.api.vo.DevopsDeployGroupDockerConfigVO
import io.choerodon.devops.api.vo.DevopsDeployGroupVO
import io.choerodon.devops.api.vo.harbor.ProdImageInfoVO
import io.choerodon.devops.app.service.DevopsDeployGroupService
import io.choerodon.devops.app.service.DevopsDeploymentService
import io.choerodon.devops.infra.constant.MiscConstants
import io.choerodon.devops.infra.util.JsonHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Subject(DevopsDeployGroupServiceImplSpec)
@Import(IntegrationTestConfiguration)
class DevopsDeployGroupServiceImplSpec extends Specification {
    @Autowired
     DevopsDeployGroupService deployGroupService

    DevopsDeploymentService deploymentService=Mock()

    def setup() {
        ReflectionTestUtils.setField(deployGroupService,"devopsDeploymentService",deploymentService)
    }

    def "createOrUpdateDeployGroup"(){
        given:
        def deployGroupVO = new DevopsDeployGroupVO();
        deployGroupVO.setProjectId(215874867621597184L)
        deployGroupVO.setEnvId(217052402308542464L)
        deployGroupVO.setCode("unit-test-"+UUID.randomUUID().toString().substring(0,12))
        deployGroupVO.setName("unit-test-"+UUID.randomUUID().toString().substring(0,12))

        def appConfig=new DevopsDeployGroupAppConfigVO()
       def labels=new HashMap<String,String>()
        labels.put("app-name",deployGroupVO.getName())
        labels.put("app-code",deployGroupVO.getCode())
        appConfig.setLabels(labels)
        def annotations=new HashMap<String,String>()
        annotations.put("app-name",deployGroupVO.getName())
        annotations.put("app-code",deployGroupVO.getCode())
        appConfig.setAnnotations(annotations)
        appConfig.setReplicas(1)
        appConfig.setMaxSurge(25)
        appConfig.setMaxUnavailable(25)
        appConfig.setDnsPolicy("ClusterFirstWithHostNet")
        appConfig.setNameServers("www.test1.com,www.test2.com,www.test3.com")
        appConfig.setSearches("open,test,www,minio,gitlab")
        def options=new HashMap<String,String>()
        options.put("timeout","3600")
        options.put("attempts","15")
        def nodeSelector=new HashMap<String,String>()
        nodeSelector.put("app-name",deployGroupVO.getName())
        nodeSelector.put("app-code",deployGroupVO.getCode())
        appConfig.setNodeSelector(nodeSelector)
        def hostAlias=new HashMap<String,String>()
        hostAlias.put("host-app-name",deployGroupVO.getName())
        hostAlias.put("host-app-code",deployGroupVO.getCode())
        appConfig.setHostAlias(hostAlias)
        def containerConfigList=new ArrayList<DevopsDeployGroupContainerConfigVO>()
        def deployGroupContainerConfigVO1=new DevopsDeployGroupContainerConfigVO()
        containerConfigList.add(deployGroupContainerConfigVO1)
        deployGroupContainerConfigVO1.setName("container1")
        deployGroupContainerConfigVO1.setRequestCpu("200")
        deployGroupContainerConfigVO1.setLimitCpu("200")
        deployGroupContainerConfigVO1.setRequestMemory("200")
        deployGroupContainerConfigVO1.setLimitMemory("200")
        deployGroupContainerConfigVO1.setType("docker")

        def envs=new HashMap<String,String>()
        envs.put("env-app-name",deployGroupVO.getName())
        envs.put("env-app-code",deployGroupVO.getCode())
        deployGroupContainerConfigVO1.setEnvs(envs)

        def deployGroupDockerConfigVO=new DevopsDeployGroupDockerConfigVO()
        def imageInfo=new ProdImageInfoVO()
        deployGroupDockerConfigVO.setSourceType("CURRENT_PROJECT")
        deployGroupDockerConfigVO.setImageInfo(imageInfo)
        imageInfo.setRepoId("215874875758350336")
        imageInfo.setRepoType("DEFAULT_REPO")
        imageInfo.setImageName("log-test")
        imageInfo.setTag("2021.8.11-103036-master")

        deployGroupContainerConfigVO1.setDockerDeployVO(deployGroupDockerConfigVO)

        deployGroupVO.setAppConfig(appConfig)
        deployGroupVO.setContainerConfig(containerConfigList)
        when:
       println(JsonHelper.marshalByJackson(deployGroupVO))
        deployGroupService.createOrUpdate(deployGroupVO.getProjectId(),deployGroupVO, MiscConstants.CREATE_TYPE)
        then:
        println "completed"
    }
}
