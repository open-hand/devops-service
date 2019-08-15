import React, { Fragment, useMemo, useCallback, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import HeaderButtons from '../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useKeyValueStore } from '../stores';
import KeyValueModal from '../../application/modals/key-value';

const KeyValueModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore,
  } = useResourceStore();
  const {
    listDs,
    itemType,
    formStore,
  } = useKeyValueStore();
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
    listDs.query();
  }

  function openModal() {
    setShowModal(true);
  }

  function closeModal(isLoad) {
    setShowModal(false);
    isLoad && refresh();
  }

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.create.${itemType}` }),
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
  }]), [formatMessage, intlPrefix, itemType, openModal, permissions, refresh]);

  return (
    <Fragment>
      <HeaderButtons items={buttons} />
      {showModal && <KeyValueModal
        modeSwitch={itemType === 'configMap'}
        title={itemType}
        visible={showModal}
        envId={parentId}
        onClose={closeModal}
        store={formStore}
      />}
    </Fragment>
  );
});

export default KeyValueModals;
