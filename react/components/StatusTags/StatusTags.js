import React, { PureComponent } from 'react/index';
import PropTypes from 'prop-types';
import { Tooltip } from 'choerodon-ui';
import './StatusTags.scss';

const Color = {
  success: '#00bf96',
  running: '#00bf96',
  error: '#f44336',
  failed: '#f44336',
  merged: '#4D90FE',
  operating: '#4D90FE',
  opened: '#FFB100',
  ready: '#57AAF8',
  unready: 'rgba(0,0,0,0.20)',
  deleted: 'rgba(0,0,0,0.36)',
  finished: '#00bf96',
};

class StatusTags extends PureComponent {
  static propTypes = {
    name: PropTypes.string.isRequired,
    color: PropTypes.string,
    colorCode: PropTypes.string,
    style: PropTypes.object,
    ellipsis: PropTypes.object,
    error: PropTypes.string,
  };

  static defaultProps = {
    name: '',
  };

  render() {
    const { name, color, colorCode, style, ellipsis, error } = this.props;
    return (
      <div
        className="c7n-status-tags"
        style={{
          background: color || Color[colorCode.toLocaleLowerCase()] || 'rgba(0, 0, 0, 0.28)',
          ...style,
        }}
      >
        <div style={ellipsis || {}}>
          <Tooltip title={ellipsis ? (name || '') : (error)}>
            {name || ''}
          </Tooltip>
        </div>
      </div>
    );
  }
}

export default StatusTags;
