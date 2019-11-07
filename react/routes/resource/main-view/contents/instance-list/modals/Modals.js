import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useIstListStore } from '../stores';
import useDeployStore from '../../../../../deployment/stores/useStore';
import Deploy from '../../../../../deployment/modals/deploy';

const modalStyle = {
  width: '26%',
};
const deployKey = Modal.key();


const CustomModals = observer(() => {
  const {
    intl: { formatMessage },
    resourceStore,
    treeDs,
  } = useResourceStore();
  const {
    istListDs,
    baseInfoDs,
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

  const buttons = useMemo(() => {
    const record = baseInfoDs.current;
    const notReady = !record;
    const connect = record && record.get('connect');
    const configDisabled = !connect || notReady;
    return [{
      permissions: ['devops-service.app-service-instance.deploy'],
      disabled: configDisabled,
      name: formatMessage({ id: `${intlPrefixDeploy}.manual` }),
      icon: 'jsfiddle',
      handler: openDeploy,
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
