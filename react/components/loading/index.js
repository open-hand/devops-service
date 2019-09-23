import React from 'react';
import PropTypes from 'prop-types';
import { Progress } from 'choerodon-ui';
import classNames from 'classnames';

import './index.less';

function Loading({ display = false }) {
  const spinClass = classNames({
    'c7ncd-spin-hidden': !display,
    'c7ncd-spin-container': display,
  });
  return (
    <div className={spinClass}>
      <Progress type="loading" />
    </div>
  );
}

Loading.propTypes = {
  display: PropTypes.bool,
};

export default Loading;
