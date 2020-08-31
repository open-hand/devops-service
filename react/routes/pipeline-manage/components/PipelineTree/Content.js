/* eslint-disable no-param-reassign */

import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { runInAction } from 'mobx';
import { Icon, TextField, Tree } from 'choerodon-ui/pro';
import { Collapse } from 'choerodon-ui';
import toUpper from 'lodash/toUpper';
import { usePipelineManageStore } from '../../stores';
import TreeItem from './TreeItem';
import ScrollArea from '../../../../components/scroll-area';

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
    return <TreeItem record={record} search={mainStore.getSearchValue} />;
  }

  /**
   * 展开节点的所有父节点
   * 通过Record内置的方法展开目标节点
   * 将展开节点缓存进传入的数组
   * @param record
   * @param expendedKeys
   */
  function expandParents(record, expendedKeys) {
    if (!record.isExpanded) {
      const { children, parent } = record;

      if (children && children.length) {
        const key = record.get('key');
        expendedKeys.push(key);
        record.isExpanded = true;
      }

      if (parent && !parent.isExpanded) {
        expandParents(parent, expendedKeys);
      }
    }
  }

  function handleSearch(value) {
    const realValue = value || '';
    const expandedKeys = [];

    // NOTE: 让多个 action 只执行一次
    runInAction(() => {
      /**
       *
       * 如果在 DataSet 的 load 方法中对原始数据进行了修改
       * 那么就不能使用 ds.reset(); 进行重置，因为该方法是基于 originalData 的
       * 应该手动将各记录重置
       *
       * */
      treeDs.forEach((record) => {
        record.reset();

        /**
         * 未清除搜索值就刷新，Record会记录expand状态，导致上一步record.reset()失效
         * */
        record.isExpanded = false;
      });
      treeDs.forEach((treeRecord) => {
        const pipelineName = treeRecord.get('name');
        const appServiceName = treeRecord.get('appServiceName');
        const parentId = treeRecord.get('parentId');
        const id = parentId && treeRecord.get('viewId') ? treeRecord.get('viewId').toString() : null;
        if (value) {
          if (!parentId && (toUpper(pipelineName).indexOf(toUpper(value)) > -1
            || toUpper(appServiceName).indexOf(toUpper(value)) > -1)
          ) {
            expandParents(treeRecord, expandedKeys);
          } else if (parentId && toUpper(id).indexOf(toUpper(value)) > -1) {
            expandParents(treeRecord, expandedKeys);
          }
        }
      });
    });

    const uniqKeys = new Set(expandedKeys);
    mainStore.setSearchValue(realValue);
    handleExpanded([...uniqKeys]);
  }

  function handleExpanded(keys) {
    mainStore.setExpandedKeys(keys);
  }

  return (
    <nav style={bounds} className={`${prefixCls}-sidebar`}>
      <TextField
        className={`${prefixCls}-sidebar-search`}
        placeholder={formatMessage({ id: 'search.placeholder' })}
        clearButton
        name="search"
        prefix={<Icon type="search" />}
        value={mainStore.getSearchValue}
        onChange={handleSearch}
      />
      <ScrollArea
        vertical
        // className="c7ncd-menu-scroll"
      >
        <Tree
          dataSet={treeDs}
          renderer={nodeRenderer}
          onExpand={handleExpanded}
          className={`${prefixCls}-sidebar-tree`}
        />
      </ScrollArea>
    </nav>
  );
});

export default TreeMenu;
