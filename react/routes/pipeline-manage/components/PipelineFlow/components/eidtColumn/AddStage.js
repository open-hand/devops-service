import React from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, SelectBox, TextField } from 'choerodon-ui/pro';

const { Option } = Select;

export default observer(({ addStepDs, curType, optType }) => (
  <Form className="addStageForm" dataSet={addStepDs}>
    <Select showHelp="tooltip" name="jdsx" help="123">
      <Option disabled={curType && (curType === 'cd')} value="ci">CI阶段</Option>
      <Option value="cd">CD阶段</Option>
    </Select>
    <TextField name="step" />
    {
      addStepDs?.current?.get('jdsx') === 'cd' ? (
        <SelectBox name="lzzcjd">
          <Option value="auto">自动流转</Option>
          <Option value="manual">手动流转</Option>
        </SelectBox>
      ) : ''
    }
    {
      addStepDs?.current?.get('lzzcjd') === 'manual' ? (
        <Select showHelp="tooltip" help="123" name="shry">
          <Option value="1">李丹丹(9221)</Option>
        </Select>
      ) : ''
    }
  </Form>
));
