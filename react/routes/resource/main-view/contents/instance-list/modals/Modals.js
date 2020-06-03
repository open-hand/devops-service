import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useIstListStore } from '../stores';
import useDeployStore from '../../../../../deployment/stores/useStore';
import Deploy from '../../../../../deployment/modals/deploy';
import BatchDeploy from '../../../../../deployment/modals/batch-deploy';

const modalStyle = {
  width: '26%',
};
const deployKey = Modal.key();
const batchDeployKey = Modal.key();

const CustomModals = observer(() => {
  const {
    intl: { formatMessage },
    resourceStore: {
      getSelectedMenu: { parentId },
    },
    treeDs,
  } = useResourceStore();
  const {
    istListDs,
    envId,
  } = useIstListStore();

  const {
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();

  const deployStore = useDeployStore();

  const intlPrefixDeploy = 'c7ncd.deploy';
  const configModalStyle = useMemo(() => ({
    width: 'calc(100vw - 3.52rem)',
    minWidth: '2rem',
  }), []);

  function refresh() {
    treeDs.query();
    istListDs.query();
  }
  function openDeploy() {
    Modal.open({
      key: deployKey,
      style: configModalStyle,
      drawer: true,
      title: formatMessage({ id: `${intlPrefixDeploy}.manual` }),
      children: <Deploy
        deployStore={deployStore}
        refresh={refresh}
        intlPrefix={intlPrefixDeploy}
        prefixCls="c7ncd-deploy"
        envId={envId}
      />,
      afterClose: () => {
        deployStore.setCertificates([]);
        deployStore.setAppService([]);
        deployStore.setConfigValue('');
      },
      okText: formatMessage({ id: 'deployment' }),
    });
  }

  function openBatchDeploy() {
    Modal.open({
      key: batchDeployKey,
      style: configModalStyle,
      drawer: true,
      title: formatMessage({ id: `${intlPrefixDeploy}.batch` }),
      children: <BatchDeploy
        deployStore={deployStore}
        refresh={refresh}
        intlPrefix={intlPrefixDeploy}
        prefixCls="c7ncd-deploy"
        envId={envId}
      />,
      afterClose: () => {
        deployStore.setCertificates([]);
        deployStore.setAppService([]);
        deployStore.setShareAppService([]);
        deployStore.setConfigValue('');
      },
      okText: formatMessage({ id: 'deployment' }),
    });
  }

  const buttons = useMemo(() => {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord && envRecord.get('connect');
    const configDisabled = !connect;
    return [{
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.resource-deploy'],
      disabled: configDisabled,
      name: formatMessage({ id: `${intlPrefixDeploy}.manual` }),
      icon: 'jsfiddle',
      handler: openDeploy,
      display: true,
      group: 1,
    }, {
      permissions: ['choerodon.code.project.deploy.app-deployment.resource.ps.resource-batch'],
      disabled: configDisabled,
      name: formatMessage({ id: `${intlPrefixDeploy}.batch` }),
      icon: 'jsfiddle',
      handler: openBatchDeploy,
      display: true,
      group: 1,
    }, {
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 1,
    }];
  }, [formatMessage, refresh]);

  return <HeaderButtons items={buttons} />;
});

export default CustomModals;
