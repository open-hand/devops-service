package io.choerodon.devops.infra.util;

import java.util.Objects;

/**
 * 分配用于操作的本地文件路径
 * 目前Dockerfile生成容器中 /choerodon 目录是有权限的
 *
 * @author zmf
 * @since 2/27/20
 */
public class FileSystemFilePathAllocator {
    private static final String FILE_SEPARATOR = java.io.File.separator;
    /**
     * 这里目录没有带/是相对于jar包的相对路径
     */
    private static final String ROOT_DIR = "choerodon" + FILE_SEPARATOR;

    /**
     * 获取文件路径
     *
     * @param relativeFilePath 相对文件路径
     * @return 文件路径
     */
    public static String getFilePath(String relativeFilePath) {
        return ROOT_DIR + Objects.requireNonNull(relativeFilePath);
    }
}
