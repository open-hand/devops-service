import React, { useContext, useState, useMemo, Fragment } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import { Tree, Select, DataSet } from 'choerodon-ui/pro';
import _ from 'lodash';
import SidebarHeading from './header';
import TreeView from '../../../components/tree-view';
import { APP_ITEM, ENV_ITEM, IST_ITEM, TreeItemIcon } from '../components/TreeItemIcon';
import TreeDataSet from './stores/TreeDataSet';
import Stores from '../stores';

import './index.less';

const TreeNode = Tree.TreeNode;
const { Option } = Select;

const DEFAULT_VIEW_TYPE = 'instance';

const getParentKey = (prevKey, list) => {
  if (!prevKey) return;

  let parentKey;
  const node = list.filter(({ key }) => prevKey === key)[0];

  if (node) {
    parentKey = node.key;
  }

  return parentKey;
};

const treeItem = (name, type, search, record) => {
  const index = name.indexOf(search);
  const beforeStr = name.substr(0, index);
  const afterStr = name.substr(index + search.length);

  return <Fragment>
    <TreeItemIcon type={type} record={record} />
    <span className="c7n-deployment-tree-text">
      {index > -1 ? <Fragment>
        {beforeStr}
        <span className="c7n-deployment-tree-text-highlight">{search}</span>
        {afterStr}
      </Fragment> : name}
    </span>
  </Fragment>;
};

const nodeRenderer = (record, search) => _.map(_.filter(record, ({ name }) => name), ({ name, id, apps, connect, synchronize }) => {
  const title = treeItem(name, ENV_ITEM, search, { connect, synchronize });
  if (!(apps && apps.length)) {
    return <TreeNode
      key={String(id)}
      title={title}
    />;
  }

  return <TreeNode
    key={`${id}`}
    title={title}
  >
    {_.map(_.filter(apps, ({ name: aName }) => aName), ({ name: aName, id: aId, instances }) => {
      const appTitle = treeItem(aName, APP_ITEM, search);
      if (!(instances && instances.length)) {
        return <TreeNode key={`${id}-${aId}`} title={appTitle} />;
      }

      return <TreeNode key={`${id}-${aId}`} title={appTitle}>
        {_.map(_.filter(instances, ({ code }) => code), ((item) => {
          const istTitle = treeItem(item.code, IST_ITEM, search, item);
          return <TreeNode key={`${id}-${aId}-${item.id}`} title={istTitle} />;
        }))}
      </TreeNode>;
    })}
  </TreeNode>;
});

const getViewOptions = formatMessage => ([
  <Option value="instance" key="instance">
    {formatMessage({ id: 'deployment.viewer.instance' })}
  </Option>,
  <Option value="resource" key="resource">
    {formatMessage({ id: 'deployment.viewer.resource' })}
  </Option>,
]);

const Sidebar = observer(({ navBounds, intl: { formatMessage }, AppState: { currentMenuType } }) => {
  const treeDataDs = useMemo(() => new DataSet(TreeDataSet(currentMenuType.id)), []);
  const { store } = useContext(Stores);
  const [value, setValue] = useState(DEFAULT_VIEW_TYPE);

  const {
    getNavData,
    getSelectedTreeNode: selectedKeys,
    getNavFormatted,
  } = store;

  const handleSelect = (keys, next) => {
    store.setSelectedTreeNode(keys);
    store.loadPreviewData(currentMenuType.id, next);
  };

  const handleChoose = (choose) => {
    setValue(choose);
  };

  return <nav className="c7n-deployment-sidebar">
    <SidebarHeading
      value={value}
      options={getViewOptions(formatMessage)}
      bounds={navBounds}
      onClick={handleChoose}
    />
    <TreeView
      dataSource={getNavData}
      dataFormatted={getNavFormatted}
      currentKeys={selectedKeys}
      getParentKey={getParentKey}
      nodesRender={nodeRenderer}
      onSelect={handleSelect}
    />
  </nav>;
});

Sidebar.propTypes = {
  navBounds: PropTypes.shape({}),
};

export default inject('AppState')(injectIntl(Sidebar));
