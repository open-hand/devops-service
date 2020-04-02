import React from 'react';
import PropTypes from 'prop-types';

import './index.less';

const tagObj = {
  load: {
    borderColor: 'rgba(77,144,254,0.65)',
    backgroundColor: 'rgba(77,144,254,0.04)',
    color: 'rgba(77,144,254,1)',
    text: '执行中',
  },
  success: {
    borderColor: 'rgba(0,191,165,.65)',
    backgroundColor: 'rgba(0,191,165,.04)',
    color: 'rgba(0,191,165,1)',
    text: '成功',
  },
  failed: {
    borderColor: 'rgba(247,122,112,.65)',
    backgroundColor: 'rgba(247,122,112,.04)',
    color: 'rgba(247,122,112,1)',
    text: '失败',
  },
  preparing: {
    borderColor: 'rgba(255,177,0,.65)',
    backgroundColor: 'rgba(255,177,0,.04)',
    color: 'rgba(255,177,0,1)',
    text: '准备中',
  },
  passed: {
    borderColor: 'rgba(216,216,216,.65)',
    backgroundColor: 'rgba(216,216,216,.04)',
    color: 'rgba(216,216,216,1)',
    text: '已跳过',
  },
  canceld: {
    borderColor: 'rgba(216,216,216,.65)',
    backgroundColor: 'rgba(216,216,216,.04)',
    color: 'rgba(216,216,216,1)',
    text: '已取消',
  },
  unexcuted: {
    borderColor: 'rgba(216,216,216,.65)',
    backgroundColor: 'rgba(216,216,216,.04)',
    color: 'rgba(216,216,216,1)',
    text: '未执行',
  },
};

const renderTag = ({ status, size }) => {
  const {
    borderColor, backgroundColor, color, text,
  } = tagObj[status];
  return (
    <span
      className="c7ncd-pipelineManage-optsDetail-header-tag"
      style={{ borderColor, backgroundColor, color, fontSize: `${size}px` }}
    >
      {text}
    </span>
  );
};

renderTag.propTypes = {
  status: PropTypes.string.isRequired,
  size: PropTypes.number,
};

renderTag.defaultProps = {
  size: 12,
};

export default renderTag;
