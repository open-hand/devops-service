# Quick start

部署文件的渲染模板，我们下文将定义一些变量，helm执行时会将变量渲染进模板文件中。

## _helpers.tpl

这个文件我们用来进行标签模板的定义，以便在上文提到的位置进行标签渲染。

## values.yaml

这个文件中的键值对，即为我们上文中所引用的变量。

将所以有变量集中在一个文件中，方便部署的时候进行归档以及灵活替换。

同时，helm命令支持使用 `--set FOO_BAR=FOOBAR` 参数对values 文件中的变量进行赋值，可以进一步简化部署流程。


## deployment.yaml

    deployment一种更加简单的更新RC和Pod的机制,deployment集成了上线部署、滚动升级、创建副本、暂停上线任务，恢复上线任务，回滚到以前某一版本（成功/稳定）的Deployment等功能,一个deployment可以生成1个或多个Pod

## service.yaml

    service对象，实现pod的流量转发

## ingress.yaml

    ingress对象，进入集群的请求提供路由规则

