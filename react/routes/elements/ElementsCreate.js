import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { Content } from '@choerodon/boot';
import { Button, Form, Select, Input, Modal, Icon, Spin } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import InterceptMask from '../../components/interceptMask';
import { handleCheckerProptError, handleProptError } from '../../utils';
import Tips from '../../components/Tips';

import '../main.scss';
import './style/ElementsCreate.scss';

const REPO_TYPE = ['chart', 'harbor'];
const LINK_TEST_ICON = {
  pass: 'check_circle',
  failed: 'cancel',
};

const { Sidebar } = Modal;
const { Item: FormItem } = Form;
const { Option } = Select;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};

@Form.create({})
@injectIntl
@inject('AppState')
@observer
export default class ElementsCreate extends Component {
  checkName = _.debounce((rule, value, callback) => {
    const {
      store,
      intl: { formatMessage },
      form: { isModifiedField },
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    const reg = /^\S+$/;
    const emojiMatch = /\uD83C[\uDF00-\uDFFF]|\uD83D[\uDC00-\uDE4F]/g;
    const isModify = isModifiedField('name');

    if (value && isModify) {
      if (reg.test(value) && !emojiMatch.test(value)) {
        store.checkName(projectId, value).then(data => {
          if (data && data.failed) {
            callback(formatMessage({ id: 'checkNameExist' }));
          } else {
            callback();
          }
        });
      } else {
        callback(formatMessage({ id: 'formatError' }));
      }
    } else {
      callback();
    }
  }, 600);

  constructor(props) {
    super(props);
    this.state = {
      submitting: false,
      type: 'chart',
      showLengthInfo: false,
    };
  }

  componentDidMount() {
    const {
      store,
      isEditMode,
      id,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    isEditMode && store.queryConfigById(projectId, id);
  }

  /**
   * 输入框显示已输入字数，无内容不显示
   */
  handleInputName = () => {
    const { showLengthInfo } = this.state;
    if (!showLengthInfo) {
      this.setState({
        showLengthInfo: true,
      });
    }
  };

  /**
   * 切换仓库类型
   * @param type
   */
  handleTypeChange = type => {
    const { store } = this.props;
    this.setState({ type });
    store.setTestResult('');
  };

  /**
   * 测试地址连接
   */
  handleTestClick = () => {
    const {
      form: { validateFields },
      store,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    const { type: currentType } = store.getConfig;
    const type = currentType || this.state.type;

    const validateFieldsList = type === 'harbor' ? ['url', 'userName', 'password', 'project', 'email'] : ['url'];

    validateFields(validateFieldsList, (err, values) => {
      if (!err) {
        store.checkRepoLink(projectId, values, type);
      }
    });
  };

  checkUrl = (rule, value, callback) => {
    const { intl: { formatMessage } } = this.props;
    const reg = /^(https?):\/\/[\w\-]+(\.[\w\-]+)+([\w\-.,@?^=%&:\/~+#]*[\w\-@?^=%&\/~+#])?$/;
    if (value) {
      if (reg.test(value)) {
        callback();
      } else {
        callback(formatMessage({ id: 'formatError' }));
      }
    } else {
      callback();
    }
  };

  handleSubmit = e => {
    e.preventDefault();
    const {
      form: { validateFieldsAndScroll },
      store,
      isEditMode,
      id,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    const { type: currentType, objectVersionNumber } = store.getConfig;
    const type = currentType || this.state.type;

    this.setState({ submitting: true });
    validateFieldsAndScroll(async (err, values) => {
      store.setTestResult('');
      store.setTestLoading(true);
      if (!err) {
        try {
          const response = await store.checkRepoLinkRequest(projectId, values, type);
          const result = handleCheckerProptError(response);
          if (result) {
            store.setTestLoading(false);
            store.setTestResult('pass');
            const config = {
              ...values,
              id,
              objectVersionNumber,
            };
            const submitResponse = await store.submitConfig(projectId, config, isEditMode);
            this.setState({ submitting: false });
            const submitResult = handleProptError(submitResponse);
            if (submitResult) {
              this.handleClose(null, true, isEditMode);
            }
          } else {
            this.setState({ submitting: false });
            store.setTestLoading(false);
            store.setTestResult('failed');
          }
        } catch (e) {
          this.setState({ submitting: false });
          store.setTestLoading(false);
          Choerodon.handleResponseError(e);
        }
      } else {
        this.setState({ submitting: false });
        store.setTestLoading(false);
      }
    });
  };

  /**
   * 关闭侧边栏后，表格的刷新
   * @param e 点击取消按钮传入的复合事件
   * @param reload 是否重新刷新
   * @param isEdit 编辑or创建
   */
  handleClose = (e, reload = false, isEdit) => {
    this.props.onClose(reload, isEdit);
  };

  render() {
    const {
      form,
      visible,
      isEditMode,
      intl: { formatMessage },
      store: {
        getTestLoading,
        getTestResult,
        getConfig,
        getDetailLoading,
      },
      AppState: {
        currentMenuType: { name: projectName },
      },
    } = this.props;
    const { getFieldDecorator } = form;
    const { submitting, type, showLengthInfo } = this.state;
    const config = getConfig.config || {};

    const currentType = getConfig.type || type;

    const typeOption = _.map(REPO_TYPE, item => (
      <Option key={item}><FormattedMessage id={`elements.type.${item}`} /></Option>));

    return (
      <Sidebar
        destroyOnClose
        title={<FormattedMessage id={`elements.header.${isEditMode ? 'edit' : 'create'}`} />}
        visible={visible}
        footer={
          [<Button
            key="submit"
            type="primary"
            funcType="raised"
            onClick={this.handleSubmit}
            loading={submitting}
            disabled={getTestLoading}
          >
            <FormattedMessage id={isEditMode ? 'testAndSave' : 'testAndCreate'} />
          </Button>,
            <Button
              key="cancel"
              funcType="raised"
              onClick={this.handleClose}
              disabled={submitting}
            >
              <FormattedMessage id="cancel" />
            </Button>]
        }
      >
        <Content
          code="elements.create"
          values={{ name: projectName }}
          className="sidebar-content"
        >
          <div className={isEditMode && getDetailLoading ? 'c7ncd-sidebar-spin-blur' : ''}>
            <Form layout="vertical">
              <FormItem
                className="c7n-select_512"
                {...formItemLayout}
              >
                {getFieldDecorator('type', {
                  initialValue: currentType || 'chart',
                  rules: [{
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  }],
                })(
                  <Select
                    disabled={isEditMode}
                    className="c7n-select_512"
                    label={<FormattedMessage id="elements.type.form" />}
                    getPopupContainer={triggerNode => triggerNode.parentNode}
                    onChange={this.handleTypeChange}
                  >
                    {typeOption}
                  </Select>,
                )}
              </FormItem>
              <FormItem
                className="c7n-select_512"
                {...formItemLayout}
              >
                {getFieldDecorator('name', {
                  initialValue: getConfig.name,
                  rules: [{
                    required: true,
                    message: formatMessage({ id: 'required' }),
                  }, {
                    validator: this.checkName,
                  }, {
                    whitespace: true,
                    message: formatMessage({ id: 'nameCanNotBeEmpty' }),
                  }],
                })(
                  <Input
                    type="text"
                    maxLength={10}
                    showLengthInfo={showLengthInfo}
                    label={<FormattedMessage id="elements.name" />}
                    onChange={this.handleInputName}
                  />,
                )}
              </FormItem>
              <FormItem
                className="c7n-select_512"
                {...formItemLayout}
              >
                {getFieldDecorator('url', {
                  initialValue: config.url,
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: 'required' }),
                    },
                    {
                      validator: this.checkUrl,
                    },
                  ],
                })(
                  <Input
                    type="text"
                    label={<FormattedMessage id="elements.url" />}
                  />,
                )}
              </FormItem>
              {currentType === 'harbor' && <Fragment>
                <FormItem
                  className="c7n-select_512"
                  {...formItemLayout}
                >
                  {getFieldDecorator('userName', {
                    initialValue: config.userName,
                    rules: [
                      {
                        required: true,
                        message: formatMessage({ id: 'required' }),
                      },
                    ],
                  })(
                    <Input
                      type="text"
                      label={<FormattedMessage id="elements.user" />}
                    />,
                  )}
                </FormItem>
                <FormItem
                  className="c7n-select_512 c7ncd-elements-password"
                  {...formItemLayout}
                >
                  {getFieldDecorator('password', {
                    initialValue: config.password,
                    rules: [
                      {
                        required: true,
                        message: formatMessage({ id: 'required' }),
                      },
                    ],
                  })(
                    <Input
                      type="password"
                      showPasswordEye
                      label={<FormattedMessage id="elements.password" />}
                    />,
                  )}
                </FormItem>
                <FormItem
                  className="c7n-select_512"
                  {...formItemLayout}
                >
                  {getFieldDecorator('email', {
                    initialValue: config.email,
                    rules: [{
                      type: 'email',
                      message: formatMessage({ id: 'checkEmailError' }),
                    }, {
                      required: true,
                      message: formatMessage({ id: 'required' }),
                    }],
                  })(
                    <Input label={<FormattedMessage id="elements.email" />} />,
                  )}
                </FormItem>
                <FormItem
                  className="c7n-select_512"
                  {...formItemLayout}
                >
                  {getFieldDecorator('project', {
                    initialValue: config.project,
                    rules: [{
                      required: type,
                      message: formatMessage({ id: 'required' }),
                    }],
                  })(
                    <Input
                      type="text"
                      label={<FormattedMessage id="elements.project" />}
                      suffix={<Tips type="form" data="elements.project.help" />}
                    />,
                  )}
                </FormItem>
              </Fragment>}
            </Form>
            <div className="c7ncd-elements-test">
              <Button
                funcType="raised"
                className="c7ncd-elements-test-button"
                onClick={this.handleTestClick}
                loading={getTestLoading}
              >
                <FormattedMessage id="elements.test" />
              </Button>
              {getTestResult && <div className="c7ncd-elements-test-result">
                <Icon type={LINK_TEST_ICON[getTestResult]}
                      className={`c7ncd-elements-test-icon c7ncd-elements-test-icon_${getTestResult}`} />
                <FormattedMessage id={`elements.link.${getTestResult}`} />
              </div>}
            </div>
          </div>

          {isEditMode && getDetailLoading && <Spin className="c7ncd-sidebar-spin" />}
        </Content>
        <InterceptMask visible={submitting || getTestLoading || getDetailLoading} />
      </Sidebar>
    );
  }
}
