import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import { Icon, TextField, Tree } from 'choerodon-ui/pro';
import { Collapse } from 'choerodon-ui';
import map from 'lodash/map';
import TimePopover from '../../../../components/timePopover';
import { usePipelineManageStore } from '../../stores';
import TreeItem from './TreeItem';

import './index.less';

const { Panel } = Collapse;

const TreeMenu = observer(() => {
  const {
    intl: { formatMessage },
    mainStore,
    prefixCls,
    treeDs,
  } = usePipelineManageStore();
  const bounds = useMemo(() => mainStore.getNavBounds, [mainStore.getNavBounds]);

  function nodeRenderer({ record }) {
    return <TreeItem record={record} />;
  }

  function handleExpanded(keys) {
    mainStore.setExpandedKeys(keys);
  }

  return <nav style={bounds} className={`${prefixCls}-sidebar`}>
    <TextField
      className={`${prefixCls}-sidebar-search`}
      placeholder={formatMessage({ id: 'search.placeholder' })}
      clearButton
      name="search"
      prefix={<Icon type="search" />}
    />
    <Tree
      dataSet={treeDs}
      renderer={nodeRenderer}
      onExpand={handleExpanded}
      className={`${prefixCls}-sidebar-tree`}
    />
  </nav>;
});

export default TreeMenu;
