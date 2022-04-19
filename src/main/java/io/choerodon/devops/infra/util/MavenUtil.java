package io.choerodon.devops.infra.util;

import org.hzero.core.base.BaseConstants;

import io.choerodon.devops.api.vo.market.JarReleaseConfigVO;

public class MavenUtil {

    private MavenUtil() {

    }

    public static String getDownloadUrl(JarReleaseConfigVO jarReleaseConfigVO) {
        //拼接download URL http://xxxx:17145/repository/lilly-snapshot/io/choerodon/springboot/0.0.1-SNAPSHOT/springboot-0.0.1-20210106.020444-2.jar
        return jarReleaseConfigVO.getNexusRepoUrl() + BaseConstants.Symbol.SLASH +
                jarReleaseConfigVO.getGroupId().replace(".", "/") +
                BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getArtifactId() + BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getVersion() +
                BaseConstants.Symbol.SLASH + jarReleaseConfigVO.getArtifactId() + BaseConstants.Symbol.MIDDLE_LINE + jarReleaseConfigVO.getSnapshotTimestamp() + ".jar";
    }
}
