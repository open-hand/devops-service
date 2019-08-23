import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action, Permission } from '@choerodon/master';
import TreeItemName from '../../../../../components/treeitem-name';
import { handlePromptError } from '../../../../../utils';
import { useEnvironmentStore } from '../../../stores';
import { useTreeItemStore } from './stores';

function GroupItem({ record, search, intl: { formatMessage }, intlPrefix }) {
  const {
    treeDs,
    AppState: { currentMenuType: { id } },
  } = useEnvironmentStore();

  async function handleClick() {
    // if (!record) return;
    //
    // const envId = record.get('parentId');
    // const appId = record.get('id');
    // try {
    // const result = await treeItemStore.removeService(id, envId, [appId]);
    //   if (handlePromptError(result, false)) {
    //     treeDs.query();
    //   }
    // } catch (error) {
    //   Choerodon.handleResponseError(error);
    // }
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
      action: handleClick,
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
