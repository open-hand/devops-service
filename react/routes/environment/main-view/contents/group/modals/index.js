import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal, Form, TextField } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useEnvironmentStore } from '../../../../stores';
import { useEnvGroupStore } from '../stores';

const groupKey = Modal.key();

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
  const { groupDs, groupFormDs } = useEnvGroupStore();

  function refresh() {
    groupDs.query();
    treeDs.query();
  }

  async function handleCreate() {
    try {
      if ((await groupFormDs.submit()) !== false) {
        treeDs.query();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  function openGroupModal() {
    Modal.open({
      key: groupKey,
      title: formatMessage({ id: `${intlPrefix}.group.create` }),
      children: <Form dataSet={groupFormDs}>
        <TextField name="name" />
      </Form>,
      drawer: true,
      onOk: handleCreate,
      style: modalStyle,
    });
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: `${intlPrefix}.create` }),
      icon: 'playlist_add',
      handler: refresh,
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
