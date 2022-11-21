package io.choerodon.devops.app.eventhandler.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/15 22:47
 */
public interface Command {

    void execute(Long jobId);
}
