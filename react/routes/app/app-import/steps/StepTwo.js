import React, { Component, Fragment } from 'react/index';
import _ from 'lodash';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Form, Input, Select, Tooltip, Icon } from 'choerodon-ui';
import { STEP_FLAG } from '../Constants';

import '../index.scss';

const { Item: FormItem } = Form;
const { Option } = Select;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 8 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 16 },
  },
};

@Form.create({})
@injectIntl
@inject('AppState')
@observer
export default class StepTwo extends Component {
  state = {
    template: '',
  };

  componentDidMount() {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      values,
    } = this.props;

    if (values && values.template) {
      this.setState({
        template: values.template,
      });
    }
    store.loadSelectData(projectId, true);
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const {
      onNext,
      form: {
        validateFields,
        isModifiedFields,
        getFieldsValue,
      },
    } = this.props;

    const { template } = this.state;

    const isModified = isModifiedFields(['code', 'name', 'applicationTemplateId']);

    if (isModified) {
      validateFields((err, values) => {
        if (!err) {
          const data = {
            ...values,
            key: STEP_FLAG.LANGUAGE_SELECT,
            template,
          };

          onNext(data, STEP_FLAG.PERMISSION_RULE);
        }
      });
    } else {
      const values = {
        ...getFieldsValue(),
        key: STEP_FLAG.LANGUAGE_SELECT,
        template,
      };

      onNext(values, STEP_FLAG.PERMISSION_RULE);
    }
  };

  /**
   * 校验应用编码规则
   * @param rule
   * @param value
   * @param callback
   */
  checkCode = _.debounce((rule, value, callback) => {
    const {
      store,
      intl: { formatMessage },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;

    if (value && pa.test(value)) {
      store.checkCode(projectId, value)
        .then(data => {
          if (data && data.failed) {
            callback(formatMessage({ id: 'template.checkCode' }));
          } else {
            callback();
          }
        });
    } else {
      callback(formatMessage({ id: 'template.checkCodeReg' }));
    }
  }, 600);

  /**
   * 校验应用名称规则
   * @param rule
   * @param value
   * @param callback
   */
  checkName = _.debounce((rule, value, callback) => {
    const {
      store,
      intl: { formatMessage },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    const pa = /^\S+$/;

    if (value && pa.test(value)) {
      store.checkName(projectId, value)
        .then(data => {
          if (data && data.failed) {
            callback(formatMessage({ id: 'template.checkName' }));
          } else {
            callback();
          }
        });
    } else {
      callback(formatMessage({ id: 'template.checkName' }));
    }
  }, 600);

  /**
   * 模板切换
   * @param id
   */
  onChange = (id) => {
    const {
      store: {
        selectData,
      },
    } = this.props;
    const template = selectData.filter((e) => e.id === parseInt(id))[0].name;
    this.setState({ template });
  };

  hasErrors(fieldsError) {
    return Object.keys(fieldsError).some(field => fieldsError[field]);
  }

  hasValues(fieldsValue) {
    return Object.values(fieldsValue).some(field => field === '');
  }

  render() {
    const {
      onPrevious,
      onCancel,
      form: { getFieldDecorator },
      intl: { formatMessage },
      values,
      store: { selectData },
    } = this.props;
    const { getFieldsError, getFieldsValue } = this.props.form;

    return (
      <Fragment>
        <div className="steps-content-des">
          <FormattedMessage id="app.import.step1.des" />
          <div>
            <Icon type="error" />
            <FormattedMessage id="app.import.step1.warn" />
          </div>
        </div>
        <div className="steps-content-section">
          <Form onSubmit={this.handleSubmit}>
            <FormItem
              {...formItemLayout}
            >
              {getFieldDecorator('code', {
                rules: [
                  { required: true, message: formatMessage({ id: 'app.import.code.required' }), whitespace: true },
                  { validator: this.checkCode },
                ],
                initialValue: values.code || '',
              })(
                <Input
                  label={<FormattedMessage id="ciPipeline.appCode" />}
                />,
              )}
            </FormItem>
            <FormItem
              {...formItemLayout}
            >
              {getFieldDecorator('name', {
                rules: [
                  { required: true, message: formatMessage({ id: 'app.import.name.required' }), whitespace: true },
                  { validator: this.checkName },
                ],
                initialValue: values.name || '',
              })(
                <Input
                  label={<FormattedMessage id="ciPipeline.appName" />}
                />,
              )}
            </FormItem>
            <FormItem
              {...formItemLayout}
            >
              {getFieldDecorator('applicationTemplateId', {
                rules: [{ required: true, message: formatMessage({ id: 'app.import.template.required' }) }],
                initialValue: values.applicationTemplateId || '',
              })(
                <Select
                  filter
                  onChange={this.onChange}
                  label={<FormattedMessage id="app.chooseTem" />}
                  filterOption={(input, option) =>
                    option.props.children.props.children
                      .toLowerCase()
                      .indexOf(input.toLowerCase()) >= 0
                  }
                >
                  {selectData.map(s => (
                    <Option key={s.id}>
                      <Tooltip placement="right" title={s.description}>
                        {s.name}
                      </Tooltip>
                    </Option>
                  ))}
                </Select>,
              )}
            </FormItem>
            <FormItem>
              <Button
                type="primary"
                funcType="raised"
                htmlType="submit"
                disabled={this.hasErrors(getFieldsError()) || this.hasValues(getFieldsValue())}
              >
                <FormattedMessage id="next" />
              </Button>
              <Button
                onClick={() => onPrevious(STEP_FLAG.IMPORT_ORIGIN)}
                funcType="raised"
                className="c7n-btn-cancel"
              >
                <FormattedMessage id="previous" />
              </Button>
              <Button
                onClick={onCancel}
                funcType="raised"
                className="c7n-btn-cancel"
              >
                <FormattedMessage id="cancel" />
              </Button>
            </FormItem>
          </Form>
        </div>
      </Fragment>
    );
  }
}
