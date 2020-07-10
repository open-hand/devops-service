import React, { Fragment } from 'react';
import PropTypes from 'prop-types';

import './index.less';

const StageType = ({ type = 'ci', parallel = 1 }) => {
  const realType = type?.toUpperCase();
  return (
    <Fragment>
      <span
        className={`c7n-piplineManage-stage-type c7n-piplineManage-stage-type-${realType}`}
      >
        {realType}
      </span>
      <span
        className={`c7n-piplineManage-stage-type-task c7n-piplineManage-stage-type-task-${parallel ? 'parallel' : 'serial'}`}
      >
        {parallel ? '任务并行' : '任务串行'}
      </span>
    </Fragment>
  );
};

StageType.propTypes = {
  type: PropTypes.string.isRequired,
};

export default StageType;
