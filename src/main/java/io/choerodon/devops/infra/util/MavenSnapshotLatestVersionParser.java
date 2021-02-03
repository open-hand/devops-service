package io.choerodon.devops.infra.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import io.choerodon.core.exception.CommonException;

/**
 * @author zmf
 * @since 1/7/21
 */
public final class MavenSnapshotLatestVersionParser {
    private static final String VERSIONING = "versioning";
    private static final String SNAPSHOT_VERSIONS = "snapshotVersions";
    private static final String SNAPSHOT_VERSION = "snapshotVersion";
    private static final String VALUE = "value";

    private MavenSnapshotLatestVersionParser() {
    }

    /**
     * 从样例数据中获取snapshot版本的最新的小版本
     * @param metadataXml 样例数据见本文件最后
     * @return 小版本号，样例数据： 0.0.1-20201209.022442-3
     */
    @Nullable
    public static String parseVersion(@Nullable String metadataXml) {
        if (metadataXml == null) {
            return null;
        }
        // 解析metadata.xml以获取snapshot的最新的小版本
        SAXReader reader = new SAXReader();
        try {
            // 通过reader对象的read方法加载pom.xml文件内容, 获取document对象
            org.dom4j.Document document = reader.read(new ByteArrayInputStream(metadataXml.getBytes(StandardCharsets.UTF_8)));
            // 通过document对象获取根节点
            Element metadata = document.getRootElement();
            // 通过element对象的elementIterator方法获取迭代器
            Iterator<Element> metaSons = metadata.elementIterator();
            // 遍历迭代器，获取根节点中的信息
            Element element;
            while (metaSons.hasNext()) {
                element = metaSons.next();
                if (VERSIONING.equals(element.getName())) {
                    Iterator<Element> versioningSons = element.elementIterator();
                    while (versioningSons.hasNext()) {
                        element = versioningSons.next();
                        if (SNAPSHOT_VERSIONS.equals(element.getName())) {
                            Iterator<Element> snapshotVersionsSons = element.elementIterator();
                            while (snapshotVersionsSons.hasNext()) {
                                element = snapshotVersionsSons.next();
                                if (SNAPSHOT_VERSION.equals(element.getName())) {
                                    Iterator<Element> snapshotVersionSons = element.elementIterator();
                                    while (snapshotVersionSons.hasNext()) {
                                        element = snapshotVersionSons.next();
                                        if (VALUE.equals(element.getName())) {
                                            return element.getStringValue();
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            throw new CommonException("error.parse.metadata", e);
        }
        return null;
    }

    /**
     * <?xml version="1.0" encoding="UTF-8"?>
     * <metadata modelVersion="1.1.0">
     *   <groupId>io.choerodon</groupId>
     *   <artifactId>springboot</artifactId>
     *   <version>0.0.1-SNAPSHOT</version>
     *   <versioning>
     *     <snapshot>
     *       <timestamp>20201209.022442</timestamp>
     *       <buildNumber>3</buildNumber>
     *     </snapshot>
     *     <lastUpdated>20201209022442</lastUpdated>
     *     <snapshotVersions>
     *       <snapshotVersion>
     *         <extension>jar</extension>
     *         <value>0.0.1-20201209.022442-3</value>
     *         <updated>20201209022442</updated>
     *       </snapshotVersion>
     *       <snapshotVersion>
     *         <extension>pom</extension>
     *         <value>0.0.1-20201209.022442-3</value>
     *         <updated>20201209022442</updated>
     *       </snapshotVersion>
     *     </snapshotVersions>
     *   </versioning>
     * </metadata>
     */
}
