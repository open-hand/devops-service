package io.choerodon.devops.infra.common.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.impl.DevopsGitServiceImpl;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitUtil {

    public static final String DEV_OPS_SYNC_TAG = "devops-sync";
    private static final String MASTER = "master";
    private static final String PATH = "/";
    private static final String REPO_NAME = "devops-service-repo";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);

    private String classPath;
    private String sshKey;

    @Value("${template.version.MicroService}")
    private String microService;
    @Value("${template.version.MicroServiceFront}")
    private String microServiceFront;
    @Value("${template.version.JavaLib}")
    private String javaLib;

    /**
     * 构造方法
     */
    public GitUtil() {
        try {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            this.classPath = resourceLoader.getResource("/").getURI().getPath();
            String repositoryPath = this.classPath == null ? "" : this.classPath + REPO_NAME;
            File repo = new File(repositoryPath);
            if (!repo.exists() && repo.mkdirs()) {
                LOGGER.info("create {} success", repositoryPath);
            }
        } catch (IOException io) {
            throw new CommonException(io.getMessage());
        }
    }

    public GitUtil(String sshKey) {
        new GitUtil();
        this.sshKey = sshKey;
    }

    private static String getLog(String repoPath, String fileName) {
        String latestCommit = "";
        File file = new File(repoPath);
        try (Repository repository = new FileRepository(file.getAbsolutePath())) {
            try (Git git = new Git(repository)) {
                Iterable<RevCommit> logs = git.log().addPath(fileName).call();
                Iterator<RevCommit> revCommitIterator = logs.iterator();
                if (revCommitIterator.hasNext()) {
                    latestCommit = revCommitIterator.next().getName();
                }
            }
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        return latestCommit;
    }

    public static String getFileLatestCommit(String path, String filePath) {
        String[] fileName = filePath.split("/");
        return GitUtil.getLog(path, fileName[fileName.length - 1]);
    }

    /**
     * clone by ssh
     *
     * @param path target path
     * @param url  git repo url
     */
    public void cloneBySsh(String path, String url) {
        SshSessionFactory sshSessionFactory = sshSessionFactor();
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(url);
        cloneCommand.setBranch(MASTER);
        TransportConfigCallback transportConfigCallback = transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        };
        cloneCommand.setTransportConfigCallback(transportConfigCallback);
        try {
            cloneCommand.setDirectory(new File(path));
            cloneCommand.call();
        } catch (GitAPIException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    private SshSessionFactory sshSessionFactor() {
        return new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                defaultJSch.getIdentityRepository().add(sshKey.getBytes());
                return defaultJSch;
            }
        };
    }

    /**
     * check git repo to commit
     *
     * @param path   git repo path
     * @param commit target commit or branch or tag
     */
    public void checkout(String path, String commit) {

        File repoGitDir = new File(path);
        try (Repository repository = new FileRepository(repoGitDir.getAbsolutePath())) {
            checkout(commit, repository);
        } catch (IOException e) {
            LOGGER.info("Get repository error", e);
        }
    }

    private void checkout(String commit, Repository repository) {
        try (Git git = new Git(repository)) {
            git.checkout().setName(commit).call();
        } catch (GitAPIException e) {
            LOGGER.info("Checkout error ", e);
        }
    }

    /**
     * pull git repo using ssh
     *
     * @param path git repo
     */
    public void pullBySsh(String path) {
        SshSessionFactory sshSessionFactory = sshSessionFactor();
        File repoGitDir = new File(path);
        try (Repository repository = new FileRepository(repoGitDir.getAbsolutePath())) {
            pullBySsh(sshSessionFactory, repository);
        } catch (IOException e) {
            LOGGER.info("Get repository error", e);
        }
    }

    private void pullBySsh(SshSessionFactory sshSessionFactory, Repository repository) {
        try (Git git = new Git(repository)) {
            git
                    .pull()
                    .setTransportConfigCallback(transport -> {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(sshSessionFactory);
                    })
                    .setRemoteBranchName(MASTER)
                    .call();
        } catch (GitAPIException e) {
            LOGGER.info("Pull error", e);
        }
    }

    /**
     * Git克隆
     */
    public Git clone(String name, String type, String remoteUrl) {
        Git git;
        String branch;
        String workingDirectory = getWorkingDirectory(name);
        File localPathFile = new File(workingDirectory);
        deleteDirectory(localPathFile);
        switch (type) {
            case "MicroServiceFront":
                branch = microServiceFront;
                break;
            case "MicroService":
                branch = microService;
                break;
            case "JavaLib":
                branch = javaLib;
                break;
            default:
                branch = MASTER;
                break;
        }
        try {
            git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setBranch(branch)
                    .setDirectory(localPathFile)
                    .call();
        } catch (GitAPIException e) {
            throw new CommonException("error.git.clone");
        }
        return git;
    }

    /**
     * 将代码推到目标库
     */
    public void push(Git git, String name, String repoUrl, String userName, String accessToken, Boolean templateType) {
        try {
            String[] url = repoUrl.split("://");
            git.add().addFilepattern(".").call();
            git.add().setUpdate(true).addFilepattern(".").call();
            git.commit().setMessage("Render Variables[skip ci]").call();
            if (templateType) {
                git.branchCreate().setName(MASTER).call();
            }
            List<Ref> refs = git.branchList().call();
            PushCommand pushCommand = git.push();
            for (Ref ref : refs) {
                pushCommand.add(ref);
            }
            pushCommand.setRemote(url[0] + "://gitlab-ci-token:" + accessToken + "@" + url[1]);
            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                    userName, accessToken));
            pushCommand.call();
        } catch (GitAPIException e) {
            throw new CommonException("error.git.push");
        } finally {
            //删除模板
            deleteWorkingDirectory(name);
            if (git != null) {
                git.close();
            }
        }
    }

    /**
     * 获取工作目录
     */
    public String getWorkingDirectory(String name) {
        String path = this.classPath == null ? REPO_NAME + PATH + name : this.classPath + REPO_NAME + PATH + name;
        return path.replace(PATH, File.separator);
    }

    /**
     * 删除工作目录
     */
    public void deleteWorkingDirectory(String name) {
        String path = getWorkingDirectory(name);
        File file = new File(path);
        deleteDirectory(file);
    }

    /**
     * 删除文件
     */
    private void deleteDirectory(File file) {
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                throw new CommonException("error.directory.delete");
            }
        }
    }
}
