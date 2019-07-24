import React, { Component, Fragment } from "react/index";
import { observer, inject } from "mobx-react";
import { withRouter } from "react-router-dom";
import { Button, Select, Steps, Table, Input } from "choerodon-ui";
import { injectIntl, FormattedMessage } from "react-intl";
import FileDownload from "js-file-download";
import {
  Content,
  Header,
  message,
  Page,
  Permission,
  stores,
} from "@choerodon/boot";
import _ from "lodash";
import MouserOverWrapper from "../../../components/MouseOverWrapper";
import "../Importexport.scss";
import "../../main.scss";
import exportChartStore from "../../../stores/project/appStore/exportChart/ExportChartStore";
import { handleProptError } from "../../../utils";

const Option = Select.Option;
const Step = Steps.Step;

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

const { AppState } = stores;

@observer
class ExportChart extends Component {
  constructor(props) {
    super(props);
    this.state = {
      current: 0,
      projectId: AppState.currentMenuType.id,
      0: { versions: [] },
      upDown: [],
      selectedRows: [],
      versionDate: {},
      selectedVersion: {},
      exportName: "chart",
    };
  }

  componentDidMount() {
    const { ExportChartStore } = this.props;
    ExportChartStore.loadApps({ projectId: this.state.projectId });
  }

  /**
   * 改变步骤条
   * @param index
   */
  changeStep = index => {
    this.setState({ current: index });
  };

  /**
   * 选择应用部分的表格修改
   * @param pagination 分页
   * @param filters 过滤
   * @param sorter 排序
   */
  appTableChange = (pagination, filters, sorter, paras) => {
    const { ExportChartStore } = this.props;
    const { projectId } = AppState.currentMenuType;
    const { current, pageSize: size } = pagination;

    const page = current;
    const search = {
      searchParam: Object.keys(filters).length ? filters : {},
      param: paras.toString(),
    };

    ExportChartStore.loadApps({
      projectId,
      search,
      page,
      size,
    });
  };

  /**
   * 加载应用版本
   * @param id 应用id
   * @param index 索引号
   */
  loadVersion = id => {
    const { ExportChartStore } = this.props;
    const { projectId } = AppState.currentMenuType;
    this.setState({ isLoading: true });
    ExportChartStore.loadVersionsByAppId(id, projectId).then(data => {
      this.setState({ isLoading: false });
      const res = handleProptError(data);
      if (res) {
        const { versionDate } = this.state;
        versionDate[id] = _.reverse(res);
        this.setState({ versionDate });
      }
    });
  };

  /**
   * 选择版本
   * @param value
   * @param id
   */
  handleSelectVersion = (value, id) => {
    const { selectedVersion } = this.state;
    selectedVersion[id] = value;
    this.setState({ selectedVersion });
  };

  /**
   * 取消选择版本
   * @param index
   */
  clearVersions = (value, id) => {
    const { selectedVersion } = this.state;
    const versions = _.assign({}, selectedVersion[id]);
    const aliveVersions = _.remove(versions, item => item === value);
    versions[id] = aliveVersions;
    this.setState({ selectedVersion: aliveVersions });
  };

  /**
   * 展开/收起第三步中的版本
   */
  handleChangeStatus = (id, length) => {
    const { upDown } = this.state;
    const cols = document.getElementsByClassName(`col-${id}`);
    if (!upDown.includes(id)) {
      _.forEach(cols, item => {
        item.style.height = "auto";
      });
      upDown.push(id);
      this.setState({ upDown });
    } else {
      _.forEach(cols, item => {
        item.style.height = "31px";
      });
      _.pull(upDown, id);
      this.setState({
        upDown,
      });
    }
  };

  /**
   * 取消导出
   */
  handleBack = () => {
    const {
      name: projectName,
      id: projectId,
      organizationId,
      type,
    } = AppState.currentMenuType;

    const appMarketUrl = `/devops/app-market?type=${type}&id=${projectId}&name=${encodeURIComponent(
      projectName
    )}&organizationId=${organizationId}`;

    this.props.history.push(appMarketUrl);
  };

  /**
   * 判断第二步是否可点击下一步
   * @returns {boolean}
   */
  get checkDisable() {
    const { selectedVersion } = this.state;
    let versionIsEmpty = false;
    const apps = Object.keys(selectedVersion);
    for (const app of apps) {
      if (
        !selectedVersion[app] ||
        (selectedVersion[app] && !selectedVersion[app].length)
      ) {
        versionIsEmpty = true;
        break;
      }
    }

    return versionIsEmpty;
  }

  /**
   *  显示第二步
   *
   * @memberof ExportChart
   */
  displaySecondPart = () => {
    const { selectedRows, selectedVersion } = this.state;
    const selectedApps = _.map(selectedRows, item => item.id);
    const aliveVersion = _.assign({}, selectedVersion);

    // 为所有选中的项初始化版本
    _.forEach(selectedRows, item => {
      const { id, appVersions } = item;
      if (!aliveVersion[id] || (aliveVersion[id] && !aliveVersion[id].length)) {
        aliveVersion[id] = _.last(appVersions) ? [_.last(appVersions).id] : [];
      }
    });

    // 去除已经取消的项目的版本
    for (const key in aliveVersion) {
      if (
        aliveVersion.hasOwnProperty(key) &&
        !selectedApps.includes(Number(key))
      ) {
        aliveVersion[key] = [];
      }
    }

    this.setState({ selectedVersion: aliveVersion });
    this.changeStep(1);
  };

  /**
   *  导出名修改
   *
   * @memberof ExportChart
   */
  onChange = e => {
    this.setState({ exportName: e.target.value });
  };

  /**
   * 导出文件
   */
  handleOk = () => {
    const { ExportChartStore, intl } = this.props;
    const { selectedRows, projectId, exportName, selectedVersion } = this.state;

    const appVersions = [];
    _.filter(selectedVersion, (s, index) => ({
      appMarketId: s.id,
      appVersionIds: _.map(s.versions, "id"),
    }));

    for (const key in selectedVersion) {
      if (selectedVersion.hasOwnProperty(key)) {
        const element = selectedVersion[key];
        if (element && element.length) {
          appVersions.push({
            appMarketId: key,
            appVersionIds: element,
          });
        }
      }
    }

    this.setState({ submitting: true });
    ExportChartStore.exportChart(projectId, exportName, appVersions).then(
      res => {
        // 导出文件名加上后缀，因为 FirFox 无法自动添加文件后缀
        FileDownload(res, `${exportName}.zip`, "application/zip;charset=utf-8");
        this.setState({ submitting: false });
        Choerodon.prompt(intl.formatMessage({ id: "appstore.exportSucc" }));
        this.handleBack();
      }
    );
  };

  /**
   * 第一步应用表格选择
   *
   * @memberof ExportChart
   */
  onSelectChange = (selectedRowKeys, selectedRows) => {
    const { selectedRows: oldSelectedRows } = this.state;
    const allSelectedRows = [...oldSelectedRows, ...selectedRows];
    const uniqKeys = [];
    const newSelectedRows = _.filter(
      allSelectedRows,
      item =>
        selectedRowKeys.includes(item.id) &&
        !uniqKeys.includes(item.id) &&
        uniqKeys.push(item.id)
    );
    this.setState({ selectedRowKeys, selectedRows: newSelectedRows });
  };

  /**
   * 渲染第一步
   */
  renderStepOne = () => {
    const { ExportChartStore, intl } = this.props;
    const { selectedRowKeys, selectedRows } = this.state;
    const appTableDate = ExportChartStore.getApp;
    const column = [
      {
        title: <FormattedMessage id="app.name" />,
        filters: [],
        dataIndex: "name",
        key: "name",
      },
      {
        title: <FormattedMessage id="appstore.contributor" />,
        filters: [],
        dataIndex: "contributor",
        key: "contributor",
      },
      {
        title: <FormattedMessage id="appstore.category" />,
        filters: [],
        dataIndex: "category",
        key: "category",
      },
      {
        title: <FormattedMessage id="appstore.desc" />,
        filters: [],
        dataIndex: "description",
        key: "description",
        render: (test, record) => (
          <MouserOverWrapper text={record.description} width={0.3}>
            {record.description}
          </MouserOverWrapper>
        ),
      },
    ];
    const rowSelection = {
      selectedRowKeys: selectedRowKeys || [],
      onChange: this.onSelectChange,
    };
    return (
      <div className="c7n-step-section-wrap">
        <p>
          <FormattedMessage id="appstore.exportStep1" />
        </p>
        <div className="c7n-step-section">
          <Table
            filterBarPlaceholder={intl.formatMessage({ id: "filter" })}
            loading={ExportChartStore.isLoading}
            pagination={ExportChartStore.pageInfo}
            rowSelection={rowSelection}
            columns={column}
            dataSource={appTableDate}
            rowKey={record => record.id}
            onChange={this.appTableChange}
          />
        </div>
        <div className="c7n-step-section">
          <Button
            type="primary"
            funcType="raised"
            className="c7n-step-button"
            disabled={!selectedRows.length}
            onClick={this.displaySecondPart}
          >
            <FormattedMessage id="next" />
          </Button>
          <Button
            funcType="raised"
            className="c7n-step-clear"
            onClick={this.handleBack}
          >
            <FormattedMessage id="cancel" />
          </Button>
        </div>
      </div>
    );
  };

  /**
   * 渲染第二步
   */
  renderStepTwo = () => {
    const {
      selectedRows,
      isLoading,
      versionDate,
      selectedVersion,
    } = this.state;
    const {
      intl: { formatMessage },
    } = this.props;

    const selectApp = _.map(selectedRows, (app, index) => {
      const { id, name, appVersions } = app;
      const versionOptions = _.map(
        versionDate[id] || _.reverse(appVersions),
        item => {
          const { id: versionId, version } = item;
          return (
            <Option key={version} value={versionId}>
              {version}
            </Option>
          );
        }
      );

      return (
        <Fragment key={id}>
          <div className="c7n-step-section_name">
            <div className="c7n-step-label">
              <FormattedMessage id="app.name" />
            </div>
            <span>{name}</span>
          </div>
          <div className="c7n-step-section">
            <Select
              filter
              showSearch
              dropdownMatchSelectWidth
              mode="multiple"
              className="c7n-step-select"
              optionFilterProp="children"
              optionLabelProp="children"
              label={formatMessage({ id: "network.column.version" })}
              notFoundContent={formatMessage({ id: "appstore.noVer" })}
              value={selectedVersion[id]}
              loading={isLoading}
              onFocus={() => this.loadVersion(id)}
              onDeselect={value => this.clearVersions(value, id)}
              onChange={value => this.handleSelectVersion(value, id)}
              filterOption={(input, option) =>
                option.props.children
                  .toLowerCase()
                  .indexOf(input.toLowerCase()) >= 0
              }
            >
              {versionOptions}
            </Select>
          </div>
        </Fragment>
      );
    });

    return (
      <div className="c7n-step-section-wrap">
        <p>
          <FormattedMessage id="appstore.exportStep2" />
        </p>
        {selectApp}
        <div className="c7n-step-section">
          <Button
            type="primary"
            funcType="raised"
            className="c7n-step-button"
            disabled={this.checkDisable}
            onClick={() => this.changeStep(2)}
          >
            <FormattedMessage id="next" />
          </Button>
          <Button
            funcType="raised"
            className="c7n-step-clear c7n-step-button"
            onClick={() => this.changeStep(0)}
          >
            <FormattedMessage id="previous" />
          </Button>
          <Button
            funcType="raised"
            className="c7n-step-clear"
            onClick={this.handleBack}
          >
            <FormattedMessage id="cancel" />
          </Button>
        </div>
      </div>
    );
  };

  /**
   * 渲染第三步
   * @returns {*}
   */
  renderStepThree = () => {
    const {
      intl: { formatMessage },
    } = this.props;
    const {
      upDown,
      selectedRows,
      exportName,
      submitting,
      selectedVersion,
    } = this.state;

    const tableData = _.map(_.assign({}, selectedRows), item => {
      const { id, appVersions } = item;
      item.versions = _.filter(
        appVersions,
        cur => selectedVersion[id] && selectedVersion[id].includes(cur.id)
      );
      return item;
    });

    const column = [
      {
        title: <FormattedMessage id="app.name" />,
        key: "name",
        render: (test, record) => (
          <MouserOverWrapper text={record.name} width={0.15}>
            {record.name}
          </MouserOverWrapper>
        ),
      },
      {
        title: <FormattedMessage id="app.code" />,
        key: "code",
        render: (test, record) => (
          <MouserOverWrapper text={record.code} width={0.15}>
            {record.code}
          </MouserOverWrapper>
        ),
      },
      {
        title: <FormattedMessage id="network.column.version" />,
        key: "version",
        render: record => {
          const { id, versions } = record;
          return (
            <div
              className={`c7n-step-table-column col-${id}`}
              onClick={this.handleChangeStatus.bind(this, id, versions.length)}
            >
              {((HEIGHT <= 900 && versions.length > 2) ||
                (HEIGHT > 900 && versions.length > 4)) && (
                <span
                  className={
                    _.indexOf(upDown, id) !== -1
                      ? "icon icon-keyboard_arrow_up c7n-step-table-icon"
                      : "icon icon-keyboard_arrow_down c7n-step-table-icon"
                  }
                />
              )}
              <div className={`${id}-col-parents`}>
                {_.map(versions, v => (
                  <div key={v.id} className="c7n-step-col-circle">
                    {v.version}
                  </div>
                ))}
              </div>
            </div>
          );
        },
      },
    ];
    return (
      <div className="c7n-step-section-wrap">
        <p>
          <FormattedMessage id="appstore.exportStep3" />
        </p>
        <div className="c7n-step-section c7n-step-section_input">
          <Input
            onChange={this.onChange}
            value={exportName}
            maxLength={30}
            label={formatMessage({ id: "appstore.exportName" })}
            size="default"
          />
        </div>
        <div className="c7n-step-section">
          <Table
            filterBar={false}
            pagination={false}
            columns={column}
            dataSource={tableData}
            rowKey={record => record.id}
          />
        </div>
        <div className="c7n-step-section">
          <Permission
            service={["devops-service.application-market.importApps"]}
          >
            <Button
              loading={submitting}
              type="primary"
              funcType="raised"
              className="c7n-step-button"
              onClick={this.handleOk}
            >
              <FormattedMessage id="appstore.exportApp" />
            </Button>
          </Permission>
          <Button
            funcType="raised"
            className="c7n-step-clear c7n-step-button"
            onClick={() => this.changeStep(1)}
          >
            <FormattedMessage id="previous" />
          </Button>
          <Button
            funcType="raised"
            className="c7n-step-clear"
            onClick={this.handleBack}
          >
            <FormattedMessage id="cancel" />
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
    const { current, selectedRows } = this.state;

    const contents = [
      this.renderStepOne,
      this.renderStepTwo,
      this.renderStepThree,
    ];

    return (
      <Page
        service={[
          "devops-service.application-market.listAllApp",
          "devops-service.application-market.queryAppVersionsInProject",
          "devops-service.application-market.exportFile",
        ]}
        className="c7n-region"
      >
        <Header
          title={formatMessage({ id: "appstore.export" })}
          backPath={`/devops/app-market?type=${type}&id=${projectId}&name=${name}&organizationId=${organizationId}`}
        />
        <Content code="appstore.export" values={{ name }}>
          <div className="c7n-store-card-wrap">
            <Steps current={current}>
              <Step title={formatMessage({ id: "deploy.step.one.app" })} />
              <Step
                title={formatMessage({ id: "deploy.step.one.version.title" })}
              />
              <Step title={formatMessage({ id: "appstore.confirm" })} />
            </Steps>
            {contents[current]()}
          </div>
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(ExportChart));
