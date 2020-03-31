import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Icon, TextField } from 'choerodon-ui/pro';
import { usePipelineManageStore } from '../../stores';

import './index.less';

const TreeMenu = observer(() => {
  const {
    intl: { formatMessage },
    mainStore,
    prefixCls,
  } = usePipelineManageStore();
  const bounds = useMemo(() => mainStore.getNavBounds, [mainStore.getNavBounds]);

  return <nav style={bounds} className={`${prefixCls}-sidebar`}>
    <TextField
      className={`${prefixCls}-sidebar-search`}
      placeholder={formatMessage({ id: 'search.placeholder' })}
      clearButton
      name="search"
      prefix={<Icon type="search" />}
    />
  </nav>;
});

export default TreeMenu;
