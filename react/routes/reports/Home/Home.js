import React, { Component } from 'react';
import { FormattedMessage } from 'react-intl';
import { Page, Header, Content, stores } from '@choerodon/master';
import _ from 'lodash';
import reportList from './reportList';
import './Home.less';

const { AppState } = stores;

class Home extends Component {
  handleClickReport = (report) => {
    const { history } = this.props;
    const { id, name, type, organizationId } = AppState.currentMenuType;
    history.push(`${report.link}?type=${type}&id=${id}&name=${name}&organizationId=${organizationId}`);
  };

  render() {
    const { name } = AppState.currentMenuType;
    return (
      <Page className="c7n-region">
        <Header title={<FormattedMessage id="report.head" />} />
        <Content code="report" values={{ name }}>
          <div className="c7n-reports-wrapper">
            {_.map(reportList, (item) => (
              <div
                role="none"
                className="c7n-devops-report"
                key={item.key}
                onClick={this.handleClickReport.bind(this, item)}
              >
                <div className="c7n-devops-report-pic">
                  <div className={`c7n-devops-report-picBox ${item.pic}`} />
                </div>
                <div className="c7n-devops-report-descr">
                  <div className="c7n-devops-report-title"><FormattedMessage id={`report.${item.key}.head`} /></div>
                  <p className="c7n-devops-report-text"><FormattedMessage id={`report.${item.key}.des`} /></p>
                </div>
              </div>
            ))}
          </div>
        </Content>
      </Page>
    );
  }
}

export default Home;
