import React, { Fragment, useState, useContext, useMemo, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import {
  Tooltip,
  Icon,
  Progress,
} from 'choerodon-ui/pro';
import { DataSet } from 'choerodon-ui/pro';
import _ from 'lodash';
import classnames from 'classnames';
import Store from '../../../../../stores';
import CasesDataSet from './stores/CasesDataSet';
import Record from './Record';

import './style/index.less';


const Cases = observer(() => {
  const {
    selectedMenu: { menuId },
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    AppState: { currentMenuType: { id } },
  } = useContext(Store);
  const casesDs = useMemo(() => new DataSet(CasesDataSet(id, menuId)), [id, menuId]);
  const [podEvent, setPodEvent] = useState([]);
  const [activeKey, setActiveKey] = useState([]);

  /**
   * 展开更多
   * @param index
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
   * @param podEventDTO
   */
  function changeEvent(podEventDTO) {
    setPodEvent(podEventDTO);
  }

  function istEventDom(record) {
    return _.map(podEvent.length ? podEvent : record[0].podEventDTO, ({ name, log, event, jobPodStatus }, index) => {
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
              <a onClick={showMore(`${index}-${name}`)}>
                <FormattedMessage id={flag ? 'shrink' : 'expand'} />
              </a>
            )}
          </div>
        </div>
      );
    });
  }

  function renderDetail() {
    const data = casesDs.data;
    if (data.length) {
      const record = data[0];
      return (
        <Fragment>
          <Record
            event={record}
            handleClick={changeEvent}
          />
          <div className="cases-operation-content">
            {istEventDom(record)}
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
      {renderDetail}
    </div>
  );
});

export default Cases;
