import React, { Component, Fragment } from 'react';
import _ from 'lodash';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Radio, Form, Input, message } from 'choerodon-ui';
import { STEP_FLAG, REPO_TYPE } from '../Constants';

import '../index.scss';

const { Group: RadioGroup } = Radio;
const { Item: FormItem } = Form;

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
export default class StepOne extends Component {
  state = {
    platformType: REPO_TYPE.REPO_GITHUB,
    loading: false,
  };

  componentDidMount() {
    const { values } = this.props;
    if (values && values.platformType) {
      this.setState({
        platformType: values.platformType,
      });
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

    if (value && !(reg.test(value) && value.includes('.git'))) {
      callback(formatMessage({ id: 'app.import.url.err' }));
    } else {
      callback();
    }
  }, 600);

  handleSubmit = (e) => {
    e.preventDefault();
    const {
      store,
      intl: { formatMessage },
      onNext,
      form: {
        validateFields,
        isModifiedFields,
        getFieldsValue,
      },
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
    } = this.props;

    const { platformType } = this.state;
    const isModified = isModifiedFields(['repositoryUrl', 'accessToken']);

    if (isModified) {
      this.setState({ loading: true });

      validateFields(async (err, { repositoryUrl, accessToken }) => {
        if (!err) {
          const res = await store.checkUrl(projectId, platformType, repositoryUrl, accessToken);

          this.setState({ loading: false });

          if (res) {
            const values = {
              repositoryUrl,
              accessToken,
              key: STEP_FLAG.IMPORT_ORIGIN,
              platformType,
            };
            onNext(values, STEP_FLAG.LANGUAGE_SELECT);
          } else {
            message.info(
              formatMessage({ id: `app.import.url.${_.isNull(res) ? 'null' : 'err1'}` }),
              undefined,
              undefined,
              'bottomLeft',
            );
          }
        }
      });
    } else {
      const values = {
        ...getFieldsValue(),
        key: STEP_FLAG.IMPORT_ORIGIN,
        platformType,
      };
      onNext(values, STEP_FLAG.LANGUAGE_SELECT);
    }
  };

  fromItem() {
    const {
      form: { getFieldDecorator },
      intl: { formatMessage },
      values: {
        repositoryUrl,
        accessToken,
      },
    } = this.props;
    const { platformType } = this.state;

    const isGitlabRepo = platformType === REPO_TYPE.REPO_GITLAB;

    return (<Fragment>
      <FormItem
        {...formItemLayout}
      >
        {getFieldDecorator('repositoryUrl', {
          rules: [
            { required: true, message: formatMessage({ id: 'app.import.repo.required' }), whitespace: true },
            { validator: this.checkUrl },
          ],
          initialValue: repositoryUrl || '',
        })(
          <Input
            onChange={isGitlabRepo ? this.onUrlChange : null}
            label={<FormattedMessage id={`app.import.${platformType}`} />}
          />,
        )}
      </FormItem>
      {isGitlabRepo && (
        <FormItem
          {...formItemLayout}
        >
          {getFieldDecorator('accessToken', {
            rules: [
              {
                message: formatMessage({ id: 'app.import.token.required' }),
                whitespace: true,
              },
            ],
            initialValue: accessToken || '',
          })(
            <Input
              onChange={this.onTokenChange.bind(this)}
              label={<FormattedMessage id="app.import.token" />}
            />,
          )}
        </FormItem>
      )}
    </Fragment>);
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

  hasErrors(fieldsError) {
    return Object.keys(fieldsError).some(field => fieldsError[field]);
  }

  hasValues(fieldsValue) {
    return _.isEmpty(fieldsValue.repositoryUrl);
  }

  render() {
    const {
      onCancel,
      form: { getFieldsError, getFieldsValue },
    } = this.props;
    const { platformType, loading } = this.state;

    return (
      <Fragment>
        <div className="steps-content-des">
          <FormattedMessage id="app.import.step0.des" />
        </div>
        <div className="steps-content-section">
          <RadioGroup
            label={<FormattedMessage id="template.type" />}
            onChange={this.onChange.bind(this)}
            value={platformType}
          >
            <Radio value={REPO_TYPE.REPO_GITHUB}>GitHub</Radio>
            <Radio value={REPO_TYPE.REPO_GITLAB}>GitLab</Radio>
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
                loading={loading}
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
    );
  }
}
