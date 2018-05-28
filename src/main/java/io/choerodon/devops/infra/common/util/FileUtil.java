package io.choerodon.devops.infra.common.util;

import java.io.*;
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

import io.choerodon.core.exception.CommonException;

/**
 * Created by younger on 2018/4/13.
 */
public class FileUtil {


    public final static int BUFFER_SIZE = 2048;
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
                for (Object o : params.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    content = content.replace(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
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
            if (a.getName().equals(".git")) {
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
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(tmpFile);
                        int length = 0;
                        byte[] b = new byte[2048];
                        while ((length = tarIn.read(b)) != -1) {
                            out.write(b, 0, length);
                        }
                    } finally {
                        IOUtils.closeQuietly(out);
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
        for (File file1 : files) {
            if (file1.isDirectory()) {
                return queryFileFromFiles(file1, fileName);
            } else if (file1.getName().equals(fileName)) {
                return file1;
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
}
