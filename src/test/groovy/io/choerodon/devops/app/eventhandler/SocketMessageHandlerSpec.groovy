package io.choerodon.devops.app.eventhandler

import io.choerodon.devops.app.service.AgentMsgHandlerService
import io.choerodon.devops.infra.enums.HelmType
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by n!Ck
 * Date: 18-12-3
 * Time: 下午4:24
 * Description: 
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik)
class SocketMessageHandlerSpec extends Specification {
//    @Autowired
//    @Qualifier("mockSocketMsgDispatcher")
//    private SocketMsgDispatcher socketMsgDispatcher

    private AgentMsgHandlerService deployMsgHandlerService = PowerMockito.mock(AgentMsgHandlerService)

//    private AgentMessageHandler socketMessageHandler = new AgentMessageHandler(deployMsgHandlerService)

    @Unroll
    def "Process"() {
//        given: '初始化Msg'
//        int size = HelmType.values().length
//        Msg[] msg = new Msg[size + 1]
//        int i = 1
//        msg[0] = new Msg()
//        msg[0].setType(null)
//        msg[0].setKey("key.key:key")
//        msg[0].setClusterId("1")
//        msg[0].setPayload("payload")
//        for (HelmType e : HelmType.values()) {
//            msg[i] = new Msg()
//            msg[i].setKey("key.key:key")
//            msg[i].setClusterId("1")
//            msg[i].setPayload("payload")
//            msg[i++].setType(e.toValue())
//        }
//
//        when: '方法调用'
//        for (int k = 0; k < size; k++) {
//            socketMessageHandler.process(msg[k])
//        }
//
//        then: '校验方法调用'
//        noExceptionThrown()
    }

    def "GetOrder"() {
        when: '方法调用'
        def order = socketMessageHandler.getOrder()

        then: '校验方法调用'
        order == 0
    }
}
