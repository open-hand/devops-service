package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.PipelineConstants.DEVOPS_CI_MAVEN_REPOSITORY_TYPE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.xml.sax.helpers.DefaultHandler;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.dto.CiPipelineMavenDTO;
import io.choerodon.devops.infra.dto.maven.*;

/**
 * 用于生成Maven的Settings文件的工具类
 *
 * @author zmf
 * @since 20-4-14
 */
public class MavenSettingsUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenSettingsUtil.class);

    private MavenSettingsUtil() {
    }

    private static final String DEFAULT_PROFILE_ID = "default";


    /**
     * 数组字节流的初始大小
     */
    private static final int BYTE_ARRAY_INIT_SIZE = 3000;

    private static final String PARENT = "parent";
    private static final String GROUP_ID = "groupId";
    private static final String VERSION = "version";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String PACKAGING = "packaging";


    public static String buildSettings(List<MavenRepoVO> mavenRepoList, List<Proxy> proxies) {
        List<Server> servers = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();

        mavenRepoList.forEach(m -> {
            if (m.getType() != null) {
                String[] types = m.getType().split(GitOpsConstants.COMMA);
                if (types.length > 2) {
                    throw new CommonException(DEVOPS_CI_MAVEN_REPOSITORY_TYPE, m.getType());
                }
            }
            if (Boolean.TRUE.equals(m.getPrivateRepo())) {
                servers.add(new Server(Objects.requireNonNull(m.getName()), Objects.requireNonNull(m.getUsername()), Objects.requireNonNull(m.getPassword())));
            }
            repositories.add(new Repository(
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getName()),
                    Objects.requireNonNull(m.getUrl()),
                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.RELEASE)),
                    m.getType() == null ? null : new RepositoryPolicy(m.getType().contains(GitOpsConstants.SNAPSHOT))));
        });
        return MavenSettingsUtil.generateMavenSettings(servers, repositories, proxies);
    }

    /**
     * 生成maven的settings文件
     *
     * @param servers      用户认证信息
     * @param repositories 仓库信息
     * @return 生成的settings文件
     */
    public static String generateMavenSettings(List<Server> servers, List<Repository> repositories, List<Proxy> proxies) {
        try {
            // 获取JAXB的上下文环境，需要传入具体的 Java bean -> 这里使用Settings
            JAXBContext context = JAXBContext.newInstance(Settings.class);

            // 创建 Marshaller 实例
            Marshaller marshaller = context.createMarshaller();

            // 设置转换参数 -> 序列化器是否格式化输出
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // 将所需对象序列化到字节数组流中
            ByteArrayOutputStream out = new ByteArrayOutputStream(BYTE_ARRAY_INIT_SIZE);
            marshaller.marshal(initSettings(servers, repositories, proxies), out);

            // 将字节流转为字节数组再转到字符串
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (JAXBException e) {
            LOGGER.warn("Maven util: internal errors: failed to generate settings: servers: {}, repositories: {}", servers, repositories);
            throw new CommonException("devops.generate.maven.settings");
        }
    }

    /**
     * 解析pom文件内容, 生成填充了 groupId, artifactId, version三个字段的{@link CiPipelineMavenDTO}实例
     *
     * @param pomContent pom文件内容
     * @return 填充了 groupId, artifactId, version三个字段的{@link CiPipelineMavenDTO}实例
     */
    public static CiPipelineMavenDTO parsePom(String pomContent) {
        String parentGroupId = null;
        String parentArtifactId = null;
        String parentVersion = null;
        String parentArtifactType = null;
        String groupId = null;
        String artifactId = null;
        String version = null;
        String artifactType = null;
        // 解析pom
        SAXReader reader = new SAXReader();
        try {
            // 通过reader对象的read方法加载pom.xml文件内容, 获取document对象
            org.dom4j.Document document = reader.read(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
            // 通过document对象获取根节点 project
            Element project = document.getRootElement();
            // 通过element对象的elementIterator方法获取迭代器
            Iterator<Element> it = project.elementIterator();
            // 遍历迭代器，获取根节点中的信息
            while (it.hasNext()) {
                Element element = it.next();
                // 获取parent节点下信息
                if (PARENT.equals(element.getName())) {
                    Iterator<Element> itt = element.elementIterator();
                    while (itt.hasNext()) {
                        Element parentElement = itt.next();
                        if (GROUP_ID.equals(parentElement.getName())) {
                            parentGroupId = parentElement.getStringValue();
                        }
                        if (ARTIFACT_ID.equals(parentElement.getName())) {
                            parentArtifactId = parentElement.getStringValue();
                        }
                        if (VERSION.equals(parentElement.getName())) {
                            parentVersion = parentElement.getStringValue();
                        }
                    }
                }
                if (GROUP_ID.equals(element.getName())) {
                    groupId = element.getStringValue();
                }
                if (ARTIFACT_ID.equals(element.getName())) {
                    artifactId = element.getStringValue();
                }
                if (VERSION.equals(element.getName())) {
                    version = element.getStringValue();
                }
                if (PACKAGING.equals(element.getName())) {
                    artifactType = element.getStringValue();
                }
            }
        } catch (DocumentException e) {
            throw new CommonException("devops.parse.pom", e.getCause());
        }
        CiPipelineMavenDTO ciPipelineMavenDTO = new CiPipelineMavenDTO();
        ciPipelineMavenDTO.setArtifactId(artifactId != null ? artifactId : parentArtifactId);
        ciPipelineMavenDTO.setGroupId(groupId != null ? groupId : parentGroupId);
        ciPipelineMavenDTO.setVersion(version != null ? version : parentVersion);
        ciPipelineMavenDTO.setArtifactType(artifactType != null ? artifactType : "jar");
        return ciPipelineMavenDTO;
    }

    /**
     * 校验字符串内容是否符合xml文件
     *
     * @param xmlFormatContent xml格式的字符串内容
     * @return true表示是, false表示不是
     */
    public static boolean isXmlFormat(String xmlFormatContent) {
        Assert.hasLength(xmlFormatContent, "Xml content to be judged can not be null or empty");
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DefaultHandler handler = new DefaultHandler();
            parser.parse(new ByteArrayInputStream(xmlFormatContent.getBytes(StandardCharsets.UTF_8)), handler);
        } catch (Exception e) {
            // 如果解析出错，则不是xml格式的内容
            return false;
        }
        return true;
    }

    private static Settings initSettings(List<Server> servers, List<Repository> repositories, List<Proxy> proxies) {
        return new Settings(servers, initProfiles(repositories), proxies);
    }

    private static List<Profile> initProfiles(List<Repository> repositories) {
        List<PluginRepository> pluginRepositories = ConvertUtils.convertList(repositories, PluginRepository.class);
        return ArrayUtil.singleAsList(new Profile(DEFAULT_PROFILE_ID, new Activation(true), repositories, pluginRepositories));
    }
}
