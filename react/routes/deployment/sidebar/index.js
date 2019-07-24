import React, { useState, Fragment } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import { Tree, Select } from 'choerodon-ui/pro';
import _ from 'lodash';
import SidebarHeading from './header';
import TreeView from './tree-view';
import { APP_ITEM, ENV_ITEM, IST_ITEM, TreeItemIcon } from './tree-view/TreeItemIcon';
import DeploymentStore from './stores';

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

const Sidebar = observer(({ navBounds, intl: { formatMessage } }) => {
  const [value, setValue] = useState(DEFAULT_VIEW_TYPE);

  const {
    getNavData,
    getSelectedTreeNode: selectedKeys,
    getNavFormatted,
  } = DeploymentStore;

  const handleSelect = (keys, next) => {
    const {
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    DeploymentStore.setSelectedTreeNode(keys);
    DeploymentStore.loadPreviewData(projectId, next);
  };

  const handleChoose = (choose) => {
    setValue(choose);
  };

  const viewOptions = [
    <Option value="instance" key="instance">
      {formatMessage({ id: 'deployment.viewer.instance' })}
    </Option>,
    <Option value="resource" key="resource">
      {formatMessage({ id: 'deployment.viewer.resource' })}
    </Option>,
  ];

  return <nav className="c7n-deployment-sidebar">
    <SidebarHeading
      value={value}
      options={viewOptions}
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
