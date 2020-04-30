import React from 'react';
import { observer } from 'mobx-react-lite';

import PipelineTitle from '../../../pipeline-type';

import './index.less';

const EditHeader = observer(({ type, name, iconSize }) => (
  type && <div className="c7ncd-pipelineManage-eidt-header">
    <PipelineTitle type={type} iconSize={iconSize} />
    <span className="c7ncd-pipelineManage-eidt-header-title">{name}</span>
  </div>
));

export default EditHeader;
