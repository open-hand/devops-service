import React, { Fragment, useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useIngressStore } from '../stores';
import DomainModal from '../../application/modals/domain';
import { useMainStore } from '../../../stores';

const EnvModals = observer(() => {
  const {
    intlPrefix,
    intl: { formatMessage },
    resourceStore,
    treeDs,
  } = useResourceStore();
  const {
    ingressDs,
  } = useIngressStore();
  const { ingressStore } = useMainStore();
  const { permissions } = useModalStore();
  const { parentId } = resourceStore.getSelectedMenu;

  const [showModal, setShowModal] = useState(false);

  function refresh() {
    treeDs.query();
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
