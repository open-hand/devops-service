package io.choerodon.devops.app.service;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface DeployMsgHandlerService {

    void handlerReleaseInstall(String msg, Long envId);

    void handlerPreInstall(String msg, Long envId, String type);

    void resourceUpdate(String key, Long envId, String msg);

    void resourceDelete(Long envId, String msg);

    void helmReleaseHookLogs(String key, String msg, Long envId);

    void updateInstanceStatus(String key, Long envId, String instanceStatus, String commandStatus, String commandMsg);

    void handlerDomainCreateMessage(String key, String msg, Long envId);

    void helmReleasePreUpgrade(String msg, Long envId, String type);

    void handlerReleaseUpgrade(String msg, Long envId);

    void helmReleaseDeleteFail(String key, String msg, Long envId);

    void helmReleaseStartFail(String key, String msg, Long envId);

    void helmReleaseRollBackFail(String key, String msg);

    void helmReleaseInstallFail(String key, String msg, Long envId);

    void helmReleaseUpgradeFail(String key, String msg, Long envId);

    void helmReleaeStopFail(String key, String msg, Long envId);

    void netWorkUpdate(String key, String msg, Long envId);

    void helmReleaseGetContent(String key, Long envId, String msg);

    void commandNotSend(Long commandId, String msg);

    void netWorkServiceFail(String key, String msg);

    void netWorkIngressFail(String key, Long envId, String msg);

    void netWorkServiceDeleteFail(String key, String value);

    void netWorkIngressDeleteFail(String key, Long envId, String value);

    void resourceSync(String key, Long envId, String value);

    void jobEvent(String key, String msg, Long envId);

    void releasePodEvent(String key, String msg, Long envId);
}
