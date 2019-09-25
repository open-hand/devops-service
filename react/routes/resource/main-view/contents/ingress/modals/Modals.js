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

  function getButtons() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    const disabled = !connect;

    return ([{
      name: formatMessage({ id: `${intlPrefix}.create.ingress` }),
      icon: 'playlist_add',
      handler: openModal,
      display: true,
      group: 1,
      service: permissions,
      disabled,
    }, {
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 1,
    }]);
  }

  return (
    <Fragment>
      <HeaderButtons items={getButtons()} />
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
