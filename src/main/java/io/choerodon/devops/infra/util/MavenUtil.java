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

    public static String calculateDownloadUrl(String repoUrl, String groupId, String artifactId, String version, String artifactType) {
        String downloadUrl = "";
        // SNAPSHOT类型
        if (version.contains(BaseConstants.Symbol.SLASH)) {
            downloadUrl = appendWithSlash(repoUrl, groupId.replace(BaseConstants.Symbol.POINT, BaseConstants.Symbol.SLASH));
            downloadUrl = appendWithSlash(downloadUrl, artifactId);
            downloadUrl = appendWithSlash(downloadUrl, version + "." + artifactType);
        } else {
            // RELEASE类型
            downloadUrl = appendWithSlash(repoUrl, groupId.replace(BaseConstants.Symbol.POINT, BaseConstants.Symbol.SLASH));
            downloadUrl = appendWithSlash(downloadUrl, artifactId);
            downloadUrl = appendWithSlash(downloadUrl, version);
            downloadUrl = appendWithSlash(downloadUrl, artifactId + BaseConstants.Symbol.MIDDLE_LINE + version + "." + artifactType);
        }
        return downloadUrl;
    }

    public static String appendWithSlash(String source, String str) {
        if (source.endsWith("/")) {
            source = source.substring(0, source.length() - 1);
        }
        if (str.startsWith("/")) {
            str = str.substring(1, str.length());
        }
        return source + BaseConstants.Symbol.SLASH + str;
    }
}
