package io.choerodon.devops.api.vo.jenkins;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/7 10:06
 */
public class BuildInfo {

    private String id;

    private String status;

    private Long startTimeMillis;
    private Long durationMillis;

    private String username;

    private String triggerType;
}
