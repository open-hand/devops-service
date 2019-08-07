import React, { PureComponent } from 'react';
import Table from 'rc-table';
import PropTypes from 'prop-types';
import styled from 'styled-components';

import 'rc-table/assets/index.css';

class SimpleTable extends PureComponent {
  static propTypes = {
    columns: PropTypes.arrayOf(PropTypes.object).isRequired,
    data: PropTypes.array,
  };

  static defaultProps = {
    data: [],
  };

  render() {
    const myTable = styled.table`
      border-collapse: collapse !important;
    `;

    const HeaderCell = styled.th`
      background: transparent !important;
      transition: none !important;
      padding: 8px 2px !important;
      font-weight: normal !important;
      font-size: 13px;
      color: rgba(0, 0, 0, 0.65);
      border-bottom: 1px solid rgba(0, 0, 0, 0.36);
    `;

    const HeaderRow = styled.tr`
      &:hover {
        background: none !important;
      }
    `;

    const BodyCell = styled.td`
      padding: 5px 2px !important;
      border-bottom: none !important;
      font-size: 13px;
      color: #000;
      line-height: 20px;
    `;

    const BodyRow = styled.tr`
      &:hover {
        background: none !important;
      }
      &:first-child {
        & > td {
          padding-top: 10px !important;
        }
      }
      &:last-child {
        & > td {
          padding-bottom: 10px !important;
        }
      }
    `;

    const components = {
      table: myTable,
      header: {
        row: HeaderRow,
        cell: HeaderCell,
      },
      body: {
        row: BodyRow,
        cell: BodyCell,
      },
    };
    const { columns, data } = this.props;
    return <Table columns={columns} data={data} components={components} />;
  }
}

export default SimpleTable;
