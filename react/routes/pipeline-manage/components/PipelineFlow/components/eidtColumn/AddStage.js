import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, SelectBox, TextField } from 'choerodon-ui/pro';

const { Option } = Select;

export default observer(({ addStepDs, curType, optType }) => {
  useEffect(() => {
    const type = addStepDs?.current?.get('type');
    addStepDs.current.set('parallel', type === 'CI' ? 1 : 0);
  }, [addStepDs?.current?.get('type')]);

  return (
    <Form className="addStageForm" dataSet={addStepDs}>
      <Select showHelp="tooltip" name="type" help="123">
        <Option disabled={curType && (curType === 'CD')} value="CI">CI阶段</Option>
        <Option value="CD">CD阶段</Option>
      </Select>
      <TextField name="step" />
      <Select name="parallel">
        <Option value={0}>任务串行</Option>
        <Option value={1}>任务并行</Option>
      </Select>
      {
        addStepDs?.current?.get('type') === 'CD' ? (
          <SelectBox name="triggerType">
            <Option value="auto">自动流转</Option>
            <Option value="manual">手动流转</Option>
          </SelectBox>
        ) : ''
      }
      {
        addStepDs?.current?.get('triggerType') === 'manual' ? (
          <Select showHelp="tooltip" help="123" name="cdAuditUserIds" />
        ) : ''
      }
    </Form>
  );
});
