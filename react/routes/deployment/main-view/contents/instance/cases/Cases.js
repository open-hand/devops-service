import React, { Fragment, useState, useContext, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import {
  Tooltip,
  Icon,
  Progress,
} from 'choerodon-ui/pro';
import _ from 'lodash';
import classnames from 'classnames';
import { useCasesStore } from './stores';
import Operation from './Operation';
import { useDeploymentStore } from '../../../../stores';

import './style/index.less';


const Cases = observer(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useDeploymentStore();
  const {
    intl: { formatMessage },
    casesDs,
  } = useCasesStore();
  const [podEvent, setPodEvent] = useState([]);
  const [activeKey, setActiveKey] = useState([]);
  const record = useMemo(() => casesDs.data, [casesDs.data]);

  /**
   * 展开更多
   * @param item
   */
  function showMore(item) {
    const data = [...activeKey];
    const flag = _.includes(data, item);
    if (flag) {
      const index = _.indexOf(data, item);
      data.splice(index, 1);
    } else {
      data.push(item);
    }
    setActiveKey(data);
  }

  /**
   * 点击操作日志
   * @param podEventVO
   */
  function changeEvent(podEventVO) {
    setPodEvent(podEventVO);
  }

  function istEventDom() {
    const podEventVO = record[0].get('podEventVO');
    return _.map(podEvent.length ? podEvent : podEventVO, ({ name, log, event, jobPodStatus }, index) => {
      const flag = _.includes(activeKey, `${index}-${name}`);
      const desClass = classnames({
        'content-step-des-hidden': !flag,
      });
      return (
        <div className="operation-content-step">
          <div className="content-step-title">
            {jobPodStatus === 'running' ? (
              <Progress strokeWidth={10} width={13} type="loading" />
            ) : (
              <Icon
                type="wait_circle"
                className={`content-step-icon-${jobPodStatus}`}
              />
            )}
            <span className="content-step-title-text">{name}</span>
            {log && (
              <Tooltip
                title={formatMessage({ id: `${intlPrefix}.instance.cases.log` })}
                placement="bottom"
              >
                <Icon
                  type="find_in_page"
                />
              </Tooltip>
            )}
          </div>
          <div className="content-step-des">
            <pre className={desClass}>
              {event}
            </pre>
            {event && event.split('\n').length > 4 && (
              <a onClick={() => showMore(`${index}-${name}`)}>
                <FormattedMessage id={flag ? 'shrink' : 'expand'} />
              </a>
            )}
          </div>
        </div>
      );
    });
  }

  function renderDetail() {
    if (record.length) {
      return (
        <Fragment>
          <Operation handleClick={changeEvent} />
          <div className="cases-operation-content">
            {istEventDom()}
          </div>
        </Fragment>
      );
    }
    return (
      <div className="cases-content-empty">
        <Icon type="info" className="case-content-empty-icon" />
        <FormattedMessage id={`${intlPrefix}.instance.cases.empty`} />
      </div>
    );
  }
  return (
    <div className={`${prefixCls}-instance-cases`}>
      {renderDetail()}
    </div>
  );
});

export default Cases;
