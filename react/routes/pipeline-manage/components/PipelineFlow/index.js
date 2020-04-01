import React from 'react';
import { observer } from 'mobx-react-lite';
import { Spin } from 'choerodon-ui/pro';
import { usePipelineManageStore } from '../../stores';

import './index.less';

const pipelineContent = observer(() => {
  const {
    mainStore,
  } = usePipelineManageStore();

  function getContent() {
    const { id, parentId } = mainStore.getSelectedMenu;
    if (!id) {
      return <Spin />;
    }
    if (parentId) {
      return `#${id} detail`;
    }
    return `${id} pipeline edit`;
  }

  return (
    <div className="pipelineManage_flow">
      {getContent()}
    </div>
  );
});

export default pipelineContent;
