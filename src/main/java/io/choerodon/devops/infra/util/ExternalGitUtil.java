package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.enums.EnvironmentType;

/**
 * 封装调用外部git的接口
 */
@Component
public class ExternalGitUtil {
    public static final String TEMPLATE = "template";
    private static final String PATH = "/";
    private static final String GIT_SUFFIX = "/.git";
    private static final String ERROR_GIT_CLONE = "devops.git.clone";
    private static final String REPO_NAME = "devops-service-repo";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalGitUtil.class);
    private String classPath;

    /**
     * 构造方法
     */
    public ExternalGitUtil() {
        try {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            this.classPath = resourceLoader.getResource("/").getURI().getPath();
            String repositoryPath = this.classPath == null ? "" : this.classPath + REPO_NAME;
            File repo = new File(repositoryPath);
            if (!repo.exists() && repo.mkdirs()) {
                LOGGER.info("create {} success", repositoryPath);
            }
        } catch (IOException io) {
            throw new CommonException(io.getMessage(), io);
        }
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
        if (filePath != null) {
            String[] fileName = filePath.split("/");
            return ExternalGitUtil.getLog(path, fileName[fileName.length - 1]);
        }
        return "";
    }

    public static String getGitlabSshUrl(Pattern pattern, String url, String orgCode, String proCode, String envCode, EnvironmentType environmentType, String clusterCode) {
        final String groupSuffix = GitOpsUtil.getGroupSuffixByEnvType(environmentType);
        String result = "";
        // 这里url分割有可能产生三段, 如: ssh://git@192.168.13.12:30022
        String[] urls = url.split(":");
        if (url.contains("@")) {
            if (urls.length == 1) {
                result = String.format("%s:%s-%s%s/%s.git",
                        url, orgCode, proCode, groupSuffix, envCode);
            } else {
                if (pattern.matcher(urls[1]).matches()) {
                    result = String.format("ssh://%s/%s-%s%s/%s.git",
                            url, orgCode, proCode, groupSuffix, envCode);
                } else if (urls.length == 3) {
                    result = String.format("%s/%s-%s%s/%s.git",
                            url, orgCode, proCode, groupSuffix, envCode);
                }
            }
        } else {
            if (urls.length == 1) {
                result = String.format("git@%s:%s-%s%s/%s.git",
                        url, orgCode, proCode, groupSuffix, envCode);
            } else {
                if (pattern.matcher(urls[1]).matches()) {
                    result = String.format("ssh://git@%s/%s-%s%s/%s.git",
                            url, orgCode, proCode, groupSuffix, envCode);
                }
            }
        }
        return result;
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
            throw new CommonException(DEVOPS_GIT_CHECKOUT, e);
        }
    }


    private void checkout(String commit, Repository repository) {
        try (Git git = new Git(repository)) {
            git.checkout().setName(commit).call();
        } catch (GitAPIException e) {
            throw new CommonException(DEVOPS_GIT_CHECKOUT, e);
        }
    }

    private static TransportConfigCallback getTransportConfigCallback(String sshKeyRsa) {
        return transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactor(sshKeyRsa));
        };
    }

    private static SshSessionFactory sshSessionFactor(String sshKeyRsa) {
        return new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                defaultJSch.getIdentityRepository().removeAll();
                defaultJSch.getIdentityRepository().add(sshKeyRsa.getBytes());
                return defaultJSch;
            }
        };
    }

    /**
     * 克隆公开仓库的或者根据access token克隆私库的代码所有分支
     *
     * @param dirName     directory name
     * @param remoteUrl   remote url to clone
     * @param accessToken the access token for access
     * @param username    username
     * @param password
     * @return the git instance of local repository
     */
    public Git cloneRepository(String dirName, String remoteUrl, String accessToken, String username, String password) {
        Git git;
        String workingDirectory = getWorkingDirectory(dirName);
        File localPathFile = new File(workingDirectory);
        deleteDirectory(localPathFile);
        try {
            UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = null;
            if (!ObjectUtils.isEmpty(accessToken)) {
                usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider("", accessToken);
            } else if (ObjectUtils.isEmpty(accessToken) && ObjectUtils.isEmpty(username)) {
                usernamePasswordCredentialsProvider = null;
            } else if (ObjectUtils.isEmpty(accessToken) && !ObjectUtils.isEmpty(username)) {
                usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
            }
            Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setCloneAllBranches(true)
                    .setCredentialsProvider(usernamePasswordCredentialsProvider)
                    .setDirectory(localPathFile)
                    .call();
            git = Git.open(new File(localPathFile + GIT_SUFFIX));
        } catch (GitAPIException | IOException e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        }
        return git;
    }

    /**
     * 将单个分支推送到远程仓库
     *
     * @param git         Git对象
     * @param repoUrl     仓库地址
     * @param accessToken 访问token
     * @param branchName  要推送的分支名
     */
    public void push(Git git, String repoUrl, String accessToken, String branchName) {
        try {
            // 对应分支名的本地引用名
            String localRefName = Constants.R_HEADS + branchName;
            // 找出对应分支名的本地分支引用
            Ref localRef = null;
            List<Ref> refs = git.branchList().call();
            for (Ref ref : refs) {
                if (ref.getName().equals(localRefName)) {
                    localRef = ref;
                    break;
                }
            }

            // 如果在本地分支找不到匹配branchName的Ref直接返回
            if (localRef == null) {
                return;
            }

            // 推代码
            git.push().add(localRef)
                    .setRemote(repoUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("", accessToken))
                    .call();
        } catch (GitAPIException e) {
            throw new CommonException(DEVOPS_GIT_PUSH, e);
        }
    }
    public void push(Git git, String name, String commit, String repoUrl, String userName, String accessToken) {
        push(git, name, commit, repoUrl, userName, accessToken, true);
    }

    /**
     * 将代码推到目标库
     */
    public void push(Git git, String name, String commit, String repoUrl, String userName, String accessToken, Boolean deleteFile) {
        try {
            String[] url = repoUrl.split("://");
            git.add().addFilepattern(".").call();
            git.add().setUpdate(true).addFilepattern(".").call();
            git.commit().setMessage(commit).call();
            List<Ref> refs = git.branchList().call();
            PushCommand pushCommand = git.push();
            for (Ref ref : refs) {
                pushCommand.add(ref);
            }
            pushCommand.setRemote(url[0] + "://gitlab-ci-token:" + accessToken + "@" + url[1]);
            LOGGER.info("push remote is: {}", pushCommand.getRemote());
            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                    userName, accessToken));
            pushCommand.call();
        } catch (GitAPIException e) {
            throw new CommonException(DEVOPS_GIT_PUSH, e);
        } finally {
            //删除模板
            if (deleteFile != null && deleteFile) {
                deleteWorkingDirectory(name);
            }
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
                throw new CommonException(DEVOPS_DIRECTORY_DELETE, e);
            }
        }
    }

    /**
     * Git克隆
     */
    public String clone(String name, String remoteUrl, String accessToken) {
        String workingDirectory = getWorkingDirectory(name);
        File oldLocalPathFile = new File(workingDirectory);
        deleteDirectory(oldLocalPathFile);
        try {
            Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(oldLocalPathFile)
                    .setCredentialsProvider(StringUtils.isEmpty(accessToken) ? null : new UsernamePasswordCredentialsProvider("", accessToken))
                    .call()
                    .close();
        } catch (GitAPIException e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        }
        return workingDirectory;
    }

    /**
     * Git克隆
     */
    public String cloneAppMarket(String name, String commit, String remoteUrl, String adminToken) {
        Git git = null;
        String workingDirectory = getWorkingDirectory(name);
        File localPathFile = new File(workingDirectory);
        deleteDirectory(localPathFile);
        try {
            git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(localPathFile)
                    .setCredentialsProvider(StringUtils.isEmpty(adminToken) ? null : new UsernamePasswordCredentialsProvider("", adminToken))
                    .call();
            git.checkout().setName(commit).call();
            git.close();
            FileUtil.deleteDirectory(new File(localPathFile + GIT_SUFFIX));
        } catch (Exception e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        }
        return workingDirectory;
    }

    /**
     * 本地创建tag并推送远程仓库
     *
     * @param git     git repo
     * @param sshKey  ssh私钥
     * @param tagName tag名称
     * @param sha     要打tag的散列值
     * @throws CommonException push error
     */
    public static void createTagAndPush(Git git, String sshKey, String tagName, String sha) {
        try {
            // 创建之前删除，保证本地不存在要创建的tag
            deleteTag(git, tagName);
            Repository repository = git.getRepository();
            ObjectId id = repository.resolve(sha);
            RevWalk walk = new RevWalk(repository);
            RevCommit commit = walk.parseCommit(id);
            git.tag().setObjectId(commit).setName(tagName).call();
            PushCommand pushCommand = git.push();
            pushCommand.add(tagName);
            pushCommand.setRemote("origin");
            pushCommand.setForce(true);
            pushCommand.setTransportConfigCallback(getTransportConfigCallback(sshKey)).call();
        } catch (Exception e) {
            throw new CommonException("create tag fail", e);
        }
    }

    /**
     * 本地删除tag
     *
     * @param git     git repo
     * @param tagName tag名称
     * @throws CommonException push error
     */
    public static void deleteTag(Git git, String tagName) {
        try {
            // 删除不存在的tag时jgit不会报错
            git.tagDelete().setTags(tagName).call();
        } catch (GitAPIException e) {
            throw new CommonException("delete tag fail", e);
        }
    }
}
