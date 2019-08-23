import React from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import GroupItem from './GroupItem';
import DetailItem from './DetailItem';
import { useEnvironmentStore } from '../../../stores';

const TreeItem = observer(({ record, search }) => {
  const {
    intlPrefix,
    itemType: {
      DETAIL_ITEM,
      GROUP_ITEM,
    },
  } = useEnvironmentStore();

  function getItem() {
    const itemName = record.get('name') || '';
    const type = record.get('itemType');

    if (type === GROUP_ITEM) {
      return <GroupItem
        record={record}
        search={search}
        intlPrefix={intlPrefix}
      />;
    } else if (type === DETAIL_ITEM) {
      return <DetailItem
        record={record}
        search={search}
        intlPrefix={intlPrefix}
      />;
    } else {
      return itemName;
    }
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
