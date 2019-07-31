import React, { useContext, useState, useMemo, Fragment } from 'react';
import PropTypes from 'prop-types';
import toUpper from 'lodash/toUpper';
import { observer } from 'mobx-react-lite';
import { Select } from 'choerodon-ui/pro';
import SidebarHeading from './header';
import TreeView from '../../../../components/tree-view';
import TreeItemIcon from './TreeItemIcon';
import MenuStore from '../stores';
import Store from './stores';

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

const TreeMenu = observer(() => {
  const {
    intl: { formatMessage },
    store,
  } = useContext(MenuStore);
  const { treeDs } = useContext(Store);

  const [value, setValue] = useState(DEFAULT_VIEW_TYPE);

  const handleChoose = (choose) => {
    setValue(choose);
  };

  const bounds = useMemo(() => store.getNavBounds, [store.getNavBounds]);

  return <nav style={bounds} className="c7n-deployment-sidebar">
    <SidebarHeading
      value={value}
      options={getViewOptions(formatMessage)}
      bounds={bounds}
      onClick={handleChoose}
    />
    <TreeView
      dataSource={treeDs}
      nodesRender={nodeRenderer}
    />
  </nav>;
});

TreeMenu.propTypes = {
  navBounds: PropTypes.shape({}),
};

export default TreeMenu;
