import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import EnvItem from '../../../../../components/env-item';
import TreeItemName from '../../../../../components/treeitem-name';
import { useEnvironmentStore } from '../../../stores';

const TreeItem = observer(({ record, search }) => {
  const {
    prefixCls,
    intlPrefix,
    itemType: {
      DETAIL_ITEM,
      GROUP_ITEM,
    },
    intl: { formatMessage },
  } = useEnvironmentStore();

  function getEnvItem(name) {
    const connect = record.get('connect');
    const synchronize = record.get('synchro');
    const active = record.get('active');

    return <EnvItem
      name={name}
      active={active}
      connect={connect}
      synchronize={synchronize}
    />;
  }

  function getItem() {
    let itemName = record.get('name') || '';
    const type = record.get('itemType');
    const active = record.get('active');

    if (!itemName && type === GROUP_ITEM) {
      itemName = formatMessage({ id: `${intlPrefix}.group.${active ? 'default' : 'stopped'}` });
    }

    const name = <TreeItemName name={itemName} search={search} />;

    if (type === DETAIL_ITEM) {
      return getEnvItem(name);
    }

    return name;
  }

  return getItem();
});

TreeItem.propTypes = {
  record: PropTypes.shape({}),
  search: PropTypes.string,
};

TreeItem.defaultProps = {
  record: {},
};

export default TreeItem;
