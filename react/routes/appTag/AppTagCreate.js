import React, { Component } from 'react/index';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Content, stores } from '@choerodon/boot';
import { Select, Modal, Form, Input, Icon } from 'choerodon-ui';
import _ from 'lodash';
import MdEditor from '../../components/MdEditor';
import Tips from "../../components/Tips/Tips";
import InterceptMask from "../../components/interceptMask/InterceptMask";

import '../main.scss';
import './style/AppTagCreate.scss';

const { AppState } = stores;
const { Option, OptGroup } = Select;
const { Sidebar } = Modal;
const { Item: FormItem } = Form;

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

@observer
class AppTagCreate extends Component {
  /**
   * 标记名称的校验规则：\d+(\.\d+){2}
   */
  checkTagName = _.debounce((rule, value, callback) => {
    const { store, intl: { formatMessage } } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const pa = /^\d+(\.\d+){2}$/;
    const SemanticVersion = /^\d+(\.\d+){2}-[a-zA-Z0-9]+(\.[a-zA-Z0-9]+)*$/;
    if (value && (pa.test(value) || SemanticVersion.test(value))) {
      store.checkTagName(projectId, value)
        .then((data) => {
          if (data) {
            callback();
          } else {
            callback(formatMessage({ id: 'apptag.checkName' }));
          }
        });
    } else {
      callback(formatMessage({ id: 'apptag.checkNameReg' }));
    }
  }, 1000);

  constructor(props) {
    super(props);
    this.state = {
      submitting: false,
      size: 3,
      release: '',
    };
  }

  componentDidMount() {
    const { store } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    store.queryBranchData({ projectId });
  }

  /**
   * 点击创建
   * @param e
   */
  handleOk = (e) => {
    e.preventDefault();
    const { store, form: { validateFieldsAndScroll } } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    this.setState({ submitting: true });
    validateFieldsAndScroll((err, data) => {
      if (!err) {
        const { tag, ref } = data;
        const { release } = this.state;
        store.createTag(projectId, tag, ref, release).then((req) => {
          if (req && req.failed) {
            Choerodon.prompt(data.message);
          } else {
            store.queryTagData(projectId, 1, 10);
            this.handleCancel();
          }
          this.setState({ submitting: false });
        }).catch((error) => {
          Choerodon.handleResponseError(error);
          this.setState({ submitting: false });
        });
      } else {
        this.setState({ submitting: false });
      }
    });
  };


  /**
   * 取消创建tag
   */
  handleCancel = () => {
    const { form: { resetFields }, close } = this.props;
    resetFields();
    close();
  };

  /**
   * 加载更多
   */
  changeSize =(e) => {
    e.stopPropagation();
    const { store } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { size, filter } = this.state;
    this.setState({ size: size + 10 });
    store.queryBranchData({ projectId, size: size + 10, postData: { searchParam: { branchName: [filter] }, param: '' } });
  };

  /**
   * 搜索分支数据
   * @param input
   */
  searchBranch = (input) => {
    this.setState({ filter: input });
    const { store } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    const { size } = this.state;
    store.queryBranchData({ projectId, size, postData: { searchParam: { branchName: [input] }, param: '' } });
  };

  /**
   * release note 内容变化
   * @param e
   */
  handleNoteChange = (e) => {
    this.setState({ release: e });
  };

  render() {
    const { intl: { formatMessage }, store, form: { getFieldDecorator }, show, app: name } = this.props;
    const { submitting, release } = this.state;
    const { list, total, size } = store.getBranchData;
    return (<Sidebar
      destroyOnClose
      title={<FormattedMessage id="apptag.create" />}
      visible={show}
      onOk={this.handleOk}
      okText={<FormattedMessage id="create" />}
      cancelText={<FormattedMessage id="cancel" />}
      confirmLoading={submitting}
      onCancel={this.handleCancel}
    >
      <Content code="apptag.create" values={{ name }} className="c7n-tag-create sidebar-content">
        <Form layout="vertical" className="c7n-sidebar-form">
          <div className="apptag-formitem">
            <Icon type="local_offer" className="c7n-apptag-icon" />
            <FormItem
              {...formItemLayout}
            >
              {getFieldDecorator('tag', {
                rules: [{
                  required: true,
                  whitespace: true,
                  message: formatMessage({ id: 'required' }),
                }, {
                  validator: this.checkTagName,
                }],
              })(
                <Input
                  autoFocus
                  label={<FormattedMessage id="apptag.name" />}
                  size="default"
                  suffix={<Tips type="form" data="apptag.name.tip" />}
                  maxLength="20"
                />,
              )}
            </FormItem>
          </div>
          <div className="apptag-formitem c7ncd-sidebar-select">
            <Icon type="wrap_text" className="c7n-apptag-icon" />
            <FormItem
              {...formItemLayout}
            >
              {getFieldDecorator('ref', {
                rules: [{
                  required: true,
                  message: formatMessage({ id: 'required' }),
                }],
              })(<Select
                  onFilterChange={this.searchBranch}
                  allowClear
                  label={<FormattedMessage id="apptag.ref" />}
                  filter
                  dropdownMatchSelectWidth
                  notFoundContent={<FormattedMessage id="apptag.noRefBranch" />}
                  size="default"
                  filterOption={false}
                >
                  <OptGroup label={<FormattedMessage id="apptag.branch" />}>
                    {
                      _.map(list, item => (<Option
                        key={item.branchName}
                        value={item.branchName}
                      >
                        <Icon className="apptag-branch-icon" type="branch" />{item.branchName}
                      </Option>))
                    }
                    {(total > size && size > 0) ? <Option key="more">
                      <div
                        role="none"
                        onClick={this.changeSize}
                        className="c7n-option-popover c7n-dom-more"
                      >{formatMessage({ id: 'loadMore' })}</div>
                    </Option> : null }
                  </OptGroup>
                </Select>
              )}
            </FormItem>
            <Tips type="form" data="apptag.tip" />
          </div>
        </Form>
        <div className="c7n-creation-title"><FormattedMessage id='apptag.release.title' /></div>
        <MdEditor
          value={release}
          onChange={this.handleNoteChange}
        />
        <InterceptMask visible={submitting} />
      </Content>
    </Sidebar>);
  }
}

export default Form.create({})(injectIntl(AppTagCreate));
