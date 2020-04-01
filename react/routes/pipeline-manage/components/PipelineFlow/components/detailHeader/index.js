import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import './index.less';

const tagObj = {
  load: {
    borderColor: 'rgba(77,144,254,0.65)',
    backgroundColor: 'rgba(77,144,254,0.04)',
    color: 'rgba(77,144,254,1)',
    text: '执行中',
  },
};

const renderTag = (props) => {
  // const {} = props;
  const {
    load: {
      borderColor, backgroundColor, color, text,
    },
  } = tagObj;
  return (
    <span
      className="c7ncd-pipelineManage-optsDetail-header-tag"
      style={{ borderColor, backgroundColor, color }}
    >
      {text}
    </span>
  );
};

export default observer(({ id, name }) => {
  useEffect(() => {

  }, []);

  return (
    <div className="c7ncd-pipelineManage-optsDetail-header">
      <span>#{id}</span>
      <span>(workFlow)</span>
      {renderTag()}
    </div>
  );
});
