import React, { useMemo, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import HeaderButtons from '../../../components/header-buttons';
import { useDeploymentStore } from '../../../../stores';
import { useEnvironmentStore } from '../stores';
import { useModalStore } from './stores';

const EnvModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    viewType: {
      RES_VIEW_TYPE,
    },
  } = useDeploymentStore();
  const {
    envStore: {
      tabKey,
    },
    tabs: {
      SYNC_TAB,
      ASSIGN_TAB,
    },
  } = useEnvironmentStore();
  const { modal } = useModalStore();

  const openModal = useCallback(() => {
    // console.log(modal);
  }, [modal]);

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
    icon: 'relate',
    handler: openModal,
    display: true,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.permission` }),
    icon: 'authority',
    handler: openModal,
    display: true,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
    icon: 'relate',
    handler: openModal,
    display: tabKey === ASSIGN_TAB,
  }]), [ASSIGN_TAB, formatMessage, intlPrefix, openModal, tabKey]);

  return <HeaderButtons items={buttons} />;
});

export default EnvModals;
