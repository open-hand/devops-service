package io.choerodon.devops.infra.enums;

/**
 * @author zmf
 * @since 2020/12/22
 */
public enum DevopsRegistryRepoType {
    DEFAULT_REPO("DEFAULT_REPO"),
    CUSTOM_REPO("CUSTOM_REPO"),
    MARKET_REPO("MARKET_REPO");
    private final String type;

    DevopsRegistryRepoType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
