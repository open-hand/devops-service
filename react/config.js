const config = {
  server: 'http://api.staging.saas.test.com',
  fileServer: 'http://minio.staging.saas.test.com',
  projectType: 'choerodon',
  buildType: 'single',
  master: '@choerodon/master',
  theme: {
    'primary-color': '#3F51B5',
    'icon-font-size-base': '16px',
  },
  dashboard: {
    devops: {
      components: './react/src/app/devops/dashboard/*',
      locale: './react/src/app/devops/locale/dashboard/*',
    },
  },
  resourcesLevel: ['site', 'organization', 'project', 'user'],
};

module.exports = config;
