import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import { Tooltip } from 'choerodon-ui';

import './index.less';

export default function StatusTag({ name, color, colorCode, style, ellipsis, error }) {
  const colorMappings = useMemo(() => ({
    success: '#00bf96',
    running: '#00bf96',
    error: '#f44336',
    failed: '#f44336',
    merged: '#4d90fe',
    operating: '#4d90fe',
    opened: '#ffb100',
    ready: '#57aaf8',
    unready: 'rgba(0,0,0,0.20)',
    deleted: 'rgba(0,0,0,0.36)',
    finished: '#00bf96',
    pendingcheck: '#ffb100',
    active: '#00bf96',
    creating: '#4d90fe',
    disconnect: '#ff9915',
    executing: '#4d90fe',
    terminated: '#ff7043',
  }), []);
  const defaultColor = 'rgba(0, 0, 0, 0.28)';

  const tagNode = <Tooltip title={ellipsis ? name : error}>
    {name || ''}
  </Tooltip>;
  return (
    <div
      className="c7ncd-status-tag"
      style={{
        background: color || colorMappings[colorCode.toLocaleLowerCase()] || defaultColor,
        ...style,
      }}
    >
      {ellipsis ? <div className="c7ncd-status-tag-wrap">
        {tagNode}
      </div> : tagNode}
    </div>
  );
}

StatusTag.propTypes = {
  name: PropTypes.string.isRequired,
  color: PropTypes.string,
  colorCode: PropTypes.string,
  style: PropTypes.object,
  ellipsis: PropTypes.object,
  error: PropTypes.string,
};
