package io.choerodon.devops.infra.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.GitConfigDTO;
import io.choerodon.devops.api.vo.GitEnvConfigDTO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.app.service.impl.DevopsGitServiceImpl;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitUtil {

    public static final String DEV_OPS_SYNC_TAG = "devops-sync";
    public static final String TEMPLATE = "template";
    private static final String MASTER = "master";
    private static final String PATH = "/";
    private static final String GIT_SUFFIX = "/.git";
    private static final String ERROR_GIT_CLONE = "error.git.clone";
    private static final String REPO_NAME = "devops-service-repo";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsGitServiceImpl.class);
    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");

    private String classPath;
    private String sshKey;


    @Autowired
    DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    IamRepository iamRepository;

    @Value("${template.url}")
    private String repoUrl;
    @Value("${template.version}")
    private String version;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;

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

    public static String getGitlabSshUrl(Pattern pattern, String url, String orgCode, String proCode, String envCode) {
        String result = "";
        if (url.contains("@")) {
            String[] urls = url.split(":");
            if (urls.length == 1) {
                result = String.format("%s:%s-%s-gitops/%s.git",
                        url, orgCode, proCode, envCode);
            } else {
                if (pattern.matcher(urls[1]).matches()) {
                    result = String.format("ssh://%s/%s-%s-gitops/%s.git",
                            url, orgCode, proCode, envCode);
                }
            }
        } else {
            String[] urls = url.split(":");
            if (urls.length == 1) {
                result = String.format("git@%s:%s-%s-gitops/%s.git",
                        url, orgCode, proCode, envCode);
            } else {
                if (pattern.matcher(urls[1]).matches()) {
                    result = String.format("ssh://git@%s/%s-%s-gitops/%s.git",
                            url, orgCode, proCode, envCode);
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
    public Git cloneBySsh(String path, String url) {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(url);
        cloneCommand.setBranch(MASTER);
        cloneCommand.setTransportConfigCallback(getTransportConfigCallback());
        try {
            cloneCommand.setDirectory(new File(path));
            return cloneCommand.call();
        } catch (GitAPIException e) {
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
        File repoGitDir = new File(path);
        try (Repository repository = new FileRepository(repoGitDir.getAbsolutePath())) {
            pullBySsh(repository);
        } catch (IOException e) {
            LOGGER.info("Get repository error", e);
        }
    }

    private void pullBySsh(Repository repository) {
        try (Git git = new Git(repository)) {
            git.pull()
                    .setTransportConfigCallback(getTransportConfigCallback())
                    .setRemoteBranchName(MASTER)
                    .call();
        } catch (GitAPIException e) {
            LOGGER.info("Pull error", e);
        }
    }

    private TransportConfigCallback getTransportConfigCallback() {
        return transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactor());
        };
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

    /**
     * Git克隆
     */
    public Git clone(String name, String type, String remoteUrl) {
        Git git = null;
        String branch;
        String workingDirectory = getWorkingDirectory(name);
        File localPathFile = new File(workingDirectory);
        deleteDirectory(localPathFile);
        switch (type) {
            case "MicroServiceFront":
                return cloneGitHubTemplate("choerodon-front-template", workingDirectory, version);
            case "MicroService":
                return cloneGitHubTemplate("choerodon-microservice-template", workingDirectory, version);
            case "JavaLib":
                return cloneGitHubTemplate("choerodon-javalib-template", workingDirectory, version);
            case "ChoerodonMoChaTemplate":
                return cloneGitHubTemplate("choerodon-mocha-template", workingDirectory, version);
            case "GoTemplate":
                return cloneGitHubTemplate("choerodon-golang-template", workingDirectory, version);
            case "SpringBootTemplate":
                return cloneGitHubTemplate("choerodon-springboot-template", workingDirectory, version);
            default:
                branch = MASTER;
                try {
                    Git.cloneRepository()
                            .setURI(remoteUrl)
                            .setBranch(branch)
                            .setDirectory(localPathFile)
                            .call();
                    FileUtil.deleteDirectory(new File(localPathFile + GIT_SUFFIX));
                    git = Git.init().setDirectory(localPathFile).call();

                } catch (GitAPIException e) {
                    throw new CommonException(ERROR_GIT_CLONE, e);
                }
                break;
        }
        return git;
    }

    private Git cloneGitHubTemplate(String type, String localPathFile, String version) {
        Git git = null;
        try {
            if (!new File(TEMPLATE).exists()) {
                Git.cloneRepository()
                        .setURI(repoUrl)
                        .setCloneSubmodules(true)
                        .setBranch(version)
                        .setDirectory(new File(TEMPLATE))
                        .call();
            }
            if (new File(TEMPLATE + "/" + type + GIT_SUFFIX).exists()) {
                FileUtil.deleteFile(new File(TEMPLATE + "/" + type + GIT_SUFFIX));
            }
            FileUtil.copyDir(new File(TEMPLATE + "/" + type), new File(localPathFile));
            git = Git.init().setDirectory(new File(localPathFile)).call();
        } catch (GitAPIException e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        }
        return git;
    }


    /**
     * clone 外部代码平台的仓库
     *
     * @param dirName     directory name
     * @param remoteUrl   remote url to clone
     * @param accessToken the access token for access
     * @return the git instance of local repository
     */
    public Git cloneRepository(String dirName, String remoteUrl, String accessToken) {
        Git git = null;
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
        } catch (GitAPIException e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        } catch (IOException e) {
            throw new CommonException(ERROR_GIT_CLONE, e);
        }
        return git;
    }

    /**
     * 提交并push到远程代码仓库
     *
     * @param git         本地git对象
     * @param repoUrl     仓库地址
     * @param accessToken token
     * @throws CommonException 异常发生时，应捕获此异常，关闭资源
     */
    public void commitAndPush(Git git, String repoUrl, String accessToken, String refName) throws CommonException {
        try {
            String[] url = repoUrl.split("://");
            git.add().addFilepattern(".").call();
            git.add().setUpdate(true).addFilepattern(".").call();
            git.commit().setMessage("Render Variables[skip ci]").call();
            List<Ref> refs = git.branchList().call();
            PushCommand pushCommand = git.push();
            for (Ref ref : refs) {
                if (ref.getName().equals(refName)) {
                    pushCommand.add(ref);
                }
            }
            pushCommand.setRemote(repoUrl);
            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                    "", accessToken));
            pushCommand.call();
        } catch (GitAPIException e) {
            throw new CommonException("error.git.push", e);
        }
    }

    /**
     * 将代码推到目标库
     */
    public void push(Git git, String name, String repoUrl, String userName, String accessToken) {
        try {
            String[] url = repoUrl.split("://");
            git.add().addFilepattern(".").call();
            git.add().setUpdate(true).addFilepattern(".").call();
            git.commit().setMessage("Render Variables[skip ci]").call();
            List<Ref> refs = git.branchList().call();
            PushCommand pushCommand = git.push();
            for (Ref ref : refs) {
                pushCommand.add(ref);
            }
            pushCommand.setRemote(url[0] + "://gitlab-ci-token:" + accessToken + "@" + url[1]);
            LOGGER.info("push remote is:" + pushCommand.getRemote());
            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                    userName, accessToken));
            pushCommand.call();
        } catch (GitAPIException e) {
            throw new CommonException("error.git.push", e);
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
                throw new CommonException("error.directory.delete", e);
            }
        }
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


    public String handDevopsEnvGitRepository(Long projectId, String envCode, String envRsa) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        //本地路径
        String path = String.format("gitops/%s/%s/%s",
                organization.getCode(), projectE.getCode(), envCode);
        //生成环境git仓库ssh地址
        String url = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organization.getCode(),
                projectE.getCode(), envCode);

        File file = new File(path);
        this.setSshKey(envRsa);
        if (!file.exists()) {
            this.cloneBySsh(path, url);
        }
        return path;
    }


    public GitConfigDTO getGitConfig(Long clusterId) {
        List<DevopsEnvironmentE> devopsEnvironments = devopsEnvironmentRepository.listByClusterId(clusterId);
        GitConfigDTO gitConfigDTO = new GitConfigDTO();
        List<GitEnvConfigDTO> gitEnvConfigDTOS = new ArrayList<>();
        devopsEnvironments.stream().filter(devopsEnvironmentE -> devopsEnvironmentE.getGitlabEnvProjectId() != null).forEach(devopsEnvironmentE -> {
            ProjectVO projectE = iamRepository.queryIamProject(devopsEnvironmentE.getProjectE().getId());
            OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            String repoUrl = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organization.getCode(), projectE.getCode(), devopsEnvironmentE.getCode());

            GitEnvConfigDTO gitEnvConfigDTO = new GitEnvConfigDTO();
            gitEnvConfigDTO.setEnvId(devopsEnvironmentE.getId());
            gitEnvConfigDTO.setGitRsaKey(devopsEnvironmentE.getEnvIdRsa());
            gitEnvConfigDTO.setGitUrl(repoUrl);
            gitEnvConfigDTO.setNamespace(devopsEnvironmentE.getCode());
            gitEnvConfigDTOS.add(gitEnvConfigDTO);
        });
        gitConfigDTO.setEnvs(gitEnvConfigDTOS);
        gitConfigDTO.setGitHost(gitlabSshUrl);
        return gitConfigDTO;
    }

    private void addFile(Git git, String relativePath) throws GitAPIException {
        git.add().setUpdate(false).addFilepattern(relativePath).call();
        git.add().setUpdate(true).addFilepattern(relativePath).call();
    }

    private void commitChanges(Git git, String commitMsg) throws GitAPIException {
        git.commit().setMessage(commitMsg).call();
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }
}
