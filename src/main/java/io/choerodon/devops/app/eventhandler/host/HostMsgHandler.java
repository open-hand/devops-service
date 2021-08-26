package io.choerodon.devops.app.eventhandler.host;

/**
 * 〈功能简述〉
 * 〈处理主机ws消息事件〉
 *
 * @author wanghao
 * @Date 2021/6/25 9:55
 */
public interface HostMsgHandler {

    /**
     * 处理ws消息事件
     * @param hostId 主机id
     * @param commandId commandId
     * @param payload 消息内容
     */
    void handler(String hostId, Long commandId, String payload);

    /**
     * 获取处理器类型
     * @return 处理器类型 {@link io.choerodon.devops.infra.enums.host.HostMsgEventEnum: value()}
     */
    String getType();
}
