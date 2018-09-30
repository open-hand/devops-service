package io.choerodon.devops.infra.common.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
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

    public static final String DEVOPS_GITOPS_TAG = "devops-sync";
    private static final String MASTER = "master";
    private static final String PATH = "/";
    private static final String REPONAME = "devops-service-repo";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);
    private ResourceLoader resourceLoader = new DefaultResourceLoader();
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
            this.classPath = resourceLoader.getResource("/").getURI().getPath();
            String repositoryPath = this.classPath == null ? "" : this.classPath + REPONAME;
            File repo = new File(repositoryPath);
            if (!repo.exists()) {
                repo.mkdirs();
            }
        } catch (IOException io) {
            throw new CommonException(io.getMessage());
        }
    }

    public GitUtil(String sshKey) {
        new GitUtil();
        this.sshKey = sshKey;
    }

    public static String getLog(String repoPath, String fileName) {
        String latestCommit = "";
        File file = new File(repoPath);
        try (Repository repository = new FileRepository(file.getAbsolutePath())) {
            try (Git git = new Git(repository)) {
                Iterable<RevCommit> logs = git.log().addPath(fileName).call();
                for (RevCommit revCommit : logs) {
                    latestCommit = revCommit.name();
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        return latestCommit;
    }

    public void cloneBySsh(String path, String url) {
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
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
            LOGGER.info(e.getMessage());
            throw new CommonException(e.getMessage());
        }
    }

    public void checkout(String path, String commit) {

        File repoGitDir = new File(path);
        try (Repository repository = new FileRepository(repoGitDir.getAbsolutePath())) {
            try (Git git = new Git(repository)) {
                CheckoutCommand checkoutCommand = git.checkout();
                checkoutCommand.setName(commit);
                checkoutCommand.call();
            }
        } catch (Exception e) {

        }
    }


    /**
     * create a file in git repo, and then commit it
     *
     * @param repoPath     git repo path
     * @param git          git repo
     * @param relativePath file relative path
     * @param fileContent  file content
     * @param commitMsg    commit msg, if null, commit msg will be '[ADD] add ' + file relative path
     * @throws IOException     if target repo is not found
     * @throws GitAPIException if target repo is not a git repo
     */
    public void createFileInRepo(String repoPath, Git git, String relativePath, String fileContent, String commitMsg)
            throws IOException, GitAPIException {
        FileUtil.saveDataToFile(repoPath, relativePath, fileContent);
        boolean gitProvided = git != null;
        git = gitProvided ? git : Git.open(new File(repoPath));
        addFile(git, relativePath);
        commitChanges(git, commitMsg == null || commitMsg.isEmpty() ? "[ADD] add " + relativePath : commitMsg);
        if (!gitProvided) {
            git.close();
        }
    }

    private void addFile(Git git, String relativePath) throws GitAPIException {
        git.add().setUpdate(false).addFilepattern(relativePath).call();
        git.add().setUpdate(true).addFilepattern(relativePath).call();
    }


    private void commitChanges(Git git, String commitMsg) throws GitAPIException {
        git.commit().setMessage(commitMsg).call();
    }

    /**
     * push current git repo
     *
     * @param git git repo
     * @throws GitAPIException push error
     */
    public void gitPush(Git git) throws GitAPIException {
        git.push().setTransportConfigCallback(getTransportConfigCallback()).call();
    }

    private TransportConfigCallback getTransportConfigCallback() {
        return transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactor());
        };
    }

    /**
     * push current git repo
     *
     * @param git git repo
     * @throws GitAPIException push error
     */
    public void gitPushTag(Git git) throws GitAPIException {
        List<Ref> refs = git.branchList().call();
        PushCommand pushCommand = git.push();
        for (Ref ref : refs) {
            pushCommand.add(ref);
        }
        pushCommand.setPushTags();
        pushCommand.setTransportConfigCallback(getTransportConfigCallback()).call();
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
                defaultJSch.getIdentityRepository().removeAll();
                defaultJSch.getIdentityRepository().add(sshKey.getBytes());
                return defaultJSch;
            }
        };
    }

    public void pullBySsh(String path) {
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
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
        File repoGitDir = new File(path);
        try (Repository repository = new FileRepository(repoGitDir.getAbsolutePath())) {
            try (Git git = new Git(repository)) {
                PullCommand pullCmd = git.pull();
                TransportConfigCallback transportConfigCallback = transport -> {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(sshSessionFactory);
                };
                pullCmd.setTransportConfigCallback(transportConfigCallback);
                pullCmd.setRemoteBranchName("master");
                pullCmd.call();
            }
        } catch (Exception e) {

        }
    }


    /**
     * Git克隆
     */
    public Git clone(String name, String type, String remoteUrl) {
        Git git = null;
        String branch = "";
        String workingDirectory = getWorkingDirectory(name);
        File localPathFile = new File(workingDirectory);
        deleteDirectory(localPathFile);
        if (type.equals("MicroServiceFront")) {
            branch = microServiceFront;
        } else if (type.equals("MicroService")) {
            branch = microService;
        } else if (type.equals("JavaLib")) {
            branch = javaLib;
        } else {
            branch = MASTER;
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
    public void push(Git git, String name, String repoUrl, String userName, String accessToken, Boolean teamplateType) {
        try {
            String[] url = repoUrl.split("://");
            git.add().addFilepattern(".").call();
            git.add().setUpdate(true).addFilepattern(".").call();
            git.commit().setMessage("Render Variables[skip ci]").call();
            if (teamplateType) {
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
        String path = this.classPath == null ? REPONAME + PATH + name : this.classPath + REPONAME + PATH + name;
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
    public void deleteDirectory(File file) {
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                throw new CommonException("error.directory.delete");
            }
        }
    }


}
