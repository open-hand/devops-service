import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import StatusDot from '../../../../../components/status-dot';
import EnvItem from '../../../../../components/env-item';
import TreeItemName from '../../../../../components/treeitem-name';
import { useEnvironmentStore } from '../../../stores';

const EnvironmentItem = ({ name, connect, synchronize }) => {
  const getPrefix = useMemo(() => <StatusDot
    connect={connect}
    synchronize={synchronize}
    size="small"
  />, [connect, synchronize]);

  return <Fragment>
    {getPrefix}
    {name}
  </Fragment>;
};

EnvironmentItem.propTypes = {
  name: PropTypes.any,
  connect: PropTypes.bool,
  synchronize: PropTypes.bool,
};

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
    const synchronize = record.get('synchronize');
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
    const type = record.get('type');
    if (!itemName && type === GROUP_ITEM) {
      itemName = formatMessage({ id: `${intlPrefix}.group.default` });
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
