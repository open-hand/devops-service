package io.choerodon.devops.infra.util;

import java.util.*;
import java.util.regex.Pattern;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * Created by younger on 2018/4/25.
 */
public class K8sUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(K8sUtil.class);

    private static final String INIT = "Init:";
    private static final String SIGNAL = "Signal:";
    private static final String EXIT_CODE = "ExitCode:";
    private static final String NONE_LABEL = "<none>";
    private static final JSON json = new JSON();
    /**
     * 名称正则
     */
    public static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*");

    /**
     * Host的正则
     */
    public static final Pattern HOST_PATTERN = Pattern.compile("^(\\*\\.)?[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$");

    /**
     * 子域名正则, Annotation的Key的一部分，可参考(https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/#syntax-and-character-set)
     */
    public static final Pattern SUB_DOMAIN_PATTERN = Pattern.compile("^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$");

    /**
     * Annotation的name正则, Annotation的Key的一部分，可参考(https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/#syntax-and-character-set)
     */
    public static final Pattern ANNOTATION_NAME_PATTERN = Pattern.compile("^([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9]$");

    /**
     * Label的name正则, Annotation的Key的一部分，可参考(https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/#syntax-and-character-set)
     */
    public static final Pattern LABEL_NAME_PATTERN = Pattern.compile("^([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9]$");

    public static final Pattern PORT_NAME_PATTERN=Pattern.compile("^[0-9a-z]([0-9a-z]+-)*[0-9a-z]*[0-9a-z]$");


    private K8sUtil() {
    }


    /**
     * get byte value from memory string of other measure format
     * ex: "1K" -> 1024, "1M" -> 1024 * 1024
     *
     * @param memory the memory string
     * @return byte value
     */
    public static long getByteFromMemoryString(String memory) {
        int index;
        if ((index = memory.indexOf('K')) != -1) {
            return Long.parseLong(memory.substring(0, index)) << 10;
        } else if ((index = memory.indexOf('M')) != -1) {
            return Long.parseLong(memory.substring(0, index)) << 20;
        } else if ((index = memory.indexOf('G')) != -1) {
            return Long.parseLong(memory.substring(0, index)) << 30;
        } else if (memory.matches("^\\d+$")) {
            return Long.parseLong(memory);
        } else if ((index = memory.indexOf('m')) != -1) {
            return Long.parseLong(memory.substring(0, index)) / 1000;
        } else {
            return 0;
        }
    }

    /**
     * get normal value of cpu measure.
     * ex: "132m" -> 0.132, "1.3" -> 1.3
     *
     * @param cpuAmount cpu string with measure 'm'
     * @return the normal value
     */
    public static double getNormalValueFromCpuString(String cpuAmount) {
        if (cpuAmount.endsWith("m")) {
            return Long.parseLong(cpuAmount.substring(0, cpuAmount.length() - 1)) / 1000.0;
        }
        if (cpuAmount.matches("^\\d+$")) {
            return Double.parseDouble(cpuAmount);
        }
        return 0.0;
    }

    public static Long getNormalValueFromPodString(String podAmount) {
        if (podAmount.endsWith("k")) {
            return Long.parseLong(podAmount.substring(0, podAmount.length() - 1)) * 1000;
        } else {
            return Long.parseLong(podAmount);
        }
    }


    private static String getPodStatus(V1ContainerStateTerminated containerStateTerminated) {
        LOGGER.debug("Get pod status: {}", containerStateTerminated);
        String podStatus;
        if (containerStateTerminated.getReason() != null) {
            if (containerStateTerminated.getReason().length() == 0) {
                podStatus = containerStateTerminated.getSignal() != 0
                        ? INIT + SIGNAL + containerStateTerminated.getSignal()
                        : INIT + EXIT_CODE + containerStateTerminated.getExitCode();
            } else {
                podStatus = INIT + containerStateTerminated.getReason();
            }
        } else {
            podStatus = "";
        }
        LOGGER.debug("Got pod status : {}", podStatus);
        return podStatus;
    }

    /**
     * pod状态生成规则
     *
     * @param pod pod信息
     * @return string
     */
    public static String changePodStatus(V1Pod pod) {
        String podStatusPhase = pod.getStatus().getPhase();
        String podStatusReason = pod.getStatus().getReason();
        String status = podStatusReason != null ? podStatusReason : podStatusPhase;
        List<V1ContainerStatus> initContainerStatuses = pod.getStatus().getInitContainerStatuses();
        List<V1ContainerStatus> containerStatusList = pod.getStatus().getContainerStatuses();
        // 只有Pod是Pending状态才去处理Pod的InitContainers的状态
        if (!ArrayUtil.isEmpty(initContainerStatuses) && "Pending".equals(podStatusPhase)) {
            V1ContainerState containerState = initContainerStatuses.get(0).getState();
            V1ContainerStateTerminated containerStateTerminated = containerState.getTerminated();
            V1ContainerStateWaiting containerStateWaiting = containerState.getWaiting();
            if (containerStateTerminated != null) {
                status = getPodStatus(containerStateTerminated);
            } else if (containerStateWaiting != null
                    && !containerStateWaiting.getReason().isEmpty()
                    && !"PodInitializing".equals(containerStateWaiting.getReason())) {
                status = INIT + containerStateWaiting.getReason();
            } else {
                status = INIT + pod.getSpec().getInitContainers().size();
            }
        } else if (!ArrayUtil.isEmpty(containerStatusList) && !"Pending".equals(podStatusPhase)) {
            V1ContainerState containerState = containerStatusList.get(0).getState();
            V1ContainerStateWaiting containerStateWaiting = containerState.getWaiting();
            V1ContainerStateTerminated containerStateTerminated = containerState.getTerminated();

            if (containerStateWaiting != null && !containerStateWaiting.getReason().isEmpty()) {
                status = containerStateWaiting.getReason();
            } else if (containerStateTerminated != null) {
                status = getPodStatus(containerStateTerminated);
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
        switch (v1Service.getSpec().getType()) {
            case "ClusterIP":
                if (v1Service.getSpec().getExternalIPs() != null && !v1Service.getSpec().getExternalIPs().isEmpty()) {
                    return String.join(",", v1Service.getSpec().getExternalIPs());
                } else {
                    return NONE_LABEL;
                }
            case "NodePort":
                if (v1Service.getSpec().getExternalIPs() != null && !v1Service.getSpec().getExternalIPs().isEmpty()) {
                    return String.join(",", v1Service.getSpec().getExternalIPs());
                } else {
                    return NONE_LABEL;
                }
            case "LoadBalancer":
                String lbips = loadBalancerStatusStringer(v1Service.getStatus().getLoadBalancer());
                if (!lbips.equals("")) {
                    List<String> result = new ArrayList<>();
                    if (lbips.length() > 0) {
                        result = Arrays.asList(lbips.split(","));
                    }
                    return String.join(",", result);
                } else {
                    return NONE_LABEL;
                }
            case "ExternalName":
                return v1Service.getSpec().getExternalName();
            default:
                break;
        }
        return "<unknown>";
    }

    /**
     * loadBalancerStatus获取
     */
    public static String loadBalancerStatusStringer(V1LoadBalancerStatus v1LoadBalancerStatus) {
        String result;
        List<V1LoadBalancerIngress> v1LoadBalancerIngresses = v1LoadBalancerStatus.getIngress();
        List<String> list = new ArrayList<>();
        if (v1LoadBalancerIngresses != null) {
            for (V1LoadBalancerIngress v1LoadBalancerIngress : v1LoadBalancerIngresses) {
                if (!StringUtils.isEmpty(v1LoadBalancerIngress.getIp())) {
                    list.add(v1LoadBalancerIngress.getIp());
                }
                if (!StringUtils.isEmpty(v1LoadBalancerIngress.getHostname())) {
                    list.add(v1LoadBalancerIngress.getHostname());
                }
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
     * 解析ingress对象所关联的所有service的名称合集(使用集合的原因是可能重复)
     *
     * @param ingress ingress对象
     * @return 空的不可修改的Set, 如果没有
     */
    public static Set<String> analyzeIngressServices(V1beta1Ingress ingress) {
        if (ingress == null || ingress.getSpec() == null) {
            return Collections.emptySet();
        }

        Set<String> services = new HashSet<>();
        if (!CollectionUtils.isEmpty(ingress.getSpec().getRules())) {
            ingress.getSpec().getRules().forEach(rule -> {
                if (rule.getHttp() != null && !CollectionUtils.isEmpty(rule.getHttp().getPaths())) {
                    rule.getHttp().getPaths().forEach(path -> {
                        if (path.getBackend() != null) {
                            services.add(path.getBackend().getServiceName());
                        }
                    });
                }
            });
        }

        // 将默认的backend相关的service加入集合
        if (ingress.getSpec().getBackend() != null) {
            services.add(ingress.getSpec().getBackend().getServiceName());
        }

        return services;
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
        int max = 3;
        boolean more = false;
        for (V1beta1IngressRule v1beta1IngressRule : v1beta1IngressRules) {
            if (results.size() == max) {
                more = true;
            }
            if (v1beta1IngressRule.getHost() != null && !more && v1beta1IngressRule.getHost().length() != 0) {
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

    /**
     * get restart count for the pod according to the logic of kubernetes's <code>printers.go#printPod</code>
     *
     * @param v1Pod a valid pod instance
     * @return the restart count of the pod.
     */
    public static long getRestartCountForPod(V1Pod v1Pod) {
        long restarts = 0;
        boolean initializing = false;
        if (!ArrayUtil.isEmpty(v1Pod.getStatus().getInitContainerStatuses())) {
            for (V1ContainerStatus containerStatus : v1Pod.getStatus().getInitContainerStatuses()) {
                restarts += containerStatus.getRestartCount();
                if (containerStatus.getState().getTerminated() != null) {
                    if (containerStatus.getState().getTerminated().getExitCode() == 0) {
                        continue;
                    } else {
                        initializing = true;
                    }
                } else if (containerStatus.getState().getWaiting() != null && !StringUtils.isEmpty(containerStatus.getState().getWaiting().getReason()) && !"PodInitializing".equals(containerStatus.getState().getWaiting().getReason())) {
                    initializing = true;
                } else {
                    initializing = true;
                }
                break;
            }
        }

        if (!initializing) {
            restarts = 0;
            if (!ArrayUtil.isEmpty(v1Pod.getStatus().getContainerStatuses())) {
                restarts = v1Pod.getStatus().getContainerStatuses().stream().map(V1ContainerStatus::getRestartCount).reduce((x, y) -> x + y).orElse(0);
            }
        }

        return restarts;
    }

    /**
     * 反序列化K8s的json字符串
     *
     * @param jsonString      k8s对象的json字符串
     * @param destK8sResource 对象的类
     * @param <T>             对象的类型
     * @return 反序列化结果
     */
    public static <T> T deserialize(String jsonString, Class<T> destK8sResource) {
        return json.deserialize(jsonString, destK8sResource);
    }

    public static <T> Yaml getYamlObject(Tag tag, Boolean isTag, T t) {
        SkipNullRepresenterUtil skipNullRepresenter = null;
        if (isTag) {
            skipNullRepresenter = new SkipNullRepresenterUtil();
            skipNullRepresenter.addClassTag(t.getClass(), tag);
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);
        return skipNullRepresenter == null ? new Yaml(options) : new Yaml(skipNullRepresenter, options);
    }
}
