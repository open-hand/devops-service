package io.choerodon.devops.api.vo.workload;

/**
 * Created by wangxiang on 2021/7/14
 */
public interface WorkLoadFactory {
    WorkLoad getWorkLoad(String kind);
}
