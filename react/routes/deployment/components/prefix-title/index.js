import React from 'react/index';
import PropTypes from 'prop-types';
import { Icon } from 'choerodon-ui/pro';
import StatusDot from '../status-dot';
import PodCircle from '../pod-circle';
import { getEnvInfo, getPodsInfo } from '../../util';
import { PADDING_COLOR, RUNNING_COLOR } from '../../Constants';

import './index.less';

const TitleWrap = ({ prefixCls, children }) => (<div className={`${prefixCls}-title`}>
  {children}
</div>);

TitleWrap.propTypes = {
  prefixCls: PropTypes.string,
};

const FailBack = ({ prefixCls }) => <TitleWrap prefixCls={prefixCls}>
  <div className={`${prefixCls}-title-loading`}>正在请求数据</div>
</TitleWrap>;

export const EnvTitle = ({ prefixCls, records }) => {
  if (!records.length) return <FailBack prefixCls={prefixCls} />;

  const record = records[0];
  const { name, synchronize, connect } = getEnvInfo(record);

  return <TitleWrap prefixCls={prefixCls}>
    <StatusDot
      connect={connect}
      synchronize={synchronize}
      style={{ width: '.12rem', height: '.12rem' }}
    />
    <span className={`${prefixCls}-title-text`}>{name}</span>
  </TitleWrap>;
};

EnvTitle.propTypes = {
  prefixCls: PropTypes.string,
  records: PropTypes.any.isRequired,
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
