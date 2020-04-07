import React from 'react';
import { Form, TextField, Select, SelectBox, Modal, Button } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { usePipelineCreateStore } from './stores';
import AddTask from './components/AddTask';
import StageEditBlock from '../PipelineFlow/components/stageEditBlock';

const { Option } = Select;

const PipelineCreate = observer(() => {
  const {
    PipelineCreateFormDataSet,
  } = usePipelineCreateStore();

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
    <Form columns={3} dataSet={PipelineCreateFormDataSet}>
      <TextField name="lsxmc" />
      {/* 应用服务只能选择目前没有关联流水线的应用服务 */}
      <Select name="glyyfw" />
      <SelectBox name="cfss">
        <Option value="M">自动触发</Option>
        <Option disabled value="F">手动触发</Option>
      </SelectBox>
      <StageEditBlock />
    </Form>
  );
});

export default PipelineCreate;
