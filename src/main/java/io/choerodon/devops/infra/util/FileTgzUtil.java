package io.choerodon.devops.infra.util;

import java.io.*;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  21:16 2019/8/20
 * Description:
 */
public class FileTgzUtil {
    /**
     * 归档
     *
     * @param entry
     * @return
     * @throws IOException
     */
    public static String archive(String entry) throws IOException {
        File file = new File(entry);

        TarArchiveOutputStream tos = new TarArchiveOutputStream(new FileOutputStream(file.getAbsolutePath() + ".tar"));
        String base = file.getName();
        if (file.isDirectory()) {
            archiveDir(file, tos, base);
        } else {
            archiveHandle(tos, file, base);
        }

        tos.close();
        return file.getAbsolutePath() + ".tar";
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
            bis.close();
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
}
