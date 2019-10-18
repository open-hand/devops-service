import React, { Fragment, useMemo, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { FormattedMessage } from 'react-intl';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useResourceStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useNetworkStore } from '../stores';
import CreateNetwork from './network-create';
import { useMainStore } from '../../../stores';

const modalKey = Modal.key();
const modalStyle = {
  width: 740,
};

const EnvModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore,
    treeDs,
  } = useResourceStore();
  const {
    networkDs,
  } = useNetworkStore();
  const {
    networkStore,
  } = useMainStore();
  const {
    permissions,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const { parentId } = resourceStore.getSelectedMenu;

  function refresh() {
    treeDs.query();
    networkDs.query();
  }

  function openModal() {
    Modal.open({
      key: modalKey,
      style: modalStyle,
      drawer: true,
      title: formatMessage({ id: 'network.header.create' }),
      children: <CreateNetwork
        envId={parentId}
        store={networkStore}
        refresh={refresh}
      />,
      okText: formatMessage({ id: 'create' }),
    });
  }

  function getButtons() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    const connect = envRecord.get('connect');
    const disabled = !connect;

    return ([{
      name: formatMessage({ id: `${intlPrefix}.create.network` }),
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
    </Fragment>
  );
});

export default EnvModals;
