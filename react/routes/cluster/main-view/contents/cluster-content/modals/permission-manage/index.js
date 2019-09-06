import React, { Fragment, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Select, SelectBox } from 'choerodon-ui/pro';
import { Form } from 'choerodon-ui';
import omit from 'lodash/omit';
import map from 'lodash/map';
import DynamicSelect from '../../../../../../../components/dynamic-select';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const FormItem = Form.Item;
const { Option } = Select;

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

    if (!projects.projectIds) return false;
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


  // TODO: 替换掉 SelectBox
  function handleChange(value) {
    setIsSkip(value);
  }

  function getSelector() {
    if (isSkip) return null;

    const { getFieldsValue } = form;
    const data = getFieldsValue();

    const options = map(projectList, ({ id, name }) => {
      const selectedValues = Object.values(omit(data, 'keys'));
      return <Select.Option
        disabled={selectedValues.includes(id)}
        key={id}
        value={id}
      >{name}</Select.Option>;
    });

    return <DynamicSelect
      options={options}
      form={form}
      fieldKeys={data}
      label={formatMessage({ id: `${intlPrefix}.project` })}
      addText={formatMessage({ id: `${intlPrefix}.add.project` })}
    />;
  }

  form.getFieldDecorator('keys', { initialValue: ['key0'] });
  return (
    <Fragment>
      <div className={`${prefixCls}-modal-head`}>{formatMessage({ id: `${intlPrefix}.visibility` })}</div>
      <Form>
        <div className={`${prefixCls}-modal-selectbox`}>
          <FormItem>
            {getFieldDecorator('skipCheckProjectPermission', { initialValue: isSkip })(<SelectBox
              onChange={handleChange}
            >
              <Option value>
                {formatMessage({ id: `${intlPrefix}.project.all` })}
              </Option>
              <Option value={false}>
                {formatMessage({ id: `${intlPrefix}.project.part` })}
              </Option>
            </SelectBox>)}
          </FormItem>
        </div>
        {getSelector()}
      </Form>
    </Fragment>
  );
});

export default Form.create()(injectIntl(Permission));
