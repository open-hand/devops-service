import React from 'react';
import SimpleBar from 'simplebar-react';
import classnames from 'classnames';

import './simplebar.less';
import './index.less';

const ScrollArea = ({ children, vertical, horizontal, className = '', ...props }) => {
  const styled = classnames({
    'c7n-scrollbar-default': true,
    'c7n-scrollbar-x': horizontal,
    'c7n-scrollbar-y': vertical,
    [className]: className,
  });
  return <SimpleBar
    className={styled}
    {...props}
  >
    {children}
  </SimpleBar>;
};

ScrollArea.defaultProps = {
  horizontal: false,
  vertical: false,
};

export default ScrollArea;
