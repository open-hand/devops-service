import React from 'react';
import { Modal, Spin } from 'choerodon-ui/pro';
import map from 'lodash/map';
import { Choerodon } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import { useClusterMainStore } from '../../../stores';
import { useClusterContentStore } from '../stores';
import MonitorCreate from './monitor-create';
import ComponentCard from './card';
import Progress from './progress';
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
    ClusterDetailDs,
  } = useClusterMainStore();
  const {
    formatMessage,
    contentStore,
    projectId,
    clusterId,
  } = useClusterContentStore();

  function getClusterConnect() {
    return ClusterDetailDs.current.get('connect');
  }

  function getContent() {
    const { getComponentList } = contentStore;
    const { length } = getComponentList;
    const content = map(getComponentList, ({
      message, type, status, operate,
    }, index) => {
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
          progress={type === 'prometheus' && status === 'processing' && operate !== 'uninstall' ? <Progress refresh={refresh} /> : null}
        />
      );
    });
    return content;
  }

  function getButtonData(type, status, operate, message) {
    const disabled = !getClusterConnect();
    let buttonData = [];
    if (type === 'prometheus') {
      switch (status) {
        case 'uninstalled':
          buttonData = [
            {
              text: formatMessage({ id: 'install' }),
              onClick: () => installMonitor(message ? 'edit' : 'create', true),
              disabled,
              loading: false,
            },
          ];
          break;
        case 'processing':
          if (operate === 'install') {
            buttonData = [
              {
                text: formatMessage({ id: 'install' }),
                loading: true,
              },
            ];
          } else {
            buttonData = [
              {
                text: formatMessage({ id: 'edit' }),
                loading: operate === 'upgrade',
                onClick: () => installMonitor('edit'),
                disabled: true,
              },
              {
                text: formatMessage({ id: 'uninstall' }),
                loading: operate !== 'upgrade',
                disabled: true,
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
              disabled,
              loading: false,
            },
            {
              text: formatMessage({ id: 'uninstall' }),
              onClick: uninstallMonitor,
              disabled,
              loading: false,
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
              disabled,
              loading: false,
            },
          ];
          break;
        case 'processing':
          if (operate === 'install') {
            buttonData = [
              {
                text: formatMessage({ id: 'install' }),
                loading: true,
              },
            ];
          } else {
            buttonData = [
              {
                text: formatMessage({ id: 'uninstall' }),
                loading: true,
              },
            ];
          }
          break;
        case 'available':
        case 'disabled':
          buttonData = [
            {
              text: formatMessage({ id: 'uninstall' }),
              onClick: uninstallCert,
              disabled,
              loading: false,
            },
          ];
          break;
        default:
      }
    }
    return buttonData;
  }

  function refresh() {
    contentStore.loadComponentList(projectId, clusterId);
  }

  async function handleInstallCert() {
    if (await contentStore.installCertManager(projectId, clusterId)) {
      refresh();
    }
  }

  function installMonitor(type, showPassword = false) {
    Modal.open({
      key: monitorInstallKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.monitor.${type}` }),
      children: <MonitorCreate
        prefixCls={prefixCls}
        intlPrefix={intlPrefix}
        refresh={refresh}
        type={type}
        clusterId={clusterId}
        showPassword={showPassword}
      />,
      okText: formatMessage({ id: type === 'edit' ? 'save' : 'install' }),
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
      footer: null,
    });
    try {
      const res = await contentStore.checkUninstallCert(projectId, clusterId);
      if (handlePromptError(res, false)) {
        if (!res) {
          deleteModal.update({
            children: formatMessage({ id: `${intlPrefix}.cert.uninstall.des` }),
            okText: formatMessage({ id: 'uninstall' }),
            okCancel: true,
            onOk: handleUninstallCert,
            okProps: { color: 'red' },
            cancelProps: { color: 'dark' },
            footer: (okBtn, cancelBtn) => (
              <div>
                {cancelBtn}
                {okBtn}
              </div>
            ),
          });
        } else {
          deleteModal.update({
            children: formatMessage({ id: `${intlPrefix}.cert.uninstall.disabled` }),
            footer: (okBtn) => okBtn,
          });
        }
      } else {
        deleteModal.close(true);
      }
    } catch (error) {
      Choerodon.handlePromptError(error);
      deleteModal.close();
    }
  }

  async function handleUninstallCert() {
    if (await contentStore.uninstallCert(projectId, clusterId)) {
      refresh();
      return true;
    }
    return false;
  }

  async function handleUninstallMonitor() {
    if (await contentStore.uninstallMonitor(projectId, clusterId)) {
      refresh();
      return true;
    }
    return false;
  }

  return (
    <div className={`${prefixCls}-component-wrap`}>
      {getContent()}
    </div>
  );
});
