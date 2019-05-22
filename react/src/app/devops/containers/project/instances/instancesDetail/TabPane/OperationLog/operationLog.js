import React, { Component, Fragment } from "react";
import { observer, inject } from "mobx-react";
import {
  Spin,
  Icon,
  Avatar,
  DatePicker,
} from "choerodon-ui";
import { stores } from "@choerodon/boot";
import { injectIntl, FormattedMessage } from "react-intl";
import _ from "lodash";
import "./index.scss"
import LoadingBar from "../../../../../../components/loadingBar/LoadingBar";

const { RangePicker } = DatePicker;

const { AppState } = stores;

@observer
class OperationLog extends Component {

  constructor(props) {
    super(props);
    this.state = {
      page: 1,
      startTime: null,
      endTime: null,
      hasMore: true,
      loading: false,
      once: true,
    }
  }

  componentDidMount() {
    const { store } = this.props;
    const total = store.getLogTotal;
    const istLog = store.getIstLog;
    const element = document.getElementsByClassName("page-content")[0];
    this.setState({ hasMore: total > istLog.length});
    element.addEventListener('scroll', this.handleScroll)
  }

  componentWillUnmount() {
    const { store } = this.props;
    const element = document.getElementsByClassName("page-content")[0];
    element.removeEventListener('scroll', this.handleScroll);
    store.setIstLog([]);
  }

  /**
   * 鼠标滚动事件
   */
  handleScroll = (e) => {
    const clientHeight = e.target.clientHeight;
    const scrollHeight = e.target.scrollHeight;
    const scrollTop = e.target.scrollTop;
    const { hasMore, once } = this.state;
    if (clientHeight + scrollTop === scrollHeight && hasMore && once) {
      this.loadMore();
    }
  };

  /**
   * 选择日期
   * @param date
   * @param dateString
   */
  handleDateChange = (date, dateString) => {
    const { store, id } = this.props;
    const { projectId } = AppState.currentMenuType;
    const startTime = dateString[0].replace(/-/g, '/');
    const endTime = dateString[1].replace(/-/g, '/');
    store.loadIstLog(projectId, id, 0, 15, startTime, endTime)
      .then(data => {
        if (data && !data.failed) {
          const istLog = store.getIstLog;
          this.setState({ hasMore: data.totalElements > istLog.length });
        }
      });
    this.setState({ page: 1, startTime, endTime });
  };

  /**
   * 滚动加载更多数据
   */
  loadMore = () => {
    const { store, id } = this.props;
    const { page, startTime, endTime } = this.state;
    const { projectId } = AppState.currentMenuType;
    this.setState({ loading: true });
    store.loadIstLog(projectId, id, page, 15, startTime, endTime, false)
      .then(data => {
        if (data && !data.failed) {
          const istLog = store.getIstLog;
          this.setState({ hasMore: data.totalElements > istLog.length });
        }
        this.setState({ loading: false, once: true });
      });
    this.setState({ page: page + 1, once: false });
  };

  /**
   * 获取操作类型
   * @param type 类型
   */
  getOperation = (type) => {
    const { intl: { formatMessage } } = this.props;
    const operation = {
      create: formatMessage({ id: "ist.instance" }),
      update: formatMessage({ id: "ist.update" }),
      stop: formatMessage({ id: "ist.stop" }),
      restart: formatMessage({ id: "ist.run" }),
      delete: formatMessage({ id: "ist.delete" }),
    };
    return <span>{operation[type]}</span>;
  };

  render() {
    const {
      store,
      intl: { formatMessage },
    } = this.props;
    const {
      hasMore,
      loading,
    } = this.state;
    const istLog = store.getIstLog;
    const logLoading = store.getLogLoading;

    return (
      <div className={`c7n-deployDetail-card c7n-deployDetail-card-content ${hasMore || loading ? "c7n-log-hasMore" : ""}`}>
        <div className="c7n-operation-log-datepicker">
          <RangePicker
            onChange={this.handleDateChange}
          />
        </div>
        {logLoading ? <LoadingBar display /> : <div>
          {
            _.map(istLog, (item, index) => {
              const {realName, userImage, createTime, loginName, type} = item;
              return (
                <div className="c7n-operate-log-content" key={index}>
                  <div className="c7n-log-date">
                    <div className="c7n-date-year">{createTime && index ? createTime.slice(0, 4) : null}</div>
                    <div className={index === 0 && istLog.length > 1 ? "mg-top-43" : ""}>{createTime ? createTime.slice(5, 16) : null}</div>
                  </div>
                  {index ? (<div className="c7n-log-step">
                    <Icon type="wait_circle"/>
                  </div>) : (istLog.length > 1 ? (<div className="c7n-date-recent">
                    <FormattedMessage id="recent"/>
                  </div>) : null)
                  }
                  <div className="c7n-log-title">
                    {this.getOperation(type)}
                  </div>
                  <div className="c7n-log-mbr">
                    <span className="log-mbr-title">{formatMessage({id: "ist.deploy.mbr"})}：</span>
                    {userImage ? (
                      <Avatar src={userImage} size="small"/>
                    ) : (
                      <Avatar size="small">{realName ? realName.toString().slice(0, 1).toUpperCase() : '?'}</Avatar>
                    )}
                    <span>{loginName}&nbsp;{realName}</span>
                  </div>
                  <div className="c7n-log-line"></div>
                </div>
              )
            })
          }
          {loading ? <div className="c7n-log-item-loading"><Spin /></div> : null}
        </div>}
      </div>
    );
  }
}

export default injectIntl(OperationLog);
