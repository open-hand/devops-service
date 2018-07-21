package io.choerodon.devops.infra.common.util;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import io.codearte.props2yaml.Props2YAML;
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
import io.choerodon.devops.domain.application.valueobject.InsertNode;
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
                if (!a.renameTo(newFile)) {
                    throw new CommonException("error.rename.file");
                }
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
    public static void deleteDirectory(File file) {
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
                        totalLine = totalLine + 1;
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
        List<InsertNode> insertNodes = new ArrayList<>();
        compareAndReplace((MappingNode) nodeOld, (MappingNode) nodeNew, insertNodes, replaceMarkers);
        List<HighlightMarker> highlightMarks = new ArrayList<>();
        List<Integer> insertLines = new ArrayList<>();
        String result = replace(yamlNew, replaceMarkers, highlightMarks, insertNodes, insertLines);
        ReplaceResult replaceResult = new ReplaceResult();
        replaceResult.setYaml(result);
        replaceResult.setHighlightMarkers(highlightMarks);
        replaceResult.setNewLines(insertLines);
        return replaceResult;
    }

    private static String replace(String yaml, List<ReplaceMarker> replaceMarkers, List<HighlightMarker> highlights, List<InsertNode> insertNodes, List<Integer> newLines) {
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

        compareAndSwap(index, values);

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
            for (InsertNode insertNode : insertNodes) {
                if (highlightMark.getLine() <= insertNode.getLine()) {
                    insertNode.setLastIndex(insertNode.getLastIndex() + lengthChange);
                }
            }
        }
        len = insertNodes.size();
        int[] lineIndex = new int[len];
        int[] lineValues = new int[len];
        for (int i = 0; i < len; i++) {
            lineValues[i] = insertNodes.get(i).getLine();
            lineIndex[i] = i;
        }
        compareAndSwap(lineIndex, lineValues);
        int[] insertLineCounts = new int[len];
        for (int i = len - 1; i >= 0; i--) {
            //行号最大的节点
            InsertNode insertNode = insertNodes.get(lineIndex[i]);
            StringBuilder stringBuilder = new StringBuilder();
            printBlank(insertNode.getStartColumn(), stringBuilder);
            stringBuilder.append(insertNode.getKey());
            stringBuilder.append(":");
            stringBuilder.append(printNode(insertNode.getValue(), insertNode.getStartColumn()));
            String insertString = stringBuilder.toString();
            temp = temp.substring(0, insertNode.getLastIndex() + 1) + insertString + temp.substring(insertNode.getLastIndex() + 1);
            int insertLineCount = countLine(insertString);
            insertLineCounts[i] = insertLineCount;
            for (HighlightMarker highlightMarker : highlights) {
                if (highlightMarker.getLine() > insertNode.getLine()) {
                    highlightMarker.setLine(highlightMarker.getLine() + insertLineCount);
                }
            }
        }
        int lineChange = 0;
        for (int i = 0; i < len; i++) {
            int lineNumber = insertNodes.get(lineIndex[i]).getLine();
            for (int n = 0; n < insertLineCounts[i]; n++) {
                newLines.add(lineNumber + n + 1 + lineChange);
            }
            lineChange += insertLineCounts[i];

        }

        return temp;
    }


    //从将old的值替换至新的值
    private static void compareAndReplace(MappingNode oldMapping, MappingNode newMapping, List<InsertNode> insertNodes, List<ReplaceMarker> replaceMarkers) {
        List<NodeTuple> oldRootTuple = oldMapping.getValue();
        List<NodeTuple> newRootTuple = newMapping.getValue();
        for (NodeTuple oldTuple : oldRootTuple) {
            Node oldKeyNode = oldTuple.getKeyNode();
            if (oldKeyNode instanceof ScalarNode) {
                ScalarNode scalarKeyNode = (ScalarNode) oldKeyNode;
                Node oldValue = oldTuple.getValueNode();
                if (oldValue instanceof ScalarNode) {
                    ScalarNode oldValueScalar = (ScalarNode) oldValue;
                    InsertNode insertNode = new InsertNode();
                    ScalarNode newValueNode = getKeyValue(scalarKeyNode.getValue(), newRootTuple, insertNode);
                    if (insertNode.getKey() != null) {
                        insertNode.setValue(oldValueScalar);
                        insertNodes.add(insertNode);
                    }
                    if (newValueNode != null) {
                        if (!oldValueScalar.getValue().equals(newValueNode.getValue())) {
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
                    }
                } else if (oldValue instanceof MappingNode) {
                    InsertNode insertNode = new InsertNode();
                    MappingNode vaMappingNode = getKeyMapping(scalarKeyNode.getValue(), newRootTuple, insertNode);
                    if (insertNode.getKey() != null) {
                        insertNode.setValue(oldValue);
                        insertNodes.add(insertNode);
                    }
                    if (vaMappingNode != null) {
                        MappingNode oldMappingNode = (MappingNode) oldValue;
                        compareAndReplace(oldMappingNode, vaMappingNode, insertNodes, replaceMarkers);
                    }
                }
            }
        }

    }

    private static int countLine(String insertString) {
        int count = 0;
        for (int i = 0; i < insertString.length(); i++) {
            if (insertString.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private static void compareAndSwap(int[] index, int[] values) {
        int tem;
        int tempIndex;
        int len = index.length;
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
    }

    //检查同一层是否存在该key
    private static MappingNode getKeyMapping(String key, List<NodeTuple> tuples, InsertNode insertNode) {
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
        ScalarNode lastScalarNode = getLastIndex(tuples.get(tuples.size() - 1));
        if (lastScalarNode == null) {
            logger.info("get last scarlar index error");
            return null;
        }
        insertNode.setStartColumn(tuples.get(tuples.size() - 1).getKeyNode().getStartMark().getColumn());
        insertNode.setLine(lastScalarNode.getEndMark().getLine());
        insertNode.setLastIndex(lastScalarNode.getEndMark().getIndex());
        insertNode.setKey(key);
        return null;
    }

    //检查同一层是否存在该key
    private static ScalarNode getKeyValue(String key, List<NodeTuple> tuples, InsertNode insertNode) {
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
        ScalarNode lastScalarNode = getLastIndex(tuples.get(tuples.size() - 1));
        if (lastScalarNode == null) {
            logger.info("get last scarlar index error");
            return null;
        }
        insertNode.setStartColumn(tuples.get(tuples.size() - 1).getKeyNode().getStartMark().getColumn());
        insertNode.setLine(lastScalarNode.getEndMark().getLine());
        insertNode.setLastIndex(lastScalarNode.getEndMark().getIndex());
        insertNode.setKey(key);
        return null;
    }


    private static ScalarNode getLastIndex(NodeTuple nodeTuple) {
        Node node = nodeTuple.getValueNode();
        if (node instanceof ScalarNode) {
            return (ScalarNode) node;
        } else if (node instanceof MappingNode) {
            MappingNode mappingNode = (MappingNode) node;
            NodeTuple last = mappingNode.getValue().get(mappingNode.getValue().size() - 1);
            return getLastIndex(last);
        } else {
            return null;
        }
    }

    private static String printNode(Node node, int startColumn) {
        StringBuilder stringBuilder = new StringBuilder();
        if (node instanceof ScalarNode) {
            appendValueNode((ScalarNode) node, stringBuilder);
        } else if (node instanceof MappingNode) {
            stringBuilder.append("\n");
            startColumn = startColumn + 2;
            for (NodeTuple nodeTuple : ((MappingNode) node).getValue()) {
                printTuple(startColumn, nodeTuple, stringBuilder);
            }
        }
        return stringBuilder.toString();
    }

    private static void appendValueNode(ScalarNode value, StringBuilder stringBuilder) {
        stringBuilder.append(" ");
        stringBuilder.append(value.getValue());
        stringBuilder.append("\n");
    }

    private static void printTuple(int startColumn, NodeTuple nodeTuple, StringBuilder stringBuilder) {
        String key = ((ScalarNode) nodeTuple.getKeyNode()).getValue();
        printBlank(startColumn, stringBuilder);
        stringBuilder.append(key);
        stringBuilder.append(":");
        Node valueNode = nodeTuple.getValueNode();
        if (valueNode instanceof ScalarNode) {
            appendValueNode((ScalarNode) valueNode, stringBuilder);
        } else if (valueNode instanceof MappingNode) {
            startColumn = startColumn + 2;
            stringBuilder.append("\n");
            for (NodeTuple nodeTuple1 : ((MappingNode) valueNode).getValue()) {
                printTuple(startColumn, nodeTuple1, stringBuilder);
            }
        }
    }

    private static void printBlank(int count, StringBuilder stringBuilder) {
        while (count > 0) {
            stringBuilder.append(" ");
            count--;
        }
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
        File file = new File(path + System.getProperty("file.separator") + fileName);
        //如果文件不存在，则新建一个
        if (!file.exists()) {
            new File(path).mkdirs();
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


    /**
     * 获取文件的md5值 ，有可能不是32位
     *
     * @param filePath 文件路径
     * @return md5HashCode
     * @throws FileNotFoundException 文件丢失
     */
    public static String md5HashCode(String filePath) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        return md5HashCode(fis);
    }

    /**
     * java获取文件的md5值
     *
     * @param fis 输入流
     * @return md5HashCode
     */
    public static String md5HashCode(InputStream fis) {
        try {
            //拿到一个MD5转换器,如果想使用SHA-1或SHA-256，则传入SHA-1,SHA-256
            MessageDigest md = MessageDigest.getInstance("MD5");

            //分多次将一个文件读入，对于大型文件而言，比较推荐这种方式，占用内存比较少。
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            fis.close();
            //转换并返回包含16个元素字节数组,返回数值范围为-128到127
            byte[] md5Bytes = md.digest();
            BigInteger bigInt = new BigInteger(1, md5Bytes);//1代表绝对值
            return bigInt.toString(16);//转换为16进制
        } catch (Exception e) {
            logger.info(e.getMessage());
            return "";
        }
    }


    /**
     * 使用renameTo移动文件，重复文件跳过
     *
     * @param fromPath 原文件路径
     * @param toPath   目标文件路径
     */
    public static void moveFiles(String fromPath, String toPath) {
        File fromFolder = new File(fromPath);
        if (fromFolder.isFile()) {
            moveFile(fromFolder, toPath, fromFolder.getName());
        } else {
            File[] fromFiles = fromFolder.listFiles();
            if (fromFiles == null) {
                return;
            }
            Arrays.stream(fromFiles).forEachOrdered(file -> {
                if (file.isDirectory()) {
                    moveFiles(file.getPath(), toPath + File.separator + file.getName());
                }
                if (file.isFile()) {
                    moveFile(file, toPath + "", file.getName());
                }
            });
        }
    }

    private static void moveFile(File file, String path, String fileName) {
        new File(path).mkdirs();
        File toFile = new File(path + File.separator + fileName);
        //移动文件
        if (!toFile.exists() && !file.renameTo(toFile)) {
            throw new CommonException("error.file.rename");
        }
    }

    /**
     * 解压文件到指定目录
     *
     * @param zipFile zip
     * @param descDir 目标文件位置
     */
    @SuppressWarnings("rawtypes")
    public static void unZipFiles(File zipFile, String descDir) {
        File pathFile = new File(descDir);
        pathFile.mkdirs();
        try (ZipFile zip = new ZipFile(zipFile)) {
            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                getUnZipPath(zip, entry, zipEntryName, descDir);
            }
        } catch (IOException e) {
            throw new CommonException("error.not.zip");
        }
        logger.info("******************解压完毕********************");
    }

    private static void getUnZipPath(ZipFile zip, ZipEntry entry, String zipEntryName, String descDir) {
        try (InputStream in = zip.getInputStream(entry)) {

            String outPath = (descDir + File.separator + zipEntryName).replaceAll("\\*", "/");
            //判断路径是否存在,不存在则创建文件路径
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            file.mkdirs();
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if (new File(outPath).isDirectory()) {
                return;
            }
            //输出文件路径信息
            logger.info(outPath);
            outPutUnZipFile(in, outPath);
        } catch (IOException e) {
            throw new CommonException("error.zip.inputStream");
        }
    }

    private static void outPutUnZipFile(InputStream in, String outPath) {
        try (OutputStream out = new FileOutputStream(outPath)) {
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = in.read(buf1)) > 0) {
                out.write(buf1, 0, len);
            }
        } catch (FileNotFoundException e) {
            throw new CommonException("error.outPath");
        } catch (IOException e) {
            throw new CommonException("error.zip.outPutStream");
        }
    }

    /**
     * 压缩成ZIP 方法1
     *
     * @param srcDir           压缩文件夹路径
     * @param outputStream     压缩文件
     * @param keepDirStructure 是否保留原来的目录结构,true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(String srcDir, OutputStream outputStream, boolean keepDirStructure) {
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            File sourceFile = new File(srcDir);
            compress(sourceFile, zos, sourceFile.getName(), keepDirStructure);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }


    /**
     * 递归压缩方法
     *
     * @param sourceFile       源文件
     * @param zos              zip输出流
     * @param name             压缩后的名称
     * @param keepDirStructure 是否保留原来的目录结构,
     *                         true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean keepDirStructure) {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            // copy文件到zip输出流中
            int len;
            try (FileInputStream in = new FileInputStream(sourceFile)) {
                // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
                zos.putNextEntry(new ZipEntry(name));
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
            } catch (IOException e) {
                throw new CommonException(e.getMessage());
            }
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (keepDirStructure) {
                    // 空文件夹的处理
                    try {
                        zos.putNextEntry(new ZipEntry(name + "/"));
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new CommonException(e.getMessage());
                    }
                }

            } else {
                // 判断是否需要保留原来的文件结构
                Arrays.stream(listFiles).forEachOrdered(file ->
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos,
                                keepDirStructure
                                        ? name + "/" + file.getName()
                                        : file.getName(), keepDirStructure)
                );
            }
        }
    }

    /**
     * 下载文件
     *
     * @param res      HttpServletResponse
     * @param filePath 文件路径
     */
    public static void downloadFile(HttpServletResponse res, String filePath) {
        res.setHeader("content-type", "application/octet-stream");
        res.setContentType("application/octet-stream");
        res.setHeader("Content-Disposition", "attachment;filename=" + filePath);
        byte[] buff = new byte[1024];
        OutputStream os = null;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(filePath)))) {
            os = res.getOutputStream();
            int i = bis.read(buff);
            while (i != -1) {
                os.write(buff, 0, buff.length);
                os.flush();
                i = bis.read(buff);
            }
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }

    }

    public static void deleteFile(String file) {
        deleteFile(new File(file));
    }

    public static void deleteFile(File file) {
        deleteFile(file.toPath());
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    public static void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String  原文件路径  如：c:/fqf.txt
     * @param newPath String  复制后上级路径  如：f:/
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            new File(newPath).mkdirs();
            if (oldfile.exists() && oldfile.isFile()) {  //文件存在时
                try (InputStream inStream = new FileInputStream(oldPath)) {  //读入原文件
                    try (FileOutputStream fs = new FileOutputStream(newPath + File.separator + oldfile.getName())) {
                        byte[] buffer = new byte[1444];
                        while ((byteread = inStream.read(buffer)) != -1) {
                            fs.write(buffer, 0, byteread);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.info("复制单个文件操作出错");
        }

    }


    public static String getChangeYaml(String oldYam1, String newYaml) {
        Map<String, Object> map1 = (Map<String, Object>) yaml.load(oldYam1);
        Map<String, Object> map2 = (Map<String, Object>) yaml.load(newYaml);
        List<String> primaryKeys = getPrimaryKey(map1);
        List<String> newprimaryKeys = getPrimaryKey(map2);
        Map<String, String> oldProperties = new HashMap<>();
        Map<String, String> newProperties = new HashMap<>();
        List<String> keys = new ArrayList<>();
        getdep(map1, 1, keys, primaryKeys, oldProperties);
        keys.clear();
        getdep(map2, 1, keys, newprimaryKeys, newProperties);
        Map<String, String> changeProperties = getChangeProperties(oldProperties, newProperties);
        return Props2YAML.fromContent(propertiesToString(changeProperties))
                .convert();
    }


    public static int getdep(Map map, int complex, List<String> keys, List<String> primaryKeys, Map<String, String> maps) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (primaryKeys.contains(entry.getKey().toString())) {
                complex = 1;
                keys.clear();
            }
            if (keys.size() != complex) {
                keys.add(entry.getKey().toString());
            } else {
                keys.set(complex - 1, entry.getKey().toString());
            }
            Object val = entry.getValue();
            if (val instanceof Map && ((Map) val).size() == 0) {
                val = "{}";
            }
            if (val instanceof Map) {
                complex++;
                complex = getdep((Map) val, complex, keys, primaryKeys, maps);
            } else {
                if (val != null) {
                    maps.put(getKeyValue(complex, keys), val.toString());
                }
            }
        }
        keys.remove(keys.get(complex - 1));
        return complex - 1;
    }

    public static String getKeyValue(int complex, List<String> keys) {
        String result = "";
        for (int i = 0; i < complex; i++) {
            result = result.equals("") ? result + keys.get(i) : result + "." + keys.get(i);
        }
        return result;
    }

    public static Map<String, String> getChangeProperties(Map<String, String> map, Map<String, String> newMap) {
        Map<String, String> properties = new HashMap<>();
        for (Map.Entry<String, String> entry : newMap.entrySet()) {
            String m1value = entry.getValue() == null ? "" : entry.getValue();
            if (!map.containsKey(entry.getKey())) {
                properties.put(entry.getKey(), entry.getValue());
            } else {
                String m2value = map.get(entry.getKey()) == null ? "" : map.get(entry.getKey());
                if (!m1value.equals(m2value)) {
                    properties.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return properties;
    }

    public static List<String> getPrimaryKey(Map<String, Object> map) {
        List<String> primaryKeys = new ArrayList<>();
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            primaryKeys.add(entry.getKey().toString());
        }
        return primaryKeys;
    }


    public static String propertiesToString(Map<String, String> map) {
        StringBuilder res = new StringBuilder();
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            res.append(key);
            res.append("=");
            res.append(map.getOrDefault(key, ""));
            res.append("\n");
        }
        return res.toString();
    }
}
