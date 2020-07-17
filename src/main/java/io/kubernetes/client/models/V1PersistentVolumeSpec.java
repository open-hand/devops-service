//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.kubernetes.client.models;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.custom.Quantity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.*;

@ApiModel(
        description = "PersistentVolumeSpec is the specification of a persistent volume."
)
public class V1PersistentVolumeSpec {
    @SerializedName("accessModes")
    private List<String> accessModes = null;
    @SerializedName("awsElasticBlockStore")
    private V1AWSElasticBlockStoreVolumeSource awsElasticBlockStore = null;
    @SerializedName("azureDisk")
    private V1AzureDiskVolumeSource azureDisk = null;
    @SerializedName("azureFile")
    private V1AzureFilePersistentVolumeSource azureFile = null;
    @SerializedName("capacity")
    private Map<String, Quantity> capacity = null;
    @SerializedName("cephfs")
    private V1CephFSPersistentVolumeSource cephfs = null;
    @SerializedName("cinder")
    private V1CinderVolumeSource cinder = null;
    @SerializedName("claimRef")
    private V1ObjectReference claimRef = null;
    @SerializedName("fc")
    private V1FCVolumeSource fc = null;
    @SerializedName("flexVolume")
    private V1FlexVolumeSource flexVolume = null;
    @SerializedName("flocker")
    private V1FlockerVolumeSource flocker = null;
    @SerializedName("gcePersistentDisk")
    private V1GCEPersistentDiskVolumeSource gcePersistentDisk = null;
    @SerializedName("glusterfs")
    private V1GlusterfsVolumeSource glusterfs = null;
    @SerializedName("hostPath")
    private V1HostPathVolumeSource hostPath = null;
    @SerializedName("iscsi")
    private V1ISCSIVolumeSource iscsi = null;
    @SerializedName("local")
    private V1LocalVolumeSource local = null;
    @SerializedName("mountOptions")
    private List<String> mountOptions = null;
    @SerializedName("nfs")
    private V1NFSVolumeSource nfs = null;
    @SerializedName("persistentVolumeReclaimPolicy")
    private String persistentVolumeReclaimPolicy = null;
    @SerializedName("photonPersistentDisk")
    private V1PhotonPersistentDiskVolumeSource photonPersistentDisk = null;
    @SerializedName("portworxVolume")
    private V1PortworxVolumeSource portworxVolume = null;
    @SerializedName("quobyte")
    private V1QuobyteVolumeSource quobyte = null;
    @SerializedName("rbd")
    private V1RBDVolumeSource rbd = null;
    @SerializedName("scaleIO")
    private V1ScaleIOPersistentVolumeSource scaleIO = null;
    @SerializedName("storageClassName")
    private String storageClassName = null;
    @SerializedName("storageos")
    private V1StorageOSPersistentVolumeSource storageos = null;
    @SerializedName("vsphereVolume")
    private V1VsphereVirtualDiskVolumeSource vsphereVolume = null;
    @SerializedName("nodeAffinity")
    private V1VolumeNodeAffinity nodeAffinity;

    public V1PersistentVolumeSpec() {
    }

    public V1PersistentVolumeSpec accessModes(List<String> accessModes) {
        this.accessModes = accessModes;
        return this;
    }

    public V1PersistentVolumeSpec addAccessModesItem(String accessModesItem) {
        if (this.accessModes == null) {
            this.accessModes = new ArrayList();
        }

        this.accessModes.add(accessModesItem);
        return this;
    }

    @ApiModelProperty("AccessModes contains all ways the volume can be mounted. More info: https://kubernetes.io/docs/concepts/storage/persistent-volumes#access-modes")
    public List<String> getAccessModes() {
        return this.accessModes;
    }

    public void setAccessModes(List<String> accessModes) {
        this.accessModes = accessModes;
    }

    public V1PersistentVolumeSpec awsElasticBlockStore(V1AWSElasticBlockStoreVolumeSource awsElasticBlockStore) {
        this.awsElasticBlockStore = awsElasticBlockStore;
        return this;
    }

    @ApiModelProperty("AWSElasticBlockStore represents an AWS Disk resource that is attached to a kubelet's host machine and then exposed to the pod. More info: https://kubernetes.io/docs/concepts/storage/volumes#awselasticblockstore")
    public V1AWSElasticBlockStoreVolumeSource getAwsElasticBlockStore() {
        return this.awsElasticBlockStore;
    }

    public void setAwsElasticBlockStore(V1AWSElasticBlockStoreVolumeSource awsElasticBlockStore) {
        this.awsElasticBlockStore = awsElasticBlockStore;
    }

    public V1PersistentVolumeSpec azureDisk(V1AzureDiskVolumeSource azureDisk) {
        this.azureDisk = azureDisk;
        return this;
    }

    @ApiModelProperty("AzureDisk represents an Azure Data Disk mount on the host and bind mount to the pod.")
    public V1AzureDiskVolumeSource getAzureDisk() {
        return this.azureDisk;
    }

    public void setAzureDisk(V1AzureDiskVolumeSource azureDisk) {
        this.azureDisk = azureDisk;
    }

    public V1PersistentVolumeSpec azureFile(V1AzureFilePersistentVolumeSource azureFile) {
        this.azureFile = azureFile;
        return this;
    }

    @ApiModelProperty("AzureFile represents an Azure File Service mount on the host and bind mount to the pod.")
    public V1AzureFilePersistentVolumeSource getAzureFile() {
        return this.azureFile;
    }

    public void setAzureFile(V1AzureFilePersistentVolumeSource azureFile) {
        this.azureFile = azureFile;
    }

    public V1PersistentVolumeSpec capacity(Map<String, Quantity> capacity) {
        this.capacity = capacity;
        return this;
    }

    public V1PersistentVolumeSpec putCapacityItem(String key, Quantity capacityItem) {
        if (this.capacity == null) {
            this.capacity = new HashMap();
        }

        this.capacity.put(key, capacityItem);
        return this;
    }

    @ApiModelProperty("A description of the persistent volume's resources and capacity. More info: https://kubernetes.io/docs/concepts/storage/persistent-volumes#capacity")
    public Map<String, Quantity> getCapacity() {
        return this.capacity;
    }

    public void setCapacity(Map<String, Quantity> capacity) {
        this.capacity = capacity;
    }

    public V1PersistentVolumeSpec cephfs(V1CephFSPersistentVolumeSource cephfs) {
        this.cephfs = cephfs;
        return this;
    }

    @ApiModelProperty("CephFS represents a Ceph FS mount on the host that shares a pod's lifetime")
    public V1CephFSPersistentVolumeSource getCephfs() {
        return this.cephfs;
    }

    public void setCephfs(V1CephFSPersistentVolumeSource cephfs) {
        this.cephfs = cephfs;
    }

    public V1PersistentVolumeSpec cinder(V1CinderVolumeSource cinder) {
        this.cinder = cinder;
        return this;
    }

    @ApiModelProperty("Cinder represents a cinder volume attached and mounted on kubelets host machine More info: https://releases.k8s.io/HEAD/examples/mysql-cinder-pd/README.md")
    public V1CinderVolumeSource getCinder() {
        return this.cinder;
    }

    public void setCinder(V1CinderVolumeSource cinder) {
        this.cinder = cinder;
    }

    public V1PersistentVolumeSpec claimRef(V1ObjectReference claimRef) {
        this.claimRef = claimRef;
        return this;
    }

    @ApiModelProperty("ClaimRef is part of a bi-directional binding between PersistentVolume and PersistentVolumeClaim. Expected to be non-nil when bound. claim.VolumeName is the authoritative bind between PV and PVC. More info: https://kubernetes.io/docs/concepts/storage/persistent-volumes#binding")
    public V1ObjectReference getClaimRef() {
        return this.claimRef;
    }

    public void setClaimRef(V1ObjectReference claimRef) {
        this.claimRef = claimRef;
    }

    public V1PersistentVolumeSpec fc(V1FCVolumeSource fc) {
        this.fc = fc;
        return this;
    }

    @ApiModelProperty("FC represents a Fibre Channel resource that is attached to a kubelet's host machine and then exposed to the pod.")
    public V1FCVolumeSource getFc() {
        return this.fc;
    }

    public void setFc(V1FCVolumeSource fc) {
        this.fc = fc;
    }

    public V1PersistentVolumeSpec flexVolume(V1FlexVolumeSource flexVolume) {
        this.flexVolume = flexVolume;
        return this;
    }

    @ApiModelProperty("FlexVolume represents a generic volume resource that is provisioned/attached using an exec based plugin. This is an alpha feature and may change in future.")
    public V1FlexVolumeSource getFlexVolume() {
        return this.flexVolume;
    }

    public void setFlexVolume(V1FlexVolumeSource flexVolume) {
        this.flexVolume = flexVolume;
    }

    public V1PersistentVolumeSpec flocker(V1FlockerVolumeSource flocker) {
        this.flocker = flocker;
        return this;
    }

    @ApiModelProperty("Flocker represents a Flocker volume attached to a kubelet's host machine and exposed to the pod for its usage. This depends on the Flocker control service being running")
    public V1FlockerVolumeSource getFlocker() {
        return this.flocker;
    }

    public void setFlocker(V1FlockerVolumeSource flocker) {
        this.flocker = flocker;
    }

    public V1PersistentVolumeSpec gcePersistentDisk(V1GCEPersistentDiskVolumeSource gcePersistentDisk) {
        this.gcePersistentDisk = gcePersistentDisk;
        return this;
    }

    @ApiModelProperty("GCEPersistentDisk represents a GCE Disk resource that is attached to a kubelet's host machine and then exposed to the pod. Provisioned by an admin. More info: https://kubernetes.io/docs/concepts/storage/volumes#gcepersistentdisk")
    public V1GCEPersistentDiskVolumeSource getGcePersistentDisk() {
        return this.gcePersistentDisk;
    }

    public void setGcePersistentDisk(V1GCEPersistentDiskVolumeSource gcePersistentDisk) {
        this.gcePersistentDisk = gcePersistentDisk;
    }

    public V1PersistentVolumeSpec glusterfs(V1GlusterfsVolumeSource glusterfs) {
        this.glusterfs = glusterfs;
        return this;
    }

    @ApiModelProperty("Glusterfs represents a Glusterfs volume that is attached to a host and exposed to the pod. Provisioned by an admin. More info: https://releases.k8s.io/HEAD/examples/volumes/glusterfs/README.md")
    public V1GlusterfsVolumeSource getGlusterfs() {
        return this.glusterfs;
    }

    public void setGlusterfs(V1GlusterfsVolumeSource glusterfs) {
        this.glusterfs = glusterfs;
    }

    public V1PersistentVolumeSpec hostPath(V1HostPathVolumeSource hostPath) {
        this.hostPath = hostPath;
        return this;
    }

    @ApiModelProperty("HostPath represents a directory on the host. Provisioned by a developer or tester. This is useful for single-node development and testing only! On-host storage is not supported in any way and WILL NOT WORK in a multi-node cluster. More info: https://kubernetes.io/docs/concepts/storage/volumes#hostpath")
    public V1HostPathVolumeSource getHostPath() {
        return this.hostPath;
    }

    public void setHostPath(V1HostPathVolumeSource hostPath) {
        this.hostPath = hostPath;
    }

    public V1PersistentVolumeSpec iscsi(V1ISCSIVolumeSource iscsi) {
        this.iscsi = iscsi;
        return this;
    }

    @ApiModelProperty("ISCSI represents an ISCSI Disk resource that is attached to a kubelet's host machine and then exposed to the pod. Provisioned by an admin.")
    public V1ISCSIVolumeSource getIscsi() {
        return this.iscsi;
    }

    public void setIscsi(V1ISCSIVolumeSource iscsi) {
        this.iscsi = iscsi;
    }

    public V1PersistentVolumeSpec local(V1LocalVolumeSource local) {
        this.local = local;
        return this;
    }

    @ApiModelProperty("Local represents directly-attached storage with node affinity")
    public V1LocalVolumeSource getLocal() {
        return this.local;
    }

    public void setLocal(V1LocalVolumeSource local) {
        this.local = local;
    }

    public V1PersistentVolumeSpec mountOptions(List<String> mountOptions) {
        this.mountOptions = mountOptions;
        return this;
    }

    public V1PersistentVolumeSpec addMountOptionsItem(String mountOptionsItem) {
        if (this.mountOptions == null) {
            this.mountOptions = new ArrayList();
        }

        this.mountOptions.add(mountOptionsItem);
        return this;
    }

    @ApiModelProperty("A list of mount options, e.g. [\"ro\", \"soft\"]. Not validated - mount will simply fail if one is invalid. More info: https://kubernetes.io/docs/concepts/storage/persistent-volumes/#mount-options")
    public List<String> getMountOptions() {
        return this.mountOptions;
    }

    public void setMountOptions(List<String> mountOptions) {
        this.mountOptions = mountOptions;
    }

    public V1PersistentVolumeSpec nfs(V1NFSVolumeSource nfs) {
        this.nfs = nfs;
        return this;
    }

    @ApiModelProperty("NFS represents an NFS mount on the host. Provisioned by an admin. More info: https://kubernetes.io/docs/concepts/storage/volumes#nfs")
    public V1NFSVolumeSource getNfs() {
        return this.nfs;
    }

    public void setNfs(V1NFSVolumeSource nfs) {
        this.nfs = nfs;
    }

    public V1PersistentVolumeSpec persistentVolumeReclaimPolicy(String persistentVolumeReclaimPolicy) {
        this.persistentVolumeReclaimPolicy = persistentVolumeReclaimPolicy;
        return this;
    }

    @ApiModelProperty("What happens to a persistent volume when released from its claim. Valid options are Retain (default) and Recycle. Recycling must be supported by the volume plugin underlying this persistent volume. More info: https://kubernetes.io/docs/concepts/storage/persistent-volumes#reclaiming")
    public String getPersistentVolumeReclaimPolicy() {
        return this.persistentVolumeReclaimPolicy;
    }

    public void setPersistentVolumeReclaimPolicy(String persistentVolumeReclaimPolicy) {
        this.persistentVolumeReclaimPolicy = persistentVolumeReclaimPolicy;
    }

    public V1PersistentVolumeSpec photonPersistentDisk(V1PhotonPersistentDiskVolumeSource photonPersistentDisk) {
        this.photonPersistentDisk = photonPersistentDisk;
        return this;
    }

    @ApiModelProperty("PhotonPersistentDisk represents a PhotonController persistent disk attached and mounted on kubelets host machine")
    public V1PhotonPersistentDiskVolumeSource getPhotonPersistentDisk() {
        return this.photonPersistentDisk;
    }

    public void setPhotonPersistentDisk(V1PhotonPersistentDiskVolumeSource photonPersistentDisk) {
        this.photonPersistentDisk = photonPersistentDisk;
    }

    public V1PersistentVolumeSpec portworxVolume(V1PortworxVolumeSource portworxVolume) {
        this.portworxVolume = portworxVolume;
        return this;
    }

    @ApiModelProperty("PortworxVolume represents a portworx volume attached and mounted on kubelets host machine")
    public V1PortworxVolumeSource getPortworxVolume() {
        return this.portworxVolume;
    }

    public void setPortworxVolume(V1PortworxVolumeSource portworxVolume) {
        this.portworxVolume = portworxVolume;
    }

    public V1PersistentVolumeSpec quobyte(V1QuobyteVolumeSource quobyte) {
        this.quobyte = quobyte;
        return this;
    }

    @ApiModelProperty("Quobyte represents a Quobyte mount on the host that shares a pod's lifetime")
    public V1QuobyteVolumeSource getQuobyte() {
        return this.quobyte;
    }

    public void setQuobyte(V1QuobyteVolumeSource quobyte) {
        this.quobyte = quobyte;
    }

    public V1PersistentVolumeSpec rbd(V1RBDVolumeSource rbd) {
        this.rbd = rbd;
        return this;
    }

    @ApiModelProperty("RBD represents a Rados Block Device mount on the host that shares a pod's lifetime. More info: https://releases.k8s.io/HEAD/examples/volumes/rbd/README.md")
    public V1RBDVolumeSource getRbd() {
        return this.rbd;
    }

    public void setRbd(V1RBDVolumeSource rbd) {
        this.rbd = rbd;
    }

    public V1PersistentVolumeSpec scaleIO(V1ScaleIOPersistentVolumeSource scaleIO) {
        this.scaleIO = scaleIO;
        return this;
    }

    @ApiModelProperty("ScaleIO represents a ScaleIO persistent volume attached and mounted on Kubernetes nodes.")
    public V1ScaleIOPersistentVolumeSource getScaleIO() {
        return this.scaleIO;
    }

    public void setScaleIO(V1ScaleIOPersistentVolumeSource scaleIO) {
        this.scaleIO = scaleIO;
    }

    public V1PersistentVolumeSpec storageClassName(String storageClassName) {
        this.storageClassName = storageClassName;
        return this;
    }

    @ApiModelProperty("Name of StorageClass to which this persistent volume belongs. Empty value means that this volume does not belong to any StorageClass.")
    public String getStorageClassName() {
        return this.storageClassName;
    }

    public void setStorageClassName(String storageClassName) {
        this.storageClassName = storageClassName;
    }

    public V1PersistentVolumeSpec storageos(V1StorageOSPersistentVolumeSource storageos) {
        this.storageos = storageos;
        return this;
    }

    @ApiModelProperty("StorageOS represents a StorageOS volume that is attached to the kubelet's host machine and mounted into the pod More info: https://releases.k8s.io/HEAD/examples/volumes/storageos/README.md")
    public V1StorageOSPersistentVolumeSource getStorageos() {
        return this.storageos;
    }

    public void setStorageos(V1StorageOSPersistentVolumeSource storageos) {
        this.storageos = storageos;
    }

    public V1PersistentVolumeSpec vsphereVolume(V1VsphereVirtualDiskVolumeSource vsphereVolume) {
        this.vsphereVolume = vsphereVolume;
        return this;
    }

    @ApiModelProperty("VsphereVolume represents a vSphere volume attached and mounted on kubelets host machine")
    public V1VsphereVirtualDiskVolumeSource getVsphereVolume() {
        return this.vsphereVolume;
    }

    public void setVsphereVolume(V1VsphereVirtualDiskVolumeSource vsphereVolume) {
        this.vsphereVolume = vsphereVolume;
    }

    @ApiModelProperty("V1VolumeNodeAffinity represents node that volume will be bound to")
    public V1VolumeNodeAffinity getNodeAffinity() {
        return nodeAffinity;
    }

    public void setNodeAffinity(V1VolumeNodeAffinity nodeAffinity) {
        this.nodeAffinity = nodeAffinity;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1PersistentVolumeSpec v1PersistentVolumeSpec = (V1PersistentVolumeSpec) o;
            return Objects.equals(this.accessModes, v1PersistentVolumeSpec.accessModes) && Objects.equals(this.awsElasticBlockStore, v1PersistentVolumeSpec.awsElasticBlockStore) && Objects.equals(this.azureDisk, v1PersistentVolumeSpec.azureDisk) && Objects.equals(this.azureFile, v1PersistentVolumeSpec.azureFile) && Objects.equals(this.capacity, v1PersistentVolumeSpec.capacity) && Objects.equals(this.cephfs, v1PersistentVolumeSpec.cephfs) && Objects.equals(this.cinder, v1PersistentVolumeSpec.cinder) && Objects.equals(this.claimRef, v1PersistentVolumeSpec.claimRef) && Objects.equals(this.fc, v1PersistentVolumeSpec.fc) && Objects.equals(this.flexVolume, v1PersistentVolumeSpec.flexVolume) && Objects.equals(this.flocker, v1PersistentVolumeSpec.flocker) && Objects.equals(this.gcePersistentDisk, v1PersistentVolumeSpec.gcePersistentDisk) && Objects.equals(this.glusterfs, v1PersistentVolumeSpec.glusterfs) && Objects.equals(this.hostPath, v1PersistentVolumeSpec.hostPath) && Objects.equals(this.iscsi, v1PersistentVolumeSpec.iscsi) && Objects.equals(this.local, v1PersistentVolumeSpec.local) && Objects.equals(this.mountOptions, v1PersistentVolumeSpec.mountOptions) && Objects.equals(this.nfs, v1PersistentVolumeSpec.nfs) && Objects.equals(this.persistentVolumeReclaimPolicy, v1PersistentVolumeSpec.persistentVolumeReclaimPolicy) && Objects.equals(this.photonPersistentDisk, v1PersistentVolumeSpec.photonPersistentDisk) && Objects.equals(this.portworxVolume, v1PersistentVolumeSpec.portworxVolume) && Objects.equals(this.quobyte, v1PersistentVolumeSpec.quobyte) && Objects.equals(this.rbd, v1PersistentVolumeSpec.rbd) && Objects.equals(this.scaleIO, v1PersistentVolumeSpec.scaleIO) && Objects.equals(this.storageClassName, v1PersistentVolumeSpec.storageClassName) && Objects.equals(this.storageos, v1PersistentVolumeSpec.storageos) && Objects.equals(this.vsphereVolume, v1PersistentVolumeSpec.vsphereVolume) && Objects.equals(this.nodeAffinity, v1PersistentVolumeSpec.nodeAffinity);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.accessModes, this.awsElasticBlockStore, this.azureDisk, this.azureFile, this.capacity, this.cephfs, this.cinder, this.claimRef, this.fc, this.flexVolume, this.flocker, this.gcePersistentDisk, this.glusterfs, this.hostPath, this.iscsi, this.local, this.mountOptions, this.nfs, this.persistentVolumeReclaimPolicy, this.photonPersistentDisk, this.portworxVolume, this.quobyte, this.rbd, this.scaleIO, this.storageClassName, this.storageos, this.vsphereVolume, this.nodeAffinity});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1PersistentVolumeSpec {\n");
        sb.append("    accessModes: ").append(this.toIndentedString(this.accessModes)).append("\n");
        sb.append("    awsElasticBlockStore: ").append(this.toIndentedString(this.awsElasticBlockStore)).append("\n");
        sb.append("    azureDisk: ").append(this.toIndentedString(this.azureDisk)).append("\n");
        sb.append("    azureFile: ").append(this.toIndentedString(this.azureFile)).append("\n");
        sb.append("    capacity: ").append(this.toIndentedString(this.capacity)).append("\n");
        sb.append("    cephfs: ").append(this.toIndentedString(this.cephfs)).append("\n");
        sb.append("    cinder: ").append(this.toIndentedString(this.cinder)).append("\n");
        sb.append("    claimRef: ").append(this.toIndentedString(this.claimRef)).append("\n");
        sb.append("    fc: ").append(this.toIndentedString(this.fc)).append("\n");
        sb.append("    flexVolume: ").append(this.toIndentedString(this.flexVolume)).append("\n");
        sb.append("    flocker: ").append(this.toIndentedString(this.flocker)).append("\n");
        sb.append("    gcePersistentDisk: ").append(this.toIndentedString(this.gcePersistentDisk)).append("\n");
        sb.append("    glusterfs: ").append(this.toIndentedString(this.glusterfs)).append("\n");
        sb.append("    hostPath: ").append(this.toIndentedString(this.hostPath)).append("\n");
        sb.append("    iscsi: ").append(this.toIndentedString(this.iscsi)).append("\n");
        sb.append("    local: ").append(this.toIndentedString(this.local)).append("\n");
        sb.append("    mountOptions: ").append(this.toIndentedString(this.mountOptions)).append("\n");
        sb.append("    nfs: ").append(this.toIndentedString(this.nfs)).append("\n");
        sb.append("    persistentVolumeReclaimPolicy: ").append(this.toIndentedString(this.persistentVolumeReclaimPolicy)).append("\n");
        sb.append("    photonPersistentDisk: ").append(this.toIndentedString(this.photonPersistentDisk)).append("\n");
        sb.append("    portworxVolume: ").append(this.toIndentedString(this.portworxVolume)).append("\n");
        sb.append("    quobyte: ").append(this.toIndentedString(this.quobyte)).append("\n");
        sb.append("    rbd: ").append(this.toIndentedString(this.rbd)).append("\n");
        sb.append("    scaleIO: ").append(this.toIndentedString(this.scaleIO)).append("\n");
        sb.append("    storageClassName: ").append(this.toIndentedString(this.storageClassName)).append("\n");
        sb.append("    storageos: ").append(this.toIndentedString(this.storageos)).append("\n");
        sb.append("    vsphereVolume: ").append(this.toIndentedString(this.vsphereVolume)).append("\n");
        sb.append("    nodeAffinity: ").append(this.toIndentedString(this.nodeAffinity)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
