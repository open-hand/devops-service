import React, { Component } from "react/index";
import { withRouter } from "react-router-dom";
import { observer } from "mobx-react";
import { Modal, Form, Radio, Input, Select, Tooltip } from "choerodon-ui";
import { Content, stores } from "@choerodon/boot";
import { injectIntl, FormattedMessage } from "react-intl";
import _ from "lodash";
import "../../main.scss";
import "./BranchCreate.scss";
import "../index.scss";
import MouserOverWrapper from "../../../../components/MouseOverWrapper";
import DevPipelineStore from "../../../../stores/project/devPipeline";
import DevConsoleStore from "../../../../stores/project/devConsole";
import InterceptMask from "../../../../components/interceptMask/InterceptMask";

const { AppState } = stores;
const Sidebar = Modal.Sidebar;
const { Option, OptGroup } = Select;
const FormItem = Form.Item;
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
class BranchCreate extends Component {
  constructor(props) {
    const menu = AppState.currentMenuType;
    super(props);
    this.state = {
      projectId: menu.id,
      submitting: false,
      type: "custom",
      branchSize: 3,
      tagSize: 3,
      filter: false,
    };
  }

  /**
   * 获取issue的options
   * @param s
   * @returns {*}
   */
  getOptionContent = s => {
    const { formatMessage } = this.props.intl;
    let mes = "";
    let icon = "";
    let color = "";
    switch (s.typeCode) {
      case "story":
        mes = formatMessage({ id: "branch.issue.story" });
        icon = "agile_story";
        color = "#00bfa5";
        break;
      case "bug":
        mes = formatMessage({ id: "branch.issue.bug" });
        icon = "agile_fault";
        color = "#f44336";
        break;
      case "issue_epic":
        mes = formatMessage({ id: "branch.issue.epic" });
        icon = "agile_epic";
        color = "#743be7";
        break;
      case "sub_task":
        mes = formatMessage({ id: "branch.issue.subtask" });
        icon = "agile_subtask";
        color = "#4d90fe";
        break;
      default:
        mes = formatMessage({ id: "branch.issue.task" });
        icon = "agile_task";
        color = "#4d90fe";
    }
    return (
      <span>
        <Tooltip title={mes}>
          <div style={{ color }} className="branch-issue">
            <i className={`icon icon-${icon}`} />
          </div>
        </Tooltip>
        <Tooltip title={s.summary}>
          <span className="branch-issue-content">
            <span style={{ color: "rgb(0,0,0,0.65)" }}>{s.issueNum}</span>
            <MouserOverWrapper
              style={{ display: "inline-block", verticalAlign: "sub" }}
              width="350px"
              text={s.summary}
            >
              {s.summary}
            </MouserOverWrapper>
          </span>
        </Tooltip>
      </span>
    );
  };

  /**
   * 获取列表的icon
   * @param type 分支类型
   * @returns {*}
   */
  getIcon = type => {
    let icon;
    switch (type) {
      case "feature":
        icon = <span className="c7n-branch-icon icon-feature">F</span>;
        break;
      case "bugfix":
        icon = <span className="c7n-branch-icon icon-develop">B</span>;
        break;
      case "hotfix":
        icon = <span className="c7n-branch-icon icon-hotfix">H</span>;
        break;
      case "master":
        icon = <span className="c7n-branch-icon icon-master">M</span>;
        break;
      case "release":
        icon = <span className="c7n-branch-icon icon-release">R</span>;
        break;
      default:
        icon = <span className="c7n-branch-icon icon-custom">C</span>;
    }
    return icon;
  };

  /**
   * 提交分支数据
   * @param e
   */
  handleOk = e => {
    e.preventDefault();
    const { store, isDevConsole } = this.props;
    const appId = DevPipelineStore.selectedApp;
    const { projectId, type } = this.state;
    this.props.form.validateFieldsAndScroll((err, data) => {
      if (!err) {
        const postData = data;
        postData.branchName =
          type && type !== "custom"
            ? `${type}-${data.branchName}`
            : data.branchName;
        this.setState({ submitting: true });
        store
          .createBranch(projectId, appId, postData)
          .then(() => {
            store.loadBranchList({ projectId, appId: this.props.appId });
            if (isDevConsole) {
              DevConsoleStore.loadBranchList(projectId, appId);
            }
            this.props.onClose();
            this.props.form.resetFields();
            this.setState({ submitting: false });
          })
          .catch(error => {
            Choerodon.handleResponseError(error);
            this.setState({ submitting: false });
          });
      }
    });
  };

  /**
   * 触发分支名称检查
   */
  triggerNameCheck = () => {
    const { getFieldValue, validateFields } = this.props.form;
    const value = getFieldValue("branchName");
    if (value) {
      validateFields(["branchName"], { force: true });
    }
  };

  /**
   * 验证分支名的正则
   * @param rule
   * @param value
   * @param callback
   */
  checkName = _.debounce((rule, value, callback) => {
    const endWith = /(\/|\.|\.lock)$/;
    const contain = /(\s|~|\^|:|\?|\*|\[|\\|\.\.|@\{|\/{2,}){1}/;
    const single = /^@+$/;
    const { intl } = this.props;
    if (endWith.test(value)) {
      callback(intl.formatMessage({ id: "branch.checkNameEnd" }));
    } else if (contain.test(value) || single.test(value)) {
      callback(intl.formatMessage({ id: "branch.check" }));
    } else {
      const { appId, store } = this.props;
      const { projectId, type } = this.state;
      const name = `${type === "custom" ? "" : `${type}-`}${value}`;
      store.checkName(projectId, appId, name).then(data => {
        if (data && data.failed) {
          callback(intl.formatMessage({ id: "branch.check.existence" }));
        } else {
          callback();
        }
      });
    }
  }, 600);

  /**
   * 关闭弹框
   */
  handleClose = () => {
    this.props.form.resetFields();
    this.props.onClose(false);
  };

  /**
   * 切换分支类型
   * @param value
   */
  changeType = value => {
    this.setState({ type: value }, () => this.triggerNameCheck());
  };

  /**
   * 切换issue
   * @param value
   * @param options
   */
  changeIssue = (value, options) => {
    const key = options.key;
    const {
      store,
      form: { setFieldsValue },
    } = this.props;
    const issue = store.issue.slice();
    const issueDto = _.filter(issue, i => i.issueId === value)[0];
    let type = "";
    switch (key) {
      case "story":
        type = "feature";
        break;
      case "bug":
        type = "bugfix";
        break;
      case "issue_epic":
        type = "custom";
        break;
      case "sub_task":
        type = "feature";
        break;
      case "task":
        type = "feature";
        break;
      default:
        type = "custom";
    }
    this.setState({ type });
    setFieldsValue({ type, branchName: issueDto ? issueDto.issueNum : '' });
    this.triggerNameCheck()
  };

  /**
   * 搜索issue
   * @param input
   * @param options
   */
  searchIssue = (input, options) => {
    const { store } = this.props;
    if (input !== "") {
      store.loadIssue(this.state.projectId, input, false);
    } else {
      store.loadIssue(this.state.projectId, "", true);
    }
  };

  /**
   * 改变长度
   * @param type
   */
  changeSize = (type, e) => {
    e.stopPropagation();
    const { branchSize, tagSize } = this.state;
    const { store } = this.props;
    if (type === "branch") {
      this.setState({ branchSize: branchSize + 10 });
      store.loadBranchData({
        projectId: this.state.projectId,
        size: branchSize + 10,
        postData: {
          searchParam: { branchName: [this.state.filter] },
          param: "",
        },
      });
    } else {
      this.setState({ tagSize: tagSize + 10 });
      store.loadTagData(this.state.projectId, 1, tagSize + 10, {
        searchParam: { tagName: [this.state.filter] },
        param: "",
      });
    }
  };

  /**
   * 搜索分支数据
   */
  searchData = input => {
    const { store } = this.props;
    const { branchSize, tagSize } = this.state;
    this.setState({ filter: input });
    store.loadBranchData({
      projectId: this.state.projectId,
      size: branchSize,
      postData: { searchParam: { branchName: [input] }, param: "" },
    });
    store.loadTagData(this.state.projectId, 1, tagSize, {
      searchParam: { tagName: [input] },
      param: "",
    });
  };

  render() {
    const {
      visible,
      intl,
      store,
      form: { getFieldDecorator },
      name,
    } = this.props;
    const issue = store.issue.slice();
    const branches = store.branchData;
    const tags = store.tagData;
    return (
      <Sidebar
        title={<FormattedMessage id="branch.create" />}
        visible={visible}
        onOk={this.handleOk}
        onCancel={this.handleClose}
        okText={<FormattedMessage id="create" />}
        cancelText={<FormattedMessage id="cancel" />}
        confirmLoading={this.state.submitting}
      >
        <Content
          code="branch.create"
          values={{ name }}
          className="sidebar-content c7n-createBranch"
        >
          <Form
            layout="vertical"
            onSubmit={this.handleOk}
            className="c7n-sidebar-form"
          >
            <FormItem className="branch-formItem" {...formItemLayout}>
              {getFieldDecorator("issueId")(
                <Select
                  dropdownClassName="createBranch-dropdown"
                  onFilterChange={this.searchIssue}
                  loading={store.issueLoading}
                  onSelect={this.changeIssue}
                  key="service"
                  allowClear
                  label={<FormattedMessage id="branch.issueName" />}
                  filter
                  dropdownMatchSelectWidth
                  size="default"
                  optionFilterProp="children"
                  filterOption={false}
                >
                  {issue.map(s => (
                    <Option value={s.issueId} key={s.typeCode}>
                      {this.getOptionContent(s)}
                    </Option>
                  ))}
                </Select>
              )}
            </FormItem>
            <FormItem className="branch-formItem" {...formItemLayout}>
              {getFieldDecorator("originBranch", {
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: intl.formatMessage({ id: "required" }),
                  },
                ],
                initialValue: this.state.originBranch,
              })(
                <Select
                  key="service"
                  allowClear
                  label={<FormattedMessage id="branch.source" />}
                  filter
                  onFilterChange={this.searchData}
                  size="default"
                  filterOption={false}
                >
                  <OptGroup
                    label={intl.formatMessage({ id: "branch.branch" })}
                    key="proGroup"
                  >
                    {branches.list.map(s => (
                      <Option value={s.branchName} key={s.branchName}>
                        <i className="icon icon-branch c7n-branch-formItem-icon" />
                        {s.branchName}
                      </Option>
                    ))}
                    {branches.total > branches.size &&
                    branches.size > 0 ? (
                      <Option key="more">
                        <div
                          role="none"
                          onClick={this.changeSize.bind(this, "branch")}
                          className="c7n-option-popover c7n-dom-more"
                        >
                          {intl.formatMessage({ id: "loadMore" })}
                        </div>
                      </Option>
                    ) : null}
                  </OptGroup>
                  <OptGroup
                    label={intl.formatMessage({ id: "branch.tag" })}
                    key="more"
                  >
                    {tags.list.map(s => (
                      <Option value={s.tagName} key={s.tagName}>
                        <i className="icon icon-local_offer c7n-branch-formItem-icon" />
                        {s.tagName}
                      </Option>
                    ))}
                    {tags.total > tags.size &&
                    tags.size > 0 ? (
                      <Option value="more">
                        <div
                          role="none"
                          onClick={this.changeSize.bind(this, "tag")}
                          className="c7n-option-popover c7n-dom-more"
                        >
                          {intl.formatMessage({ id: "loadMore" })}
                        </div>
                      </Option>
                    ) : null}
                  </OptGroup>
                </Select>
              )}
            </FormItem>
            <FormItem className="c7n-formItem_180" {...formItemLayout}>
              {getFieldDecorator("type", {
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: intl.formatMessage({ id: "required" }),
                  },
                ],
                initialValue: this.state.type,
              })(
                <Select
                  onChange={this.changeType}
                  key="service"
                  label={<FormattedMessage id="branch.type" />}
                  dropdownMatchSelectWidth
                  onSelect={this.selectTemplate}
                  size="default"
                >
                  {["feature", "bugfix", "release", "hotfix", "custom"].map(
                    s => (
                      <Option value={s} key={s}>
                        {this.getIcon(s)}
                        <span className="c7n-branch-text">{s}</span>
                      </Option>
                    )
                  )}
                </Select>
              )}
            </FormItem>
            <FormItem className="c7n-formItem_281" {...formItemLayout}>
              {getFieldDecorator("branchName", {
                rules: [
                  {
                    required: true,
                    whitespace: true,
                    message: intl.formatMessage({ id: "required" }),
                  },
                  {
                    validator: this.checkName,
                  },
                ],
              })(
                <Input
                  maxLength={50}
                  label={<FormattedMessage id="branch.name" />}
                  prefix={`${
                    this.state.type === "custom" ? "" : `${this.state.type}-`
                  }`}
                />
              )}
            </FormItem>
          </Form>
          <InterceptMask visible={this.state.submitting} />
        </Content>
      </Sidebar>
    );
  }
}
export default Form.create({})(withRouter(injectIntl(BranchCreate)));
