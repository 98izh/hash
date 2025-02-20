###### About this setup ######
The setup consists of two repositories hash and length where the source code of each service resides.
Programing language: JAVA21.
Framework: Spring boot 3.4.2.
Build tool: Maven.
Platform: Kubernetes minikube 1.32.


###### Architecture ######
hash/
├── src/
│   └── main/
│       ├── java/com/example/proxidize_assienment/
│       │   ├── hash/HashApplication.java
│       │   └── controller/HashController.java
│       └── resources/application.properties
│  └── docker/
│       ├── config.yaml
│       ├── Dockerfile
│       └── entrypoint.sh
│  └── k8s/
│       ├── auto-qurey.yaml
│       ├── hash-configMap.yaml
│       ├── hash-deployment.yaml
│       ├── hash-service.yaml
│       ├── hash-serviceMonitor.yaml
│       ├── hash-np.yaml
│       ├── jaeger-values.yaml     #values for jaeger chart
│       ├── loki-values.yaml       #values for loki chart (log aggregation in Grafana).
│       └── prometheus-values.yaml #values for prometheus chart
│  └── screenshots
│       ├── Jeger for length service.png
│       ├── Key JVM metrics of length service.png
│       ├── Key JVM metrics for hash service.png
│       ├── key spring metrics for both services.png
│       ├── Testing hash service from inside the pod.png
│       ├── kubectl get all -n proxidize -n monitoring
│       ├── Testing hash service from outside the pod.png
│       ├── Testing length service from inside the pod.png
│       └── Testing length service from outside the pod.png
|  └── pom.xml
|  └── readme.md


length/
├── src/
│   └── main/
│       ├── java/com/example/proxidize_assienment/
│       │   ├── length/LengthApplication.java
│       │   └── controller/LengthController.java
│       └── resources/application.properties
│  └── docker/
│       ├── config.yaml
│       ├── Dockerfile
│       └── entrypoint.sh
│  └── k8s/
│       ├── length-configMap.yaml
│       ├── length-deployment.yaml
│       ├── length-np.yaml
│       ├── lrngth-service.yaml
│       └── length-serviceMonitor.yaml
|  └── pom.xml




###### Proof of concept ######

Screenshots in hash/secreenshots/ shows the result of querying the services both from within and outside the pods, the same concpt can be testing from anywhere inside the minikube using 

curl -X POST http://hash-service.proxidize.svc.cluster.local:8080/hash -H "Content-Type: text/plain" -d "Apple"
curl -X POST http://length-service.proxidize.svc.cluster.local:8081/length -H "Content-Type: text/plain" -d "Apple"

*To reach the services from outside the cluster we can configure a nodePort service or use ingress with ingress controllers such as Traefik or Nginx.




###### Instructions ######

1- Install and run Prometheus using Helm
     $helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
     $helm repo update
     $helm install prometheus prometheus-community/kube-prometheus-stack   -f prometheus-values.yaml   --namespace monitoring
     
     Note: this chart come with Grafana out-of-the-box so no need to install it separately.

2- Install and run Loki using Helm
     $helm repo add grafana https://grafana.github.io/helm-charts
     $helm repo update
     $helm upgrade --install loki --namespace=monitoring grafana/loki-stack --values loki-stack-values.yaml

3- Install and run Jaeger using Helm
     $helm repo add jaegertracing https://jaegertracing.github.io/helm-charts
     $helm repo update
     $helm install jaeger-operator jaegertracing/jaeger-operator -n monitoring   --set rbac.clusterRole=true   --set watchNamespace=proxidize
     
     Note: you might need to install certmanger if not installed as it's a requirement for Jaeger, or you can skip that by adding the flag in helm install command.

4- Run Hash and Length deployments with all other config files
     $kubectl apply -f hash-configMap.yaml -f hash-deployment.yaml -f hash-service.yaml -f hash-serviceMonitor.yaml -f hash-np.yaml
     $kubectl apply -f length-configMap.yaml -f length-deployment.yaml -f length-service.yaml -f length-serviceMonitor.yaml -f length-np.yaml

5- You can access Promenthues, Grafana and Jaeger using port-forward
    $kubectl port-forward svc/jaeger-query -n monitoring 16686:16686
    $kubectl port-forward svc/prometheus-grafana 3000:80 -n monitoring
    $kubectl port-forward svc/prometheus-kube-prometheus-prometheus 9090:9090 -n monitoring

    Note: Grafana creds will be provided via email.
    Note: Once in Grafana there are two main custom dashboards to check required metrics JVM and Spring Boot Observability.
    Note: in JVM dashboard you can filter based on the endpoint IP, to get the IP of each service endpoint use: $kubectl get endpoints -n proxidize.
    Note: You might need to add Loki and a data soruce in Grafana if not added automatically to be able to use Spring Boot bashboard.
    



###### Security ######

some of the securtiy best practices that was implemented during the setup:
    1. Runing non-root containers but creating a "proxidize" user and making him the owner of the application.
    2. Deploy in non-default k8s namespace.
    3. configure network polices to drop any incoming traffic other than port 8080 for Hash service and 8081 for Length service.





###### Enhancements ######

This setup has a huge room for improvment, below are some ideas:
    1. Automate CI process by building multi-branch pipelines, to make IC process faster I've created a simple Jenkins that package the artifact then build and push docker image to my registry but a multi-branch pipeline is a better approch.
    2. Run code-quality and securtiy checks during CI process by integrating SonarQube in the pipelines.
    3. Use a dedicated CD tool to track and automate deployments like ArgoCD.
    4. Configure custom dashboards to display more sophisticated metrics like 90th, 95th and 98th percentiles.
    5. Setup alert system using Prometheus alert manager (already installed and integrated with Grafana).
