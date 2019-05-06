import React, { Component, Fragment } from "react";
import { Link, withRouter } from "react-router-dom";
import { DashBoardNavBar, stores } from "@choerodon/boot";
import { injectIntl, FormattedMessage } from "react-intl";
import { observer } from "mobx-react";
import { Select, Spin, Tooltip } from "choerodon-ui";
import moment from "moment";
import _ from "lodash";
import ReportsStore from "../../stores/project/reports/ReportsStore";
import LineChart from "../../containers/project/reports/Submission/LineChart";
import MaxTagPopover from "../../containers/project/reports/Component/MaxTagPopover";
import "../common.scss";
import "./index.scss";

const START = moment()
  .subtract(6, "days")
  .format()
  .split("T")[0]
  .replace(/-/g, "/");
const END = moment()
  .format()
  .split("T")[0]
  .replace(/-/g, "/");

function formatData(data) {
  const { totalCommitsDate } = data;
  const total = {};
  if (totalCommitsDate) {
    total.items = totalCommitsDate.slice();
    total.count = totalCommitsDate.length;
  }

  return total;
}

const { AppState } = stores;
const { Option } = Select;

@observer
class DevOpsCommit extends Component {
  constructor(props) {
    super(props);
    this.state = {
      appId: [],
      loading: true,
    };
  }

  componentDidMount() {
    this.loadCommits();
  }

  componentWillUnmount() {
    ReportsStore.setAllApps([]);
    ReportsStore.setCommits({});
    ReportsStore.setCommitsRecord([]);
    ReportsStore.setCommitLoading(false);
  }

  handleChange = id => {
    const { id: projectId } = AppState.currentMenuType;
    this.setState({ appId: id });
    ReportsStore.loadCommits(projectId, START, END, id);
  };

  getContent = () => {
    const { loading } = this.state;
    if (loading) {
      return (
        <div className="c7ncd-dashboard-loading">
          <Spin />
        </div>
      );
    }
    const commits = ReportsStore.getCommits;
    const total = formatData(commits);
    return (
      <LineChart
        languageType="dashboard"
        loading={ReportsStore.getCommitLoading}
        tooltip={false}
        hasAvatar={false}
        legend
        name=""
        color="#4677dd"
        grid={[30, 10, 20, 0]}
        style={{ width: "100%", height: 286 }}
        data={total}
        start={moment().subtract(7, "days")}
        end={moment()}
      />
    );
  };

  loadCommits = () => {
    const { id: projectId } = AppState.currentMenuType;
    this.setState({ loading: true });
    ReportsStore.loadAllApps(projectId).then(data => {
      const appData =
        data && data.length ? _.filter(data, ["permission", true]) : [];
      if (appData.length) {
        const selectApp = _.map(appData, item => item.id);
        this.setState({ appId: selectApp });
        ReportsStore.loadCommits(projectId, START, END, selectApp);
      }
      this.setState({ loading: false });
    });
  };

  choiceRender = (liNode, value) =>
    React.cloneElement(liNode, {
      className: "c7ncd-db-select-li",
      title: "",
    });

  maxTagNode = (data, value) => (
    <MaxTagPopover
      placement="bottomRight"
      width={220}
      dataSource={data}
      value={value}
    />
  );

  render() {
    const {
      history,
      intl: { formatMessage },
    } = this.props;
    const { appId } = this.state;
    const {
      id: projectId,
      name: projectName,
      organizationId,
      type,
    } = AppState.currentMenuType;
    const apps = ReportsStore.getAllApps;
    const options = _.map(apps, item => (
      <Option key={item.id} value={item.id}>
        <Tooltip
          // arrowPointAtCenter
          placement="bottomLeft"
          title={item.name}
        >
          <span className="c7ncd-db-option-text">{item.name}</span>
        </Tooltip>
      </Option>
    ));

    let selectWidth = 100;

    if (appId.length > 2) {
      selectWidth = 206;
    } else if (appId.length === 2) {
      selectWidth = 185;
    }

    return (
      <Fragment>
        <Select
          className="c7ncd-db-select c7n-report-select"
          mode="multiple"
          value={appId}
          placeholder={formatMessage({ id: "dashboard.environment.select" })}
          style={{ maxWidth: selectWidth, minWidth: 100 }}
          maxTagCount={2}
          choiceRender={this.choiceRender}
          maxTagPlaceholder={this.maxTagNode.bind(this, apps)}
          onChange={this.handleChange}
          notFoundContent={formatMessage({ id: "dashboard.environment.none" })}
          choiceRemove={false}
        >
          {options}
        </Select>
        <div className="c7ncd-db-panel c7ncd-db-panel-size">
          {this.getContent()}
        </div>
        <DashBoardNavBar>
          <Link
            to={{
              pathname: "/devops/reports/submission",
              search: `?type=${type}&id=${projectId}&name=${encodeURIComponent(
                projectName
              )}&organizationId=${organizationId}`,
              state: { appId },
            }}
          >
            <FormattedMessage id="dashboard.commits" />
          </Link>
        </DashBoardNavBar>
      </Fragment>
    );
  }
}

export default withRouter(injectIntl(DevOpsCommit));
