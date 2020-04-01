import React from 'react';
import PropTypes from 'prop-types';

import './index.less';

const pipelineTitle = ({ type, iconSize }) => (
  <span
    className={`c7ncd-pipelineMange-title-type c7ncd-pipelineMange-title-type-${type}`}
    style={{ width: `${iconSize}px`, height: `${iconSize}px`, lineHeight: `${iconSize}px` }}
  >
    {(type || 'A').slice(0, 1).toUpperCase()}
  </span>
);

pipelineTitle.propTypes = {
  type: PropTypes.string.isRequired,
  iconSize: PropTypes.number,
};

pipelineTitle.defaultProps = {
  iconSize: 16,
};

export default pipelineTitle;
