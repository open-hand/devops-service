import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Form, Select, SelectBox, TextField, Tooltip, Icon,
} from 'choerodon-ui/pro';
import Tips from '../../../../../../components/new-tips';

const { Option } = Select;

export default observer(({ addStepDs, curType, optType, appServiceType, appServiceId, projectId, firstIf, nextStageType }) => {
  useEffect(() => {
    const type = addStepDs?.current?.get('type');
    addStepDs.current.set('parallel', type === 'CI' ? 1 : 0);
  }, [addStepDs?.current?.get('type')]);

  const renderer = ({ text }) => text;

  const optionRenderer = ({ text }) => {
    if (text.includes('CI') && curType && (curType === 'CD') && !firstIf) {
      return (
        <Tooltip title="CD阶段后，无法添加CI阶段">
          {renderer({ text })}
        </Tooltip>
      );
    } if (text.includes('CD')) {
      if (appServiceId && appServiceType === 'test') {
        return (
          <Tooltip title="测试类型应用服务不能添加CD阶段">
            {renderer({ text })}
          </Tooltip>
        );
      } else if (nextStageType === 'CI') {
        return (
          <Tooltip title="CI阶段前，无法添加CD阶段">
            {renderer({ text })}
          </Tooltip>
        );
      }
    }
    return renderer({ text });
  };

  function hanldeTypeDisabled(record) {
    const isCi = record.get('value') === 'CI'; // opts得value
    const isCd = record.get('value') === 'CD';
    const hasCurTypeCd = curType && curType === 'CD';
    const hasCurTypeCi = curType && (curType === 'CI');
    if (firstIf) {
      if (hasCurTypeCi && isCd) {
        return true;
      } else if (hasCurTypeCd && isCi) {
        return false;
      }
    }
    if (hasCurTypeCi) {
      if (nextStageType === 'CD') {
        return false;
      } else if (nextStageType === 'CI' && isCd) {
        return true;
      } else if (appServiceType === 'test' && isCd) {
        return true;
      }
      return false;
    } else if (hasCurTypeCd) {
      if (isCi) return true;
      if (isCd && appServiceType === 'test') {
        return false;
      }
    }
  }

  return (
    <Form className="addStageForm" dataSet={addStepDs}>
      <Select
        name="type"
        addonAfter={<Tips helpText="CI阶段中支持添加构建、发布Chart、代码检查以及自定义类型的CI任务；CD阶段中支持添加部署、主机部署以及人工卡点的CD任务。且流水线中任何CD阶段后，不能再添加CI阶段" />}
        onOption={({ record }) => (optType !== 'edit' ? {
          disabled: hanldeTypeDisabled(record),
        } : {})}
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
    </Form>
  );
});
