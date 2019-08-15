import React, { Fragment, useMemo, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import HeaderButtons from '../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useNetworkStore } from '../stores';
import CreateNetwork from './network-create';

const EnvModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore,
  } = useResourceStore();
  const {
    networkDs,
    networkStore,
  } = useNetworkStore();
  const {
    permissions,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const { parentId } = resourceStore.getSelectedMenu;

  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    resourceStore.setNoHeader(false);
  }, [resourceStore]);

  function refresh() {
    networkDs.query();
  }

  function openModal() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && refresh();
  }

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.create.network` }),
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
        <CreateNetwork
          envId={parentId}
          visible={showModal}
          store={networkStore}
          onClose={closeModal}
        />
      )}
    </Fragment>
  );
});

export default EnvModals;
