import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import moment from 'moment';
import setEnvRecentItem from '../../../../utils/setEnvRecentItem';
import { itemTypeMappings } from '../../stores/mappings';
import SidebarHeading from './header';
import setTreeMenuSelect from '../../../../utils/setTreeMenuSelect';
import TreeView from '../../../../components/tree-view';
import TreeItem from './tree-item';
import { useResourceStore } from '../../stores';
import { useMainStore } from '../stores';

import './index.less';

const { ENV_ITEM } = itemTypeMappings;

const TreeMenu = observer(() => {
  const {
    treeDs,
    prefixCls,
    resourceStore,
    AppState: { currentMenuType: { projectId, organizationId, name: projectName } },
  } = useResourceStore();
  const { mainStore } = useMainStore();

  const bounds = useMemo(() => mainStore.getNavBounds, [mainStore.getNavBounds]);
  const nodeRenderer = useCallback((record, search) => <TreeItem record={record} search={search} />, []);

  useEffect(() => {
    setTreeMenuSelect(treeDs, resourceStore, getEnvItem);
  }, [treeDs.data]);

  function getEnvItem(record) {
    const item = getParentRecord(record);
    if (item && item.itemType === ENV_ITEM) {
      const recentEnv = {
        ...item,
        active: true,
        projectId,
        organizationId,
        projectName,
        clickTime: moment().format('YYYY-MM-DD HH:mm:ss'),
      };
      setEnvRecentItem({ value: recentEnv });
    }
  }

  function getParentRecord(record) {
    if (record.parent) {
      return getParentRecord(record.parent);
    } else {
      return record.toData();
    }
  }

  return <nav style={bounds} className={`${prefixCls}-sidebar`}>
    <SidebarHeading />
    <div className={`${prefixCls}-sidebar-menu`}>
      <TreeView
        ds={treeDs}
        store={resourceStore}
        nodesRender={nodeRenderer}
      />
    </div>
  </nav>;
});

export default TreeMenu;
