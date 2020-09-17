import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import {
  Page,
  Header,
  Breadcrumb,
  Content,
  Permission,
  Action,
} from '@choerodon/boot';
import {
  Button, Form, Pagination, Select, TextField,
} from 'choerodon-ui/pro';
import map from 'lodash/map';
import countBy from 'lodash/countBy';
import { ButtonColor, FuncType } from 'choerodon-ui/pro/lib/button/enum';
import EmptyPage from '@/components/empty-page';
import Loading from '@/components/loading';
import HostsItem from './components/hostItem';
import { useHostConfigStore } from '../../stores';

const data = [
  {
    name: '主机001',
    hostStatus: 'success',
    jmeterStatus: 'success', // jmeter的测试连接状态, 测试类型必填 success / failed / operating
    hostIp: '172.23.40.37',
    sshPort: '22',
    authType: null,
    username: '翁恺敏',
    password: null,
    jmeterPort: '8080', // jmeter的端口, 测试类型必填
    jmeterPath: '/home/apache-jmeter - 4.0：8080', // jmeter地址, 测试类型必填
    date: '2020-09-04 14:27:23',
    imgUrl:
      'https://minio.choerodon.com.cn/iam-service/file_11b8ef213e724602abd9facf66c0271a_u%3D2233506214%2C1519914781',
  },
  {
    name: '主机002',
    hostStatus: null,
    jmeterStatus: 'operating', // jmeter的测试连接状态, 测试类型必填 success / failed / operating
    hostIp: '172.23.40.37',
    sshPort: '22',
    authType: null,
    username: '翁恺敏',
    password: null,
    jmeterPort: '8080', // jmeter的端口, 测试类型必填
    jmeterPath: '/home/apache-jmeter - 4.0：8080', // jmeter地址, 测试类型必填
    date: '2020-09-04 14:27:23',
    imgUrl: '',
  },
  {
    name: '主机003',
    hostStatus: null,
    jmeterStatus: 'failed', // jmeter的测试连接状态, 测试类型必填 success / failed / operating
    hostIp: '172.23.40.37',
    sshPort: '22',
    authType: null,
    username: '翁恺敏',
    password: null,
    jmeterPort: '8080', // jmeter的端口, 测试类型必填
    jmeterPath: '/home/apache-jmeter - 4.0：8080', // jmeter地址, 测试类型必填
    date: '2020-09-04 14:27:23',
    imgUrl: '',
  },
  {
    name: '主机004000000000000000000',
    hostStatus: 'success',
    jmeterStatus: 'success', // jmeter的测试连接状态, 测试类型必填 success / failed / operating
    hostIp: '172.23.40.37',
    sshPort: '22',
    authType: null,
    username: '翁恺敏',
    password: null,
    jmeterPort: '8080', // jmeter的端口, 测试类型必填
    jmeterPath: '/home/apache-jmeter - 4.0：8080', // jmeter地址, 测试类型必填
    date: '2020-09-04 14:27:23',
    imgUrl: '',
  },
];

const ContentList: React.FC<any> = observer((): any => {
  const {
    prefixCls, intlPrefix, formatMessage, listDs,
  } = useHostConfigStore();

  useEffect(() => {
  }, []);

  if (listDs.status === 'loading' || !listDs) {
    return <Loading display />;
  }

  if (listDs && !listDs.length) {
    return <EmptyPage />;
  }

  return (
    <>
      <div className={`${prefixCls}-content-list`}>
        {listDs.map((record) => (
          <HostsItem
            {...record.data}
            record={record}
            listDs={listDs}
          />
        ))}
      </div>
      <Pagination
        className={`${prefixCls}-content-pagination`}
        dataSet={listDs}
      />
    </>
  );
});

export default ContentList;
