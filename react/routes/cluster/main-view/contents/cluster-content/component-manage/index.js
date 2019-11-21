import React, { Fragment, useMemo } from 'react';
import { Modal, Spin } from 'choerodon-ui/pro';
import map from 'lodash/map';
import sortBy from 'lodash/sortBy';
import { observer } from 'mobx-react-lite';
import { useClusterMainStore } from '../../../stores';
import { useClusterContentStore } from '../stores';
import MonitorCreate from './monitor-create';
import ComponentCard from './card';
import { handlePromptError } from '../../../../../../utils';

import './index.less';

const monitorInstallKey = Modal.key();
const monitorUninstallKey = Modal.key();
const certUninstallKey = Modal.key();
const modalStyle = {
  width: 380,
};

export default observer((props) => {
  const {
    intlPrefix,
    prefixCls,
  } = useClusterMainStore();
  const {
    formatMessage,
    contentStore,
    projectId,
    clusterId,
  } = useClusterContentStore();

  function getContent() {
    const { getComponentList } = contentStore;
    const length = getComponentList.length;
    const content = map(getComponentList, ({ message, type, status, operate }, index) => {
      const componentType = type === 'prometheus' ? 'monitor' : 'cert';
      return (
        <ComponentCard
          key={type}
          className={index !== length - 1 ? `${prefixCls}-component-wrap-card` : ''}
          name={formatMessage({ id: `${intlPrefix}.component.${componentType}` })}
          describe={formatMessage({ id: `${intlPrefix}.component.${componentType}.des` })}
          buttonData={getButtonData(type, status, operate, message)}
          status={status}
          errorMessage={message}
        />
      );
    });
    return content;
  }

  function getButtonData(type, status, operate, message) {
    let buttonData = [];
    if (type === 'prometheus') {
      switch (status) {
        case 'uninstalled':
          buttonData = [
            {
              text: formatMessage({ id: 'install' }),
              onClick: () => installMonitor(message ? 'edit' : 'create'),
            },
          ];
          break;
        case 'processing':
          if (operate === 'install') {
            buttonData = [
              {
                text: formatMessage({ id: 'install' }),
                loading: true,
                popoverContent: getPopoverContent(),
              },
            ];
          } else {
            buttonData = [
              {
                text: formatMessage({ id: 'edit' }),
                onClick: () => installMonitor('edit'),
              },
              {
                text: formatMessage({ id: 'uninstall' }),
                loading: true,
                popoverContent: getPopoverContent(),
              },
            ];
          }
          break;
        case 'available':
        case 'disabled':
          buttonData = [
            {
              text: formatMessage({ id: 'edit' }),
              onClick: () => installMonitor('edit'),
            },
            {
              text: formatMessage({ id: 'uninstall' }),
              onClick: uninstallMonitor,
            },
          ];
          break;
        default:
      }
    } else {
      switch (status) {
        case 'uninstalled':
          buttonData = [
            {
              text: formatMessage({ id: 'install' }),
              onClick: handleInstallCert,
            },
          ];
          break;
        case 'processing':
          buttonData = [
            {
              text: formatMessage({ id: 'install' }),
              loading: true,
            },
          ];
          break;
        case 'available':
        case 'disabled':
          buttonData = [
            {
              text: formatMessage({ id: 'uninstall' }),
              onClick: uninstallCert,
            },
          ];
          break;
        default:
      }
    }
    return buttonData;
  }

  function getPopoverContent() {
    const { getPrometheusStatus } = contentStore;
    return (
      <Fragment>
        {map(getPrometheusStatus, (value, key) => (
          <div className={`${prefixCls}-install-step`} key={key}>
            <div className={`${prefixCls}-install-step-content`}>
              <span className={`${prefixCls}-install-step-status ${prefixCls}-install-step-status-${value}`} />
              <span className={`${prefixCls}-install-step-text`}>
                {formatMessage({ id: `${intlPrefix}.install.step.${key}` })}
              </span>
            </div>
            {value !== 'pending' && (
              <div className={`${prefixCls}-install-step-line ${prefixCls}-install-step-${value}`} />
            )}
          </div>
        ))}
      </Fragment>
    );
  }

  function refresh() {
    contentStore.loadComponentList(projectId, clusterId);
  }

  async function handleInstallCert() {
    if (await contentStore.installCertManager(projectId, clusterId)) {
      refresh();
    }
  }

  function installMonitor(type) {
    Modal.open({
      key: monitorInstallKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.monitor.install` }),
      children: <MonitorCreate
        prefixCls={prefixCls}
        intlPrefix={intlPrefix}
        refresh={refresh}
        type={type}
        clusterId={clusterId}
      />,
      okText: formatMessage({ id: 'install' }),
    });
  }

  function uninstallMonitor() {
    Modal.open({
      key: monitorUninstallKey,
      title: formatMessage({ id: `${intlPrefix}.monitor.uninstall` }),
      children: formatMessage({ id: `${intlPrefix}.monitor.uninstall.des` }),
      okText: formatMessage({ id: 'uninstall' }),
      onOk: handleUninstallMonitor,
    });
  }

  async function uninstallCert() {
    const deleteModal = Modal.open({
      key: certUninstallKey,
      title: formatMessage({ id: `${intlPrefix}.cert.uninstall` }),
      children: <Spin />,
      okCancel: false,
      okText: formatMessage({ id: 'iknow' }),
    });
    const res = await contentStore.checkUninstallCert(projectId, clusterId);
    if (handlePromptError(res)) {
      if (res) {
        deleteModal.update({
          children: formatMessage({ id: `${intlPrefix}.cert.uninstall.des` }),
          okText: formatMessage({ id: 'uninstall' }),
          okCancel: true,
          onOk: handleUninstallCert,
        });
      } else {
        deleteModal.update({
          children: formatMessage({ id: `${intlPrefix}.cert.uninstall.disabled` }),
        });
      }
    }
  }

  async function handleUninstallCert() {
    if (await contentStore.uninstallCert(projectId, clusterId)) {
      refresh();
    } else {
      return false;
    }
  }

  async function handleUninstallMonitor() {
    if (await contentStore.uninstallMonitor(projectId, clusterId)) {
      refresh();
    } else {
      return false;
    }
  }

  return (
    <div className={`${prefixCls}-component-wrap`}>
      {getContent()}
    </div>
  );
});
