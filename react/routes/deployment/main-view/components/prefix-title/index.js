import React from 'react';
import PropTypes from 'prop-types';

import './index.less';

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

export const ResourceTitle = ({ record }) => {
};
