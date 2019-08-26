import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Form, Modal, TextField } from 'choerodon-ui/pro';
import { Action, Permission } from '@choerodon/master';
import TreeItemName from '../../../../../components/treeitem-name';
import { handlePromptError } from '../../../../../utils';
import { useEnvironmentStore } from '../../../stores';
import { useTreeItemStore } from './stores';

const modalKey = Modal.key();
const confirmKey = Modal.key();

function GroupItem({ record, search, intl: { formatMessage }, intlPrefix }) {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    treeDs,
    envStore,
    AppState: { currentMenuType: { id } },
  } = useEnvironmentStore();
  const { groupFormDs } = useTreeItemStore();

  async function handleUpdate() {
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

  function handleClick() {
    const groupId = record.get('id');
    const name = record.get('name');
    groupFormDs.transport.submit = ({ data: [data] }) => ({
      url: `/devops/v1/projects/${id}/env_groups`,
      method: 'put',
      data: {
        id: groupId,
        name: data.name,
      },
    });
    if (!groupFormDs.length) {
      groupFormDs.create({ name });
    }
    Modal.open({
      key: modalKey,
      title: formatMessage({ id: `${intlPrefix}.group.edit` }),
      children: <Form dataSet={groupFormDs}>
        <TextField name="name" />
      </Form>,
      drawer: true,
      onOk: handleUpdate,
      style: modalStyle,
    });
  }

  async function handleDelete() {
    const groupId = record.get('id');
    try {
      const result = await envStore.deleteGroup(id, groupId);
      handlePromptError(result, false);
    } finally {
      treeDs.query();
    }
  }

  function confirmDelete() {
    const name = record.get('name');
    Modal.open({
      key: confirmKey,
      title: formatMessage({ id: `${intlPrefix}.group.delete` }, { name }),
      children: <div>{formatMessage({ id: `${intlPrefix}.group.delete.warn` })}</div>,
      onOk: handleDelete,
    });
  }

  function getName() {
    const itemName = record.get('name');
    return <TreeItemName name={itemName} search={search} />;
  }

  function getSuffix() {
    const groupId = record.get('id');
    if (!groupId) return null;

    const actionData = [{
      service: [],
      text: formatMessage({ id: `${intlPrefix}.modal.group.modify` }),
      action: handleClick,
    }, {
      service: [],
      text: formatMessage({ id: `${intlPrefix}.modal.group.delete` }),
      action: confirmDelete,
    }];
    return <Action placement="bottomRight" data={actionData} />;
  }

  return <Fragment>
    {getName()}
    <Permission service={[]}>
      {getSuffix()}
    </Permission>
  </Fragment>;
}

GroupItem.propTypes = {
  search: PropTypes.string,
};

export default injectIntl(observer(GroupItem));
