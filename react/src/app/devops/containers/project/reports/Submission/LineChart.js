import React, { PureComponent, Fragment } from "react";
import ReactEchartsCore from "echarts-for-react/lib/core";
import PropTypes from "prop-types";
import { Spin, Avatar, Tooltip } from "choerodon-ui";
import echarts from "echarts/lib/echarts";
import { injectIntl } from "react-intl";
import { getNear7Day, dateSplitAndPad, pickEntries } from "../../../../utils";

import "echarts/lib/chart/line";
import "echarts/lib/component/tooltip";
import "echarts/lib/component/title";
import "echarts/lib/component/grid";
import "echarts/lib/component/legend";
import "./Submission.scss";

class LineChart extends PureComponent {
  static propTypes = {
    color: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    loading: PropTypes.bool.isRequired,
    hasAvatar: PropTypes.bool.isRequired,
    languageType: PropTypes.string,
    tooltip: PropTypes.bool,
    legend: PropTypes.bool,
    /* eslint-disable-next-line */
    grid: PropTypes.array,
  };

  static defaultProps = {
    grid: [42, 14, 20, 0],
    tooltip: true,
    legend: false,
    languageType: "report",
  };

  getOption = () => {
    const {
      languageType,
      color,
      data: { items, count },
      intl: { formatMessage },
      start,
      end,
      grid,
      legend,
    } = this.props;
    const { keys, values } = pickEntries(dateSplitAndPad(start, end, items));
    const xAxis = keys && keys.length ? keys.reverse() : getNear7Day();
    const yAxis = values && values.length ? values.reverse() : [];
    return {
      title: {
        show: false,
      },
      tooltip: {
        trigger: "item",
        backgroundColor: "#fff",
        textStyle: {
          color: "#000",
        },
        formatter(obj) {
          return `${formatMessage({ id: `${languageType}.commit.date` })}${
            obj.name
          }<br/>${formatMessage({ id: `${languageType}.commit.count` })}${
            obj.value
          }`;
        },
      },
      legend: {
        show: legend,
        left: "right",
        itemWidth: 14,
        itemGap: 20,
        formatter(name) {
          // 在series中必须有对应的name，否则不显示
          return `${name}：${count || 0}`;
        },
        selectedMode: false,
      },
      grid: {
        top: grid[0],
        left: grid[1],
        right: grid[2],
        bottom: grid[3],
        // 防止标签溢出
        containLabel: true,
      },
      xAxis: {
        boundaryGap: false,
        axisTick: {
          show: false,
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: "#eee",
          },
          onZero: true,
        },
        splitLine: {
          show: true,
          lineStyle: {
            color: ["#eee"],
          },
        },
        axisLabel: {
          color: "rgba(0,0,0,0.65)",
          formatter(item, idx) {
            return item
              .split("-")
              .slice(1)
              .join("/");
          },
        },
        data: xAxis,
      },
      yAxis: {
        name: formatMessage({ id: `${languageType}.commit.num` }),
        min: Math.max(...yAxis) > 3 ? null : 0,
        max: Math.max(...yAxis) > 3 ? null : 4,
        minInterval: 1,
        nameTextStyle: {
          color: "#000",
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: "#eee",
          },
        },
        axisTick: {
          show: false,
        },
        splitLine: {
          show: true,
          lineStyle: {
            color: ["#eee"],
          },
        },
        axisLabel: {
          color: "rgba(0,0,0,0.65)",
        },
        type: "value",
      },
      series: [
        {
          data: yAxis,
          type: "line",
          smooth: true,
          smoothMonotone: "x",
          symbol: "circle",
          itemStyle: {
            normal: {
              color,
            },
          },
          areaStyle: {
            color,
            opacity: "0.5",
          },
          lineStyle: {
            color,
          },
        },
        {
          name: `${formatMessage({ id: `${languageType}.devops.total` })}`,
          type: "line",
          color: "#fff",
        },
      ],
    };
  };

  render() {
    const {
      style,
      data: { avatar, count, id, name: userName },
      name,
      loading,
      hasAvatar,
      intl: { formatMessage },
      tooltip,
      languageType,
    } = this.props;
    return (
      <Spin spinning={loading}>
        {tooltip ? (
          <div className="c7n-report-commits-title">
            {hasAvatar ? (
              <span className="c7n-report-commits-avatar">
                {avatar ? (
                  <Avatar size="small" src={avatar} />
                ) : (
                  <Avatar size="small">
                    {name && userName
                      ? name
                          .toString()
                          .slice(0, 1)
                          .toUpperCase()
                      : "?"}
                  </Avatar>
                )}
              </span>
            ) : null}
            {id === 0 ? (
              <Tooltip
                placement="top"
                title={formatMessage({ id: `${languageType}.commits.unknown` })}
              >
                {name}
              </Tooltip>
            ) : (
              name
            )}
            {count ? (
              <span className="c7n-report-commits-text">{count} commits</span>
            ) : null}
          </div>
        ) : null}
        <ReactEchartsCore
          echarts={echarts}
          option={this.getOption()}
          style={style}
          notMerge
          lazyUpdate
        />
      </Spin>
    );
  }
}

export default injectIntl(LineChart);
