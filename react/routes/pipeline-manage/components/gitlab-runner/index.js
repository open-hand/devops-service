import React from 'react';
import { Icon } from 'choerodon-ui/pro';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import { Choerodon } from '@choerodon/boot';
import {
  A_KEY_INSTALLATION,
  ADD_CHART,
  CRETE_PV,
  DEPLOY_RUNNER,
} from './Constant';

import './index.less';

export default function GitlabRunner() {
  const prefixCls = 'c7ncd-pipelineManage-runner';
  const options = {
    format: 'text/plain',
  };

  const handleCopy = () => { Choerodon.prompt('复制成功'); };

  return (
    <div className={`${prefixCls}`}>
      <p>Gitlab Runner，用于代码提交后自动进行代码测试、构建服务的镜像及生成helm chart并将结果发回给Choerodon。
        它与GitLab CI一起使用，Gitlab CI是Gitlab中包含的开源持续集成服务，用于协调作业。
      </p>
      <blockquote className="warning">
        注意：若在安装Choerodon系统时，已经成功部署和配置过GitLab Runner。便不用重复进行以下操作。
      </blockquote>
      <h3>预备知识</h3>
      <p>
        <span className="block-span">如果你还不知道Gitlab Runner是做什么的，请参考下面链接进行学习：</span>
        <a
          href="https://docs.gitlab.com/runner/"
          target="_blank"
          rel="nofollow me noopener noreferrer"
        >
          https://docs.gitlab.com/runner/
        </a>
      </p>
      <h3>方式一：一键安装Runner</h3>
      <p>如你使用一键部署安装的猪齿鱼，在同一集群中可以使用下面命令一键部署Gitlab-Runner</p>
      <blockquote className="code">
        {A_KEY_INSTALLATION}
        <CopyToClipboard
          text={A_KEY_INSTALLATION}
          onCopy={handleCopy}
          options={options}
        >
          <Icon type="library_books" className="copy-button" />
        </CopyToClipboard>
      </blockquote>
      <h3>方式二：手动安装Runner</h3>
      <h4>Step1：获取Runner注册Token</h4>
      <blockquote>
        {'Note：此教程注册的Runner属性为共享，若需注册私有Runner或者无法进入Gitlab管理界面，注册Token请在Git项目仓库 Settings > CI/CD > Runners settings 菜单中获取。'}
      </blockquote>
      <div className="image-1" />
      <h4>Step2：添加choerodon chart仓库</h4>
      <pre className="code">
        {ADD_CHART}
        <CopyToClipboard
          text={ADD_CHART}
          onCopy={handleCopy}
          options={options}
        >
          <Icon type="library_books" className="copy-button" />
        </CopyToClipboard>
      </pre>
      <h4>Step3：部署Runner</h4>
      <blockquote>
        Note：启用持久化存储请执行提前创建所对应的物理目录，PV和PVC可使用以下语句进行创建；可在部署命令中添加--debug --dry-run参数，进行渲染预览不进行部署。
      </blockquote>
      <p>- 创建缓存所需PV和PVC</p>
      <pre className="code">
        {CRETE_PV}
        <CopyToClipboard
          text={CRETE_PV}
          onCopy={handleCopy}
          options={options}
        >
          <Icon type="library_books" className="copy-button" />
        </CopyToClipboard>
      </pre>
      <p>- 部署Runner</p>
      <blockquote>
        Note：请确认你所搭建的K8S集群是否已开启RBAC授权（按照本站Kubernetes部署教程部署的集群默认是开启
        RBAC授权的），若未开启，请删除下面执行命令中的--set rbac.create=true设置后执行。
      </blockquote>
      <pre className="code">
        {DEPLOY_RUNNER}
        <CopyToClipboard
          text={DEPLOY_RUNNER}
          onCopy={handleCopy}
          options={options}
        >
          <Icon type="library_books" className="copy-button" />
        </CopyToClipboard>
      </pre>
      <p>- 参数</p>
      <p>
        1. <span className="code_block">env.environment.*</span>为CI时Pod的环境变量键值对，
        <span className="code_block">*</span>就是环境变量名，等号后面的为该变量的值，这里例子中添加这几个环境变量建议配置，使用Choerodon管理的项目进行CI时会用到它们，若还需其他环境变量请自定义。</p>
      <p>2. <span className="code_block">env.persistence.*</span>为CI时Pod的挂载PVC与Pod内目录的键值对，
        <span className="code_block">*</span>就是PVC的名称，等号后面的值为要挂载到Pod的哪个目录，这里注意一点用引号引起来。
        本例中我们新建了两个PVC即<span className="code_block">runner-maven-pvc</span>、<span className="code_block">runner-cache-pvc</span>分别挂载到<span className="code_block">/root/.m2</span>和<span className="code_block">/cache</span>目录中。</p>
      <div className="gitlab-runner-table">
        <table border="1">
          <tr>
            <td>参数</td>
            <td>含义</td>
          </tr>
          <tr>
            <td>rbac.create</td>
            <td>创建sa及授权</td>
          </tr>
          <tr>
            <td>env.concurrent</td>
            <td>可以同时进行的CI数量</td>
          </tr>
          <tr>
            <td>env.gitlabUrl</td>
            <td>Gitlab地址</td>
          </tr>
          <tr>
            <td>env.runnerRegistrationToken</td>
            <td>Runner注册Token</td>
          </tr>
          <tr>
            <td>env.environment.CHOERODON_URL</td>
            <td>Choerodon API地址</td>
          </tr>
          <tr>
            <td>env.persistence.runner-maven-pvc</td>
            <td>持久化数据，此处的<code>runner-maven-pvc</code>为PVC名称,值要挂载到Pod的<code>/root/.m2</code>目录</td>
          </tr>
          <tr>
            <td>env.persistence.runner-cache-pvc</td>
            <td>持久化数据，此处的<span>runner-cache-pvc</span>为PVC名称,值要挂载到Pod的<code>/cache</code>目录</td>
          </tr>
          <tr>
            <td>enabled_mount_host_docker_sock</td>
            <td>是否将<code>dockers.sock</code>节点文件挂在到Pod中，以便build docker镜像</td>
          </tr>
        </table>
      </div>
      <p>
        <span>- 更多Runner设置请参考 </span>
        <a
          href="https://docs.gitlab.com/runner/"
          target="_blank"
          rel="nofollow me noopener noreferrer"
        >
          https://docs.gitlab.com/runner/
        </a>
      </p>

    </div>
  );
}
