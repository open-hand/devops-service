package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.ExceptionConstants.GitopsCode.DEVOPS_FILE_CREATE;
import static io.choerodon.devops.infra.constant.ExceptionConstants.PublicCode.DEVOPS_YAML_FORMAT_INVALID;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
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
import io.choerodon.devops.api.vo.kubernetes.HighlightMarker;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;

/**
 * Created by younger on 2018/4/13.
 */
public class FileUtil {

    private static final int BUFFER_SIZE = 2048;
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    private static final String EXEC_PATH;

    static {
        String values_yaml_path = System.getenv("VALUES_YAML_PATH");
        if (ObjectUtils.isEmpty(values_yaml_path)) {
            EXEC_PATH = "/usr/lib/yaml/values_yaml";
        } else {
            EXEC_PATH = values_yaml_path;
        }
    }


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
            }
            inputStream.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new CommonException("devops.param.render", e);
        }
    }

    /**
     * 替换变量
     *
     * @param rawString 原始的字符串
     * @param params    要替换的字符串映射
     * @return 替换后的字符串
     */
    public static String replaceReturnString(final String rawString, Map<String, String> params) {
        String result = rawString;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 通过inputStream流 替换文件的参数 ，将file转换为流渲染参数后写回文件
     *
     * @param file   文件
     * @param params 参数
     */
    public static void replaceReturnFile(File file, Map<String, String> params) {
        File[] files = file.listFiles();

        // files 可能为 null
        if (files == null) {
            logger.warn("There is no file in the file: {}", file.getAbsolutePath());
            return;
        }

        for (File a : files) {
            if (a.getName().equals(".git") || a.getName().endsWith(".xlsx") || a.getName().equals("java")) {
                continue;
            }
            File newFile = null;
            if (a.getName().equals("model-service") || a.getName().equals(params.get("the-oldService-name"))) {
                String parentPath = a.getParent();
                newFile = new File(parentPath + File.separator + params.get("{{service.code}}"));
                if (!a.renameTo(newFile)) {
                    throw new CommonException("devops.rename.file");
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
                throw new CommonException("devops.param.replace", e);
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
        return multipartFileToFileWithSuffix(path, files, "");
    }

    /**
     * 将文件上传到项目指定目录
     *
     * @param path   项目目录
     * @param files  来源文件
     * @param suffix 文件后缀（如：.zip)
     */
    public static String multipartFileToFileWithSuffix(String path, MultipartFile files, String suffix) {
        File repo = new File(path);
        String filename = files.getOriginalFilename() + suffix;
        try {
            if (!repo.exists()) {
                repo.mkdirs();
            }
            try (BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(new File(
                            path, filename)))) {
                out.write(files.getBytes());
                out.flush();
            }
        } catch (IOException e) {
            throw new CommonException("devops.file.transfer", e);
        }
        return path + System.getProperty("file.separator") + filename;
    }


    /**
     * yaml字符串转json
     *
     * @param yamlString yaml字符串
     * @return string
     */
    public static String yamlStringtoJson(String yamlString) {
        Yaml yaml = new Yaml();
        Gson gs = new Gson();
        ByteArrayInputStream stream = new ByteArrayInputStream(yamlString.getBytes());
        Map<String, Object> loaded = yaml.load(stream);
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

        try (FileInputStream inputStream = new FileInputStream(tarFile)) {
            unTar(new GzipCompressorInputStream(inputStream), destDir);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        }
    }

    public static void unTar(InputStream inputStream, String destDir) {

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
     * 创建目录
     */
    public static void createDirectory(String outputDir) {
        File file = new File(outputDir);
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
     * 从文件夹中查找指定文件, 广度优先遍历
     */
    public static File queryFileFromFilesBFS(File file, String fileName) {
        File[] files = file.listFiles();
        if (files != null) {
            Queue<File> fileQueue = new ArrayDeque<>();
            // 将当前目录下的文件先加入队列
            ArrayUtil.offerAllToQueue(fileQueue, files);

            File current;
            // 遍历队列, 取出队列中元素
            while ((current = fileQueue.poll()) != null) {
                if (current.isDirectory()) {
                    // 如果是目录, 将目录下的文件加入队列尾部
                    ArrayUtil.offerAllToQueue(fileQueue, current.listFiles());
                }
                if (current.getName().equals(fileName)) {
                    return current;
                }
            }
        }
        return null;
    }

    public static List<String> getFilesPath(String filepath) {
        File file = new File(filepath);
        List<String> filepaths = getFilesPath(file);
        if (!filepaths.isEmpty()) {
            return filepaths.stream()
                    .map(t -> t.replaceFirst(filepath + "/", "")).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 获取文件夹下所有 yml 文件路径
     *
     * @param file 文件夹
     * @return 路径列表
     */
    public static List<String> getFilesPath(File file) {
        List<String> filesPath = new ArrayList<>();
        if (file != null) {
            if (file.isDirectory()) {
                Arrays.stream(Objects.requireNonNull(file.listFiles())).parallel()
                        .forEach(t -> filesPath.addAll(getFilesPath(t)));
            } else if (file.isFile()
                    && (file.getName().endsWith(".yml") || file.getName().endsWith("yaml"))) {
                filesPath.add(file.getPath());
            }
        }
        return filesPath;
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
     * 删除多个文件
     *
     * @param fileNames 文件名称数组
     */
    public static void deleteDirectories(String... fileNames) {
        if (fileNames == null) {
            return;
        }

        for (String filename : fileNames) {
            if (filename == null) {
                continue;
            }
            deleteDirectory(new File(filename));
        }
    }

    /**
     * 删除多个文件
     *
     * @param files 文件数组
     */
    public static void deleteDirectories(File... files) {
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file == null) {
                continue;
            }
            deleteDirectory(file);
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
                     new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8))) {
            try (InputStreamReader inputStreamReader =
                         new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8)) {
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
     * 指定values文件路径
     *
     * @param path 路径
     * @return 返回替换结果
     */
    public static InstanceValueVO replaceNew(String path) {
        BufferedReader stdInput = null;
        BufferedReader stdError = null;
        InstanceValueVO instanceValueVO = null;
        try {

            String command = EXEC_PATH + " " + path;
            // TODO 这里每次执行都是开启了一个新进程，可能开销不小
            Process p = Runtime.getRuntime().exec(command);

            stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            StringBuilder stringBuilder = new StringBuilder();
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                stringBuilder.append(s).append("\n");
            }
            String result = stringBuilder.toString();
            String err = null;
            instanceValueVO = loadResult(result);
            while ((err = stdError.readLine()) != null) {
                err += err;
            }
        } catch (IOException e) {
            throw new CommonException(e);
        } finally {
            try {
                if (stdError != null) {
                    stdError.close();
                }
                if (stdInput != null) {
                    stdInput.close();
                }
            } catch (IOException e) {
                logger.info(e.getMessage(), e);
            }
        }
        return instanceValueVO;
    }

    private static InstanceValueVO loadResult(String yml) {
        String[] strings = yml.split("------love------you------choerodon------");
        if (strings.length < 2) {
            throw new CommonException("devops.value.illegal");
        }
        Yaml yaml = new Yaml();
        Object map = yaml.load(strings[2]);
        InstanceValueVO instanceValueVO = replaceNew(strings[0], (Map) map);
        instanceValueVO.setDeltaYaml(strings[1]);
        return instanceValueVO;
    }

    private static InstanceValueVO replaceNew(String yaml, Map map) {
        Composer composer = new Composer(new ParserImpl(new StreamReader(yaml)), new Resolver());
        MappingNode mappingNode = (MappingNode) composer.getSingleNode();
        List<Integer> addLines = new ArrayList<>();

        //处理新增
        ArrayList addLists = (ArrayList) map.get("add");
        for (Object add : addLists) {
            ArrayList<String> addList = (ArrayList<String>) add;
            Node node = getKeysNode(addList, mappingNode);
            if (node != null) {
                appendLine(node.getStartMark().getLine(), node.getEndMark().getLine(), addLines);
            }
        }

        List<HighlightMarker> highlightMarkers = new ArrayList<>();

        //处理修改
        ArrayList updateList = (ArrayList) map.get("update");
        for (Object add : updateList) {
            ArrayList<String> addList = (ArrayList<String>) add;
            Node node = getKeysNode(addList, mappingNode);
            HighlightMarker highlightMarker = new HighlightMarker();
            if (node != null) {
                highlightMarker.setLine(node.getStartMark().getLine());
                highlightMarker.setEndLine(node.getEndMark().getLine());
                highlightMarker.setStartColumn(node.getStartMark().getColumn());
                highlightMarker.setEndColumn(node.getEndMark().getColumn());
                highlightMarkers.add(highlightMarker);
            }
        }

        InstanceValueVO instanceValueVO = new InstanceValueVO();
        instanceValueVO.setNewLines(addLines);
        instanceValueVO.setHighlightMarkers(highlightMarkers);
        instanceValueVO.setYaml(yaml);
        return instanceValueVO;

    }

    private static void appendLine(int start, int end, List<Integer> adds) {
        for (int i = start; i <= end; i++) {
            adds.add(i);
        }
    }

    private static Node getKeysNode(List<String> keys, MappingNode mappingNode) {
        Node value = null;
        for (int i = 0; i < keys.size(); i++) {
            List<NodeTuple> nodeTuples = mappingNode.getValue();
            for (NodeTuple nodeTuple : nodeTuples) {
                if (nodeTuple.getKeyNode() instanceof ScalarNode && ((ScalarNode) nodeTuple.getKeyNode()).getValue().equals(keys.get(i))) {
                    if (i == keys.size() - 1) {
                        value = nodeTuple.getValueNode();
                    } else {
                        mappingNode = (MappingNode) nodeTuple.getValueNode();

                    }
                }
            }
        }
        return value;
    }

    /**
     * format yaml
     *
     * @param value json value
     * @return yaml
     */
    public static String checkValueFormat(String value) {
        try {
            if (value.equals("")) {
                return "{}";
            }
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
     * @param yamlStr yaml value
     */
    public static void checkYamlFormat(String yamlStr) {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        Yaml yaml = new Yaml(options);
        Iterable<Object> objects = yaml.loadAll(yamlStr);
        Iterator<Object> iterator = objects.iterator();
        try {
            while (iterator.hasNext()) {
                iterator.next();
            }
        } catch (Exception e) {
            throw new CommonException(DEVOPS_YAML_FORMAT_INVALID, e);
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
            throw new CommonException("devops.file.read", e);
        }
        return content.toString();
    }

    /**
     * 保存为文件
     *
     * @param path     目标路径
     * @param fileName 存储文件名
     * @param data     内容
     */
    public static void saveDataToFile(String path, String fileName, String data) {
        saveDataToFile(path + System.getProperty("file.separator") + fileName, data);
    }

    /**
     * 保存为文件
     *
     * @param filePath 文件路径
     * @param data     内容
     */
    public static void saveDataToFile(String filePath, String data) {
        File file = new File(filePath);
        //如果文件不存在，则新建一个
        if (!file.exists()) {
            new File(file.getParent()).mkdirs();
            try {
                if (!file.createNewFile()) {
                    throw new CommonException(DEVOPS_FILE_CREATE);
                }
            } catch (IOException e) {
                logger.info(e.getMessage());
            }
        }
        //写入
        try (FileOutputStream fileOutputStream = new FileOutputStream(file, false)) {
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
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
            throw new CommonException("devops.file.rename");
        }
    }

    public static void unpack(File zipFile, File descDir) {
        try (ZipArchiveInputStream inputStream = getZipFile(zipFile)) {
            if (!descDir.exists()) {
                descDir.mkdirs();
            }
            ZipArchiveEntry entry = null;
            while ((entry = inputStream.getNextZipEntry()) != null) {
                if (entry.isDirectory()) {
                    File directory = new File(descDir, entry.getName());
                    directory.mkdirs();
                } else {
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(new File(descDir, entry.getName())));
                        //输出文件路径信息
                        System.out.println("解压文件的当前路径为:" + descDir + entry.getName());
                        IOUtils.copy(inputStream, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }

        } catch (Exception e) {
            throw new CommonException("unpack error", e);
        }
    }

    private static ZipArchiveInputStream getZipFile(File zipFile) throws Exception {
        return new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
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
            throw new CommonException("devops.not.zip", e);
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
            throw new CommonException("devops.zip.inputStream", e);
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
            throw new CommonException("devops.outPath", e);
        } catch (IOException e) {
            throw new CommonException("devops.zip.outPutStream", e);
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
            throw new CommonException(e.getMessage(), e);
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
            isFile(sourceFile, zos, name, buf);

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
                        throw new CommonException(e.getMessage(), e);
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

    private static void isFile(File sourceFile, ZipOutputStream zos, String name, byte[] buf) {
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
            throw new CommonException(e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     *
     * @param res      HttpServletResponse
     * @param filePath 文件路径
     */
    public static void downloadFile(HttpServletResponse res, String filePath) {
        res.setHeader("Content-type", "application/octet-stream");
        res.setContentType("application/octet-stream");
        res.setHeader("Content-Disposition", "attachment;filename=" + filePath);
        File file = new File(filePath);
        res.setHeader("Content-Length", "" + file.length());
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file)); OutputStream os = res.getOutputStream()) {
            byte[] buff = new byte[bis.available()];
            int count = bis.read(buff);
            if (count > 0) {
                os.write(buff);
                os.flush();
            }
        } catch (IOException e) {
            throw new CommonException(e);
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

    /**
     * copy file to the destination
     *
     * @param sourceFile the source file
     * @param destFile   the destination file that can not exist.
     */
    public static void copyFile(File sourceFile, File destFile) {
        try {
            if (sourceFile.exists() && sourceFile.isFile()) {  //文件存在时
                FileUtils.copyFile(sourceFile, destFile);
            }
        } catch (Exception e) {
            logger.info("Failure occurs when copy file. from {} to {}. Exception is {}", sourceFile.toString(), destFile.toString(), e);
        }

    }

    /**
     * copy directory to the destination
     *
     * @param sourceDir the source directory
     * @param destDir   the destination directory
     */
    public static void copyDir(File sourceDir, File destDir) {
        if (sourceDir.exists() && sourceDir.isDirectory()) {
            try {
                FileUtils.copyDirectory(sourceDir, destDir);
            } catch (IOException e) {
                logger.info("Failure occurs when copy directory. from {} to {}. Exception is {}", sourceDir.toString(), destDir.toString(), e);
            }
        }
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

    /**
     * 生成一对RSA的ssh公私钥
     *
     * @param publicKeyComment 公钥的注释
     * @return 列表第一个元素为私钥，第二个为公钥
     */
    public static List<String> getSshKey(String publicKeyComment) {
        List<String> sshKeyPair = new ArrayList<>();
        int type = KeyPair.RSA;
        JSch jsch = new JSch();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, type);
            keyPair.writePrivateKey(outputStream);
            sshKeyPair.add(new String(outputStream.toByteArray()));
            outputStream.reset();
            keyPair.writePublicKey(outputStream, publicKeyComment);
            sshKeyPair.add(new String(outputStream.toByteArray()));
            keyPair.dispose();
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return sshKeyPair;
    }


    public static Yaml getYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowReadOnlyProperties(true);
        return new Yaml(options);
    }

    /**
     * 归档
     *
     * @param entry
     * @return
     * @throws IOException
     */
    public static String archive(String entry, String outputFilePath) throws IOException {
        File file = new File(entry);

        TarArchiveOutputStream tos = new TarArchiveOutputStream(new FileOutputStream(outputFilePath + ".tar"));
        String base = file.getName();
        if (file.isDirectory()) {
            archiveDir(file, tos, base);
        } else {
            archiveHandle(tos, file, base);
        }

        tos.close();
        return outputFilePath + ".tar";
    }

    /**
     * 递归处理，准备好路径
     *
     * @param file
     * @param tos
     * @throws IOException
     */
    private static void archiveDir(File file, TarArchiveOutputStream tos, String basePath) throws IOException {
        File[] listFiles = file.listFiles();
        for (File fi : listFiles) {
            if (fi.isDirectory()) {
                archiveDir(fi, tos, basePath + File.separator + fi.getName());
            } else {
                archiveHandle(tos, fi, basePath);
            }
        }
    }

    /**
     * 具体归档处理（文件）
     *
     * @param tos
     * @param fi
     * @throws IOException
     */
    private static void archiveHandle(TarArchiveOutputStream tos, File fi, String basePath) throws IOException {
        TarArchiveEntry tEntry = new TarArchiveEntry(basePath + File.separator + fi.getName());
        tEntry.setSize(fi.length());

        tos.putArchiveEntry(tEntry);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fi))) {

            byte[] buffer = new byte[1024];
            int read = -1;
            while ((read = bis.read(buffer)) != -1) {
                tos.write(buffer, 0, read);
            }
        }
        tos.closeArchiveEntry();//这里必须写，否则会失败
    }

    /**
     * 把tar包压缩成gz
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static String compressArchive(String path, String fileName) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
             GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(fileName + ".tgz")))) {
            byte[] buffer = new byte[1024];
            int read = -1;
            while ((read = bis.read(buffer)) != -1) {
                gcos.write(buffer, 0, read);
            }
        }
        return fileName + ".tgz";
    }

    public static void toTgz(String inputFilePath, String outputFilePath) {
        try {
            String tarFilePath = archive(inputFilePath, outputFilePath);
            compressArchive(tarFilePath, outputFilePath);
            deleteFile(tarFilePath);
        } catch (IOException e) {
            throw new CommonException("devops.package.tgz", e.getMessage());
        }
    }

    public static String ensureEndWithSlash(String repo) {
        return !org.springframework.util.StringUtils.isEmpty(repo) && repo.endsWith(BaseConstants.Symbol.SLASH) ? repo : repo + BaseConstants.Symbol.SLASH;
    }

}
