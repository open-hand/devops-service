import React from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, TextField } from 'choerodon-ui/pro';
import { useAddTaskStore } from './stores';

import './index.less';

const AddTask = observer(() => {
  const {
    AddTaskFormDataSet,
  } = useAddTaskStore();

  const steps = [{
    name: 'Maven构建',
  }, {
    name: '上传软件包至发布库',
  }];

  const generateSteps = () => (
    <div className="AddTask_stepItemsContainer">
      {
          steps.map((s, index) => (
            <div className="AddTask_stepMapContent">
              <div style={{ display: index === 0 ? 'flex' : 'none' }} className="AddTask_stepAdd">
                +
              </div>
              <div className="AddTask_stepItem">
                {s.name}
              </div>
              <div className="AddTask_stepAdd">
                +
              </div>
            </div>
          ))
        }
    </div>
  );

  return (
    <React.Fragment>
      <Form dataSet={AddTaskFormDataSet} columns={2}>
        <Select name="rwlx" />
        <TextField name="rwmc" />
        <Select name="glyyfw" />
        <Select name="cffzlx" />
        <div colSpan={2} className="AddTask_configStep">
          <p>配置步骤</p>
        </div>
        <Select style={{ marginTop: 45 }} name="gjmb" />
      </Form>
      <div className="AddTask_stepContent">
        {generateSteps()}
      </div>
    </React.Fragment>
  );
});

export default AddTask;
