package io.choerodon.devops.infra.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.kubernetes.client.models.*;

/**
 * Created by younger on 2018/4/25.
 */
public class K8sUtil {

    private static final String INIT = "Init:";
    private static final String SIGNAL = "Signal:";
    private static final String EXIT_CODE = "ExitCode:";
    private static final String NONE_LABEL = "<none>";

    private K8sUtil() {
    }

    /**
     * pod状态生成规则
     *
     * @param pod pod信息
     * @return string
     */
    public static String changePodStatus(V1Pod pod) {
        String status = pod.getStatus().getPhase();
        if (pod.getStatus().getReason() != null) {
            status = pod.getStatus().getReason();
        }
        List<V1ContainerStatus> initContainerStatuses = pod.getStatus().getInitContainerStatuses();
        if (initContainerStatuses != null && !initContainerStatuses.isEmpty()) {
            V1ContainerStatus containerStatus = initContainerStatuses.get(0);
            V1ContainerState containerState = containerStatus.getState();
            V1ContainerStateTerminated containerStateTerminated = containerState.getTerminated();
            V1ContainerStateWaiting containerStateWaiting = containerState.getWaiting();
            if (containerStateTerminated != null) {
                if (containerStateTerminated.getReason().length() == 0) {
                    status = containerStateTerminated.getSignal() != 0
                            ? INIT + SIGNAL + containerStateTerminated.getSignal()
                            : INIT + EXIT_CODE + containerStateTerminated.getExitCode();

                } else {
                    status = INIT + containerStateTerminated.getReason();
                }
            } else if (containerStateWaiting != null
                    && containerStateWaiting.getReason().length() > 0
                    && !"PodInitializing".equals(containerStateWaiting.getReason())) {
                status = INIT + containerStateWaiting.getReason();
            } else {
                status = INIT + pod.getSpec().getInitContainers().size();
            }
        } else {
            if (!"Pending".equals(pod.getStatus().getPhase()) && !pod.getStatus().getContainerStatuses().isEmpty()) {
                V1ContainerStatus containerStatus = pod.getStatus().getContainerStatuses().get(0);
                V1ContainerState containerState = containerStatus.getState();
                V1ContainerStateWaiting containerStateWaiting = containerState.getWaiting();
                V1ContainerStateTerminated containerStateTerminated = containerState.getTerminated();

                if (containerStateWaiting != null && containerStateWaiting.getReason().length() != 0) {
                    status = containerStateWaiting.getReason();
                } else if (containerStateTerminated != null) {
                    if (containerStateTerminated.getReason().length() != 0) {
                        status = containerStateTerminated.getReason();
                    } else {
                        status = containerStateTerminated.getSignal() != 0
                                ? SIGNAL + containerStateTerminated.getSignal()
                                : EXIT_CODE + containerStateTerminated.getExitCode();
                    }
                }
            }
        }
        return status;
    }

    /**
     * 获取外部ip
     *
     * @param v1Service service对象
     * @return string
     */
    public static String getServiceExternalIp(V1Service v1Service) {
        if (v1Service.getSpec().getExternalIPs() == null) {
            return NONE_LABEL;
        }
        switch (v1Service.getSpec().getType()) {
            case "ClusterIP":
                if (!v1Service.getSpec().getExternalIPs().isEmpty()) {
                    return String.join(",", v1Service.getSpec().getExternalIPs());
                } else {
                    return NONE_LABEL;
                }
            case "NodePort":
                if (!v1Service.getSpec().getExternalIPs().isEmpty()) {
                    return String.join(",", v1Service.getSpec().getExternalIPs());
                } else {
                    return NONE_LABEL;
                }
            case "LoadBalancer":
                String lbips = loadBalancerStatusStringer(v1Service.getStatus().getLoadBalancer());
                List<String> result = new ArrayList<>();
                if (!v1Service.getSpec().getExternalIPs().isEmpty()) {
                    if (lbips.length() > 0) {
                        result = Arrays.asList(lbips.split(","));
                    }
                    result.addAll(v1Service.getSpec().getExternalIPs());
                    return String.join(",", result);
                }
                if (lbips.length() > 0) {
                    return lbips;
                }
                return "<pending>";
            case "ExternalName":
                return v1Service.getSpec().getExternalName();
            default:
                break;
        }
        return "<unknow>";
    }

    /**
     * loadBalancerStatus获取
     */
    public static String loadBalancerStatusStringer(V1LoadBalancerStatus v1LoadBalancerStatus) {
        String result = "";
        List<V1LoadBalancerIngress> v1LoadBalancerIngresses = v1LoadBalancerStatus.getIngress();
        List<String> list = new ArrayList<>();
        for (V1LoadBalancerIngress v1LoadBalancerIngress : v1LoadBalancerIngresses) {
            if (v1LoadBalancerIngress.getIp() != "") {
                list.add(v1LoadBalancerIngress.getIp());
            }
            if (v1LoadBalancerIngress.getHostname() != "") {
                list.add(v1LoadBalancerIngress.getHostname());
            }
        }
        result = String.join(",", list);
        if (result.length() > 16) {
            result = result.substring(0, 13) + "...";
        }
        return result;
    }

    /**
     * 获取网络端口
     *
     * @param servicePorts service端口
     * @return string
     */
    public static String makePortString(List<V1ServicePort> servicePorts) {
        List<String> results = new ArrayList<>();
        for (V1ServicePort v1ServicePort : servicePorts) {
            String result = v1ServicePort.getPort() + "/" + v1ServicePort.getProtocol();
            if (v1ServicePort.getNodePort() != null) {
                result = v1ServicePort.getPort() + ":"
                        + v1ServicePort.getNodePort() + "/" + v1ServicePort.getProtocol();
            }
            results.add(result);
        }
        return String.join(",", results);
    }

    /**
     * 获取目标网络端口
     *
     * @param servicePorts service端口
     * @return string
     */
    public static String makeTargetPortString(List<V1ServicePort> servicePorts) {
        List<String> results = new ArrayList<>();
        for (V1ServicePort v1ServicePort : servicePorts) {
            String result = v1ServicePort.getTargetPort() + "/" + v1ServicePort.getProtocol();
            if (v1ServicePort.getNodePort() != null) {
                result = v1ServicePort.getTargetPort() + ":"
                        + v1ServicePort.getNodePort() + "/" + v1ServicePort.getProtocol();
            }
            results.add(result);
        }
        return String.join(",", results);
    }
        /**
         * 获取ip
         *
         * @param v1beta1IngressRules ingress对象
         * @return string
         */
    public static String formatHosts(List<V1beta1IngressRule> v1beta1IngressRules) {
        List<String> results = new ArrayList<>();
        Integer max = 3;
        Boolean more = false;
        for (V1beta1IngressRule v1beta1IngressRule : v1beta1IngressRules) {
            if (results.size() == max) {
                more = true;
            }
            if (!more && v1beta1IngressRule.getHost().length() != 0) {
                results.add(v1beta1IngressRule.getHost());
            }
        }
        if (results.isEmpty()) {
            return "*";
        }
        String result = String.join(",", results);
        if (more) {
            return result + (v1beta1IngressRules.size() - max) + "more...";
        }
        return result;
    }

    /**
     * 获取端口
     *
     * @param v1beta1IngressTLS ingress对象
     * @return string
     */
    public static String formatPorts(List<V1beta1IngressTLS> v1beta1IngressTLS) {
        if (v1beta1IngressTLS != null && !v1beta1IngressTLS.isEmpty()) {
            return "80,443";
        }
        return "80";
    }

}
