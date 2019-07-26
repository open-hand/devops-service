import React, { useContext, useState, memo, Fragment } from 'react';
import { inject } from 'mobx-react';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import toUpper from 'lodash/toUpper';
import { Select } from 'choerodon-ui/pro';
import SidebarHeading from './header';
import TreeView from '../../../components/tree-view';
import { TreeItemIcon } from '../components/TreeItemIcon';
import Stores from '../stores';

import './index.less';

const { Option } = Select;
const DEFAULT_VIEW_TYPE = 'instance';

const nodeRenderer = (record, search) => {
  const name = record.get('name');
  const type = record.get('itemType');

  const index = toUpper(name).indexOf(toUpper(search));
  const beforeStr = name.substr(0, index);
  const currentStr = name.substr(index, search.length);
  const afterStr = name.substr(index + search.length);

  return <Fragment>
    <TreeItemIcon type={type} record={record} />
    <span className="c7n-deployment-tree-text">
      {index > -1 ? <Fragment>
        {beforeStr}
        <span className="c7n-deployment-tree-text-highlight">{currentStr}</span>
        {afterStr}
      </Fragment> : name}
    </span>
  </Fragment>;
};

const getViewOptions = formatMessage => ([
  <Option value="instance" key="instance">
    {formatMessage({ id: 'deployment.viewer.instance' })}
  </Option>,
  <Option value="resource" key="resource">
    {formatMessage({ id: 'deployment.viewer.resource' })}
  </Option>,
]);

const Sidebar = memo(({ navBounds, intl: { formatMessage }, AppState: { currentMenuType } }) => {
  const { treeDataSet } = useContext(Stores);
  const [value, setValue] = useState(DEFAULT_VIEW_TYPE);

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
      dataSource={treeDataSet}
      nodesRender={nodeRenderer}
    />
  </nav>;
});

Sidebar.propTypes = {
  navBounds: PropTypes.shape({}),
};

export default inject('AppState')(injectIntl(Sidebar));
