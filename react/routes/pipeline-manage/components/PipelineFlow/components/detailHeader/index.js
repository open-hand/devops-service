import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Tag } from 'choerodon-ui';
import './index.less';

export default observer(() => {
  useEffect(() => {

  }, []);
  return (
    <div className="c7ncd-pipelineManage-optsDetail-header">
      <span>#109725</span>
      <span>(workFlow)</span>
      <Tag color="geekblue">执行中</Tag>
    </div>
  );
});
