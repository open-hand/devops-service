import React, { Fragment, useMemo, useCallback } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { runInAction } from 'mobx';
import toUpper from 'lodash/toUpper';
import { Tree } from 'choerodon-ui/pro';
import classnames from 'classnames';
import ScrollArea from '../scroll-area';
import TreeSearch from './tree-search';

import './index.less';

/**
 * 展开节点的所有父节点
 * 通过Record内置的方法展开目标节点
 * 将展开节点缓存进传入的数组
 * @param record
 * @param expendedKeys
 */
function expandParents(record, expendedKeys) {
  if (!record.isExpanded) {
    const children = record.children;

    if (children && children.length) {
      const key = record.get('key');
      expendedKeys.push(key);
      record.isExpanded = true;
    }

    const parent = record.parent;
    if (parent && !parent.isExpanded) {
      expandParents(parent, expendedKeys);
    }
  }
}

const TreeView = observer(({ ds, store, nodesRender, searchAble }) => {
  const treeClass = useMemo(() => classnames({
    'c7ncd-menu-wrap': true,
    'c7ncd-menu-scroll': searchAble,
  }), [searchAble]);

  const nodeRenderer = useCallback(({ record }) => nodesRender(record, store.getSearchValue),
    [store.getSearchValue]);

  function handleSearch(value) {
    /**
     *
     * 如果在 DataSet 的 load 方法中对原始数据进行了修改
     * 那么就不能使用 ds.reset(); 进行重置，因为该方法是基于 originalData 的
     * 应该手动将各记录重置
     *
     * */
    ds.map((record) => record.reset());

    const treeData = ds.data;
    const realValue = value || '';
    const expandedKeys = [];

    // NOTE: 让多个 action 只执行一次
    runInAction(() => {
      // eslint-disable-next-line no-plusplus
      for (let i = 0; i < treeData.length; i++) {
        const record = treeData[i];
        const name = record.get('name');

        if (value && toUpper(name).indexOf(toUpper(value)) > -1) {
          expandParents(record, expandedKeys);
        }
      }
    });

    const uniqKeys = new Set(expandedKeys);
    store.setExpandedKeys([...uniqKeys]);
    store.setSearchValue(realValue);
  }

  function handleExpanded(keys) {
    store.setExpandedKeys(keys);
  }

  return (
    <Fragment>
      {searchAble && <TreeSearch value={store.getSearchValue} onChange={handleSearch} />}
      <ScrollArea
        vertical
        className={treeClass}
      >
        <Tree
          className="c7ncd-menu"
          onExpand={handleExpanded}
          dataSet={ds}
          renderer={nodeRenderer}
        />
      </ScrollArea>
    </Fragment>
  );
});

TreeView.propTypes = {
  ds: PropTypes.shape({}).isRequired,
  nodesRender: PropTypes.func.isRequired,
  searchAble: PropTypes.bool,
};

TreeView.defaultProps = {
  searchAble: true,
};

export default TreeView;
