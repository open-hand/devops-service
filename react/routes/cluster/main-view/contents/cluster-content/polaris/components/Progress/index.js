import React from 'react';
import _ from 'lodash';

import './index.less';

const ProgressApp = (props) => {
  const {
    loading,
    num,
  } = props;

  const getContent = () => {
    if (loading) {
      return (
        <div
          className="movingBlock"
        />
      );
    } else if (!_.isNull(num)) {
      return (
        <React.Fragment>
          <div
            className="alreadyBlock"
            style={{
              width: `${num}%`,
            }}
          >
            <div className="alreadyMovingBar" />
          </div>
          <div
            className="notReachBlock"
            style={{
              width: `${99 - parseInt(num, 10)}%`,
            }}
          >
            <div className="notReachMovingBar" />
          </div>
        </React.Fragment>
      );
    }
  };

  return (
    <div
      className="ProgressApp"
      style={{
        background: !_.isNull(num) ? 'rgba(247, 122, 112, 1)' : 'gainsboro',
      }}
    >
      {getContent()}
    </div>
  );
};

export default ProgressApp;
