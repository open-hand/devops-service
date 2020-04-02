import React from 'react';
import { Icon, Tooltip } from 'choerodon-ui';

import './index.less';

const statusObj = {
  success: {
    icon: 'check_circle',
    text: '成功',
  },
  load: {
    icon: 'timelapse',
    text: '执行中',
  },
  failed: {
    icon: 'cancel',
    text: '失败',
  },
};

export default ((props) => {
  const { size, status } = props;
  return (
    <Tooltip title={statusObj[status].text}>
      <Icon type={statusObj[status].icon} style={{ fontSize: `${size}px` }} />
    </Tooltip>
  );
});
