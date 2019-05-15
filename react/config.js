const config = {
  local: true,
  clientid: 'localhost',
  port: 9090,
  output: './dist',
  server: 'http://api.staging.saas.hand-china.com',
  // server: 'http://api.c7nf.choerodon.staging.saas.hand-china.com',
  fileServer: 'http://minio.staging.saas.hand-china.com',
  titlename: 'Choerodon | 企业数字化服务平台',
  projectType: 'choerodon',
  buildType: 'single',
  master: '@choerodon/master',
  favicon: 'favicon.ico',
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
  //对应 主菜单 | 组织层菜单 | 项目层菜单 | 用户菜单
  resourcesLevel: ['site', 'organization', 'project', 'user'],

  // 指定路由入口文件，不设置则默认选择 master 属性对应路径
  // routes: [],
  // 子模块
  // modules: [],
};

module.exports = config;
