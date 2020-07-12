import React from 'react';
import PropTypes from 'prop-types';

import './index.less';

const tagObj = {
  running: {
    borderColor: 'rgba(77,144,254,0.12)',
    backgroundColor: 'rgba(77,144,254,0.12)',
    color: 'rgba(77,144,254,1)',
    text: '执行中',
  },
  success: {
    borderColor: 'rgba(0,191,165,.12)',
    backgroundColor: 'rgba(0,191,165,.12)',
    color: 'rgba(0,191,165,1)',
    text: '成功',
  },
  failed: {
    borderColor: 'rgba(247,122,112,.12)',
    backgroundColor: 'rgba(247,122,112,.12)',
    color: 'rgba(247,122,112,1)',
    text: '失败',
  },
  pending: {
    borderColor: 'rgba(255,177,0,.12)',
    backgroundColor: 'rgba(255,177,0,.12)',
    color: 'rgba(255,177,0,1)',
    text: '准备中',
  },
  skipped: {
    borderColor: 'rgba(216,216,216,.12)',
    backgroundColor: 'rgba(216,216,216,.12)',
    color: 'rgba(216,216,216,1)',
    text: '已跳过',
  },
  canceled: {
    borderColor: 'rgba(216,216,216,.12)',
    backgroundColor: 'rgba(216,216,216,.12)',
    color: 'rgba(216,216,216,1)',
    text: '已取消',
  },
  created: {
    borderColor: 'rgba(216,216,216,.12)',
    backgroundColor: 'rgba(216,216,216,.12)',
    color: 'rgba(216,216,216,1)',
    text: '未执行',
  },
  not_audit: {
    borderColor: 'rgba(255, 177, 0,.12)',
    backgroundColor: 'rgba(255, 177, 0,.12)',
    color: 'rgba(255, 177, 0,1)',
    text: '待审核',
  },
  stop: {
    borderColor: 'rgba(255, 112, 67, .12)',
    backgroundColor: 'rgba(255, 112, 67, .12)',
    color: 'rgba(255, 112, 67, 1)',
    text: '已终止',
  },
};

const renderTag = ({ status, size, className }) => {
  const {
    borderColor, backgroundColor, color, text,
  } = tagObj[status] || {};
  return (
    tagObj[status]
      ? <span
        className={`${className} c7ncd-pipelineManage-optsDetail-header-tag`}
        style={{ borderColor, backgroundColor, color, fontSize: `${size}px` }}
      >
        {text}
      </span> : null
  );
};

renderTag.propTypes = {
  status: PropTypes.string.isRequired,
  size: PropTypes.number,
  className: PropTypes.string,
};

renderTag.defaultProps = {
  size: 12,
  className: '',
};

export default renderTag;
