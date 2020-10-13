import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Form,
  Select,
  SelectBox,
  TextField,
  Tooltip,
  Icon,
} from 'choerodon-ui/pro';
import { axios } from '@choerodon/master';
import { forEach } from 'lodash';
import Tips from '../../../../../../components/new-tips';

const { Option } = Select;

export default observer(
  ({
    addStepDs,
    curType,
    optType,
    appServiceType,
    appServiceId,
    projectId,
    firstIf,
    nextStageType,
  }) => {
    useEffect(() => {
      const type = addStepDs?.current?.get('type');
      addStepDs.current.set('parallel', type === 'CI' ? 1 : 0);
      handleMore();
    }, [addStepDs?.current?.get('type')]);

    const renderer = ({ text }) => text;

    const optionRenderer = ({ text }) => {
      if (text.includes('CI') && curType && curType === 'CD' && !firstIf) {
        return (
          <Tooltip title="CD阶段后，无法添加CI阶段">
            {renderer({ text })}
          </Tooltip>
        );
      }
      if (text.includes('CD')) {
        if (appServiceId && appServiceType === 'test') {
          return (
            <Tooltip title="测试类型应用服务不能添加CD阶段">
              {renderer({ text })}
            </Tooltip>
          );
        } if (nextStageType === 'CI') {
          return (
            <Tooltip title="CI阶段前，无法添加CD阶段">
              {renderer({ text })}
            </Tooltip>
          );
        }
      }
      return renderer({ text });
    };

    async function handleMore(e, realName) {
      e && e.stopPropagation();
      const pageSize = !e
        ? addStepDs.current.get('pageSize')
        : addStepDs.current.get('pageSize') + 20;
      const url = `/devops/v1/projects/${projectId}/users/list_users?page=0&size=${pageSize}`;
      const cdAuditsUserIds = [];
      forEach(addStepDs?.current?.get('cdAuditUserIds'), (obj) => {
        if (typeof obj === 'string') {
          cdAuditsUserIds.push(obj);
        } else if (typeof obj === 'object') {
          cdAuditsUserIds.push(obj?.id);
        }
      });
      const res = await axios.post(url, {
        param: [],
        searchParam: {
          realName: realName || '',
        },
        ids: cdAuditsUserIds || [],
      });
      if (res.content.length % 20 === 0 && res.content.length !== 0) {
        res.content.push({
          realName: '加载更多',
          id: 'more',
        });
      }
      addStepDs.current.set('pageSize', pageSize);
      if (realName) {
        // eslint-disable-next-line no-param-reassign
        addStepDs.getField('cdAuditUserIds').props.lookup = [
          ...res.content,
          ...addStepDs.getField('cdAuditUserIds').props.lookup,
        ];
      } else {
        // eslint-disable-next-line no-param-reassign
        addStepDs.getField('cdAuditUserIds').props.lookup = res.content;
      }
    }

    const renderderAuditUsersList = ({ text, record }) => (text === '加载更多' ? (
      <a
        role="none"
        style={{ display: 'block', width: '100%', height: '100%' }}
        onClick={handleMore}
      >
        {text}
      </a>
    ) : (
      `${text}(${record.get('loginName')})`
    ));

    // eslint-disable-next-line consistent-return
    function hanldeTypeDisabled(record) {
      const isCi = record.get('value') === 'CI'; // opts得value
      const isCd = record.get('value') === 'CD';
      const hasCurTypeCd = curType && curType === 'CD';
      const hasCurTypeCi = curType && curType === 'CI';
      if (firstIf) {
        if (hasCurTypeCi && isCd) {
          return true;
        } if (hasCurTypeCd && isCi) {
          return false;
        }
      }
      if (hasCurTypeCi) {
        if (nextStageType === 'CD') {
          return false;
        } if (nextStageType === 'CI' && isCd) {
          return true;
        } if (appServiceType === 'test' && isCd) {
          return true;
        }
        return false;
      } if (hasCurTypeCd) {
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
  },
);
