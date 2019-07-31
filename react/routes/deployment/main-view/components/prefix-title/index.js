import React from 'react';
import PropTypes from 'prop-types';
import { Icon } from 'choerodon-ui/pro';
import PodCircle from '../pod-circle';
import { getEnvInfo, getPodsInfo } from '../../util';

import './index.less';

const RUNNING_COLOR = '#0bc2a8';
const PADDING_COLOR = '#fbb100';

export const TitleWrap = ({ prefixCls, children }) => (<div className={`${prefixCls}-title`}>
  {children}
</div>);

TitleWrap.propTypes = {
  prefixCls: PropTypes.string,
};

export const FailBack = ({ prefixCls }) => <TitleWrap prefixCls={prefixCls}>
  <div className={`${prefixCls}-title-loading`}>正在请求数据</div>
</TitleWrap>;

FailBack.propTypes = {
  prefixCls: PropTypes.string,
};

export default function PrefixTitle({ prefixCls, fallback, children }) {
  return fallback
    ? <FailBack prefixCls={prefixCls} />
    : <TitleWrap prefixCls={prefixCls}>
      {children}
    </TitleWrap>;
}

PrefixTitle.propTypes = {
  prefixCls: PropTypes.string,
  fallback: PropTypes.bool,
};

export const AppTitle = ({ prefixCls, records }) => {
  if (!records.length) return <FailBack prefixCls={prefixCls} />;

  const record = records[0];
  const { name } = getEnvInfo(record);

  return <TitleWrap prefixCls={prefixCls}>
    <Icon type="widgets" />
    <span className={`${prefixCls}-title-text`}>{name}</span>
  </TitleWrap>;
};

AppTitle.propTypes = {
  prefixCls: PropTypes.string,
  records: PropTypes.any.isRequired,
};

export const IstTitle = ({ prefixCls, records }) => {
  if (!records.length) return <FailBack prefixCls={prefixCls} />;

  const record = records[0];
  const { name, podRunningCount, podUnlinkCount } = getPodsInfo(record);

  return <TitleWrap prefixCls={prefixCls}>
    <PodCircle
      style={{
        width: 22,
        height: 22,
      }}
      dataSource={[{
        name: 'running',
        value: podRunningCount,
        stroke: RUNNING_COLOR,
      }, {
        name: 'unlink',
        value: podUnlinkCount,
        stroke: PADDING_COLOR,
      }]}
    />
    <span className={`${prefixCls}-title-text`}>{name}</span>
  </TitleWrap>;
};

IstTitle.propTypes = {
  prefixCls: PropTypes.string,
  records: PropTypes.any.isRequired,
};

export const ResourceTitle = ({ record }) => {
};
