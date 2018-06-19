package io.choerodon.devops.infra.common.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.valueobject.HighlightMarker;
import io.choerodon.devops.domain.application.valueobject.ReplaceMarker;
import io.choerodon.devops.domain.application.valueobject.ReplaceResult;

/**
 * Created by younger on 2018/4/13.
 */
public class FileUtil {
    private static final int BUFFER_SIZE = 2048;
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    private static final Yaml yaml = new Yaml();

    private FileUtil() {
    }

    /**
     * 通过inputStream流 替换文件的参数
     *
     * @param inputStream 流
     * @param params      参数
     * @return String
     */
    public static String replaceReturnString(InputStream inputStream, Map<String, String> params) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            byte[] b = new byte[32768];
            for (int n; (n = inputStream.read(b)) != -1; ) {
                String content = new String(b, 0, n);
                if (params != null) {
                    for (Object o : params.entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        content = content.replace(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                    }
                }
                stringBuilder.append(content);
                stringBuilder.append(System.getProperty("line.separator"));
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new CommonException("error.param.render");
        }
    }

    /**
     * 通过inputStream流 替换文件的参数 ，将file转换为流渲染参数后写回文件
     *
     * @param file   文件
     * @param params 参数
     */
    public static void replaceReturnFile(File file, Map<String, String> params) {
        File[] files = file.listFiles();
        for (File a : files) {
            if (a.getName().equals(".git") || a.getName().endsWith(".xlsx")) {
                continue;
            }
            File newFile = null;
            if (a.getName().equals("model-service")) {
                String parentPath = a.getParent();
                newFile = new File(parentPath + File.separator + params.get("{{service.code}}"));
                a.renameTo(newFile);
            }
            if (newFile == null) {
                fileToInputStream(a, params);
            } else {
                fileToInputStream(newFile, params);
            }
        }
    }

    /**
     * 文件转输入流
     *
     * @param file   来源文件
     * @param params 参数
     */
    public static void fileToInputStream(File file, Map<String, String> params) {
        if (file.isDirectory()) {
            replaceReturnFile(file, params);
        } else {
            try (InputStream inputStream = new FileInputStream(file)) {
                FileUtils.writeStringToFile(file, replaceReturnString(inputStream, params));
            } catch (IOException e) {
                throw new CommonException("error.param.replace");
            }
        }
    }

    /**
     * 将文件上传到项目指定目录
     *
     * @param path  项目目录
     * @param files 来源文件
     */
    public static String multipartFileToFile(String path, MultipartFile files) {
        File repo = new File(path);
        try {
            if (!repo.exists()) {
                repo.mkdirs();
            }
            try (BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(new File(
                            path, files.getOriginalFilename())))) {
                out.write(files.getBytes());
                out.flush();
            }
        } catch (IOException e) {
            throw new CommonException("error.file.transfer");
        }
        return path + System.getProperty("file.separator") + files.getOriginalFilename();
    }

    /**
     * yaml格式转字符串
     *
     * @param path 来源地址
     * @return string
     * @throws IOException ioexception
     */
    public static String yamltoString(String path) throws IOException {
        InputStream inputStream = FileUtil.class.getResourceAsStream(path);
        Reader r = new InputStreamReader(inputStream);
        try (StringWriter w = new StringWriter()) {
            copy(r, w);
            return w.toString();
        }
    }

    /**
     * yaml转json
     *
     * @param path 来源地址
     * @return string
     * @throws IOException ioexception
     */
    public static String yamltoJson(String path) throws IOException {
        Gson gs = new Gson();
        Map<String, Object> loaded = null;
        InputStream inputStream = new FileInputStream(new File(path));
        loaded = (Map<String, Object>) yaml.load(inputStream);
        return gs.toJson(loaded);
    }

    /**
     * yaml字符串转json
     *
     * @param yamlString yaml字符串
     * @return string
     */
    public static String yamlStringtoJson(String yamlString) {
        Gson gs = new Gson();
        ByteArrayInputStream stream = new ByteArrayInputStream(yamlString.getBytes());
        Map<String, Object> loaded = (Map<String, Object>) yaml.load(stream);
        return gs.toJson(loaded);
    }

    /**
     * json转yaml
     *
     * @param jsonValue json字符串
     * @return
     */
    public static String jsonToYaml(String jsonValue) {
        JsonNode jsonNodeTree = null;
        String json = "";
        try {
            jsonNodeTree = new ObjectMapper().readTree(jsonValue);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        }
        try {
            json = new YAMLMapper().writeValueAsString(jsonNodeTree);
        } catch (JsonProcessingException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        }
        return json;
    }

    private static void copy(Reader reader, Writer writer) throws IOException {
        char[] buffer = new char[8192];
        int len;
        for (; ; ) {
            len = reader.read(buffer);
            if (len > 0) {
                writer.write(buffer, 0, len);
            } else {
                writer.flush();
                break;
            }
        }
    }

    /**
     * 解压tgz包
     */
    public static void unTarGZ(String file, String destDir) {
        File tarFile = new File(file);
        unTarGZ(tarFile, destDir);
    }

    /**
     * 解压tgz包
     */
    public static void unTarGZ(File tarFile, String destDir) {
        if (StringUtils.isBlank(destDir)) {
            destDir = tarFile.getParent();
        }
        destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
        try {
            unTar(new GzipCompressorInputStream(new FileInputStream(tarFile)), destDir);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        }
    }

    private static void unTar(InputStream inputStream, String destDir) {

        TarArchiveInputStream tarIn = new TarArchiveInputStream(inputStream, BUFFER_SIZE);
        TarArchiveEntry entry = null;
        try {
            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    createDirectory(destDir, entry.getName());
                } else {
                    File tmpFile = new File(destDir + File.separator + entry.getName());
                    createDirectory(tmpFile.getParent() + File.separator, null);
                    try (OutputStream out = new FileOutputStream(tmpFile)) {
                        int length = 0;
                        byte[] b = new byte[2048];
                        while ((length = tarIn.read(b)) != -1) {
                            out.write(b, 0, length);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        } finally {
            IOUtils.closeQuietly(tarIn);
        }
    }

    /**
     * 创建目录
     */
    public static void createDirectory(String outputDir, String subDir) {
        File file = new File(outputDir);
        if (!(subDir == null || subDir.trim().equals(""))) {
            file = new File(outputDir + File.separator + subDir);
        }
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 从文件夹中查找指定文件
     */
    public static File queryFileFromFiles(File file, String fileName) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    file1 = queryFileFromFiles(file1, fileName);
                }
                if (file1 != null && file1.getName().equals(fileName)) {
                    return file1;
                }
            }
        }
        return null;
    }

    /**
     * 删除文件
     */
    public static void deleteFile(File file) {
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        }
    }

    /**
     * 获取文件总行数
     *
     * @param file 目标文件
     * @return 文件函数
     */
    public static int getFileTotalLine(String file) {
        Integer totalLine = 0;
        try (ByteArrayInputStream byteArrayInputStream =
                     new ByteArrayInputStream(file.getBytes(Charset.forName("utf8")))) {
            try (InputStreamReader inputStreamReader =
                         new InputStreamReader(byteArrayInputStream, Charset.forName("utf8"))) {
                try (BufferedReader br = new BufferedReader(inputStreamReader)) {
                    String lineTxt;
                    while ((lineTxt = br.readLine()) != null) {
                        totalLine =  totalLine + 1;
                    }
                }
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return totalLine;
    }


    /**
     * 合并２个json字符串
     */
    public static String mergeJsonString(String jsonString, String newJsonString) {
        JSONObject jsonOne = JSONObject.parseObject(jsonString);
        JSONObject jsonTwo = JSONObject.parseObject(newJsonString);
        JSONObject jsonThree = new JSONObject();
        jsonThree.putAll(jsonOne);
        jsonThree.putAll(jsonTwo);
        return jsonThree.toJSONString();
    }


    /**
     * 结果以新的yaml结构为主,主要将旧yaml文件的属性值替换至新yaml文件中
     *
     * @param yamlNew new yaml ,to present
     * @param yamlOld old yaml
     * @return result yaml and highlightMarkers
     */
    public static ReplaceResult replace(String yamlNew, String yamlOld) {
        Composer composerNew = new Composer(new ParserImpl(new StreamReader(yamlNew)), new Resolver());
        Composer composerOld = new Composer(new ParserImpl(new StreamReader(yamlOld)), new Resolver());
        Node nodeNew = composerNew.getSingleNode();
        Node nodeOld = composerOld.getSingleNode();
        if (!(nodeNew instanceof MappingNode) || !(nodeOld instanceof MappingNode)) {
            logger.info("not mapping node");
            return null;
        }
        List<ReplaceMarker> replaceMarkers = new ArrayList<>();
        compare((MappingNode) nodeOld, (MappingNode) nodeNew, replaceMarkers);
        List<HighlightMarker> highlightMarks = new ArrayList<>();
        String result = replace(yamlNew, replaceMarkers, highlightMarks);
        ReplaceResult replaceResult = new ReplaceResult();
        replaceResult.setYaml(result);
        replaceResult.setHighlightMarkers(highlightMarks);
        return replaceResult;
    }

    private static String replace(String yaml, List<ReplaceMarker> replaceMarkers, List<HighlightMarker> highlights) {
        String temp = yaml;
        if (highlights == null) {
            highlights = new ArrayList<>();
        }
        int lengthChangeSum = 0;

        int len = replaceMarkers.size();

        int[] index = new int[len];
        int[] values = new int[len];

        for (int i = 0; i < len; i++) {
            values[i] = replaceMarkers.get(i).getLine();
            index[i] = i;
        }

        int tem;
        int tempIndex;
        for (int i = 0; i < len; i++) {
            for (int j = len - 1; j > i; j--) {
                if (values[j] < values[j - 1]) {
                    tem = values[j];
                    values[j] = values[j - 1];
                    values[j - 1] = tem;

                    tempIndex = index[j - 1];
                    index[j - 1] = index[j];
                    index[j] = tempIndex;


                }
            }
        }

        ReplaceMarker replaceMarker;
        for (int i = 0; i < len; i++) {
            replaceMarker = replaceMarkers.get(index[i]);
            int originalLength = replaceMarker.getEndIndex() - replaceMarker.getStartIndex();
            int replaceLength = replaceMarker.getToReplace().length();
            int lengthChange = replaceLength - originalLength;
            String before = temp.substring(0, replaceMarker.getStartIndex() + lengthChangeSum);
            String after = temp.substring(replaceMarker.getEndIndex() + lengthChangeSum);
            temp = before + replaceMarker.getToReplace() + after;
            HighlightMarker highlightMark = new HighlightMarker();
            highlightMark.setStartIndex(replaceMarker.getStartIndex() + lengthChangeSum);
            highlightMark.setEndIndex(highlightMark.getStartIndex() + replaceLength);
            highlightMark.setLine(replaceMarker.getLine());
            highlightMark.setStartColumn(replaceMarker.getStartColumn());
            highlightMark.setEndColumn(replaceMarker.getStartColumn() + replaceLength);
            highlights.add(highlightMark);
            lengthChangeSum += lengthChange;
        }
        return temp;
    }

    //从将old的值替换至新的值
    private static void compare(MappingNode oldMapping, MappingNode newMapping, List<ReplaceMarker> replaceMarkers) {
        List<NodeTuple> oldRootTuple = oldMapping.getValue();
        List<NodeTuple> newRootTuple = newMapping.getValue();
        for (NodeTuple oldTuple : oldRootTuple) {
            Node oldKeyNode = oldTuple.getKeyNode();
            if (oldKeyNode instanceof ScalarNode) {
                ScalarNode scalarKeyNode = (ScalarNode) oldKeyNode;
                Node oldValue = oldTuple.getValueNode();
                if (oldValue != null && oldValue instanceof ScalarNode) {
                    ScalarNode oldValueScalar = (ScalarNode) oldValue;
                    ScalarNode newValueNode = getKeyValue(scalarKeyNode.getValue(), newRootTuple);
                    if (newValueNode != null && !oldValueScalar.getValue().equals(newValueNode.getValue())) {
                        ReplaceMarker replaceMarker = new ReplaceMarker();
                        replaceMarker.setStartIndex(newValueNode.getStartMark().getIndex());
                        replaceMarker.setEndIndex(newValueNode.getEndMark().getIndex());
                        replaceMarker.setStartColumn(newValueNode.getStartMark().getColumn());
                        replaceMarker.setEndColumn(newValueNode.getEndMark().getColumn());
                        replaceMarker.setLine(newValueNode.getStartMark().getLine());
                        if (newValueNode.getValue().isEmpty()) {
                            replaceMarker.setToReplace(" " + oldValueScalar.getValue());
                        } else {
                            replaceMarker.setToReplace(oldValueScalar.getValue());
                        }
                        //记录相关并进行替换
                        replaceMarkers.add(replaceMarker);
                    }
                } else if (oldValue instanceof MappingNode) {
                    MappingNode vaMappingNode = getKeyMapping(scalarKeyNode.getValue(), newRootTuple);
                    if (vaMappingNode != null) {
                        MappingNode oldMappingNode = (MappingNode) oldValue;
                        compare(oldMappingNode, vaMappingNode, replaceMarkers);
                    }
                }
            }
        }

    }

    //检查同一层是否存在该key
    private static MappingNode getKeyMapping(String key, List<NodeTuple> tuples) {
        for (NodeTuple nodeTuple : tuples) {
            Node keyNode = nodeTuple.getKeyNode();
            if (keyNode instanceof ScalarNode) {
                ScalarNode scalarKeyNode = (ScalarNode) keyNode;
                if (scalarKeyNode.getValue().equals(key)) {
                    if (nodeTuple.getValueNode() instanceof MappingNode) {
                        return (MappingNode) nodeTuple.getValueNode();
                    } else {
                        logger.info("found key but value is not mapping");
                        return null;
                    }
                }
            }
        }
        logger.info("not found key in tuple");
        return null;
    }

    //检查同一层是否存在该key
    private static ScalarNode getKeyValue(String key, List<NodeTuple> tuples) {
        for (NodeTuple nodeTuple : tuples) {
            Node keyNode = nodeTuple.getKeyNode();
            if (keyNode instanceof ScalarNode) {
                ScalarNode scalarKeyNode = (ScalarNode) keyNode;
                if (scalarKeyNode.getValue().equals(key)) {
                    if (nodeTuple.getValueNode() instanceof ScalarNode) {
                        return (ScalarNode) nodeTuple.getValueNode();
                    } else {
                        logger.info("found key but value is not scalar");
                        return null;
                    }
                }
            }
        }
        logger.info("not found key in tuple");
        return null;
    }

    /**
     * format yaml
     *
     * @param value json value
     * @return yaml
     */
    public static String jungeValueFormat(String value) {
        try {
            JSONObject.parseObject(value);
            value = FileUtil.jsonToYaml(value);
            return value;
        } catch (Exception ignored) {
            return value;
        }
    }

    /**
     * yaml format
     *
     * @param yaml yaml value
     */
    public static void jungeYamlFormat(String yaml) {
        try {
            Composer composer = new Composer(new ParserImpl(new StreamReader(yaml)), new Resolver());
            composer.getSingleNode();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    /**
     * 获取目录下 README.md 文件内容
     *
     * @param path 目录路径
     * @return README.md 文件内容
     */
    public static String getReadme(String path) {
        String readme = "";
        File readmeFile = null;
        try {
            readmeFile = FileUtil.queryFileFromFiles(new File(path), "README.md");
        } catch (Exception e) {
            logger.info("file not found");
            readme = "# 暂无。";
        }
        if (readme.isEmpty()) {
            readme = readmeFile == null
                    ? "# 暂无"
                    : getFileContent(readmeFile);
        }
        return readme;
    }

    /**
     * 读取文件内容
     *
     * @param file 文件
     * @return 文件内容
     */
    public static String getFileContent(File file) {
        StringBuilder content = new StringBuilder();
        try {
            try (FileReader fileReader = new FileReader(file)) {
                try (BufferedReader reader = new BufferedReader(fileReader)) {
                    String lineTxt;
                    while ((lineTxt = reader.readLine()) != null) {
                        content.append(lineTxt).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            throw new CommonException("error.file.read");
        }
        return content.toString();
    }

    /**
     * 保存 json 为文件
     *
     * @param path     目标路径
     * @param fileName 存储文件名
     * @param data     json 内容
     */
    public static void saveDataToFile(String path, String fileName, String data) {
        File file = new File(path + System.lineSeparator() + fileName + ".json");
        //如果文件不存在，则新建一个
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new CommonException("error.file.create");
                }
            } catch (IOException e) {
                logger.info(e.getMessage());
            }
        }
        //写入
        try (FileOutputStream fileOutputStream = new FileOutputStream(file, false)) {
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8")) {
                try (BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {
                    writer.write(data);
                }
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        logger.info("文件写入成功！");
    }
}
