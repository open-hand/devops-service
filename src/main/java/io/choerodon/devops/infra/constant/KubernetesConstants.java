package io.choerodon.devops.infra.constant;

/**
 * @author zmf
 * @since 11/1/19
 */
public class KubernetesConstants {
    private KubernetesConstants()  {
    }

    public static final String CHOERODON_IO_RESOURCE = "choerodon.io/resource";
    public static final String CHOERODON_IO_V1_COMMAND = "choerodon.io/v1-command";

    public static final String METADATA = "metadata";
    public static final String CUSTOM = "custom";
    public static final String LABELS = "labels";
    public static final String KIND = "kind";
    public static final String NAME = "name";

    public static final String STORAGE = "storage";
    public static final String TYPE = "type";
}
