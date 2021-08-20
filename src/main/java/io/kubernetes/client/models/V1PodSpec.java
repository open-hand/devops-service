//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.kubernetes.client.models;

import java.util.*;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(
        description = "PodSpec is a description of a pod."
)
public class V1PodSpec {
    @SerializedName("activeDeadlineSeconds")
    private Long activeDeadlineSeconds = null;
    @SerializedName("affinity")
    private V1Affinity affinity = null;
    @SerializedName("automountServiceAccountToken")
    private Boolean automountServiceAccountToken = null;
    @SerializedName("containers")
    private List<V1Container> containers = new ArrayList();
    @SerializedName("dnsPolicy")
    private String dnsPolicy = null;
    @SerializedName("dnsConfig")
    private V1PodDNSConfig dnsConfig = null;
    @SerializedName("hostAliases")
    private List<V1HostAlias> hostAliases = null;
    @SerializedName("hostIPC")
    private Boolean hostIPC = null;
    @SerializedName("hostNetwork")
    private Boolean hostNetwork = null;
    @SerializedName("hostPID")
    private Boolean hostPID = null;
    @SerializedName("hostname")
    private String hostname = null;
    @SerializedName("imagePullSecrets")
    private List<V1LocalObjectReference> imagePullSecrets = null;
    @SerializedName("initContainers")
    private List<V1Container> initContainers = null;
    @SerializedName("nodeName")
    private String nodeName = null;
    @SerializedName("nodeSelector")
    private Map<String, String> nodeSelector = null;
    @SerializedName("priority")
    private Integer priority = null;
    @SerializedName("priorityClassName")
    private String priorityClassName = null;
    @SerializedName("restartPolicy")
    private String restartPolicy = null;
    @SerializedName("schedulerName")
    private String schedulerName = null;
    @SerializedName("securityContext")
    private V1PodSecurityContext securityContext = null;
    @SerializedName("serviceAccount")
    private String serviceAccount = null;
    @SerializedName("serviceAccountName")
    private String serviceAccountName = null;
    @SerializedName("subdomain")
    private String subdomain = null;
    @SerializedName("terminationGracePeriodSeconds")
    private Long terminationGracePeriodSeconds = null;
    @SerializedName("tolerations")
    private List<V1Toleration> tolerations = null;
    @SerializedName("volumes")
    private List<V1Volume> volumes = null;

    public V1PodSpec() {
    }

    public V1PodSpec activeDeadlineSeconds(Long activeDeadlineSeconds) {
        this.activeDeadlineSeconds = activeDeadlineSeconds;
        return this;
    }

    @ApiModelProperty("Optional duration in seconds the pod may be active on the node relative to StartTime before the system will actively try to mark it failed and kill associated containers. Value must be a positive integer.")
    public Long getActiveDeadlineSeconds() {
        return this.activeDeadlineSeconds;
    }

    public void setActiveDeadlineSeconds(Long activeDeadlineSeconds) {
        this.activeDeadlineSeconds = activeDeadlineSeconds;
    }

    public V1PodSpec affinity(V1Affinity affinity) {
        this.affinity = affinity;
        return this;
    }

    @ApiModelProperty("If specified, the pod's scheduling constraints")
    public V1Affinity getAffinity() {
        return this.affinity;
    }

    public void setAffinity(V1Affinity affinity) {
        this.affinity = affinity;
    }

    public V1PodSpec automountServiceAccountToken(Boolean automountServiceAccountToken) {
        this.automountServiceAccountToken = automountServiceAccountToken;
        return this;
    }

    @ApiModelProperty("AutomountServiceAccountToken indicates whether a service account token should be automatically mounted.")
    public Boolean isAutomountServiceAccountToken() {
        return this.automountServiceAccountToken;
    }

    public void setAutomountServiceAccountToken(Boolean automountServiceAccountToken) {
        this.automountServiceAccountToken = automountServiceAccountToken;
    }

    public V1PodSpec containers(List<V1Container> containers) {
        this.containers = containers;
        return this;
    }

    public V1PodSpec addContainersItem(V1Container containersItem) {
        this.containers.add(containersItem);
        return this;
    }

    @ApiModelProperty(
            required = true,
            value = "List of containers belonging to the pod. Containers cannot currently be added or removed. There must be at least one container in a Pod. Cannot be updated."
    )
    public List<V1Container> getContainers() {
        return this.containers;
    }

    public void setContainers(List<V1Container> containers) {
        this.containers = containers;
    }

    public V1PodSpec dnsPolicy(String dnsPolicy) {
        this.dnsPolicy = dnsPolicy;
        return this;
    }

    public V1PodSpec dnsConfig(V1PodDNSConfig v1PodDNSConfig) {
        this.dnsConfig = v1PodDNSConfig;
        return this;
    }

    public V1PodDNSConfig getDnsConfig() {
        return dnsConfig;
    }

    public void setDnsConfig(V1PodDNSConfig dnsConfig) {
        this.dnsConfig = dnsConfig;
    }

    @ApiModelProperty("Set DNS policy for containers within the pod. One of 'ClusterFirstWithHostNet', 'ClusterFirst' or 'Default'. Defaults to \"ClusterFirst\". To have DNS options set along with hostNetwork, you have to specify DNS policy explicitly to 'ClusterFirstWithHostNet'.")
    public String getDnsPolicy() {
        return this.dnsPolicy;
    }

    public void setDnsPolicy(String dnsPolicy) {
        this.dnsPolicy = dnsPolicy;
    }

    public V1PodSpec hostAliases(List<V1HostAlias> hostAliases) {
        this.hostAliases = hostAliases;
        return this;
    }

    public V1PodSpec addHostAliasesItem(V1HostAlias hostAliasesItem) {
        if (this.hostAliases == null) {
            this.hostAliases = new ArrayList();
        }

        this.hostAliases.add(hostAliasesItem);
        return this;
    }

    @ApiModelProperty("HostAliases is an optional list of hosts and IPs that will be injected into the pod's hosts file if specified. This is only valid for non-hostNetwork pods.")
    public List<V1HostAlias> getHostAliases() {
        return this.hostAliases;
    }

    public void setHostAliases(List<V1HostAlias> hostAliases) {
        this.hostAliases = hostAliases;
    }

    public V1PodSpec hostIPC(Boolean hostIPC) {
        this.hostIPC = hostIPC;
        return this;
    }

    @ApiModelProperty("Use the host's ipc namespace. Optional: Default to false.")
    public Boolean isHostIPC() {
        return this.hostIPC;
    }

    public void setHostIPC(Boolean hostIPC) {
        this.hostIPC = hostIPC;
    }

    public V1PodSpec hostNetwork(Boolean hostNetwork) {
        this.hostNetwork = hostNetwork;
        return this;
    }

    @ApiModelProperty("Host networking requested for this pod. Use the host's network namespace. If this option is set, the ports that will be used must be specified. Default to false.")
    public Boolean isHostNetwork() {
        return this.hostNetwork;
    }

    public void setHostNetwork(Boolean hostNetwork) {
        this.hostNetwork = hostNetwork;
    }

    public V1PodSpec hostPID(Boolean hostPID) {
        this.hostPID = hostPID;
        return this;
    }

    @ApiModelProperty("Use the host's pid namespace. Optional: Default to false.")
    public Boolean isHostPID() {
        return this.hostPID;
    }

    public void setHostPID(Boolean hostPID) {
        this.hostPID = hostPID;
    }

    public V1PodSpec hostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    @ApiModelProperty("Specifies the hostname of the Pod If not specified, the pod's hostname will be set to a system-defined value.")
    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public V1PodSpec imagePullSecrets(List<V1LocalObjectReference> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
        return this;
    }

    public V1PodSpec addImagePullSecretsItem(V1LocalObjectReference imagePullSecretsItem) {
        if (this.imagePullSecrets == null) {
            this.imagePullSecrets = new ArrayList();
        }

        this.imagePullSecrets.add(imagePullSecretsItem);
        return this;
    }

    @ApiModelProperty("ImagePullSecrets is an optional list of references to secrets in the same namespace to use for pulling any of the images used by this PodSpec. If specified, these secrets will be passed to individual puller implementations for them to use. For example, in the case of docker, only DockerConfig type secrets are honored. More info: https://kubernetes.io/docs/concepts/containers/images#specifying-imagepullsecrets-on-a-pod")
    public List<V1LocalObjectReference> getImagePullSecrets() {
        return this.imagePullSecrets;
    }

    public void setImagePullSecrets(List<V1LocalObjectReference> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }

    public V1PodSpec initContainers(List<V1Container> initContainers) {
        this.initContainers = initContainers;
        return this;
    }

    public V1PodSpec addInitContainersItem(V1Container initContainersItem) {
        if (this.initContainers == null) {
            this.initContainers = new ArrayList();
        }

        this.initContainers.add(initContainersItem);
        return this;
    }

    @ApiModelProperty("List of initialization containers belonging to the pod. Init containers are executed in order prior to containers being started. If any init container fails, the pod is considered to have failed and is handled according to its restartPolicy. The name for an init container or normal container must be unique among all containers. Init containers may not have Lifecycle actions, Readiness probes, or Liveness probes. The resourceRequirements of an init container are taken into account during scheduling by finding the highest request/limit for each resource type, and then using the max of of that value or the sum of the normal containers. Limits are applied to init containers in a similar fashion. Init containers cannot currently be added or removed. Cannot be updated. More info: https://kubernetes.io/docs/concepts/workloads/pods/init-containers/")
    public List<V1Container> getInitContainers() {
        return this.initContainers;
    }

    public void setInitContainers(List<V1Container> initContainers) {
        this.initContainers = initContainers;
    }

    public V1PodSpec nodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    @ApiModelProperty("NodeName is a request to schedule this pod onto a specific node. If it is non-empty, the scheduler simply schedules this pod onto that node, assuming that it fits resource requirements.")
    public String getNodeName() {
        return this.nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public V1PodSpec nodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
        return this;
    }

    public V1PodSpec putNodeSelectorItem(String key, String nodeSelectorItem) {
        if (this.nodeSelector == null) {
            this.nodeSelector = new HashMap();
        }

        this.nodeSelector.put(key, nodeSelectorItem);
        return this;
    }

    @ApiModelProperty("NodeSelector is a selector which must be true for the pod to fit on a node. Selector which must match a node's labels for the pod to be scheduled on that node. More info: https://kubernetes.io/docs/concepts/configuration/assign-pod-node/")
    public Map<String, String> getNodeSelector() {
        return this.nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public V1PodSpec priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    @ApiModelProperty("The priority value. Various system components use this field to find the priority of the pod. When Priority Admission Controller is enabled, it prevents users from setting this field. The admission controller populates this field from PriorityClassName. The higher the value, the higher the priority.")
    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public V1PodSpec priorityClassName(String priorityClassName) {
        this.priorityClassName = priorityClassName;
        return this;
    }

    @ApiModelProperty("If specified, indicates the pod's priority. \"SYSTEM\" is a special keyword which indicates the highest priority. Any other name must be defined by creating a PriorityClass object with that name. If not specified, the pod priority will be default or zero if there is no default.")
    public String getPriorityClassName() {
        return this.priorityClassName;
    }

    public void setPriorityClassName(String priorityClassName) {
        this.priorityClassName = priorityClassName;
    }

    public V1PodSpec restartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
        return this;
    }

    @ApiModelProperty("Restart policy for all containers within the pod. One of Always, OnFailure, Never. Default to Always. More info: https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#restart-policy")
    public String getRestartPolicy() {
        return this.restartPolicy;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public V1PodSpec schedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
        return this;
    }

    @ApiModelProperty("If specified, the pod will be dispatched by specified scheduler. If not specified, the pod will be dispatched by default scheduler.")
    public String getSchedulerName() {
        return this.schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public V1PodSpec securityContext(V1PodSecurityContext securityContext) {
        this.securityContext = securityContext;
        return this;
    }

    @ApiModelProperty("SecurityContext holds pod-level security attributes and common container settings. Optional: Defaults to empty.  See type description for default values of each field.")
    public V1PodSecurityContext getSecurityContext() {
        return this.securityContext;
    }

    public void setSecurityContext(V1PodSecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public V1PodSpec serviceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
        return this;
    }

    @ApiModelProperty("DeprecatedServiceAccount is a depreciated alias for ServiceAccountName. Deprecated: Use serviceAccountName instead.")
    public String getServiceAccount() {
        return this.serviceAccount;
    }

    public void setServiceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
    }

    public V1PodSpec serviceAccountName(String serviceAccountName) {
        this.serviceAccountName = serviceAccountName;
        return this;
    }

    @ApiModelProperty("ServiceAccountName is the name of the ServiceAccount to use to run this pod. More info: https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/")
    public String getServiceAccountName() {
        return this.serviceAccountName;
    }

    public void setServiceAccountName(String serviceAccountName) {
        this.serviceAccountName = serviceAccountName;
    }

    public V1PodSpec subdomain(String subdomain) {
        this.subdomain = subdomain;
        return this;
    }

    @ApiModelProperty("If specified, the fully qualified Pod hostname will be \"<hostname>.<subdomain>.<pod namespace>.svc.<cluster domain>\". If not specified, the pod will not have a domainname at all.")
    public String getSubdomain() {
        return this.subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public V1PodSpec terminationGracePeriodSeconds(Long terminationGracePeriodSeconds) {
        this.terminationGracePeriodSeconds = terminationGracePeriodSeconds;
        return this;
    }

    @ApiModelProperty("Optional duration in seconds the pod needs to terminate gracefully. May be decreased in delete request. Value must be non-negative integer. The value zero indicates delete immediately. If this value is nil, the default grace period will be used instead. The grace period is the duration in seconds after the processes running in the pod are sent a termination signal and the time when the processes are forcibly halted with a kill signal. Set this value longer than the expected cleanup time for your process. Defaults to 30 seconds.")
    public Long getTerminationGracePeriodSeconds() {
        return this.terminationGracePeriodSeconds;
    }

    public void setTerminationGracePeriodSeconds(Long terminationGracePeriodSeconds) {
        this.terminationGracePeriodSeconds = terminationGracePeriodSeconds;
    }

    public V1PodSpec tolerations(List<V1Toleration> tolerations) {
        this.tolerations = tolerations;
        return this;
    }

    public V1PodSpec addTolerationsItem(V1Toleration tolerationsItem) {
        if (this.tolerations == null) {
            this.tolerations = new ArrayList();
        }

        this.tolerations.add(tolerationsItem);
        return this;
    }

    @ApiModelProperty("If specified, the pod's tolerations.")
    public List<V1Toleration> getTolerations() {
        return this.tolerations;
    }

    public void setTolerations(List<V1Toleration> tolerations) {
        this.tolerations = tolerations;
    }

    public V1PodSpec volumes(List<V1Volume> volumes) {
        this.volumes = volumes;
        return this;
    }

    public V1PodSpec addVolumesItem(V1Volume volumesItem) {
        if (this.volumes == null) {
            this.volumes = new ArrayList();
        }

        this.volumes.add(volumesItem);
        return this;
    }

    @ApiModelProperty("List of volumes that can be mounted by containers belonging to the pod. More info: https://kubernetes.io/docs/concepts/storage/volumes")
    public List<V1Volume> getVolumes() {
        return this.volumes;
    }

    public void setVolumes(List<V1Volume> volumes) {
        this.volumes = volumes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1PodSpec v1PodSpec = (V1PodSpec) o;
            return Objects.equals(this.activeDeadlineSeconds, v1PodSpec.activeDeadlineSeconds) && Objects.equals(this.affinity, v1PodSpec.affinity) && Objects.equals(this.automountServiceAccountToken, v1PodSpec.automountServiceAccountToken) && Objects.equals(this.containers, v1PodSpec.containers) && Objects.equals(this.dnsPolicy, v1PodSpec.dnsPolicy) && Objects.equals(this.hostAliases, v1PodSpec.hostAliases) && Objects.equals(this.hostIPC, v1PodSpec.hostIPC) && Objects.equals(this.hostNetwork, v1PodSpec.hostNetwork) && Objects.equals(this.hostPID, v1PodSpec.hostPID) && Objects.equals(this.hostname, v1PodSpec.hostname) && Objects.equals(this.imagePullSecrets, v1PodSpec.imagePullSecrets) && Objects.equals(this.initContainers, v1PodSpec.initContainers) && Objects.equals(this.nodeName, v1PodSpec.nodeName) && Objects.equals(this.nodeSelector, v1PodSpec.nodeSelector) && Objects.equals(this.priority, v1PodSpec.priority) && Objects.equals(this.priorityClassName, v1PodSpec.priorityClassName) && Objects.equals(this.restartPolicy, v1PodSpec.restartPolicy) && Objects.equals(this.schedulerName, v1PodSpec.schedulerName) && Objects.equals(this.securityContext, v1PodSpec.securityContext) && Objects.equals(this.serviceAccount, v1PodSpec.serviceAccount) && Objects.equals(this.serviceAccountName, v1PodSpec.serviceAccountName) && Objects.equals(this.subdomain, v1PodSpec.subdomain) && Objects.equals(this.terminationGracePeriodSeconds, v1PodSpec.terminationGracePeriodSeconds) && Objects.equals(this.tolerations, v1PodSpec.tolerations) && Objects.equals(this.volumes, v1PodSpec.volumes);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.activeDeadlineSeconds, this.affinity, this.automountServiceAccountToken, this.containers, this.dnsPolicy, this.hostAliases, this.hostIPC, this.hostNetwork, this.hostPID, this.hostname, this.imagePullSecrets, this.initContainers, this.nodeName, this.nodeSelector, this.priority, this.priorityClassName, this.restartPolicy, this.schedulerName, this.securityContext, this.serviceAccount, this.serviceAccountName, this.subdomain, this.terminationGracePeriodSeconds, this.tolerations, this.volumes});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1PodSpec {\n");
        sb.append("    activeDeadlineSeconds: ").append(this.toIndentedString(this.activeDeadlineSeconds)).append("\n");
        sb.append("    affinity: ").append(this.toIndentedString(this.affinity)).append("\n");
        sb.append("    automountServiceAccountToken: ").append(this.toIndentedString(this.automountServiceAccountToken)).append("\n");
        sb.append("    containers: ").append(this.toIndentedString(this.containers)).append("\n");
        sb.append("    dnsPolicy: ").append(this.toIndentedString(this.dnsPolicy)).append("\n");
        sb.append("    hostAliases: ").append(this.toIndentedString(this.hostAliases)).append("\n");
        sb.append("    hostIPC: ").append(this.toIndentedString(this.hostIPC)).append("\n");
        sb.append("    hostNetwork: ").append(this.toIndentedString(this.hostNetwork)).append("\n");
        sb.append("    hostPID: ").append(this.toIndentedString(this.hostPID)).append("\n");
        sb.append("    hostname: ").append(this.toIndentedString(this.hostname)).append("\n");
        sb.append("    imagePullSecrets: ").append(this.toIndentedString(this.imagePullSecrets)).append("\n");
        sb.append("    initContainers: ").append(this.toIndentedString(this.initContainers)).append("\n");
        sb.append("    nodeName: ").append(this.toIndentedString(this.nodeName)).append("\n");
        sb.append("    nodeSelector: ").append(this.toIndentedString(this.nodeSelector)).append("\n");
        sb.append("    priority: ").append(this.toIndentedString(this.priority)).append("\n");
        sb.append("    priorityClassName: ").append(this.toIndentedString(this.priorityClassName)).append("\n");
        sb.append("    restartPolicy: ").append(this.toIndentedString(this.restartPolicy)).append("\n");
        sb.append("    schedulerName: ").append(this.toIndentedString(this.schedulerName)).append("\n");
        sb.append("    securityContext: ").append(this.toIndentedString(this.securityContext)).append("\n");
        sb.append("    serviceAccount: ").append(this.toIndentedString(this.serviceAccount)).append("\n");
        sb.append("    serviceAccountName: ").append(this.toIndentedString(this.serviceAccountName)).append("\n");
        sb.append("    subdomain: ").append(this.toIndentedString(this.subdomain)).append("\n");
        sb.append("    terminationGracePeriodSeconds: ").append(this.toIndentedString(this.terminationGracePeriodSeconds)).append("\n");
        sb.append("    tolerations: ").append(this.toIndentedString(this.tolerations)).append("\n");
        sb.append("    volumes: ").append(this.toIndentedString(this.volumes)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
