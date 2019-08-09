import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import HeaderButtons from '../../../components/header-buttons';
import { useDeploymentStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useCertificateStore } from '../stores';

const modalStyle = {
  width: '26%',
};

const EnvModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    deploymentStore,
  } = useDeploymentStore();
  const {
    certificateDs,
  } = useCertificateStore();
  const {
    permissions,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const { menuId } = deploymentStore.getSelectedMenu;

  const openModal = useCallback(() => {
    // console.log(modal);
  }, []);

  useEffect(() => {
    deploymentStore.setNoHeader(false);
  }, [deploymentStore]);

  function refresh() {
    certificateDs.query();
  }
  
  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.create.certificate` }),
    icon: 'playlist_add',
    handler: openModal,
    display: true,
    group: 1,
    service: permissions,
  }, {
    name: formatMessage({ id: 'refresh' }),
    icon: 'refresh',
    handler: refresh,
    display: true,
    group: 1,
  }]), [formatMessage, intlPrefix, openModal, permissions, refresh]);

  return <HeaderButtons items={buttons} />;
});

export default EnvModals;
