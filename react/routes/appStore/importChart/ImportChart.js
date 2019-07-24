import React, { Component } from "react/index";
import { observer, inject } from "mobx-react";
import { withRouter } from "react-router-dom";
import {
  Button,
  Select,
  Steps,
  Icon,
  Upload,
  Radio,
  Table,
} from "choerodon-ui";
import { injectIntl, FormattedMessage } from "react-intl";
import {
  Content,
  Header,
  Page,
  Permission,
  stores,
} from "@choerodon/boot";
import _ from "lodash";
import "../Importexport.scss";
import "../../main.scss";

const Step = Steps.Step;
const Option = Select.Option;
const Dragger = Upload.Dragger;
const RadioGroup = Radio.Group;

const { AppState } = stores;

@observer
class ImportChart extends Component {
  constructor(props) {
    super(props);
    this.state = {
      current: 1,
      publish: "否",
      level: "false",
      visible: false,
      fileList: false,
      defaultFileList: [],
      fileCode: false,
      upDown: [],
    };
  }

  /**
   * 选择是否发布
   * @param e
   */
  onChangePublish = e => {
    this.setState({
      publish: e.target.value,
    });
    if (e.target.value === "是") {
      this.setState({
        visible: true,
      });
    } else {
      this.setState({
        visible: false,
      });
    }
  };

  /**
   * 选择发布范围
   * @param e
   */
  onChangeLevel = e => {
    this.setState({
      level: e.target.value,
    });
  };

  /**
   * 获取步骤条状态
   * @param index
   * @returns {string}
   */
  getStatus = index => {
    const { current } = this.state;
    let status = "process";
    if (index === current) {
      status = "process";
    } else if (index > current) {
      status = "wait";
    } else {
      status = "finish";
    }
    return status;
  };

  /**
   * 改变步骤条
   * @param index
   */
  changeStep = index => {
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    const { AppStoreStore } = this.props;
    if (index === 2 && this.state.current === 1) {
      const formdata = new FormData();
      formdata.append("file", this.state.fileList);
      AppStoreStore.uploadChart(projectId, formdata).then(data => {
        if (!data.failed) {
          this.setState({
            current: index,
            fileCode: data.fileCode,
          });
        }
      });
    } else if (index === 1 && this.state.current === 2) {
      if (this.state.fileCode) {
        AppStoreStore.uploadCancel(projectId, this.state.fileCode).then(() => {
          this.setState({
            fileCode: false,
          });
        });
      }
      this.setState({ current: index });
    } else {
      this.setState({ current: index });
    }
  };

  /**
   * 展开/收起实例
   */
  handleChangeStatus = (id, length) => {
    const { upDown } = this.state;
    const cols = document.getElementsByClassName(`col-${id}`);
    if (_.indexOf(upDown, id) === -1) {
      for (let i = 0; i < cols.length; i += 1) {
        cols[i].style.height = `${Math.ceil(length / 4) * 31}px`;
      }
      upDown.push(id);
      this.setState({
        upDown,
      });
    } else {
      for (let i = 0; i < cols.length; i += 1) {
        cols[i].style.height = "31px";
      }
      _.pull(upDown, id);
      this.setState({
        upDown,
      });
    }
  };

  /**
   * 取消
   */
  handleBack = () => {
    const { AppStoreStore } = this.props;
    const projectName = AppState.currentMenuType.name;
    const projectId = AppState.currentMenuType.id;
    const organizationId = AppState.currentMenuType.organizationId;
    const type = AppState.currentMenuType.type;
    this.props.history.push(
      `/devops/app-market?type=${type}&id=${projectId}&name=${projectName}&organizationId=${organizationId}`
    );
    if (this.state.fileCode) {
      AppStoreStore.uploadCancel(projectId, this.state.fileCode).then(() => {
        this.setState({
          fileCode: false,
        });
      });
    }
  };

  importChart = fileCode => {
    const { publish, level } = this.state;
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    const { AppStoreStore, intl } = this.props;
    if (publish === "是") {
      AppStoreStore.importPublishStep(projectId, fileCode, level).then(data => {
        if (data === true) {
          Choerodon.prompt(intl.formatMessage({ id: "appstore.importSucc" }));
          this.handleBack();
        }
      });
    } else {
      AppStoreStore.importStep(projectId, fileCode).then(data => {
        if (data === true) {
          Choerodon.prompt(intl.formatMessage({ id: "appstore.importSucc" }));
          this.handleBack();
        }
      });
    }
  };

  /**
   * 渲染选择文件步骤
   */
  renderStepOne = () => {
    const projectId = parseInt(AppState.currentMenuType.id, 10);
    const { AppStoreStore, intl } = this.props;
    const { fileList, defaultFileList, fileCode } = this.state;
    const props = {
      name: "file",
      action: "//jsonplaceholder.typicode.com/posts/",
      multiple: false,
      disabled: Boolean(fileList),
      fileList: defaultFileList,
      onChange: info => {
        const status = info.file.status;
        if (status === "done") {
          Choerodon.prompt(`${info.file.name} file uploaded successfully.`);
          this.setState({
            defaultFileList: info.fileList,
          });
        } else if (status === "error") {
          this.setState({
            defaultFileList: info.fileList,
          });
        }
      },
      beforeUpload: file => {
        if (file.size > 1024 * 1024) {
          const tmp = file;
          tmp.status = "error";
          this.setState({ fileList: file });
          Choerodon.prompt(intl.formatMessage({ id: "appstore.fileSize" }));
          return false;
        } else if (file.name.slice(file.name.length - 3) !== "zip") {
          const tmp = file;
          tmp.status = "error";
          this.setState({ fileList: file });
          Choerodon.prompt(intl.formatMessage({ id: "appstore.fileType" }));
          return false;
        } else {
          const tmp = file;
          tmp.status = "done";
          this.setState({ fileList: file });
        }
        return false;
      },
      onRemove: () => {
        this.setState({
          fileList: false,
          defaultFileList: [],
        });
        if (fileCode) {
          AppStoreStore.uploadCancel(projectId, fileCode).then(() => {
            this.setState({
              fileCode: false,
            });
          });
        }
      },
    };
    return (
      <div className="c7n-step-section-wrap">
        <p>{intl.formatMessage({ id: "appstore.importStep1" })}</p>
        <div className="c7n-step-section-upload">
          <Dragger {...props}>
            <p className="c7n-upload-drag-icon">
              <Icon type="inbox" />
            </p>
            <p className="c7n-upload-text">
              {intl.formatMessage({ id: "appstore.importDesTitle" })}
            </p>
            <p className="c7n-upload-hint">
              {intl.formatMessage({ id: "appstore.importDesTip" })}
            </p>
          </Dragger>
        </div>
        <div className="c7n-step-section">
          <Button
            type="primary"
            funcType="raised"
            className="c7n-step-button"
            disabled={!(fileList.status === "done")}
            onClick={this.changeStep.bind(this, 2)}
          >
            {intl.formatMessage({ id: "next" })}
          </Button>
          <Button
            funcType="raised"
            className="c7n-step-clear"
            onClick={this.handleBack}
          >
            {intl.formatMessage({ id: "cancel" })}
          </Button>
        </div>
      </div>
    );
  };

  /**
   * 渲染选择文件步骤
   */
  renderStepTwo = () => {
    const { intl } = this.props;
    const { visible } = this.state;
    return (
      <div className="c7n-step-section-wrap">
        <p>{intl.formatMessage({ id: "appstore.importStep2" })}</p>
        <div className="c7n-step-section">
          <RadioGroup
            label={intl.formatMessage({ id: "appstore.ynRelease" })}
            onChange={this.onChangePublish}
            value={this.state.publish}
          >
            <Radio value="否" className="c7n-step-radio">
              {intl.formatMessage({ id: "appstore.N" })}
              <span>
                <Icon type="error" className="c7n-step-section-waring" />
                {intl.formatMessage({ id: "appstore.reTip" })}
              </span>
            </Radio>
            <Radio value="是" className="c7n-step-radio">
              {intl.formatMessage({ id: "appstore.Y" })}
            </Radio>
          </RadioGroup>
        </div>
        {visible && (
          <div className="c7n-step-section">
            <RadioGroup
              label={intl.formatMessage({ id: "release.column.level" })}
              onChange={this.onChangeLevel}
              value={this.state.level}
            >
              <Radio value="false" className="c7n-step-radio">
                {intl.formatMessage({ id: "organization" })}
              </Radio>
              <Radio value="true" className="c7n-step-radio">
                {intl.formatMessage({ id: "public" })}
              </Radio>
            </RadioGroup>
          </div>
        )}
        <div className="c7n-step-section">
          <Button
            type="primary"
            funcType="raised"
            className="c7n-step-button"
            onClick={this.changeStep.bind(this, 3)}
          >
            {intl.formatMessage({ id: "next" })}
          </Button>
          <Button
            funcType="raised"
            className="c7n-step-button"
            onClick={this.changeStep.bind(this, 1)}
          >
            {intl.formatMessage({ id: "previous" })}
          </Button>
          <Button
            funcType="raised"
            className="c7n-step-clear"
            onClick={this.handleBack}
          >
            {intl.formatMessage({ id: "cancel" })}
          </Button>
        </div>
      </div>
    );
  };

  /**
   * 渲染总览
   */
  renderStepThree = () => {
    const {
      AppStoreStore,
      intl: { formatMessage },
    } = this.props;
    const { upDown } = this.state;
    const data = AppStoreStore.getImpApp;
    const columns = [
      {
        title: <FormattedMessage id="app.name" />,
        dataIndex: "name",
        key: "name",
      },
      {
        title: <FormattedMessage id="app.code" />,
        dataIndex: "code",
        key: "code",
      },
      {
        title: formatMessage({ id: "app.version" }),
        key: "version",
        render: record => (
          <div>
            <div
              role="none"
              className={`c7n-step-table-column col-${record.id}`}
              onClick={this.handleChangeStatus.bind(
                this,
                record.id,
                record.appVersions.length
              )}
            >
              {record.appVersions &&
                document.getElementsByClassName(`${record.id}-col-parent`)[0] &&
                parseInt(
                  window.getComputedStyle(
                    document.getElementsByClassName(
                      `${record.id}-col-parent`
                    )[0]
                  ).height,
                  10
                ) > 31 && (
                  <span
                    className={
                      _.indexOf(upDown, record.id) !== -1
                        ? "icon icon-keyboard_arrow_up c7n-step-table-icon"
                        : "icon icon-keyboard_arrow_down c7n-step-table-icon"
                    }
                  />
                )}
              <div className={`${record.id}-col-parent`}>
                {_.map(record.appVersions, v => (
                  <div key={v.id} className="c7n-step-col-circle">
                    {v.version}
                  </div>
                ))}
              </div>
            </div>
          </div>
        ),
      },
    ];
    return (
      <div className="c7n-step-section-wrap">
        <p>
          <FormattedMessage id="appstore.importStep3" />
        </p>
        <div className="c7n-step-section">
          <p>
            <span>{formatMessage({ id: "appstore.ynRelease" })}：</span>
            <span>{this.state.publish}</span>
          </p>
          {this.state.visible && (
            <p>
              <span>{formatMessage({ id: "release.column.level" })}：</span>
              <span>
                {this.state.level === "false"
                  ? formatMessage({ id: "organization" })
                  : formatMessage({ id: "public" })}
              </span>
            </p>
          )}
          <Table
            filterBar={false}
            pagination={false}
            columns={columns}
            dataSource={data.appMarketList}
            rowKey={record => record.id}
          />
        </div>
        <div className="c7n-step-section">
          <Button
            type="primary"
            funcType="raised"
            className="c7n-step-button"
            disabled={!(this.state.fileList.status === "done")}
            onClick={this.importChart.bind(this, data.fileCode)}
          >
            {formatMessage({ id: "appstore.importApp" })}
          </Button>
          <Button
            funcType="raised"
            className="c7n-step-button"
            onClick={this.changeStep.bind(this, 2)}
          >
            {formatMessage({ id: "previous" })}
          </Button>
          <Button
            funcType="raised"
            className="c7n-step-clear"
            onClick={this.handleBack}
          >
            {formatMessage({ id: "cancel" })}
          </Button>
        </div>
      </div>
    );
  };

  render() {
    const {
      type,
      organizationId,
      name,
      id: projectId,
    } = AppState.currentMenuType;
    const {
      intl: { formatMessage },
    } = this.props;
    const { current } = this.state;

    return (
      <Page
        className="c7n-region"
        service={[
          "devops-service.application-market.uploadApps",
          "devops-service.application-market.deleteZip",
          "devops-service.application-market.importApps",
        ]}
      >
        <Header
          title={<FormattedMessage id="appstore.import.title" />}
          backPath={`/devops/app-market?type=${type}&id=${projectId}&name=${name}&organizationId=${organizationId}`}
        />
        <Content code="appstore.import" values={{ name }}>
          <div className="c7n-store-content-wrap">
            <div
              className="c7n-store-card-wrap"
              style={{ minHeight: window.innerHeight - 277 }}
            >
              <Steps current={current}>
                <Step
                  title={
                    <span className={current === 1 ? "c7n-step-active" : ""}>
                      {formatMessage({ id: "appstore.ChooseFile" })}
                    </span>
                  }
                  status={this.getStatus(1)}
                />
                <Step
                  className={
                    !this.state.defaultFileList.length
                      ? "c7n-step-disabled"
                      : ""
                  }
                  title={
                    <span className={current === 2 ? "c7n-step-active" : ""}>
                      {formatMessage({ id: "appstore.ynRelease" })}
                    </span>
                  }
                  status={this.getStatus(2)}
                />
                <Step
                  className={
                    !this.state.defaultFileList.length
                      ? "c7n-step-disabled"
                      : ""
                  }
                  title={
                    <span className={current === 3 ? "c7n-step-active" : ""}>
                      {formatMessage({ id: "appstore.confirm" })}
                    </span>
                  }
                  status={this.getStatus(3)}
                />
              </Steps>
              {current === 1 && this.renderStepOne()}
              {current === 2 && this.renderStepTwo()}
              {current === 3 && this.renderStepThree()}
            </div>
          </div>
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(ImportChart));
