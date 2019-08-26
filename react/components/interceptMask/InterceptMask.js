import React from 'react';

import './InterceptMask.less';

export default function InterceptMask(props) {
  const { visible } = props;
  const classNames = `c7n-sidebar-mask c7n-sidebar-mask_${
    visible ? 'visible' : 'hide'
  }`;
  return <div className={classNames} />;
}
