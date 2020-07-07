import React from 'react';
import PropTypes from 'prop-types';

import './index.less';

const StageType = ({ type = 'ci' }) => {
  const realType = type?.toUpperCase();
  return (
    <span
      className={`c7n-piplineManage-stage-type c7n-piplineManage-stage-type-${realType}`}
    >
      {realType}
    </span>
  );
};

StageType.propTypes = {
  type: PropTypes.string.isRequired,
};

export default StageType;
