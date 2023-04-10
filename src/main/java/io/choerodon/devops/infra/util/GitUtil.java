package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.ExceptionConstants.GitlabCode.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.GitConfigVO;
import io.choerodon.devops.api.vo.GitEnvConfigVO;
import io.choerodon.devops.api.vo.GitlabRepositoryInfo;
import io.choerodon.devops.app.service.DevopsClusterResourceService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.EnvironmentType;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsClusterMapper;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitUtil {
    public static final String DEV_OPS_SYNC_TAG = "devops-sync";
    public static final String DEV_OPS_REFS = "refs/tags/";
    public static final String TEMPLATE = "template";
    private static final String MASTER = "master";
    private static final String PATH = "/";
    private static final String GIT_SUFFIX = "/.git";
    private static final String ERROR_GIT_CLONE = "devops.git.clone";
    private static final String REPO_NAME = "devops-service-repo";
    private static final Logger LOGGER = LoggerFactory.getLogger(GitUtil.class);
    private static final Pattern PATTERN = Pattern.compile("^[-\\+]?[\\d]*$");
    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    private String classPath;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;
    @Value("${services.gitlab.internalsshUrl:}")
    private String gitlabInternalsshUrl;


    public String getSshUrl() {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(gitlabInternalsshUrl)) {
            return gitlabInternalsshUrl;
        } else {
            return gitlabSshUrl;
        }

    }

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
            throw new CommonException(io.getMessage(), io);
        }
    }

    /**
     * 验证无需token就可以进行访问的代码仓库的克隆地址是否有效
     *
     * @param repositoryUrl 代码仓库克隆地址
     * @param token         访问仓库所需的token（可为空）
     * @return true if the url is valid and the ls-remote result is not empty.
     */
    public static Boolean validRepositoryUrl(String repositoryUrl, String token) {
        LsRemoteCommand lsRemoteCommand = new LsRemoteCommand(null);
        lsRemoteCommand.setRemote(repositoryUrl);
        if (!StringUtils.isEmpty(token)) {
            lsRemoteCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("", token));
        }
        try {
            int size = lsRemoteCommand.call().size();
            if (size == 0) {
                return null;
            } else {
                return Boolean.TRUE;
            }
        } catch (GitAPIException e) {
            return Boolean.FALSE;
        }
    }

    public static Boolean validRepositoryUrl(String repositoryUrl, String username, String password) {
        LsRemoteCommand lsRemoteCommand = new LsRemoteCommand(null);
        lsRemoteCommand.setRemote(repositoryUrl);
        if (!ObjectUtils.isEmpty(username) && !ObjectUtils.isEmpty(password)) {
            lsRemoteCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
        }

        try {
            int size = lsRemoteCommand.call().size();
            if (size == 0) {
                return null;
            } else {
                return Boolean.TRUE;
            }
        } catch (GitAPIException e) {
            return Boolean.FALSE;
        }
    }

    /**
     * https://code.choerodon.com.cn/hzero-c7ncd/devops-service
     *
     * @param repositoryUrl
     * @return
     */
    public static GitlabRepositoryInfo calaulateRepositoryInfo(String repositoryUrl) {
        String[] protolAndDomain = repositoryUrl.split("://");
        String groupAndProject = protolAndDomain[1];
        if (protolAndDomain[1].endsWith("/")) {
            groupAndProject = protolAndDomain[1].substring(0, protolAndDomain[1].length() - 1);
        }

        int start = groupAndProject.indexOf("/");
        int end = groupAndProject.lastIndexOf("/");

        String domain = groupAndProject.substring(0, start);
        String group = groupAndProject.substring(start + 1, end);
        String project = groupAndProject.substring(end + 1);

        return new GitlabRepositoryInfo(protolAndDomain[0] + "://" + domain, group, project);
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
            return GitUtil.getLog(path, fileName[fileName.length - 1]);
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
     * 获取克隆应用服务的代码库的ssh地址
     *
     * @param sshUrl         ssh格式的域名
     * @param orgCode        组织code
     * @param proCode        项目code
     * @param appServiceCode 应用服务code
     * @return ssh地址
     */
    public static String getAppServiceSshUrl(String sshUrl, String orgCode, String proCode, String appServiceCode) {
        String result = "";
        // 这里url分割有可能产生三段, 如: ssh://git@192.168.13.12:30022
        String[] urls = sshUrl.split(":");
        if (sshUrl.contains("@")) {
            if (urls.length == 1) {
                result = String.format("%s:%s-%s/%s.git", sshUrl, orgCode, proCode, appServiceCode);
            } else {
                if (PATTERN.matcher(urls[1]).matches()) {
                    result = String.format("ssh://%s/%s-%s/%s.git",
                            sshUrl, orgCode, proCode, appServiceCode);
                } else if (urls.length == 3) {
                    result = String.format("%s/%s-%s/%s.git",
                            sshUrl, orgCode, proCode, appServiceCode);
                }
            }
        } else {
            if (urls.length == 1) {
                result = String.format("git@%s:%s-%s/%s.git",
                        sshUrl, orgCode, proCode, appServiceCode);
            } else {
                if (PATTERN.matcher(urls[1]).matches()) {
                    result = String.format("ssh://git@%s/%s-%s/%s.git",
                            sshUrl, orgCode, proCode, appServiceCode);
                } else {
                    LOGGER.debug("Unexpected case occurred when getting app-service ssh url: the gitlabSshUrl is {}, the orgCode is {}, the proCode is {} and the appServiceCode is {}", sshUrl, orgCode, proCode, appServiceCode);
                }
            }
        }
        return result;
    }


    /**
     * clone by ssh
     *
     * @param path target path
     * @param url  git repo url
     */
    public Git cloneBySsh(String path, String url, String sshKeyRsa) {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(url);
        cloneCommand.setBranch(MASTER);
        cloneCommand.setTransportConfigCallback(getTransportConfigCallback(sshKeyRsa));
        try {
            cloneCommand.setDirectory(new File(path));
            return cloneCommand.call();
        } catch (GitAPIException e) {
            LOGGER.debug("Failed to clone by ssh: path: {}, url: {}", path, url);
            throw new CommonException(e.getMessage(), e);
        }
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


    private static Git pullBySsh(Repository repository, String sshKeyRsa) throws GitAPIException {
        try (Git git = new Git(repository)) {
            git.pull()
                    .setTransportConfigCallback(getTransportConfigCallback(sshKeyRsa))
                    .call();
            return git;
        }
    }

    /**
     * pull git repo using ssh
     *
     * @param path git repo
     */
    public Git pullBySsh(String path, String envRas) throws GitAPIException {
        File repoGitDir = new File(path);
        try (Repository repository = new FileRepository(repoGitDir.getAbsolutePath())) {
            return pullBySsh(repository, envRas);
        } catch (IOException e) {
            throw new CommonException("devops.git.pull", e);
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
     * clone 并checkout
     *
     * @param dirName
     * @param remoteUrl
     * @param accessToken
     * @return
     */
    public void cloneAndCheckout(String dirName, String remoteUrl, String accessToken, String commit) {
        File localPathFile = new File(dirName);
        try {
            Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(localPathFile)
                    .setCredentialsProvider(StringUtils.isEmpty(accessToken) ? null : new UsernamePasswordCredentialsProvider("", accessToken))
                    .call();
            git.checkout().setName(commit).call();
            git.close();
            FileUtil.deleteDirectory(new File(localPathFile + GIT_SUFFIX));
        } catch (Exception e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        }
    }

    /**
     * 克隆公开仓库的或者根据access token克隆私库的代码所有分支
     *
     * @param dirName     directory name
     * @param remoteUrl   remote url to clone
     * @param accessToken the access token for access
     * @return the git instance of local repository
     */
    public Git cloneRepository(String dirName, String remoteUrl, String accessToken) {
        Git git;
        String workingDirectory = getWorkingDirectory(dirName);
        File localPathFile = new File(workingDirectory);
        deleteDirectory(localPathFile);
        try {
            Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setCloneAllBranches(true)
                    .setCredentialsProvider(StringUtils.isEmpty(accessToken) ? null : new UsernamePasswordCredentialsProvider("", accessToken))
                    .setDirectory(localPathFile)
                    .call();
            git = Git.open(new File(localPathFile + GIT_SUFFIX));
        } catch (GitAPIException | IOException e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        }
        return git;
    }

    public Git cloneRepository(File localPathFile, String remoteUrl, String accessToken) {
        Git git;
        deleteDirectory(localPathFile);
        try {
            Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setCloneAllBranches(true)
                    .setCredentialsProvider(StringUtils.isEmpty(accessToken) ? null : new UsernamePasswordCredentialsProvider("", accessToken))
                    .setDirectory(localPathFile)
                    .call();
            git = Git.open(new File(localPathFile + GIT_SUFFIX));
        } catch (GitAPIException | IOException e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        }
        return git;
    }

    /**
     * 克隆公开仓库或者根据access token克隆代码克隆特定分支
     *
     * @param dirName     directory name
     * @param remoteUrl   remote url to clone
     * @param accessToken the access token for access
     * @param branchName  branch name
     * @return the git instance of local repository
     */
    public Git cloneRepository(String dirName, String remoteUrl, String accessToken, String branchName) {
        String workingDirectory = getWorkingDirectory(dirName);
        File localPathFile = new File(workingDirectory);
        deleteDirectory(localPathFile);
        try {
            return Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setCloneAllBranches(false)
                    .setBranch(branchName)
                    .setCredentialsProvider(StringUtils.isEmpty(accessToken) ? null : new UsernamePasswordCredentialsProvider("", accessToken))
                    .setDirectory(localPathFile)
                    .call();
        } catch (GitAPIException e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        }
    }

    /**
     * 提交并push到远程代码仓库
     *
     * @param git         本地git对象
     * @param repoUrl     仓库地址
     * @param accessToken token
     * @throws CommonException 异常发生时，应捕获此异常，关闭资源
     */
    public void commitAndPush(Git git, String repoUrl, String accessToken, String refName) {
        try {
            git.add().addFilepattern(".").call();
            git.add().setUpdate(true).addFilepattern(".").call();
            git.commit().setMessage("Render Variables[skip ci]").call();
            List<Ref> refs = git.branchList().call();
            PushCommand pushCommand = git.push();
            for (Ref ref : refs) {
                if (ref.getName().equals(refName)) {
                    pushCommand.add(ref);
                    break;
                }
            }
            pushCommand.setRemote(repoUrl);
            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                    "", accessToken));
            pushCommand.call();
        } catch (GitAPIException e) {
            throw new CommonException(DEVOPS_GIT_PUSH, e);
        }
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

    /**
     * 将本地已经存在的单个tag推送到远程仓库
     *
     * @param git         Git对象
     * @param repoUrl     仓库地址
     * @param accessToken 访问token
     * @param tagName     要推送的tag名
     */
    public void pushLocalTag(Git git, String repoUrl, String accessToken, String tagName) {
        try {
            // 推代码
            git.push().add(tagName)
                    .setRemote(repoUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("", accessToken))
                    .call();
        } catch (GitAPIException e) {
            throw new CommonException(DEVOPS_GIT_PUSH, e);
        }
    }

    public void commitAndPushForMaster(Git git, String repoUrl, String commitMessage, String accessToken) {
        try {
            git.add().addFilepattern(".").call();
            git.add().setUpdate(true).addFilepattern(".").call();
            git.commit().setMessage("The download version：" + commitMessage).call();
            PushCommand pushCommand = git.push();
            pushCommand.add(MASTER);
            pushCommand.setRemote(repoUrl);
            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("admin", accessToken));
            pushCommand.call();
        } catch (GitAPIException e) {
            throw new CommonException(DEVOPS_GIT_PUSH, e);
        }
    }

    /**
     * 获取最近一次commit
     *
     * @param git
     * @return
     */
    public String getFirstCommit(Git git) {
        String commit;
        try {
            Iterable<RevCommit> log = git.log().call();
            commit = log.iterator().next().getName();
        } catch (GitAPIException e) {
            throw new CommonException("devops.get.commit");
        }
        return commit;
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

    public Git initGit(File file) {
        Git git = null;
        try {
            git = Git.init().setDirectory(file).call();
        } catch (GitAPIException e) {
            throw new CommonException("devops.git.init", e);
        }
        return git;
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
     * @param sshKey  ssh私钥
     * @param tagName 要删除的tag的名称
     * @throws CommonException push error
     */
    public static void deleteTagAndPush(Git git, String sshKey, String tagName) {
        try {
            PushCommand pushCommand = git.push();
            List<Ref> refs = git.tagList().call();
            for (Ref ref : refs) {
                if (ref.getName().equals(DEV_OPS_REFS + tagName)) {
                    pushCommand.add(":" + ref.getName());
                }
            }
            pushCommand.setRemote("origin");
            pushCommand.setForce(true);
            pushCommand.setTransportConfigCallback(getTransportConfigCallback(sshKey)).call();
            git.tagDelete().setTags(tagName).call();
        } catch (GitAPIException e) {
            throw new CommonException("delete tag fail", e);
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

    /**
     * 本地删除tag后，创建新tag推送至远程仓库
     *
     * @param git     本地git仓库的引用
     * @param sshKey  ssh私钥
     * @param tagName tag名称
     * @param sha     要打tag的commit的散列值
     */
    public static void pushTag(Git git, String sshKey, String tagName, String sha) {
        deleteTag(git, tagName);
        createTagAndPush(git, sshKey, tagName, sha);
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


    public GitConfigVO getGitConfig(Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterMapper.selectByPrimaryKey(clusterId);
        List<DevopsEnvironmentDTO> devopsEnvironments = devopsEnvironmentService.listEnvWithInstancesByClusterIdForAgent(clusterId);
        GitConfigVO gitConfigVO = new GitConfigVO();
        gitConfigVO.setAgentName("choerodon-cluster-agent-" + devopsClusterDTO.getCode());
        LOGGER.info("Get git config to init cluster: agent name: {}", gitConfigVO.getAgentName());
        List<GitEnvConfigVO> gitEnvConfigDTOS = new ArrayList<>();
        devopsEnvironments.forEach(devopsEnvironmentDTO -> {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(devopsEnvironmentDTO.getProjectId());
            Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            String repoUrl = GitUtil.getGitlabSshUrl(PATTERN, gitlabSshUrl, organizationDTO.getTenantNum(), projectDTO.getDevopsComponentCode(), devopsEnvironmentDTO.getCode(), EnvironmentType.forValue(devopsEnvironmentDTO.getType()), devopsClusterDTO.getCode());

            GitEnvConfigVO gitEnvConfigVO = new GitEnvConfigVO();
            gitEnvConfigVO.setEnvId(devopsEnvironmentDTO.getId());
            gitEnvConfigVO.setGitRsaKey(devopsEnvironmentDTO.getEnvIdRsa());
            gitEnvConfigVO.setGitUrl(repoUrl);
            gitEnvConfigVO.setInstances(devopsEnvironmentDTO.getInstances());
            gitEnvConfigVO.setNamespace(GitOpsUtil.getEnvNamespace(devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getType()));
            LOGGER.info("Agent Init: instances for env with id {} is {}", devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getInstances());
            gitEnvConfigDTOS.add(gitEnvConfigVO);
        });
        gitConfigVO.setEnvs(gitEnvConfigDTOS);
        gitConfigVO.setGitHost(gitlabSshUrl);
        gitConfigVO.setCertManagerVersion(devopsClusterResourceService.queryCertManagerVersion(clusterId));
        return gitConfigVO;
    }

    private void addFile(Git git, String relativePath) throws GitAPIException {
        git.add().setUpdate(false).addFilepattern(relativePath).call();
        git.add().setUpdate(true).addFilepattern(relativePath).call();
    }

    private void commitChanges(Git git, String commitMsg) throws GitAPIException {
        git.commit().setMessage(commitMsg).call();
    }
}
