import React, { PureComponent } from 'react/index';
import PropTypes from 'prop-types';

class Percentage extends PureComponent {
  static propTypes = {
    data: PropTypes.number.isRequired,
    successColor: PropTypes.string,
    failedColor: PropTypes.string,
    size: PropTypes.number,
    strokeWidth: PropTypes.number,
  };

  render() {
    const { data, size, successColor, failedColor, strokeWidth } = this.props;
    const realSize =size || 20;
    const cx = (realSize / 2).toString();
    const width =strokeWidth || 4;
    const radius = (realSize - width) / 2;
    const circumference = (data / 100) * Math.PI * radius * 2;

    return (
      <svg width={realSize} height={realSize} className="c7ncd-percentage-wrap">
        <circle
          cx={cx}
          cy={cx}
          r={radius}
          strokeWidth={width}
          stroke={failedColor || "#f44336"}
          fill="none"
        />
        <circle
          cx={cx}
          cy={cx}
          r={radius}
          strokeWidth={width}
          stroke={successColor || "#00bf96"}
          fill="none"
          transform={`matrix(0,-1,1,0,0,${realSize})`}
          strokeDasharray={`${circumference}, 10000`}
        />
      </svg>
    );
  };
}

export default Percentage;
