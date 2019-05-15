/*
* Loading
* 加载效果组件
*
* */

import React from 'react';
import { observer } from 'mobx-react';
import PropTypes from 'prop-types';
import { Progress } from 'choerodon-ui';
import classNames from 'classnames';
import './LoadingBar.scss';

const LoadingBarRequiredProps = {
  display: PropTypes.bool,
};

function LoadingBar({ display = false }) {
  const spinClass = classNames({
    'spin-hidden': !display,
    'spin-container': display,
  });
  return (
    <div className={spinClass}>
      <Progress type="loading" />
    </div>
  );
}

LoadingBar.propTypes = LoadingBarRequiredProps;
export default observer(LoadingBar);
