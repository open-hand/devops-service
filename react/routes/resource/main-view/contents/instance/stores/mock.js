const resourceData = {
  replicaSetVOS: [
    {
      name: 'devops-service-ed8ad-6f56c4d5-knzvg',
      desired: 1,
      current: 1,
      ready: 1,
      age: '2019-08-06T16:37:34.000+08:00',
    },
  ],
  statefulSetVOS: [
    {
      desiredReplicas: 1,
      name: 'devops-service-ed8ad-6f56c4d5-knzvg',
      readyReplicas: 1,
      currentReplicas: 1,
      devopsEnvPodVOS: [
        {
          ready: true,
        },
      ],
      age: 'string',
    },
  ],
  serviceVOS: [
    {
      port: '9000',
      name: 'devops-service-ed8ad-6f56c4d5-knzvg',
      type: 'string',
      externalIp: '127.0.0.1',
      targetPort: '9527',
      age: '2019-08-06T16:39:44.000+08:00',
      clusterIp: '192.168.1.1',
    },
  ],
  deploymentVOS: [
    {
      name: 'devops-service-ed8ad-6f56c4d5-knzvg',
      desired: 1,
      current: 1,
      upToDate: 1,
      available: 1,
      devopsEnvPodVOS: [
        {
          id: 71072,
          name: 'devops-service-ed8ad-6f56c4d5-knzvg',
          ip: '10.233.69.72',
          status: 'Running',
          creationDate: '2019-08-06 16:37:34',
          appName: null,
          namespace: 'c7ncd-staging',
          appVersion: null,
          publishLevel: null,
          instanceCode: null,
          envId: null,
          projectId: null,
          envCode: null,
          envName: null,
          objectVersionNumber: 5,
          clusterId: null,
          containers: null,
          nodeName: 'staging05',
          restartCount: 0,
          ready: true,
          connect: null,
        },
      ],
      ports: ['9000', '8080', '8081', '8848'],
      age: '2019-08-06T16:39:44.000+08:00',
      labels: {
        'choerodon.io': '0.18.0',
        'choerodon.io/application': 'gitlab-service',
        'choerodon.io/metrics-port': '8071',
        'choerodon.io/release': 'gitlab-service-fa318',
        'choerodon.io/service': 'gitlab-service',
        'choerodon.io/version': '2019.7.20-112555-hotfix-0-18-1',
      },
    },
  ],
  persistentVolumeClaimVOS: [
    {
      name: 'devops-service-ed8ad-6f56c4d5-knzvg',
      accessModes: 'string',
      age: '2019-08-06T16:39:44.000+08:00',
      capacity: 'string',
      status: 'Running',
    },
  ],
  daemonSetVOS: [
    {
      numberAvailable: 2,
      name: 'devops-service-ed8ad-6f56c4d5-knzvg',
      currentScheduled: 2,
      desiredScheduled: 0,
      age: '2019-08-06T16:39:44.000+08:00',
      devopsEnvPodDTOS: [
        {
          ready: 'boolean',
        },
      ],
    },
  ],
  podVOS: [
    {
      desire: 1,
      ready: 1,
      name: 'devops-service-ed8ad-6f56c4d5-knzvg',
      restarts: 0,
      age: '2019-08-06T16:37:34.000+08:00',
      status: 'Pending',
    },
  ],
  ingressVOS: [
    {
      address: '192.168.1.1',
      hosts: 'example.com',
      name: 'devops-service-ed8ad-6f56c4d5-knzvg',
      ports: '8096',
      age: '2019-08-06T16:37:34.000+08:00',
    },
  ],
};

export default resourceData;
