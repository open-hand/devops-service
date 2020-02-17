import React, { Fragment, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin, Button, Icon } from 'choerodon-ui';
import { useClusterMainStore } from '../../../../stores';
import { useClusterContentStore } from '../../stores';
import './index.less';

const checkGroup = {
  checkColor: {

  },
  checkText: {

  },
};

const numberDetail = observer((props) => {
  const {
    intlPrefix,
    prefixCls,
  } = useClusterMainStore();

  const {
    contentStore: {
      setTabKey,
    },
    formatMessage,
    tabs: {
      POLARIS_TAB,
    },
    ClusterDetailDs,
  } = useClusterContentStore();

  function refresh() {

  }


  return (
    <div className={`${prefixCls}-number`}>

      {/* number详情的左半部分 */}
      <div className={`${prefixCls}-number-left`}>

        {/* 左半部分上部分 */}
        <div className={`${prefixCls}-number-leftTop`}>

          <Button
            type="primary"
            funcType="raised"
            style={{
              width: '.92rem',
              boShadow: '0px 2px 4px 0px rgba(106,117,203,0.6)',
              borderRadius: '6px',
            }}
          >手动扫描</Button>

          {/* 最新一次扫描时间 */}
          <span className={`${prefixCls}-number-leftTop-lastestDate`}>
            上次扫描时间：-
          </span>

        </div>

        {/* 左半部分下部分 */}
        <div className={`${prefixCls}-number-leftDown`}>
          <div className={`${prefixCls}-number-leftDown-left`}>

            <div className={`${prefixCls}-number-check`}>
              <Icon type="check" /> <span>- checks passed</span>
            </div>
            <div className={`${prefixCls}-number-check`}>
              <Icon type="priority_high" /> <span>- checks had warning</span>
            </div>
            <div className={`${prefixCls}-number-check`}>
              <Icon type="close" /> <span>- checks had errors</span>
            </div>

          </div>

          {/* ---------------- 皓天！！！这个雷达的组件你可以替换掉，换成动画 ------------- */}
          <div className={`${prefixCls}-number-leftDown-right`}>

            <div className={`${prefixCls}-number-leftDown-right-rate`}>
              reigh
            </div>

          </div>

        </div>

      </div>

      {/* 下部分 */}
      <div className={`${prefixCls}-number-right`}>
        <div className={`${prefixCls}-number-right-list`}>
          <div className={`${prefixCls}-number-category`}>
            <Icon type="saga_define" />
            <div className={`${prefixCls}-number-category-detail`}>
              <span>Kubernetes版本</span>
              <span>1.15</span>
            </div>
          </div>

          <div className={`${prefixCls}-number-category`}>
            <Icon type="instance_outline" />
            <div className={`${prefixCls}-number-category-detail`}>
              <span>实例数量</span>
              <span>22</span>
            </div>
          </div>

          <div className={`${prefixCls}-number-category`}>
            <Icon type="toll" />
            <div className={`${prefixCls}-number-category-detail`}>
              <span>Pods数量</span>
              <span>11</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
});

export default numberDetail;
