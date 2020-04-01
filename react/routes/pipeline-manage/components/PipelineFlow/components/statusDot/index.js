import React from 'react';
import { Icon } from 'choerodon-ui';

import './index.less';

const statusObj = {
  success: 'check_circle',
  load: 'timelapse',
};

export default ((props) => {
  const { size, status } = props;
  return (
    <Icon type={statusObj[status]} style={{ fontSize: `${size}px` }} />
  );
});
