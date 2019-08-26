import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';

import './Rating.less';

class Rating extends PureComponent {
  static propTypes = {
    rating: PropTypes.string.isRequired,
    size: PropTypes.string,
    fontSize: PropTypes.string,
    type: PropTypes.string,
  };

  render() {
    const { rating, size, fontSize, type } = this.props;
    const realSize = size || (type === 'pie' ? '18px' : '16px');
    return (
      <div className="c7ncd-rating-wrap">
        {type === 'pie' ? (
          <div
            className={`c7ncd-rating-pie c7ncd-rating-pie-${rating}`}
            style={{ width: realSize, height: realSize }}
          />) : (
            <div
              className={`c7ncd-rating-letter c7ncd-rating-letter-${rating}`}
              style={{ width: realSize, height: realSize, lineHeight: realSize, fontSize: fontSize || '13px' }}
            >
              {rating}
            </div>
        )}
      </div>
    );
  }
}

export default Rating;
