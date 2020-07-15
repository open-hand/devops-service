import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, SelectBox, TextField, Tooltip } from 'choerodon-ui/pro';

const { Option } = Select;

export default observer(({ addStepDs, curType, optType }) => {
  useEffect(() => {
    const type = addStepDs?.current?.get('type');
    addStepDs.current.set('parallel', type === 'CI' ? 1 : 0);
  }, [addStepDs?.current?.get('type')]);

  const renderer = ({ text }) => text;

  return (
    <Form className="addStageForm" dataSet={addStepDs}>
      <Select
        showHelp="tooltip"
        name="type"
        help="CI阶段中支持添加构建、发布Chart、代码检查以及自定义类型的CI任务；CD阶段中支持添加部署、主机部署以及人工卡点的CD任务。且流水线中任何CD阶段后，不能再添加CI阶段"
        onOption={({ record }) => ({
          disabled: record.get('value') === 'CI' && curType && (curType === 'CD'),
        })}
        optionRenderer={({ text }) => (text.includes('CI') && curType && (curType === 'CD') ? (
          <Tooltip title="CD阶段后，无法添加CI阶段">
            {renderer({ text })}
          </Tooltip>
        ) : renderer({ text }))}
        renderer={renderer}
        disabled={optType === 'edit'}
      />
      <TextField name="step" />
      <Select name="parallel">
        <Option value={0}>任务串行</Option>
        <Option value={1}>任务并行</Option>
      </Select>
      {
        addStepDs?.current?.get('type') === 'CD' ? (
          <SelectBox
            name="triggerType"
            className="addStageForm-triggerType-select"
            help="自动流转表示成功执行完上一阶段后，流水线会自动流转并开始执行此阶段；手动流转则表示上一阶段成功执行后，需要人工审核通过后才能执行此阶段。"
          >
            <Option value="auto">自动流转</Option>
            <Option value="manual">手动流转</Option>
          </SelectBox>
        ) : ''
      }
      {
        addStepDs?.current?.get('triggerType') === 'manual' ? (
          <Select showHelp="tooltip" help="此处的人工审核默认为”或签“的方式，若选择的审核人员为多个，那么其中一个审核通过，便会开始执行下一阶段。" name="cdAuditUserIds" />
        ) : ''
      }
    </Form>
  );
});
