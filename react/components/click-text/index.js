import React from 'react';
import PropTypes from 'prop-types';
import { Permission } from '@choerodon/boot';
import isEmpty from 'lodash/isEmpty';

import './index.less';

export default function ClickText(props) {
  const { value, clickAble, onClick, record, permissionCode } = props;
  const text = clickAble
    ? <a className="c7ncd-click-text" onClick={handleClick}>{value}</a>
    : <span>{ value }</span>;

  function handleClick() {
    record ? onClick(record) : onClick();
  }

  return (isEmpty(permissionCode) ? text : (
    <Permission
      service={permissionCode}
      noAccessChildren={value}
      defaultChildren={value}
    >
      {text}
    </Permission>
  ));
}

ClickText.propTypes = {
  value: PropTypes.string.isRequired,
  clickAble: PropTypes.bool,
  onClick: PropTypes.func,
  record: PropTypes.any,
  permissionCode: PropTypes.array,
};

ClickText.defaultProps = {
  clickAble: false,
  permissionCode: [],
};
