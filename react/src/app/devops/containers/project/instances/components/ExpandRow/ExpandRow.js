import React, { Component, Fragment } from "react";
import { FormattedMessage, injectIntl } from "react-intl";
import { observer } from "mobx-react";
import { withRouter } from "react-router-dom";
import _ from "lodash";
import TimeAgo from "timeago-react";
import { stores, Content } from "@choerodon/boot";
import { Tooltip, Button, Modal, Collapse, Spin } from "choerodon-ui";
import { formatDate } from "../../../../../utils/index";
import DeploymentStore from "../../../../../stores/project/instances/DeploymentStore";
import InstancesStore from "../../../../../stores/project/instances/InstancesStore";
import SimpleTable from "./SimpleTable";
import PodCircle from "./PodCircle";

import "./index.scss";


const { AppState } = stores;
const { Sidebar } = Modal;
const { Panel } = Collapse;

const PANEL_TYPE = [
  'ports',
  'volume',
  'health',
  'security',
  'label',
  'variables',
];

@observer
class ExpandRow extends Component {
  constructor(props) {
    super(props);
    this.state = {
      visible: false,
      sideName: '',
      activeKey: [],
      isAllExpand: false,
    };
    this.getExpandContent = this.getExpandContent.bind(this);
    this.renderPorts = this.renderPorts.bind(this);
    this.renderVolume = this.renderVolume.bind(this);
    this.renderHealth = this.renderHealth.bind(this);
    this.renderSecurity = this.renderSecurity.bind(this);
    this.renderLabel = this.renderLabel.bind(this);
    this.renderVar = this.renderVar.bind(this);
    this.handleExpandAll = this.handleExpandAll.bind(this);
    this.containerLabel = (
      <span className="c7ncd-deploy-container-label">
        {this.props.intl.formatMessage({ id: 'ist.deploy.container' })}
      </span>
    );
  }

  handleLink() {
    InstancesStore.setIsCache({ isCache: true });
  }

  changeTargetCount = count => {
    InstancesStore.setTargetCount(count);
  };

  getExpandContent() {
    const content = [];
    const {
      record,
      intl: { formatMessage },
    } = this.props;

    const getPodContent = dto =>
      _.map(record[dto], item => this.getDeployContent(dto, item, record));

    const getNoPodContent = dto =>
      _.map(record[dto], item => this.getNoPodContent(dto, item));

    const contentList = [
      {
        title: 'Deployments',
        main: getPodContent('deploymentDTOS'),
      },
      {
        title: 'Stateful Set',
        main: getPodContent('statefulSetDTOS'),
      },
      {
        title: 'Daemon Set',
        main: getPodContent('daemonSetDTOS'),
      },
      {
        title: 'PVC',
        main: getNoPodContent('persistentVolumeClaimDTOS'),
      },
      {
        title: 'Service',
        main: getNoPodContent('serviceDTOS'),
      },
      {
        title: 'Ingress',
        main: getNoPodContent('ingressDTOS'),
      },
    ];

    const hasContent = _.find(contentList, item => item.main.length);

    return (
      <div className="c7n-deploy-expanded">
        {_.map(contentList, dto => {
          const { main, title } = dto;
          return main.length ? (
            <Fragment key={title}>
              <div className="c7n-deploy-expanded-title">
                <span>{title}</span>
              </div>
              {main}
            </Fragment>
          ) : null;
        })}
        {!hasContent ? (
          <div className="c7n-deploy-expanded-empty">
            <FormattedMessage id="ist.expand.empty" />
          </div>
        ) : null}
      </div>
    );
  }

  /**
   *
   * @param {string} podType
   * @param {object} item
   * @param {object} record
   * @returns
   * @memberof ExpandRow
   */
  getDeployContent = (podType, item, record) => {
    const { envId, appId, status, id, connect } = record;
    const { name, age, devopsEnvPodDTOS } = item;

    const POD_TYPE = {
      // 确保“当前/需要/可提供”的顺序
      deploymentDTOS: ['current', 'desired', 'available'],
      daemonSetDTOS: [
        'currentScheduled',
        'desiredScheduled',
        'numberAvailable',
      ],
      statefulSetDTOS: ['currentReplicas', 'desiredReplicas', 'readyReplicas'],
    };
    const [current, desired, available] = POD_TYPE[podType];

    const targetCount = InstancesStore.getTargetCount;

    // 计算 pod 数量和环形图占比
    const count = _.countBy(devopsEnvPodDTOS, pod => !!pod.ready);
    const correctCount = count['true'] || 0;
    const errorCount = count['false'] || 0;
    const sum = correctCount + errorCount;
    const correct = sum > 0 ? (correctCount / sum) * (Math.PI * 2 * 30) : 0;

    /**
     * 返回路径
     * 从实例点过去的返回实例
     * 从环境总览点过去的返回实力总览
     */
    const currentPage = window.location.href.includes('env-overview')
      ? 'env-overview'
      : 'instance';

    return (
      <div key={name} className="c7n-deploy-expanded-item">
        <ul className="c7n-deploy-expanded-text">
          <li className="c7n-deploy-expanded-lists">
            <span className="c7n-deploy-expanded-keys c7n-expanded-text_bold">
              <FormattedMessage id="ist.expand.name" />：
            </span>
            <span
              title={name}
              className="c7n-deploy-expanded-values c7n-expanded-text_bold"
            >
              {name}
            </span>
          </li>
          <li className="c7n-deploy-expanded-lists">
            <span className="c7n-deploy-expanded-keys">
              {podType === 'deploymentDTOS' ? (
                'ReplicaSet'
              ) : (
                <FormattedMessage id={`ist.expand.net.status`} />
              )}
              ：
            </span>
            <span
              title={`${item[available] || 0} available / ${item[current] ||
              0} current / ${item[desired] || 0} desired`}
              className="c7n-deploy-expanded-values"
            >{`${item[available] || 0} available / ${item[current] ||
            0} current / ${item[desired] || 0} desired`}</span>
          </li>
          <li className="c7n-deploy-expanded-lists">
            <span className="c7n-deploy-expanded-keys">
              <FormattedMessage id="ist.expand.date" />：
            </span>
            <span className="c7n-deploy-expanded-values">
              <Tooltip title={formatDate(age)}>
                <TimeAgo
                  datetime={age}
                  locale={Choerodon.getMessage('zh_CN', 'en')}
                />
              </Tooltip>
            </span>
          </li>
          <li className="c7n-deploy-expanded-lists">
            <Button
              className="c7ncd-detail-btn"
              onClick={() => this.handleClick(podType, id, name)}
            >
              <FormattedMessage id="detailMore" />
            </Button>
          </li>
        </ul>
        <div className="c7n-deploy-expanded-pod">
          <PodCircle
            podType={podType}
            connect={connect}
            appId={appId}
            envId={envId}
            instanceId={id}
            name={name}
            count={{
              sum,
              correct,
              correctCount,
            }}
            targetCount={targetCount}
            status={status}
            handleLink={this.handleLink}
            handleChangeCount={this.changeTargetCount}
            currentPage={currentPage}
            store={DeploymentStore}
          />
        </div>
      </div>
    );
  };

  /**
   * PVC service ingress 三个没有pod圈
   * @param {string} type
   * @param {array} data
   */
  getNoPodContent = (type, data) => {
    const TYPE_KEY = {
      serviceDTOS: {
        leftItems: ['type', 'externalIp', 'age'],
        rightItems: ['clusterIp', 'port'],
      },
      ingressDTOS: {
        leftItems: ['type', 'ports'],
        rightItems: ['address', 'age'],
      },
      persistentVolumeClaimDTOS: {
        leftItems: ['status', 'accessModes'],
        rightItems: ['capacity', 'age'],
      },
    };
    const content = key => {
      let text = null;
      switch (key) {
        case 'age':
          text = (
            <Tooltip title={formatDate(data[key])}>
              <TimeAgo
                datetime={data[key]}
                locale={Choerodon.getMessage('zh_CN', 'en')}
              />
            </Tooltip>
          );
          break;
        case 'status':
          text = (
            <span className={`c7n-deploy-pvc c7n-deploy-pvc_${data[key]}`}>
              {data[key]}
            </span>
          );
          break;
        default:
          text = data[key];
          break;
      }
      return (
        <li className="c7n-deploy-expanded-lists">
          <span className="c7n-deploy-expanded-keys">
            <FormattedMessage id={`ist.expand.net.${key}`} />：
          </span>
          <span className="c7n-deploy-expanded-values">{text}</span>
        </li>
      );
    };
    return (
      <div key={data.name} className="c7n-deploy-expanded-item">
        <div className="c7n-deploy-expanded-lists">
          <span className="c7n-deploy-expanded-keys c7n-expanded-text_bold">
            <FormattedMessage id="ist.expand.name" />：
          </span>
          <span
            title={data.name}
            className="c7n-deploy-expanded-values c7n-expanded-text_bold"
          >
            {data.name}
          </span>
        </div>
        <ul className="c7n-deploy-expanded-text c7n-deploy-expanded_half">
          {_.map(TYPE_KEY[type].leftItems, item => content(item))}
        </ul>
        <ul className="c7n-deploy-expanded-text c7n-deploy-expanded_half">
          {_.map(TYPE_KEY[type].rightItems, item => content(item))}
        </ul>
      </div>
    );
  };

  /**
   * 打开Deployment详情侧边栏，并加载数据
   * @param {string} type
   * @param {*} id
   * @param {*} name
   */
  handleClick = (type, id, name) => {
    const { id: projectId } = AppState.currentMenuType;
    this.setState({ visible: true, sideName: name });
    DeploymentStore.loadDeploymentsJson(type, projectId, id, name);
  };

  hideSidebar = () => {
    this.setState({ visible: false, activeKey: [], isExpand: false });
    DeploymentStore.setData([]);
  };

  handlePanelChange = key => {
    const isExpand = key.length === PANEL_TYPE.length;
    this.setState({ activeKey: key, isExpand });
  };

  handleExpandAll() {
    this.setState(prev => ({
      isExpand: !prev.isExpand,
      activeKey: !prev.isExpand ? PANEL_TYPE : [],
    }));
  }

  renderPorts(containers, isLoading) {
    let portsContent = null;
    let hasPorts = false;

    if (containers && containers.length) {
      const colItems = ['name', 'containerPort', 'protocol', 'hostPort'];

      const columns = _.map(colItems, item => ({
        title: <FormattedMessage id={`ist.deploy.ports.${item}`} />,
        key: item,
        dataIndex: item,
        render: _textOrNA,
      }));

      portsContent = _.map(containers, item => {
        const { name, ports } = item;
        if (ports && ports.length) {
          hasPorts = true;
        }
        return (
          <Fragment key={name}>
            <div className="c7ncd-deploy-container-title">
              <span className="c7ncd-deploy-container-name">{name}</span>
              {this.containerLabel}
            </div>
            <div className="c7ncd-deploy-container-table">
              <SimpleTable columns={columns} data={ports && ports.slice()} />
            </div>
          </Fragment>
        );
      });
    } else {
      portsContent = (
        <div className="c7ncd-deploy-detail-empty">
          <FormattedMessage id="ist.deploy.ports.map" />
          <FormattedMessage id="ist.deploy.ports.empty" />
        </div>
      );
    }

    if (!hasPorts) {
      portsContent = (
        <div className="c7ncd-deploy-detail-empty">
          <FormattedMessage id="ist.deploy.ports.map" />
          <FormattedMessage id="ist.deploy.ports.empty" />
        </div>
      );
    }

    return isLoading ? (
      <div className="c7ncd-deploy-spin">
        <Spin />
      </div>
    ) : (
      portsContent
    );
  }

  renderHealth(containers, isLoading) {
    let healthContent = null;

    if (containers && containers.length) {
      healthContent = _.map(containers, item => {
        const { name } = item;
        const readinessProbe = item.readinessProbe || {};
        const livenessProbe = item.livenessProbe || {};

        const readDom = _returnHealthDom('readiness', readinessProbe);
        const liveDom = _returnHealthDom('liveness', livenessProbe);

        return (
          <div key={name} className="c7ncd-deploy-health-wrap">
            <div className="c7ncd-deploy-container-title">
              <span className="c7ncd-deploy-container-name">{name}</span>
              {this.containerLabel}
            </div>
            <div className="c7ncd-deploy-health-content">
              {readDom}
              {liveDom}
            </div>
          </div>
        );
      });
    } else {
      healthContent = (
        <div className="c7ncd-deploy-detail-empty">
          <p>
            <FormattedMessage id="ist.deploy.health.readiness" />
          </p>
          <FormattedMessage id="ist.deploy.volume.type" />
          <span className="c7ncd-deploy-health-empty">
            <FormattedMessage id="ist.deploy.none" />
          </span>
        </div>
      );
    }

    return isLoading ? (
      <div className="c7ncd-deploy-spin">
        <Spin />
      </div>
    ) : (
      healthContent
    );
  }

  renderVar(containers, isLoading) {
    const columns = [
      {
        width: '50%',
        title: <FormattedMessage id="ist.deploy.variables.key" />,
        key: 'name',
        dataIndex: 'name',
      },
      {
        width: '50%',
        title: <FormattedMessage id="ist.deploy.variables.value" />,
        key: 'value',
        dataIndex: 'value',
      },
    ];

    let hasEnv = false;
    let envContent = _.map(containers, item => {
      const { name, env } = item;
      if (env && env.length) {
        hasEnv = true;
      }
      return (
        <Fragment key={name}>
          <div className="c7ncd-deploy-container-title">
            <span className="c7ncd-deploy-container-name">{name}</span>
            {this.containerLabel}
          </div>
          <div className="c7ncd-deploy-container-table">
            <SimpleTable columns={columns} data={env && env.slice()} />
          </div>
        </Fragment>
      );
    });

    if (!hasEnv) {
      envContent = (
        <div className="c7ncd-deploy-empty-table">
          <SimpleTable columns={columns} data={[]} />
        </div>
      );
    }

    return isLoading ? (
      <div className="c7ncd-deploy-spin">
        <Spin />
      </div>
    ) : (
      envContent
    );
  }

  renderLabel(labels, annotations, isLoading) {
    /**
     * 表格数据
     * @param {object} obj
     * @param {array} col
     */
    function format(obj, col) {
      const arr = [];
      for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
          const value = obj[key];
          arr.push({ key, value });
        }
      }
      return (
        <div className="c7ncd-deploy-container-table">
          <SimpleTable columns={columns} data={arr} />
        </div>
      );
    }

    const columns = [
      {
        width: '50%',
        title: <FormattedMessage id="ist.deploy.key" />,
        key: 'key',
        dataIndex: 'key',
      },
      {
        width: '50%',
        title: <FormattedMessage id="ist.deploy.value" />,
        key: 'value',
        dataIndex: 'value',
      },
    ];

    const labelContent = format(labels, columns);

    const annoContent = format(annotations, columns);

    return isLoading ? (
      <div className="c7ncd-deploy-spin">
        <Spin />
      </div>
    ) : (
      <Fragment>
        <div className="c7ncd-deploy-label">Labels</div>
        {labelContent}
        <div className="c7ncd-deploy-label">Annotations</div>
        {annoContent}
      </Fragment>
    );
  }

  renderVolume(containers, volumes, isLoading) {
    let volumeContent = null;

    const _volumeType = (vol, mounts) => {
      const vDom = _volumesTemplate(vol);
      const columnsItem = ['mountPath', 'subPath', 'readOnly'];
      const columns = _.map(columnsItem, item => ({
        title: <FormattedMessage id={`ist.deploy.volume.${item}`} />,
        key: item,
        dataIndex: item,
        width: item === 'readOnly' ? '16%' : '42%',
        render(text) {
          return _.isBoolean(text) ? text.toString() : text;
        },
      }));

      return (
        <div key={vol.name} className="c7ncd-deploy-volume-wrap">
          {vDom}
          <SimpleTable columns={columns} data={mounts} />
        </div>
      );
    };

    if (volumes && volumes.length) {
      volumeContent = _.map(volumes, vol => {
        const { name } = vol;
        const mounts = [];
        _.forEach(containers, item => {
          const { volumeMounts } = item;
          const filterVol = _.filter(volumeMounts, m => m.name === name);
          mounts.push(...filterVol);
        });
        return _volumeType(vol, mounts);
      });
    } else {
      volumeContent = (
        <div className="c7ncd-deploy-detail-empty">
          <FormattedMessage id="ist.deploy.volume" />
          <FormattedMessage id="ist.deploy.volume.empty" />
        </div>
      );
    }

    return isLoading ? (
      <div className="c7ncd-deploy-spin">
        <Spin />
      </div>
    ) : (
      volumeContent
    );
  }

  renderSecurity(containers, hostIPC, hostNetwork, isLoading) {
    const containerArr = containers.length ? containers : [{}];
    const securityCtx = _.map(containerArr, item => {
      const { imagePullPolicy, name } = item;
      const securityContext = item.securityContext || {};
      const {
        privileged,
        allowPrivilegeEscalation,
        readOnlyRootFilesystem,
        runAsNonRoot,
        capabilities,
      } = securityContext;

      let capAdd = [];
      let capDrop = [];

      if (capabilities) {
        capAdd = capabilities.add;
        capDrop = capabilities.drop;
      }

      const addArr = capAdd.length ? (
        _.map(capAdd, item => (
          <p className="c7ncd-deploy-detail-text">{item}</p>
        ))
      ) : (
        <FormattedMessage id="ist.deploy.none" />
      );
      const dropArr = capDrop.length ? (
        _.map(capDrop, item => (
          <p className="c7ncd-deploy-detail-text">{item}</p>
        ))
      ) : (
        <FormattedMessage id="ist.deploy.none" />
      );

      return (
        <Fragment key={name}>
          <div className="c7ncd-deploy-container-title">
            <span className="c7ncd-deploy-container-name">{name}</span>
            {this.containerLabel}
          </div>
          <div className="c7ncd-deploy-security-block">
            {_securityItem('imagePullPolicy', imagePullPolicy, '_flex')}
            {_securityItem('privileged', privileged, '_flex')}
            {_securityItem(
              'allowPrivilegeEscalation',
              allowPrivilegeEscalation,
              '_flex',
            )}
          </div>
          <div className="c7ncd-deploy-security-block">
            {_securityItem('runAsNonRoot', runAsNonRoot)}
            {_securityItem('readOnlyRootFilesystem', readOnlyRootFilesystem)}
          </div>
          <div className="c7ncd-deploy-security-block">
            {_securityItem('capabilities.add', addArr)}
            {_securityItem('capabilities.drop', dropArr)}
          </div>
        </Fragment>
      );
    });

    const securityContent = (
      <div className="c7ncd-deploy-security-wrap">
        <div className="c7ncd-deploy-security-block">
          {_securityItem('hostIPC', hostIPC)}
          {_securityItem('hostNetwork', hostNetwork)}
        </div>
        {securityCtx}
      </div>
    );

    return isLoading ? (
      <div className="c7ncd-deploy-spin">
        <Spin />
      </div>
    ) : (
      securityContent
    );
  }

  render() {
    const {
      url,
      intl: { formatMessage },
    } = this.props;

    const { visible, sideName, activeKey, isExpand } = this.state;

    const {
      getData: { detail },
      getLoading,
    } = DeploymentStore;

    let containers = [];
    let volumes = [];
    let hostIPC = null;
    let hostNetwork = null;
    let labels = [];
    let annotations = [];

    if (detail) {
      if (detail.metadata) {
        labels = detail.metadata.labels;
        annotations = detail.metadata.annotations;
      }

      if (detail.spec && detail.spec.template && detail.spec.template.spec) {
        const spec = detail.spec.template.spec;
        containers = spec.containers;
        volumes = spec.volumes;
        hostIPC = spec.hostIPC;
        hostNetwork = spec.hostNetwork;
      }
    }

    const renderFun = {
      ports: () => this.renderPorts(containers, getLoading),
      volume: () => this.renderVolume(containers, volumes, getLoading),
      health: () => this.renderHealth(containers, getLoading),
      variables: () => this.renderVar(containers, getLoading),
      security: () =>
        this.renderSecurity(containers, hostIPC, hostNetwork, getLoading),
      label: () => this.renderLabel(labels, annotations, getLoading),
    };

    return (
      <Fragment>
        {this.getExpandContent()}
        <Sidebar
          destroyOnClose
          footer={[
            <Button
              type="primary"
              funcType="raised"
              key="close"
              onClick={this.hideSidebar}
            >
              <FormattedMessage id="close" />
            </Button>,
          ]}
          title={formatMessage({ id: 'ist.deploy.detail' })}
          visible={visible}
        >
          <Content
            code="ist.deploy"
            values={{ name: sideName }}
            className="sidebar-content"
          >
            <div className="c7ncd-expand-btn-wrap">
              <Button
                className="c7ncd-expand-btn"
                onClick={this.handleExpandAll}
              >
                <FormattedMessage id={isExpand ? 'collapseAll' : 'expandAll'} />
              </Button>
            </div>
            <Collapse
              bordered={false}
              activeKey={activeKey}
              onChange={this.handlePanelChange}
            >
              {_.map(PANEL_TYPE, item => (
                <Panel
                  key={item}
                  header={
                    <div className="c7ncd-deploy-panel-header">
                      <div className="c7ncd-deploy-panel-title">
                        <FormattedMessage id={`ist.deploy.${item}`} />
                      </div>
                      <div className="c7ncd-deploy-panel-text">
                        <FormattedMessage id={`ist.deploy.${item}.describe`} />
                      </div>
                    </div>
                  }
                  className="c7ncd-deploy-panel"
                >
                  {visible ? renderFun[item]() : null}
                </Panel>
              ))}
            </Collapse>
          </Content>
        </Sidebar>
      </Fragment>
    );
  }
}

/**
 * 内容为空时返回 n/a
 */
function _textOrNA(text) {
  if (!text && !_.isBoolean(text)) {
    return 'n/a';
  }
  return String(text);
}

/**
 * 返回健康检查的DOM
 * @param {string} name
 * @param {obj} data
 */
function _returnHealthDom(name, data) {
  const items = [
    'failureThreshold',
    'initialDelaySeconds',
    'periodSeconds',
    'successThreshold',
    'timeoutSeconds',
  ];

  return (
    <div className="c7ncd-deploy-health-block">
      <div className="c7ncd-deploy-health-title">
        <FormattedMessage id={`ist.deploy.health.${name}`} />
      </div>
      <div className="c7ncd-deploy-health-main">
        {_.map(items, item => (
          <div className="c7ncd-deploy-health-item">
            <p className="c7ncd-deploy-detail-label">
              <FormattedMessage id={`ist.deploy.health.${item}`} />
            </p>
            <p className="c7ncd-deploy-detail-text">{_textOrNA(data[item])}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * 返回数据卷的项目DOM
 * @param {string} name
 * @param {string} data
 * @param {bool} isBool 该项是不是Bool类型
 */
function _volumesItem(name, data, isBool = false) {
  let value = data;
  if (isBool) {
    value = _.isBoolean(data) ? data.toString() : data;
  }
  return (
    <div className="c7ncd-deploy-volume-item">
      <p className="c7ncd-deploy-detail-label">
        <FormattedMessage id={`ist.deploy.volume.${name}`} />
      </p>
      <p className="c7ncd-deploy-detail-text">{value}</p>
    </div>
  );
}

function _volumesTemplate(data) {
  let template = null;
  const VOL_TYPE = ['configMap', 'persistentVolumeClaim', 'secret', 'hostPath'];

  const { name } = data;
  const vKey = Object.keys(data);

  let type = _.toString(_.filter(VOL_TYPE, item => vKey.includes(item)));

  switch (type) {
    case 'configMap':
    case 'secret':
      const { defaultMode, items, optional, name, secretName } = data[type];
      let itemDom = null;
      if (items && items.length) {
        const columns = [
          {
            title: <FormattedMessage id="ist.deploy.volume.config.key" />,
            key: 'key',
            dataIndex: 'key',
          },
          {
            title: <FormattedMessage id="ist.deploy.volume.config.mode" />,
            key: 'mode',
            dataIndex: 'mode',
          },
          {
            title: <FormattedMessage id="ist.deploy.volume.config.path" />,
            key: 'path',
            dataIndex: 'path',
          },
        ];
        itemDom = <SimpleTable columns={columns} data={items} />;
      } else {
        itemDom = (
          <p className="c7ncd-deploy-detail-text">
            {/* <FormattedMessage id="ist.deploy.none" /> */}
          </p>
        );
      }
      template = (
        <div className="c7ncd-deploy-volume-main">
          {_volumesItem('defaultMode', defaultMode)}
          {_volumesItem('optional', optional, true)}
          <div className={`c7ncd-deploy-volume-item${items ? '_full' : ''}`}>
            <p className="c7ncd-deploy-detail-label">
              <FormattedMessage id="ist.deploy.volume.item" />
            </p>
            {itemDom}
          </div>
        </div>
      );
      break;
    case 'persistentVolumeClaim':
      const { claimName, readOnly } = data[type];
      template = (
        <div className="c7ncd-deploy-volume-main">
          {_volumesItem('claimName', claimName)}
          {_volumesItem('readOnly', readOnly, true)}
        </div>
      );
      break;
    case 'hostPath':
      const { path, type: hostType } = data[type];
      template = (
        <div className="c7ncd-deploy-volume-main">
          {_volumesItem('path', path)}
          {_volumesItem('type', type)}
        </div>
      );
      break;

    default:
      type = '未知';
      break;
  }
  return (
    <Fragment>
      <div className="c7ncd-deploy-volume-main">
        {_volumesItem('name', name)}
        {_volumesItem('volume.type', type)}
      </div>
      {template}
    </Fragment>
  );
}

function _securityItem(name, data, type = '') {
  let content =
    _.isArray(data) || _.isObject(data) ? (
      data
    ) : (
      <p className="c7ncd-deploy-detail-text">{_textOrNA(data)}</p>
    );
  return (
    <div className={`c7ncd-deploy-security-item${type}`}>
      <p className="c7ncd-deploy-detail-label">
        <FormattedMessage id={`ist.deploy.security.${name}`} />
      </p>
      {content}
    </div>
  );
}

export default withRouter(injectIntl(ExpandRow));
