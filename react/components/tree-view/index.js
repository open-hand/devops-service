import React, { Fragment, useMemo, useState, useCallback } from 'react';
import PropTypes from 'prop-types';
import { runInAction } from 'mobx';
import toUpper from 'lodash/toUpper';
import { Tree } from 'choerodon-ui/pro';
import classnames from 'classnames';
import ScrollArea from '../scroll-area';
import TreeSearch from './tree-search';

import './index.less';

function expandParents(record) {
  if (!record.isExpanded) {
    record.isExpanded = true;

    const parent = record.parent;
    if (parent && !parent.isExpanded) {
      expandParents(parent);
    }
  }
}

const TreeView = ({ dataSource, nodesRender, searchAble }) => {
  const [searchValue, setSearchValue] = useState('');

  const treeClass = useMemo(() => classnames({
    'c7n-deployment-scroll': searchAble,
  }), [searchAble]);
  const nodeRenderer = useCallback(({ record }) => nodesRender(record, searchValue), [nodesRender, searchValue]);

  const handleSearch = (value) => {
    dataSource.reset();
    const treeData = dataSource.data;
    const realValue = value || '';

    // NOTE: 让多个 action 只执行一次，设置 isExpanded 就是一次action
    runInAction(() => {
      // eslint-disable-next-line no-plusplus
      for (let i = 0; i < treeData.length; i++) {
        const record = treeData[i];
        const name = record.get('name');

        if (value && toUpper(name).indexOf(toUpper(value)) > -1) {
          expandParents(record);
        }
      }
    });

    setSearchValue(realValue);
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
          dataSet={dataSource}
          renderer={nodeRenderer}
        />
      </ScrollArea>
    </Fragment>
  );
};

TreeView.propTypes = {
  dataSource: PropTypes.shape({}).isRequired,
  nodesRender: PropTypes.func.isRequired,
  searchAble: PropTypes.bool,
};

TreeView.defaultProps = {
  searchAble: true,
};

export default TreeView;
