import React, { Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Select, Radio, Form, Tooltip } from 'choerodon-ui';
import { Choerodon } from '@choerodon/boot';
import omit from 'lodash/omit';
import map from 'lodash/map';
import DynamicSelect from '../../../../../../../components/dynamic-select';
import { handlePromptError } from '../../../../../../../utils';
import Tips from '../../../../../../../components/new-tips';

import './index.less';

const FormItem = Form.Item;
const { Option } = Select;
const RadioGroup = Radio.Group;

const Permission = observer(({ refreshPermission, modal, form, tree, onOk, projectList, intlPrefix, prefixCls, formatMessage, clusterDetail }) => {
  const defaultSkip = clusterDetail.get('skipCheckProjectPermission');
  const { getFieldDecorator } = form;
  const [isSkip, setIsSkip] = useState(defaultSkip);

  modal.handleOk(async () => {
    let projects = null;
    form.validateFields((err, values) => {
      if (!err) {
        const selectedProjects = omit(values, ['keys', 'skipCheckProjectPermission']);
        const skipCheckProjectPermission = values.skipCheckProjectPermission;
        const projectIds = Object.values(selectedProjects);
        projects = {
          skipCheckProjectPermission,
          projectIds,
        };
      }
    });

    if (!(projects && projects.projectIds)) return false;
    try {
      const res = await onOk(projects);
      if (handlePromptError(res, false)) {
        refreshPermission();
        return true;
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  });


  function handleChange(e) {
    setIsSkip(e.target.value);
  }

  function getSelector() {
    if (isSkip) return null;

    const { getFieldsValue } = form;
    const data = getFieldsValue();

    const options = map(projectList, ({ id, name, code }) => {
      const selectedValues = Object.values(omit(data, 'keys'));
      return (
        <Option
          disabled={selectedValues.includes(id)}
          key={id}
          value={id}
        >
          <Tooltip title={code}>{name}</Tooltip>
        </Option>
      );
    });

    return <DynamicSelect
      options={options}
      form={form}
      fieldKeys={data}
      label={formatMessage({ id: `${intlPrefix}.project` })}
      addText={formatMessage({ id: `${intlPrefix}.add.project` })}
      requireText={formatMessage({ id: `${intlPrefix}.project.require` })}
      notFoundContent={formatMessage({ id: `${intlPrefix}.project.empty` })}
    />;
  }

  form.getFieldDecorator('keys', { initialValue: ['key0'] });
  return (
    <Fragment>
      <div className={`${prefixCls}-modal-head`}>{formatMessage({ id: `${intlPrefix}.visibility` })}</div>
      <Form>
        <div className={`${prefixCls}-modal-selectbox`}>
          <FormItem>
            {getFieldDecorator('skipCheckProjectPermission', { initialValue: isSkip })(
              <RadioGroup onChange={handleChange}>
                <Radio value>
                  <span className={`${prefixCls}-modal-selectbox-text`}>
                    {formatMessage({ id: `${intlPrefix}.project.all` })}
                  </span>
                </Radio>
                <Radio value={false}>
                  <span className={`${prefixCls}-modal-selectbox-text`}>
                    <Tips
                      helpText={formatMessage({ id: `${intlPrefix}.permission.some.tips` })}
                      title={formatMessage({ id: `${intlPrefix}.project.part` })}
                    />
                  </span>
                </Radio>
              </RadioGroup>
            )}
          </FormItem>
        </div>
        {getSelector()}
      </Form>
    </Fragment>
  );
});

export default Form.create()(injectIntl(Permission));
