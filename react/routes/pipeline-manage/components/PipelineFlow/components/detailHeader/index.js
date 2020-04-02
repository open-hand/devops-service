import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import './index.less';
import StatusTag from '../StatusTag';

export default observer(({ id, name }) => {
  useEffect(() => {

  }, []);

  return (
    <div className="c7ncd-pipelineManage-optsDetail-header">
      <span>#{id}</span>
      <span>(workFlow)</span>
      <StatusTag status="load" size={12} />
    </div>
  );
});
