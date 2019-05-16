import React, { Component, Fragment } from 'react';
import _ from "lodash";
import { observer } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Radio, Form, Input, message, Icon } from 'choerodon-ui';
import { stores } from '@choerodon/boot';
import '../AppImport.scss';

const RadioGroup = Radio.Group;
const FormItem = Form.Item;
const { AppState } = stores;
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

@observer
class StepOne extends Component {
  constructor() {
    super(...arguments);
    this.state = {
      platformType: this.props.values.platformType || 'github',
      accessToken: undefined,
      repositoryUrl: undefined,
    }
  }

  /**
   * 检查编码是否合法
   * @param rule
   * @param value
   * @param callback
   */
  checkUrl = _.debounce((rule, value, callback) => {
    const { intl: { formatMessage } } = this.props;
    const reg = /^(https?):\/\/[\w\-]+(\.[\w\-]+)+([\w\-.,@?^=%&:\/~+#]*[\w\-@?^=%&\/~+#])?$/;
    if (value && reg.test(value) && value.indexOf('.git') !== -1) {
      callback();
    } else if (value && (!reg.test(value) || value.indexOf('.git') === -1)) {
      callback(formatMessage({ id: 'app.import.url.err' }));
    } else {
      callback();
    }
  }, 600);

  handleSubmit = (e) => {
    e.preventDefault();
    const { store, intl: { formatMessage }, onNext, form: { validateFields, isModifiedFields, getFieldsValue } } = this.props;
    const { platformType } = this.state;
    const { id: projectId } = AppState.currentMenuType;
    const isModified = isModifiedFields(['repositoryUrl', 'accessToken']);
    if (isModified) {
      validateFields((err, values) => {
        if (!err) {
          store.checkUrl(projectId, platformType, values.repositoryUrl, values.accessToken)
            .then((error) => {
              if (error === false) {
                message.info(formatMessage({ id: 'app.import.url.err1' }), undefined, undefined, 'bottomLeft');
              } else if (error === null) {
                message.info(formatMessage({ id: 'app.import.url.null' }), undefined, undefined, 'bottomLeft');
              } else {
                values.key = 'step0';
                values.platformType = platformType;
                onNext(values, 1);
              }
            });
        }
      });
    } else {
      const values = getFieldsValue();
      values.key = 'step0';
      values.platformType = this.state.platformType;
      onNext(values, 1);
    }
  };

  fromItem() {
    const { platformType } = this.state;
    const { form: { getFieldDecorator }, intl: { formatMessage }, values } = this.props;
    if (platformType === 'github') {
      return (<Fragment>
        <FormItem
          {...formItemLayout}
        >
          {getFieldDecorator('repositoryUrl', {
            rules: [
              { required: true, message: formatMessage({ id: "app.import.repo.required" }), whitespace: true },
              { validator: this.checkUrl },
              ],
            initialValue: values.repositoryUrl || '',
          })(
            <Input
              label={<FormattedMessage id="app.import.github" />}
            />
          )}
        </FormItem>
      </Fragment>);
    } else if (platformType === 'gitlab') {
      return (<Fragment>
        <FormItem
          {...formItemLayout}
        >
          {getFieldDecorator('repositoryUrl', {
            rules: [
              { required: true, message: formatMessage({ id: "app.import.repo.required" }), whitespace: true },
              { validator: this.checkUrl },
            ],
            initialValue: values.repositoryUrl || '',
          })(
            <Input
              onChange={this.onUrlChange}
              label={<FormattedMessage id="app.import.gitlab" />}
            />
          )}
        </FormItem>
        <FormItem
          {...formItemLayout}
        >
          {getFieldDecorator('accessToken', {
            rules: [
              { message: formatMessage({ id: "app.import.token.required" }), whitespace: true },
            ],
            initialValue: values.accessToken || '',
          })(
            <Input
              onChange={this.onTokenChange.bind(this)}
              label={<FormattedMessage id="app.import.token" />}
            />
          )}
        </FormItem>
      </Fragment>);
    }
  }

  onChange = (e) => {
    this.setState({
      platformType: e.target.value,
      accessToken: undefined,
      repositoryUrl: undefined,
    }, () => {
      this.props.form.setFieldsValue({
        repositoryUrl: undefined,
        accessToken: undefined,
      });
    });
  };

  onUrlChange = (e) => {
    this.setState({
      repositoryUrl: e.target.value,
    });
  };

  onTokenChange = (e) => {
    this.setState({
      accessToken: e.target.value,
    });
  };

  hasErrors(fieldsError) { return Object.keys(fieldsError).some(field => fieldsError[field]) }

  hasValues(fieldsValue) { return _.isEmpty(fieldsValue.repositoryUrl) }

  render() {
    const { onCancel } = this.props;
    const { platformType } = this.state;
    const { getFieldsError, getFieldsValue } = this.props.form;

    return (
      <Fragment>
        <div className="steps-content-des">
          <FormattedMessage id="app.import.step1.des" />
          <div>
            <Icon type="error" />
            <FormattedMessage id="app.import.step1-1.des" />
          </div>
        </div>
        <div className="steps-content-section">
          <RadioGroup label={<FormattedMessage id="template.type" />} onChange={this.onChange.bind(this)} value={platformType}>
            <Radio value="github">GitHub</Radio>
            <Radio value="gitlab">GitLab</Radio>
          </RadioGroup>
        </div>
        <div className="steps-content-section">
          <Form onSubmit={this.handleSubmit}>
            {this.fromItem()}
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
    )
  }
}

export default Form.create({})(injectIntl(StepOne));
