import React, { Component, Fragment } from "react/index";
import { observer, inject } from "mobx-react";
import { withRouter } from "react-router-dom";
import {
  Button,
  Radio,
  Steps,
  Table,
  Tooltip,
  Form,
  Icon,
  Input,
} from "choerodon-ui";
import { injectIntl, FormattedMessage } from "react-intl";
import _ from "lodash";
import {
  Content,
  Header,
  Page,
  Permission,
  stores,
} from "@choerodon/boot";
import "../../../main.scss";
import "./index.scss";
import VersionTable from "./VersionTable";
import TimePopover from "../../../../components/timePopover";

const { TextArea } = Input;
const RadioGroup = Radio.Group;
const Step = Steps.Step;
const { AppState } = stores;

@observer
class AddAppRelease extends Component {
  constructor(props) {
    super(props);
    this.state = {
      appId: props.match.params.appId || undefined,
      current: props.match.params.appId ? 1 : 0,
      projectId: AppState.currentMenuType.id,
      mode: "organization",
    };
  }

  componentDidMount() {
    const { EditReleaseStore } = this.props;
    EditReleaseStore.loadAppTableData({ projectId: this.state.projectId });
    EditReleaseStore.loadAppDetail(this.state.projectId, this.state.appId);
    EditReleaseStore.setSelectData([]);
  }

  /**
   * 处理图片回显
   * @param img
   * @param callback
   */
  getBase64 = (img, callback) => {
    const reader = new FileReader();
    reader.addEventListener("load", () => callback(reader.result));
    reader.readAsDataURL(img);
  };

  /**
   * 改变步骤条
   * @param index
   */
  changeStep = index => {
    this.setState({ current: index });
  };

  handleChangeMode = value => {
    this.setState({ mode: value.target.value });
  };

  /**
   * 取消第一步
   */
  clearStepOne = () => {
    const projectName = AppState.currentMenuType.name;
    const projectId = AppState.currentMenuType.id;
    const type = AppState.currentMenuType.type;
    this.props.history.push(
      `/devops/app-release/1?type=${type}&id=${projectId}&name=${projectName}&organizationId=${
        AppState.currentMenuType.organizationId
      }`
    );
  };

  handleSubmit = e => {
    e.preventDefault();
    const { EditReleaseStore } = this.props;
    const selectData = EditReleaseStore.getSelectData;
    const {
      projectId,
      id,
      img,
      category,
      appId,
      contributor,
      description,
      mode,
    } = this.state;
    const postData = {
      appId,
      appVersions: EditReleaseStore.selectData.slice(),
      category,
      contributor,
      description,
      publishLevel: mode,
    };
    postData.imgUrl = img;
    postData.appVersions = selectData;
    if (!id) {
      this.setState({ submitting: true });
      EditReleaseStore.addData(projectId, postData)
        .then(datass => {
          this.setState({ submitting: false });
          if (datass) {
            const projectName = AppState.currentMenuType.name;
            const type = AppState.currentMenuType.type;
            EditReleaseStore.setSelectData([]);
            this.props.history.push(
              `/devops/app-release/2?type=${type}&id=${projectId}&name=${projectName}&organizationId=${
                AppState.currentMenuType.organizationId
              }`
            );
          }
        })
        .catch(err => {
          this.setState({ submitting: false });
          Choerodon.prompt(err.response.data.message);
        });
    }
  };

  /**
   * 图标的上传button显示
   */
  showBth = () => {
    this.setState({ showBtn: true });
  };

  /**
   * 图标的上传button隐藏
   */
  hideBth = () => {
    this.setState({ showBtn: false });
  };

  /**
   * 触发上传按钮
   */
  triggerFileBtn = () => {
    this.setState({ showBtn: true });
    const ele = document.getElementById("file");
    ele.click();
  };

  /**
   * 选择文件
   * @param e
   */
  selectFile = e => {
    const { EditReleaseStore } = this.props;
    const formdata = new FormData();
    const img = e.target.files[0];
    formdata.append("file", e.target.files[0]);
    EditReleaseStore.uploadFile(
      "devops-service",
      img.name.split(".")[0],
      formdata
    ).then(data => {
      if (data) {
        this.setState({ img: data });
        this.getBase64(formdata.get("file"), imgUrl => {
          const ele = document.getElementById("img");
          ele.style.backgroundImage = `url(${imgUrl})`;
          this.setState({ imgback: imgUrl });
        });
      }
    });
    this.setState({ showBtn: false });
  };

  /**
   * 发布应用的描述信息
   */
  handleDescribeChange = value => {
    this.setState({ description: value.target.value });
  };

  /**
   * 选择要发布的应用
   * @param record
   */
  hanldeSelectApp = record => {
    const { EditReleaseStore } = this.props;
    EditReleaseStore.setSelectData([]);
    if (this.state.appId && this.state.appId === record.id) {
      EditReleaseStore.setAppDetailById(null);
      this.setState({ appId: "" });
    } else {
      EditReleaseStore.setAppDetailById(record);
      this.setState({ appId: record.id });
    }
  };

  /**
   * table app表格搜索
   * @param pagination 分页
   * @param filters 过滤
   * @param sorter 排序
   */
  appTableChange = (pagination, filters, sorter, paras) => {
    const { EditReleaseStore } = this.props;
    const { projectId } = AppState.currentMenuType;

    const sort = { field: "id", order: "desc" };
    if (sorter.column) {
      sort.field = sorter.field || sorter.columnKey;
      if (sorter.order === "ascend") {
        sort.order = "asc";
      } else if (sorter.order === "descend") {
        sort.order = "desc";
      }
    }

    let searchParam = {};
    const page = pagination.current;
    if (Object.keys(filters).length) {
      searchParam = filters;
    }

    const postData = {
      searchParam,
      param: paras.toString(),
    };

    EditReleaseStore.loadAppTableData({
      projectId,
      sort,
      postData,
      page,
      size: pagination.pageSize,
    });
  };

  /**
   * 渲染第一步
   */
  handleRenderApp = () => {
    const {
      EditReleaseStore: { getAppTableData, getAppDetailById: app, getPageInfo },
      intl: { formatMessage },
    } = this.props;

    const { appId } = this.state;

    const column = [
      {
        key: "check",
        width: "50px",
        render: record =>
          app &&
          record.id === app.id && <i className="icon icon-check icon-select" />,
      },
      {
        title: <FormattedMessage id="app.name" />,
        dataIndex: "name",
        key: "name",
        sorter: true,
        filters: [],
      },
      {
        title: <FormattedMessage id="app.code" />,
        dataIndex: "code",
        key: "code",
        sorter: true,
        filters: [],
      },
    ];
    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: "release.add.step.one.description" })}
        </p>
        <div className="c7ncd-step-item">
          <Table
            rowClassName="col-check"
            filterBarPlaceholder={formatMessage({ id: "filter" })}
            onRow={record => ({
              onClick: this.hanldeSelectApp.bind(this, record),
            })}
            pagination={getPageInfo}
            columns={column}
            dataSource={getAppTableData}
            rowKey={record => record.id}
            onChange={this.appTableChange}
          />
        </div>
        <div className="c7ncd-step-btn">
          <Button
            type="primary"
            funcType="raised"
            disabled={!appId}
            onClick={() => this.changeStep(1)}
          >
            {formatMessage({ id: "next" })}
          </Button>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.clearStepOne}
          >
            {formatMessage({ id: "cancel" })}
          </Button>
        </div>
      </Fragment>
    );
  };

  /**
   * 渲染第二步
   */
  handleRenderEnv = () => {
    const {
      EditReleaseStore,
      intl: { formatMessage },
    } = this.props;
    const data = EditReleaseStore.selectData;
    const columns = [
      {
        title: <FormattedMessage id="deploy.ver" />,
        dataIndex: "version",
      },
      {
        title: <FormattedMessage id="app.createTime" />,
        render: (text, record) => <TimePopover content={record.creationDate} />,
      },
      {
        width: 64,
        key: "action",
        render: record => (
          <div>
            <Tooltip
              trigger="hover"
              placement="bottom"
              content={<div>{formatMessage({ id: "delete" })}</div>}
            >
              <Button
                shape="circle"
                funcType="flat"
                onClick={this.removeVersion.bind(this, record.id)}
                icon="delete"
              />
            </Tooltip>
          </div>
        ),
      },
    ];
    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: "release.add.step.two.description" })}
        </p>
        <div className="c7ncd-step-item">
          <Permission
            service={["devops-service.application-version.pageByOptions"]}
          >
            <Button
              style={{ color: "rgb(63, 81, 181)" }}
              funcType="raised"
              onClick={this.handleAddVersion}
              icon="add"
            >
              {formatMessage({
                id: "release.add.step.two.btn.add",
              })}
            </Button>
          </Permission>
        </div>
        <div className="c7ncd-step-item">
          <Table
            columns={columns}
            dataSource={data.slice() || []}
            pagination={data.pageInfo}
            rowKey={record => record.id}
          />
        </div>
        <div className="c7ncd-step-btn">
          <Button
            type="primary"
            funcType="raised"
            onClick={this.changeStep.bind(this, 2)}
            disabled={!data.length}
          >
            {formatMessage({ id: "next" })}
          </Button>
          <Button
            className="c7ncd-step-cancel-btn"
            onClick={this.changeStep.bind(this, 0)}
            funcType="raised"
          >
            {formatMessage({ id: "previous" })}
          </Button>
          <Button
            className="c7ncd-step-cancel-btn"
            funcType="raised"
            onClick={this.clearStepOne}
          >
            {formatMessage({ id: "cancel" })}
          </Button>
        </div>
      </Fragment>
    );
  };

  /**
   * 渲染第三步
   * @returns {*}
   */
  handleRenderMode = () => {
    const {
      intl: { formatMessage },
    } = this.props;
    const radioStyle = {
      display: "block",
      height: "30px",
      lineHeight: "30px",
    };
    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: "release.add.step.three.description" })}
        </p>
        <div className="c7ncd-step-item c7ncd-release-item-indent">
          <RadioGroup
            onChange={this.handleChangeMode}
            value={this.state.mode}
            label={
              <span className="deploy-text">
                {formatMessage({
                  id: "release.add.step.three.title",
                })}
              </span>
            }
          >
            <Radio style={radioStyle} value="organization">
              {formatMessage({ id: "organization" })}
            </Radio>
            <Radio style={radioStyle} value="public">
              {formatMessage({ id: "public" })}
            </Radio>
          </RadioGroup>
          <div className="c7ncd-step-item-header c7ncd-release-tip">
            <Icon className="c7ncd-step-item-tip-icon" type="error" />
            <span className="c7ncd-step-item-tip-text">
              {formatMessage({ id: "release.add.step.three.tooltip" })}
            </span>
          </div>
        </div>
        <div className="c7ncd-step-btn">
          <Button
            type="primary"
            funcType="raised"
            onClick={this.changeStep.bind(this, 3)}
          >
            {formatMessage({ id: "next" })}
          </Button>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.changeStep.bind(this, 1)}
          >
            {formatMessage({ id: "previous" })}
          </Button>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.clearStepOne}
          >
            {formatMessage({ id: "cancel" })}
          </Button>
        </div>
      </Fragment>
    );
  };

  /**
   * 渲染第四步
   * @returns {*}
   */
  handleRenderDescription = () => {
    const {
      intl: { formatMessage },
    } = this.props;
    const {
      description,
      category,
      contributor,
      imgback,
      showBtn,
    } = this.state;
    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: "release.add.step.four.description" })}
        </p>
        <div className="c7ncd-release-info">
          <div className="c7ncd-step-item">
            <div className="c7n-appRelease-img">
              <div
                style={{
                  backgroundImage: imgback ? `url(${imgback})` : "",
                }}
                className="c7n-appRelease-img-hover"
                id="img"
                onMouseLeave={this.hideBth}
                onMouseEnter={this.showBth}
                onClick={this.triggerFileBtn}
                role="none"
              >
                {showBtn && (<div className="c7n-appRelease-img-child">
                  <Icon type="photo_camera" />
                </div>)}
                <Input
                  id="file"
                  type="file"
                  onChange={this.selectFile}
                  style={{ display: "none" }}
                />
              </div>
              <span className="c7n-appRelease-img-title">
                {formatMessage({
                  id: "release.add.step.four.app.icon",
                })}
              </span>
            </div>
          </div>
          <div className="c7ncd-step-item">
            <Input
              value={contributor}
              onChange={value => {
                this.setState({ contributor: value.target.value });
              }}
              style={{ width: 512 }}
              maxLength={30}
              label={
                <span className="apprelease-formItem-label">
                  <FormattedMessage id="appstore.contributor" />
                </span>
              }
              size="default"
            />
          </div>
          <div className="c7ncd-step-item">
            <Input
              value={category}
              style={{ width: 512 }}
              onChange={value => {
                this.setState({ category: value.target.value });
              }}
              maxLength={10}
              label={
                <span className="apprelease-formItem-label">
                  <FormattedMessage id="appstore.category" />
                </span>
              }
              size="default"
            />
          </div>
          <div className="c7ncd-step-item">
            <TextArea
              value={description}
              onChange={this.handleDescribeChange}
              style={{ width: 512 }}
              maxLength={100}
              label={
                <span className="apprelease-formItem-label">
                  <FormattedMessage id="appstore.description.label" />
                </span>
              }
              autosize={{ minRows: 2, maxRows: 6 }}
            />
          </div>
        </div>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-tip-icon" type="error" />
            <span className="c7ncd-step-item-tip-text">
              {formatMessage({ id: "release.add.step.four.tooltip" })}
            </span>
          </div>
        </div>
        <div className="c7ncd-step-btn">
          <Button
            type="primary"
            funcType="raised"
            disabled={!(category && contributor && description)}
            onClick={this.changeStep.bind(this, 4)}
          >
            {formatMessage({ id: "next" })}
          </Button>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.changeStep.bind(this, 2)}
          >
            {formatMessage({ id: "previous" })}
          </Button>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.clearStepOne}
          >
            {formatMessage({ id: "cancel" })}
          </Button>
        </div>
      </Fragment>
    );
  };

  /**
   * 渲染第五步
   * @returns {*}
   */
  handleRenderReview = () => {
    const {
      EditReleaseStore,
      intl: { formatMessage },
    } = this.props;
    return (
      <Fragment>
        <p className="c7ncd-step-describe">
          {formatMessage({ id: "release.add.step.five.description" })}
        </p>
        <div className="c7ncd-step-item">
          <div>
            <div className="app-release-title">
              {formatMessage({ id: "network.form.app" })}：
            </div>
            <div className="deployApp-text">
              {EditReleaseStore.getAppDetailById &&
                EditReleaseStore.getAppDetailById.name}
            </div>
          </div>
          <div>
            <div className="app-release-title">
              {formatMessage({ id: "deploy.step.one.version" })}：
            </div>
            <div className="deployApp-text">
              {EditReleaseStore.selectData.length &&
                EditReleaseStore.selectData.map(v => (
                  <div key={v.id}>{v.version}</div>
                ))}
            </div>
          </div>
          <div>
            <div className="app-release-title">
              {formatMessage({ id: "appstore.contributor" })}：
            </div>
            <div className="deployApp-text">{this.state.contributor}</div>
          </div>
          <div>
            <div className="app-release-title">
              {formatMessage({ id: "appstore.category" })}：
            </div>
            <div className="deployApp-text">{this.state.category}</div>
          </div>
          <div>
            <div className="app-release-title">
              {formatMessage({
                id: "appstore.description.label",
              })}
              ：
            </div>
            <div className="deployApp-text">{this.state.description}</div>
          </div>
          <div>
            <div className="app-release-title">
              {formatMessage({ id: "release.column.level" })}：
            </div>
            <div className="deployApp-text">
              {formatMessage({ id: this.state.mode })}
            </div>
          </div>
        </div>
        <div className="c7ncd-step-item">
          <div className="c7ncd-step-item-header">
            <Icon className="c7ncd-step-item-tip-icon" type="error" />
            <span className="c7ncd-step-item-tip-text">
              {formatMessage({ id: "release.add.step.five.tooltip" })}
            </span>
          </div>
        </div>

        <div className="c7ncd-step-btn">
          <Permission service={["devops-service.application-market.create"]}>
            <Button
              type="primary"
              loading={this.state.submitting}
              funcType="raised"
              onClick={this.handleSubmit}
            >
              {formatMessage({
                id: "release.add.step.five.btn.confirm",
              })}
            </Button>
          </Permission>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.changeStep.bind(this, 3)}
          >
            {formatMessage({ id: "previous" })}
          </Button>
          <Button
            funcType="raised"
            className="c7ncd-step-cancel-btn"
            onClick={this.clearStepOne}
          >
            {formatMessage({ id: "cancel" })}
          </Button>
        </div>
      </Fragment>
    );
  };

  handleAddVersion = () => {
    const { EditReleaseStore } = this.props;
    EditReleaseStore.changeShow(true);
  };

  removeVersion = id => {
    const { EditReleaseStore } = this.props;
    const data = _.cloneDeep(EditReleaseStore.selectData.slice());
    _.remove(data, app => app.id === id);
    EditReleaseStore.setSelectData(data);
  };

  render() {
    const { EditReleaseStore } = this.props;
    const data = EditReleaseStore.selectData;
    const projectName = AppState.currentMenuType.name;
    const { formatMessage } = this.props.intl;
    const { id, type } = AppState.currentMenuType;
    const {
      appId,
      appName,
      current,
    } = this.state;

    const STEP_LIST = ["one", "two", "three", "four", "five"];
    const STEP_LIST_DOM = [
      this.handleRenderApp,
      this.handleRenderEnv,
      this.handleRenderMode,
      this.handleRenderDescription,
      this.handleRenderReview,
    ];

    const stepNodes = _.map(STEP_LIST, item => (
      <Step title={formatMessage({ id: `release.add.step.${item}.title` })} />
    ));

    return (
      <Page
        service={[
          "devops-service.application.listByActiveAndPubAndVersion",
          "devops-service.application.queryByAppId",
          "devops-service.application-market.create",
          "devops-service.application-version.pageByOptions",
        ]}
        className="c7n-region"
      >
        <Header
          title={<FormattedMessage id="release.home.header.title" />}
          backPath={`/devops/app-release/1?type=${type}&id=${id}&name=${projectName}&organizationId=${
            AppState.currentMenuType.organizationId
          }`}
        />
        <Content
          className="c7n-addRelease-wrapper c7ncd-step-page"
          code="release.add"
          values={{ name: projectName }}
        >
          <div className="c7ncd-step-wrap">
            <Steps className="c7ncd-step-bar" current={current}>
              {stepNodes}
            </Steps>
            <div className="c7ncd-step-card">{STEP_LIST_DOM[current]()}</div>
          </div>
          {EditReleaseStore.show && (
            <VersionTable
              show={EditReleaseStore.show}
              appName={appName}
              appId={appId}
              store={EditReleaseStore}
            />
          )}
        </Content>
      </Page>
    );
  }
}

export default Form.create({})(withRouter(injectIntl(AddAppRelease)));
