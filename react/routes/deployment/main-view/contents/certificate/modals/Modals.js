import React, { Fragment, useMemo, useCallback, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import HeaderButtons from '../../../components/header-buttons';
import { useDeploymentStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useCertificateStore } from '../stores';
import FormView from './form-view';

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
    formStore,
  } = useCertificateStore();
  const {
    permissions,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const { parentId } = deploymentStore.getSelectedMenu;

  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    deploymentStore.setNoHeader(false);
  }, [deploymentStore]);

  function refresh() {
    certificateDs.query();
  }

  function openModal() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && refresh();
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

  return (
    <Fragment>
      <HeaderButtons items={buttons} />
      {showModal && (
        <FormView
          visible={showModal}
          store={formStore}
          envId={parentId}
          onClose={closeModal}
        />
      )}
    </Fragment>
  );
});

export default EnvModals;
