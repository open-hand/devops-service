import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import HeaderButtons from '../../../components/header-buttons';
import { useDeploymentStore } from '../../../../stores';
import { useEnvironmentStore } from '../stores';
import { useModalStore } from './stores';

const EnvModals = observer(() => {
  const {
    intlPrefix,
    intl: { formatMessage },
    deploymentStore,
  } = useDeploymentStore();
  const {
    envStore: {
      tabKey,
    },
    tabs: {
      SYNC_TAB,
      ASSIGN_TAB,
    },
    permissionsDs,
  } = useEnvironmentStore();
  const { modal } = useModalStore();

  const openModal = useCallback(() => {
    // console.log(modal);
  }, []);

  useEffect(() => {
    deploymentStore.setNoHeader(false);
  }, [deploymentStore]);

  function refresh() {
    permissionsDs.query();
  }

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
    icon: 'relate',
    handler: openModal,
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.permission` }),
    icon: 'authority',
    handler: openModal,
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
    icon: 'relate',
    handler: openModal,
    display: tabKey === ASSIGN_TAB,
    group: 1,
  }, {
    name: formatMessage({ id: 'refresh' }),
    icon: 'refresh',
    handler: refresh,
    display: tabKey === ASSIGN_TAB,
    group: 2,
  }]), [ASSIGN_TAB, formatMessage, intlPrefix, openModal, refresh, tabKey]);

  return <HeaderButtons items={buttons} />;
});

export default EnvModals;
