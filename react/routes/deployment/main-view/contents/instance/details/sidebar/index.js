import React, { Component, Fragment } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react';
import _ from 'lodash';
import { Button, Modal, Collapse, Spin } from 'choerodon-ui';
import Store from '../../stores';
import SimpleTable from './SimpleTable';

import './index.less';

const { Sidebar } = Modal;
const { Panel } = Collapse;

const PANEL_TYPE = [
  'volume',
  'health',
  'security',
  'variables',
];

const ContainerLabel = () => (<span className="c7ncd-deploy-container-label">
  <FormattedMessage id="ist.deploy.container" />
</span>);

@observer
export default class DetailsSidebar extends Component {
  static contextType = Store;

  state = {
    activeKey: [],
  };

  handlePanelChange = (key) => {
    const isExpand = key.length === PANEL_TYPE.length;
    this.setState({ activeKey: key, isExpand });
  };

  handleExpandAll = () => {
    this.setState(prev => ({
      isExpand: !prev.isExpand,
      activeKey: !prev.isExpand ? PANEL_TYPE : [],
    }));
  };

  renderHealth = (containers, isLoading) => {
    let healthContent = null;

    if (containers && containers.length) {
      healthContent = _.map(containers, (item) => {
        const { name } = item;
        const readinessProbe = item.readinessProbe || {};
        const livenessProbe = item.livenessProbe || {};

        const readDom = returnHealthDom('readiness', readinessProbe);
        const liveDom = returnHealthDom('liveness', livenessProbe);

        return (
          <div key={name} className="c7ncd-deploy-health-wrap">
            <div className="c7ncd-deploy-container-title">
              <span className="c7ncd-deploy-container-name">{name}</span>
              <ContainerLabel />
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
  };

  renderVar = (containers, isLoading) => {
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
    let envContent = _.map(containers, (item) => {
      const { name, env } = item;
      if (env && env.length) {
        hasEnv = true;
      }
      return (
        <Fragment key={name}>
          <div className="c7ncd-deploy-container-title">
            <span className="c7ncd-deploy-container-name">{name}</span>
            <ContainerLabel />
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
  };

  renderVolume = (containers, volumes, isLoading) => {
    let volumeContent = null;

    const volumeType = (vol, mounts) => {
      const vDom = volumesTemplate(vol);
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
          <SimpleTable
            columns={columns}
            data={mounts}
            rowKey={record => record.key}
          />
        </div>
      );
    };

    if (volumes && volumes.length) {
      volumeContent = _.map(volumes, (vol) => {
        const { name } = vol;
        const mounts = [];
        _.forEach(containers, (item) => {
          const { volumeMounts } = item;
          const filterVol = _.filter(volumeMounts, m => m.name === name);
          mounts.push(...filterVol);
        });
        return volumeType(vol, mounts);
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
  };

  renderSecurity = (containers, hostIPC, hostNetwork, isLoading) => {
    const containerArr = containers.length ? containers : [{}];
    const securityCtx = _.map(containerArr, (item) => {
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
        _.map(capAdd, text => (
          <p className="c7ncd-deploy-detail-text">{text}</p>
        ))
      ) : (
        <FormattedMessage id="ist.deploy.none" />
      );
      const dropArr = capDrop.length ? (
        _.map(capDrop, text => (
          <p className="c7ncd-deploy-detail-text">{text}</p>
        ))
      ) : (
        <FormattedMessage id="ist.deploy.none" />
      );

      return (
        <Fragment key={name}>
          <div className="c7ncd-deploy-container-title">
            <span className="c7ncd-deploy-container-name">{name}</span>
            <ContainerLabel />
          </div>
          <div className="c7ncd-deploy-security-block">
            {securityItem('imagePullPolicy', imagePullPolicy, '_flex')}
            {securityItem('privileged', privileged, '_flex')}
            {securityItem(
              'allowPrivilegeEscalation',
              allowPrivilegeEscalation,
              '_flex',
            )}
          </div>
          <div className="c7ncd-deploy-security-block">
            {securityItem('runAsNonRoot', runAsNonRoot)}
            {securityItem('readOnlyRootFilesystem', readOnlyRootFilesystem)}
          </div>
          <div className="c7ncd-deploy-security-block">
            {securityItem('capabilities.add', addArr)}
            {securityItem('capabilities.drop', dropArr)}
          </div>
        </Fragment>
      );
    });

    const securityContent = (
      <div className="c7ncd-deploy-security-wrap">
        <div className="c7ncd-deploy-security-block">
          {securityItem('hostIPC', hostIPC)}
          {securityItem('hostNetwork', hostNetwork)}
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
  };

  render() {
    const { detailsStore, intl: { formatMessage } } = this.context;
    const { visible, onClose } = this.props;
    const { activeKey, isExpand } = this.state;
    const {
      getDeployments: { detail },
      getModalLoading,
    } = detailsStore;

    let containers = [];
    let volumes = [];
    let hostIPC = null;
    let hostNetwork = null;

    if (detail && detail.spec && detail.spec.template && detail.spec.template.spec) {
      const spec = detail.spec.template.spec;
      containers = spec.containers;
      volumes = spec.volumes;
      hostIPC = spec.hostIPC;
      hostNetwork = spec.hostNetwork;
    }

    const renderFun = {
      volume: () => this.renderVolume(containers, volumes, getModalLoading),
      health: () => this.renderHealth(containers, getModalLoading),
      variables: () => this.renderVar(containers, getModalLoading),
      security: () => this.renderSecurity(containers, hostIPC, hostNetwork, getModalLoading),
    };

    return (<Sidebar
      destroyOnClose
      visible
      footer={[
        <Button
          type="primary"
          funcType="raised"
          key="close"
          onClick={onClose}
        >
          <FormattedMessage id="close" />
        </Button>,
      ]}
      title={formatMessage({ id: 'ist.deploy.detail' })}
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
            {visible && renderFun[item]()}
          </Panel>
        ))}
      </Collapse>
    </Sidebar>);
  }
}

/**
 * 内容为空时返回 n/a
 */
function textOrNA(text) {
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
function returnHealthDom(name, data) {
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
            <p className="c7ncd-deploy-detail-text">{textOrNA(data[item])}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * 返回数据卷的项目 DOM
 * @param name
 * @param data
 * @param isBool
 * @returns {*}
 */
function volumesItem(name, data, isBool = false) {
  const value = isBool && _.isBoolean(data) ? data.toString() : data;

  return (
    <div className="c7ncd-deploy-volume-item">
      <p className="c7ncd-deploy-detail-label">
        <FormattedMessage id={`ist.deploy.volume.${name}`} />
      </p>
      <p className="c7ncd-deploy-detail-text">{value}</p>
    </div>
  );
}

function volumesTemplate(data) {
  let template = null;
  const VOL_TYPE = ['configMap', 'persistentVolumeClaim', 'secret', 'hostPath'];
  const vKey = Object.keys(data);
  const { name } = data;

  let type = _.toString(_.filter(VOL_TYPE, item => vKey.includes(item)));
  switch (type) {
    case 'configMap':
    case 'secret': {
      const { defaultMode, items, optional } = data[type];
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
          {volumesItem('defaultMode', defaultMode)}
          {volumesItem('optional', optional, true)}
          <div className={`c7ncd-deploy-volume-item${items ? '_full' : ''}`}>
            <p className="c7ncd-deploy-detail-label">
              <FormattedMessage id="ist.deploy.volume.item" />
            </p>
            {itemDom}
          </div>
        </div>
      );
      break;
    }
    case 'persistentVolumeClaim': {
      const { claimName, readOnly } = data[type];
      template = (
        <div className="c7ncd-deploy-volume-main">
          {volumesItem('claimName', claimName)}
          {volumesItem('readOnly', readOnly, true)}
        </div>
      );
      break;
    }
    case 'hostPath': {
      const { path } = data[type];
      template = (
        <div className="c7ncd-deploy-volume-main">
          {volumesItem('path', path)}
          {volumesItem('type', type)}
        </div>
      );
      break;
    }

    default:
      type = '未知';
      break;
  }
  return (
    <Fragment>
      <div className="c7ncd-deploy-volume-main">
        {volumesItem('name', name)}
        {volumesItem('volume.type', type)}
      </div>
      {template}
    </Fragment>
  );
}

function securityItem(name, data, type = '') {
  const content = _.isArray(data) || _.isObject(data) ? (
    data
  ) : (
    <p className="c7ncd-deploy-detail-text">{textOrNA(data)}</p>
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
