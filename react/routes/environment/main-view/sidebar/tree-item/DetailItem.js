import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action, Permission } from '@choerodon/master';
import TreeItemName from '../../../../../components/treeitem-name';
import { handlePromptError } from '../../../../../utils';
import { useEnvironmentStore } from '../../../stores';
import { useTreeItemStore } from './stores';
import EnvItem from '../../../../../components/env-item';

function DetailItem({ record, search, intl: { formatMessage }, intlPrefix }) {
  const {
    treeDs,
    AppState: { currentMenuType: { id } },
  } = useEnvironmentStore();

  async function handleClick() {
    // console.log('click');
  }

  function getName() {
    const itemName = record.get('name') || '';
    const connect = record.get('connect');
    const synchronize = record.get('synchro');
    const active = record.get('active');
    const name = <TreeItemName name={itemName} search={search} />;
    return <EnvItem
      name={name}
      active={active}
      connect={connect}
      synchronize={synchronize}
    />;
  }

  function getSuffix() {
    const active = record.get('active');
    const actionData = [{
      service: [],
      text: formatMessage({ id: `${intlPrefix}.modal.detail.${active ? 'stop' : 'start'}` }),
      action: handleClick,
    }, {
      service: [],
      text: formatMessage({ id: `${intlPrefix}.modal.detail.${active ? 'modify' : 'delete'}` }),
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

DetailItem.propTypes = {
  search: PropTypes.string,
};

export default injectIntl(observer(DetailItem));
