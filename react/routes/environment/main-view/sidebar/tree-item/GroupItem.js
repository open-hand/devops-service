import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Form, Modal, TextField } from 'choerodon-ui/pro';
import { Action, Permission } from '@choerodon/master';
import TreeItemName from '../../../../../components/treeitem-name';
import { useEnvironmentStore } from '../../../stores';
import { useTreeItemStore } from './stores';

const modalKey = Modal.key();

function GroupItem({ record, search, intl: { formatMessage }, intlPrefix }) {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    treeDs,
    AppState: { currentMenuType: { id } },
  } = useEnvironmentStore();
  const { groupFormDs } = useTreeItemStore();

  async function handleCreate() {
    try {
      if ((await groupFormDs.create()) !== false) {
        treeDs.query();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  function handleClick() {
    Modal.open({
      key: modalKey,
      title: formatMessage({ id: `${intlPrefix}.group.create` }),
      children: <Form dataSet={groupFormDs}>
        <TextField name="name" />
      </Form>,
      drawer: true,
      onOk: handleCreate,
      style: modalStyle,
    });
  }

  function handleDelete() {
    treeDs.query();
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
      action: handleDelete,
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
