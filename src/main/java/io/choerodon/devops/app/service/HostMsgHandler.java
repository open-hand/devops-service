package io.choerodon.devops.app.service;

/**
 * 〈功能简述〉
 * 〈〉
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
    void handler(Long hostId, Long commandId, String payload);

    /**
     * 获取处理器类型
     * @return 处理器类型
     */
    String getType();
}
