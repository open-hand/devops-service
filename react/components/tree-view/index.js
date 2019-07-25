import React, { useState, Fragment } from 'react/index';
import PropTypes from 'prop-types';
import { Tree } from 'choerodon-ui/pro';
import classnames from 'classnames';
import ScrollArea from '../scroll-area';
import TreeSearch from './tree-search';

import './index.less';

const TreeView = ({
  dataSource,
  currentKeys,
  nodesRender,
  searchAble,
  dataFormatted,
  getParentKey,
  onSelect,
}) => {
  const [expandedKeys, setExpandedKeys] = useState([]);
  const [searchValue, setSearchValue] = useState('');
  const [autoExpandParent, setAutoExpandParent] = useState(true);

  const treeNodes = nodesRender(dataSource, searchValue);
  const treeClass = classnames({
    'c7n-deployment-scroll': searchAble,
  });

  const handleSearch = (value) => {
    const keys = dataFormatted.map((item, index, arr) => {
      if ((item.name || item.code).indexOf(value) > -1) {
        return getParentKey(item.prevKey, arr);
      }
      return null;
    }).filter((item, i, self) => item && self.indexOf(item) === i);

    setAutoExpandParent(true);
    setExpandedKeys(keys);
    setSearchValue(value || '');
  };

  const handleExpand = (expanded) => {
    setExpandedKeys(expanded);
    setAutoExpandParent(false);
  };

  const handleSelect = (selectedKeys) => {
    const currentKey = currentKeys[0];
    const nextKey = selectedKeys[0];

    if (nextKey && nextKey !== currentKey) {
      onSelect(selectedKeys, nextKey);
    }
  };

  return (
    <Fragment>
      {searchAble && <TreeSearch onChange={handleSearch} />}
      <ScrollArea
        vertical
        className={treeClass}
      >
        <Tree
          className="c7n-deployment-tree"
          onSelect={handleSelect}
          onExpand={handleExpand}
          selectedKeys={currentKeys}
          expandedKeys={expandedKeys}
          autoExpandParent={autoExpandParent}
        >
          {treeNodes}
        </Tree>
      </ScrollArea>
    </Fragment>
  );
};

TreeView.propTypes = {
  dataSource: PropTypes.array.isRequired,
  dataFormatted: PropTypes.array.isRequired,
  nodesRender: PropTypes.func.isRequired,
  onSelect: PropTypes.func.isRequired,
  getParentKey: PropTypes.func,
  currentKeys: PropTypes.array,
  searchAble: PropTypes.bool,
};

TreeView.defaultProps = {
  currentKeys: [],
  searchAble: true,
};

export default TreeView;
