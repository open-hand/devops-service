package io.choerodon.devops;

import java.io.File;

import io.choerodon.devops.infra.util.FileUtil;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:45 2019/8/22
 * Description:
 */
public class Test {
    public static void main(String[] args) {
//        File file=new File("D:\\devops-service\\target\\classes\\devops-service-repo\\file_5f1d1d4c28724b7da5f00c1e919a152e_0.18.0.tgz");
        File file=new File("D:\\devops-service\\target\\classes\\devops-service-repo\\application1566461171624.tgz");
        String unZipPath="D:\\devops-service\\target\\classes\\devops-service-repo";
        FileUtil.unTarGZ(file, unZipPath);

    }
}
