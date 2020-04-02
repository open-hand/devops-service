import React from 'react';

import PipelineTitle from '../../../pipeline-type';

import './index.less';

const EditHeader = ({ type, name, iconSize }) => (
  <div className="c7ncd-pipelineManage-eidt-header">
    <PipelineTitle type={type} iconSize={iconSize} />
    <span className="c7ncd-pipelineManage-eidt-header-title">{name}</span>
  </div>
);

export default EditHeader;
