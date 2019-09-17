import React from 'react';
import PropTypes from 'prop-types';

import './index.less';

export default function ClickText(props) {
  const { value, clickAble, onClick, record } = props;

  function handleClick() {
    record ? onClick(record) : onClick();
  }

  return clickAble
    ? <a className="c7ncd-click-text" onClick={handleClick}>{value}</a>
    : value;
}

ClickText.propTypes = {
  value: PropTypes.string.isRequired,
  clickAble: PropTypes.bool,
  onClick: PropTypes.func,
  record: PropTypes.any,
};

ClickText.defaultProps = {
  clickAble: false,
};
