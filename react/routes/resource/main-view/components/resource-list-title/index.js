import React from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { useResourceStore } from '../../../stores';

import './index.less';

function ResourceListTitle({ type }) {
  const {
    treeDs,
    resourceStore: { getSelectedMenu: { parentId } },
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
  } = useResourceStore();

  function getEnvName() {
    const envRecord = treeDs.find((record) => record.get('key') === parentId);
    return envRecord.get('name');
  }

  return (
    <div className={`${prefixCls}-resource-list-title`}>
      {formatMessage({ id: `${intlPrefix}.env` }, { name: getEnvName() })}
      {formatMessage({ id: type || 'null' })}
    </div>
  );
}

ResourceListTitle.propTypes = {
  type: PropTypes.string.isRequired,
};

export default observer(ResourceListTitle);
