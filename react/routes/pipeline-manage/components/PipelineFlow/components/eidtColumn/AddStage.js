import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Form, Select, SelectBox, TextField, Tooltip, Icon } from 'choerodon-ui/pro';
import Tips from '../../../../../../components/new-tips';

const { Option } = Select;

export default observer(({ addStepDs, curType, optType, appServiceType }) => {
  useEffect(() => {
    const type = addStepDs?.current?.get('type');
    addStepDs.current.set('parallel', type === 'CI' ? 1 : 0);
  }, [addStepDs?.current?.get('type')]);

  const renderer = ({ text }) => text;

  const optionRenderer = ({ text }) => {
    if (text.includes('CI') && curType && (curType === 'CD')) {
      return (
        <Tooltip title="CD阶段后，无法添加CI阶段">
          {renderer({ text })}
        </Tooltip>
      );
    } else if (text.includes('CD') && appServiceType === 'test') {
      return (
        <Tooltip title="测试类型应用服务不能添加CD阶段">
          {renderer({ text })}
        </Tooltip>
      );
    }
    return renderer({ text });
  };

  const renderOpts = ({ value, text, record }) => (`${text}(${record.get('loginName')})`);

  return (
    <Form className="addStageForm" dataSet={addStepDs}>
      <Select
        name="type"
        addonAfter={<Tips helpText="CI阶段中支持添加构建、发布Chart、代码检查以及自定义类型的CI任务；CD阶段中支持添加部署、主机部署以及人工卡点的CD任务。且流水线中任何CD阶段后，不能再添加CI阶段" />}
        onOption={({ record }) => ({
          disabled: (record.get('value') === 'CI' && curType && (curType === 'CD')) || (record.get('value') === 'CD' && appServiceType === 'test'),
        })}
        optionRenderer={optionRenderer}
        renderer={renderer}
        disabled={optType === 'edit'}
      />
      <TextField name="step" />
      <Select
        name="parallel"
        addonAfter={<Tips helpText="目前CI阶段中的任务只支持并行，CD中的阶段仅支持串行" />}
      >
        <Option value={0}>任务串行</Option>
        <Option value={1}>任务并行</Option>
      </Select>
      {
        addStepDs?.current?.get('type') === 'CD' ? (
          <div style={{ position: 'relative' }}>
            <SelectBox
              name="triggerType"
              className="addStageForm-triggerType-select"
              style={{
                marginTop: '0.123rem',
              }}
            >
              <Option value="auto">自动流转</Option>
              <Option value="manual">手动流转</Option>
            </SelectBox>
            <Tooltip title="自动流转表示成功执行完上一阶段后，流水线会自动流转并开始执行此阶段；手动流转则表示上一阶段成功执行后，需要人工审核通过后才能执行此阶段。">
              <Icon
                type="help"
                className="c7ncd-select-tips-icon"
                style={{
                  position: 'absolute',
                  top: '-6px',
                  left: '79px',
                }}
              />
            </Tooltip>
          </div>
        ) : ''
      }
      {
        addStepDs?.current?.get('triggerType') === 'manual' ? (
          <Select
            name="cdAuditUserIds"
            optionRenderer={renderOpts}
            addonAfter={<Tips helpText="此处的人工审核默认为”或签“的方式，若选择的审核人员为多个，那么其中一个审核通过，便会开始执行下一阶段。" />}
          />
        ) : ''
      }
    </Form>
  );
});
