# 一、注解说明

Nginx Ingress Controller通过下列`canary-*`Annotation来支持应用服务的灰度发布机制。

- `nginx.ingress.kubernetes.io/canary`: 必须设置该Annotation值为true，否则其它规则将不会生效。
- `nginx.ingress.kubernetes.io/canary-by-header`: 表示基于请求头的名称进行灰度发布。
  当请求头名称的取值为`always`时，无论什么情况下，流量均会进入灰度服务。当请求头名称的取值为`never`
  时,无论什么情况下，流量均不会进入灰度服务。若没有指定请求头名称的值，则只要该头存在，都会进行流量转发。
- `nginx.ingress.kubernetes.io/canary-by-header-value`: 表示基于请求头的值进行灰度发布。需要与canary-by-header头配合使用。
- `nginx.ingress.kubernetes.io/canary-by-header-pattern`:
  表示基于请求头的值进行灰度发布，并对请求头的值进行正则匹配。需要与canary-by-header头配合使用。取值为用于匹配请求头的值的正则表达式。
- `nginx.ingress.kubernetes.io/canary-by-cookie`:
  表示基于Cookie进行灰度发布。例如，`nginx.ingress.kubernetes.io/canary-by-cookie: foo`。当cookie值设置为`always`
  时，流量会进入灰度服务。当cookie值设置为`never`时流量不会进入灰度服务。只有当Cookie存在，且值为always时，才会进行流量转发
- `nginx.ingress.kubernetes.io/canary-weight`: 表示基于权重进行灰度发布。取值范围：0~权重总值。若未设定总值，默认总值为100。
- `nginx.ingress.kubernetes.io/canary-weight-total`: 表示设定的权重总值。若未设定总值，默认总值为100。

不同灰度方式的优先级由高到低为：`canary-by-header` > `canary-by-cookie` > `canary-weight`

**注意**：目前每个Ingress规则只支持同时指定一个Canary Ingress，大于一个的Canary Ingress将会被忽略。

更多详细说明，请参考NGINX Ingress
Controller官网文档：https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#canary

# 二、使用示例

## 1. 部署正式版本服务

1. 创建Deployment和Service

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
spec:
  replicas: 1
  selector:
    matchLabels:
      run: nginx
  template:
    metadata:
      labels:
        run: nginx
    spec:
      containers:
      - image: nginx:1.22
        name: nginx
        ports:
        - containerPort: 80
          protocol: TCP
---
apiVersion: v1
kind: Service
metadata:
  name: nginx
spec:
  ports:
  - port: 80
    protocol: TCP
    targetPort: 80
  selector:
    run: nginx
```

2.创建 Ingress 路由规则

```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: production
spec:
  rules:
  - host: www.example.com
    http:
      paths:
      # 老版本服务。
      - path: /
        backend:
          service: 
            name: nginx
            port:
              number: 80
        pathType: ImplementationSpecific
```

## 灰度发布新版本服务

发布一个新版本的Nginx服务并配置路由规则。

1. 部署新版本的Deployment和Service

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-v1.24
spec:
  replicas: 1
  selector:
    matchLabels:
      run: nginx-v1.24
  template:
    metadata:
      labels:
        run: nginx-v1.24
    spec:
      containers:
      - image: nginx:1.24
        name: nginx-v1.24
        ports:
        - containerPort: 80
          protocol: TCP
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-v1.24
spec:
  ports:
  - port: 80
    protocol: TCP
    targetPort: 80
  selector:
    run: nginx-v1.24
```

2. 设置访问新版本服务的路由规则

- 基于权重的 Canary 规则测试,以下示例中仅50%的流量被路由到新版本服务中

```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: canary
  annotations:
    # 开启Canary。
    nginx.ingress.kubernetes.io/canary: "true"
    # 仅允许50%的流量会被路由到新版本服务new-nginx中。
    # 默认总值为100。
    nginx.ingress.kubernetes.io/canary-weight: "50"
spec:
  rules:
  - host: www.example.com
    http:
      paths:
      # 新版本服务。
      - path: /
        backend:
          service: 
            name: nginx-v1.24
            port:
              number: 80
```

- 基于用户请求的 Canary 规则测试，以下示例仅请求头中满足foo=bar的客户端请求才能路由到新版本服务

```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: canary
  annotations:
    # 开启Canary。
    nginx.ingress.kubernetes.io/canary: "true"
    # 请求头为foo。
    nginx.ingress.kubernetes.io/canary-by-header: "foo"
    # 请求头foo的值为bar时，请求才会被路由到新版本服务nginx-v1.24中。
    nginx.ingress.kubernetes.io/canary-by-header-value: "bar"
    
spec:
  rules:
  - host: www.example.com
    http:
      paths:
      # 新版本服务。
      - path: /
        backend:
          service: 
            name: nginx-v1.24
            port:
              number: 80
```

## 三、删除旧版本服务

系统运行一段时间后，当新版本服务已经稳定并且符合预期后，需要下线老版本的服务
，仅保留新版本服务在线上运行。为了达到该目标，需要将生产版本的Ingress指向新版本的Service，并且删除旧版本的Deployment和Service。

1. 修改生产版本Ingress，使其指向新版本Service

```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: production
spec:
  rules:
  - host: www.example.com
    http:
      paths:
      # 新版本服务。
      - path: /
        backend:
          service: 
            name: nginx-v1.24
            port:
              number: 80
```

2. 删除Canary Ingress
3. 删除旧版本的Deployment和Service