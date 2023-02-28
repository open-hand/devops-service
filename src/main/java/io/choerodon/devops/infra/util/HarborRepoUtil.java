package io.choerodon.devops.infra.util;

import io.choerodon.devops.api.vo.ImageRepoInfoVO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.enums.DevopsRegistryRepoType;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/2/28 9:27
 */
public class HarborRepoUtil {
    public static ImageRepoInfoVO getHarborRepoInfo(String repoType, String repoCode, HarborRepoDTO harborRepoDTO) {
        ImageRepoInfoVO imageRepoInfoVO;
        String dockerRegistry;
        String groupName;
        String dockerUsername;
        String dockerPassword;
        if (DevopsRegistryRepoType.CUSTOM_REPO.getType().equals(repoType)) {
            dockerRegistry = harborRepoDTO.getHarborRepoConfig().getRepoUrl();
            groupName = harborRepoDTO.getHarborRepoConfig().getRepoName();
            dockerUsername = harborRepoDTO.getHarborRepoConfig().getLoginName();
            dockerPassword = harborRepoDTO.getHarborRepoConfig().getPassword();

        } else {
            dockerRegistry = harborRepoDTO.getHarborRepoConfig().getRepoUrl();
            groupName = harborRepoDTO.getHarborRepoConfig().getRepoName();
            dockerUsername = harborRepoDTO.getPushRobot().getName();
            dockerPassword = harborRepoDTO.getPushRobot().getToken();
        }
        imageRepoInfoVO = new ImageRepoInfoVO();
        imageRepoInfoVO.setDockerRegistry(trimPrefix(dockerRegistry));
        imageRepoInfoVO.setGroupName(groupName);
        imageRepoInfoVO.setDockerUsername(dockerUsername);
        imageRepoInfoVO.setDockerPassword(dockerPassword);
        imageRepoInfoVO.setRepoType(repoType);
        imageRepoInfoVO.setRepoCode(groupName);
        return imageRepoInfoVO;
    }

    public static String trimPrefix(String dockerRegistry) {
        String dockerUrl = dockerRegistry.replace("http://", "").replace("https://", "");
        return dockerUrl.endsWith("/") ? dockerUrl.substring(0, dockerUrl.length() - 1) : dockerUrl;
    }
}
