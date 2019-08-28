import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal, Form, TextField } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import EnvCreateForm from '../../../modals/EnvCreateForm';
import GroupCreateForm from '../../../modals/GroupCreateForm';
import { useEnvironmentStore } from '../../../../stores';
import { useMainStore } from '../../../stores';
import { useEnvGroupStore } from '../stores';

const groupKey = Modal.key();
const envKey = Modal.key();

const AppModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    envStore,
    treeDs,
  } = useEnvironmentStore();
  const { envFormDs, groupFormDs, clusterDs } = useMainStore();
  const { groupDs } = useEnvGroupStore();

  function refresh() {
    groupDs.query();
    treeDs.query();
  }

  function openGroupModal() {
    groupFormDs.reset();
    Modal.open({
      key: groupKey,
      title: formatMessage({ id: `${intlPrefix}.group.create` }),
      children: <GroupCreateForm dataSet={groupFormDs} treeDs={treeDs} />,
      drawer: true,
      style: modalStyle,
    });
  }

  function openEnvModal() {
    clusterDs.query();
    Modal.open({
      key: envKey,
      title: formatMessage({ id: `${intlPrefix}.create` }),
      children: <EnvCreateForm dataSet={envFormDs} clusterDs={clusterDs} />,
      drawer: true,
      style: modalStyle,
    });
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: `${intlPrefix}.create` }),
      icon: 'playlist_add',
      handler: openEnvModal,
      display: true,
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.group.create` }),
      icon: 'playlist_add',
      handler: openGroupModal,
      display: true,
      group: 1,
    }, {
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 2,
    }];
  }

  return (<div>
    <HeaderButtons items={getButtons()} />
  </div>);
});

export default AppModals;
