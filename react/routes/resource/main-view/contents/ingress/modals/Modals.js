import React, { Fragment, useMemo, useCallback, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import HeaderButtons from '../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useIngressStore } from '../stores';
import DomainModal from '../../application/modals/domain';

const modalStyle = {
  width: '26%',
};

const EnvModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore,
  } = useResourceStore();
  const {
    ingressDs,
    ingressStore,
  } = useIngressStore();
  const {
    permissions,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const { menuId, parentId } = resourceStore.getSelectedMenu;

  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    resourceStore.setNoHeader(false);
  }, [resourceStore]);

  function refresh() {
    ingressDs.query();
  }

  function openModal() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && refresh();
  }

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.create.ingress` }),
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
        <DomainModal
          envId={parentId}
          visible={showModal}
          type="create"
          store={ingressStore}
          onClose={closeModal}
        />
      )}
    </Fragment>
  );
});

export default EnvModals;
