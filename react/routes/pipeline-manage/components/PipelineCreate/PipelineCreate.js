import React, { useEffect } from 'react';
import { Form, TextField, Select, SelectBox, Modal, Button, DataSet } from 'choerodon-ui/pro';
import { message } from 'choerodon-ui';
import { observer } from 'mobx-react-lite';
import { usePipelineCreateStore } from './stores';
import AddTask from './components/AddTask';
import { usePipelineManageStore } from '../../stores';
import StageEditBlock from '../PipelineFlow/components/stageEditBlock';

import './pipelineCreate.less';

const { Option } = Select;

const PipelineCreate = observer(() => {
  const {
    PipelineCreateFormDataSet,
    modal,
    editBlockStore,
    createUseStore,
    AppState: {
      currentMenuType: {
        id,
      },
    },
    refreshTree,
  } = usePipelineCreateStore();


  const handleCreate = async () => {
    const result = await PipelineCreateFormDataSet.validate();
    if (result) {
      const data = {
        ...PipelineCreateFormDataSet.toData()[0],
        stageList: editBlockStore.getStepData2,
      };
      return createUseStore.axiosCreatePipeline(data, id).then((res) => {
        if (res.failed) {
          message.error(res.message);
          return false;
        } else {
          refreshTree();
          return true;
        }
      });
    } else {
      return false;
    }
  };

  modal.handleOk(handleCreate);

  // const handleAddMission = () => {
  //   Modal.open({
  //     key: Modal.key(),
  //     title: '添加任务',
  //     style: {
  //       width: '740px',
  //     },
  //     children: <AddTask />,
  //     drawer: true,
  //     okText: '添加',
  //   });
  // };

  return (
    <div>
      <Form columns={3} dataSet={PipelineCreateFormDataSet}>
        <TextField name="name" />
        {/* 应用服务只能选择目前没有关联流水线的应用服务 */}
        <Select
          name="appServiceId"
          searchable
          searchMatcher="appServiceName"
        />
        {/* <SelectBox name="triggerType"> */}
        {/*  <Option value="auto">自动触发</Option> */}
        {/*  <Option disabled value="F">手动触发</Option> */}
        {/* </SelectBox> */}
      </Form>
      <p className="pipeline_createInfo"><span>!</span>此页面定义了阶段与任务后，GitLab仓库中的.gitlab-ci.yml文件也会同步修改。</p>
      <StageEditBlock editBlockStore={editBlockStore} edit />
    </div>
  );
});

export default PipelineCreate;
