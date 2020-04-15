import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    yaml: {
      Maven: `
# 功能： 更新pom文件中指定的项目的版本号
# 说明： 此函数是猪齿鱼内置的shell函数，用于更新pom文件的版本号为对应commit的版本号,
#        (这个值在猪齿鱼内置变量 CI_COMMIT_TAG 中)
# update_pom_version


# 功能： 以jacoco为代理进行单元测试，可以分析单元测试覆盖率
# 参数说明：
#  -Dmaven.test.skip=false：不跳过单元测试
#  -U：每次构建检查依赖更新，可避免缓存中快照版本依赖不更新问题，但会牺牲部分性能
#  -e -X ：打印调试信息，定位疑难构建问题时建议使用此参数构建
#  -B：以batch模式运行，可避免日志打印时出现ArrayIndexOutOfBoundsException异常
# 更多帮助信息请执行此命令进行查看：mvn org.jacoco:jacoco-maven-plugin:help
# mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent test -Dmaven.test.failure.ignore=true -DskipTests=false -U -e -X -B


# 功能： springboot项目打包
# repackage可以将已经存在的jar和war格式的文件重新打包
# 打出的jar包将可以在命令行使用java -jar命令执行。
# 更多帮助信息请执行此命令进行查看：mvn spring-boot:help
# mvn package spring-boot:repackage


# 功能：  打包
# 参数说明：
#  -Dmaven.test.skip=true：跳过单元测试，不建议
#  -U：每次构建检查依赖更新，可避免缓存中快照版本依赖不更新问题，但会牺牲部分性能
#  -e -X ：打印调试信息，定位疑难构建问题时建议使用此参数构建
#  -B：以batch模式运行，可避免日志打印时出现ArrayIndexOutOfBoundsException异常
# 使用场景： 打包项目且不需要执行单元测试时使用
# 更多帮助信息请执行此命令进行查看：mvn help:describe -Dcmd=package
mvn package -Dmaven.test.skip=true -U -e -X -B`,
      npm: `
export PATH=$PATH:/root/.npm-global/bin
#设置Devcloud镜像仓加速构建
npm config set registry https://mirrors.huaweicloud.com/repository/npm/
npm config set prefix '~/.npm-global'
#如需安装node-sass
#npm config set sass_binary_site https://repo.huaweicloud.com/node-sass/
#npm install node-sass
#加载依赖
npm install
#默认构建
npm run build`,
    },

    get getYaml() {
      return this.yaml;
    },

    axiosConnectTest(data, projectId) {
      return axios.post(`/devops/v1/projects/${projectId}/ci_jobs/sonar/connect`, data);
    },
  }));
}
