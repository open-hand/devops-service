import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';

import './Rating.scss';

class Rating extends PureComponent {
  static propTypes = {
    rating: PropTypes.string.isRequired,
    size: PropTypes.string,
    fontSize: PropTypes.string,
  };

  render() {
    const { rating, size, fontSize } = this.props;
    const realSize = size || "16px";

    return (
      <span
        className={`c7ncd-rating-wrap c7ncd-rating-wrap-${rating}`}
        style={{width: realSize, height: realSize, lineHeight: realSize, fontSize: fontSize || "13px"}}
      >
        {rating}
      </span>
    );
  };
}

export default Rating;
