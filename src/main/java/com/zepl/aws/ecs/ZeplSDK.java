package com.zepl.aws.ecs;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.ecs.AmazonECSClient;
import com.amazonaws.services.ecs.model.Cluster;
import com.amazonaws.services.ecs.model.ContainerDefinition;
import com.amazonaws.services.ecs.model.CreateClusterRequest;
import com.amazonaws.services.ecs.model.CreateClusterResult;
import com.amazonaws.services.ecs.model.DescribeClustersRequest;
import com.amazonaws.services.ecs.model.DescribeClustersResult;
import com.amazonaws.services.ecs.model.ListClustersRequest;
import com.amazonaws.services.ecs.model.ListClustersResult;
import com.amazonaws.services.ecs.model.ListContainerInstancesRequest;
import com.amazonaws.services.ecs.model.ListContainerInstancesResult;
import com.amazonaws.services.ecs.model.ListTasksResult;
import com.amazonaws.services.ecs.model.PortMapping;
import com.amazonaws.services.ecs.model.RegisterContainerInstanceRequest;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionResult;
import com.amazonaws.services.ecs.model.StartTaskRequest;
import com.amazonaws.services.ecs.model.StartTaskResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shim on 2017. 1. 3..
 */
public class ZeplSDK {
  private static AmazonECSClient client;

  public ZeplSDK() {
        /*
     * The ProfileCredentialsProvider will return your [default]
     * credential profile by reading from the credentials file located at
     * (~/.aws/credentials).
     */
    AWSCredentials credentials = null;
    try {
      credentials = new ProfileCredentialsProvider().getCredentials();
    } catch (Exception e) {
      throw new AmazonClientException(
        "Cannot load the credentials from the credential profiles file. " +
          "Please make sure that your credentials file is at the correct " +
          "location (~/.aws/credentials), and is in valid format.",
        e);
    }

    client = new AmazonECSClient(credentials);
    Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    client.setRegion(usWest2);
  }

  public void createCluster(CreateClusterRequest request) {
    request.putCustomQueryParameter("EcsInstanceType", "m4.large");
    request.putCustomQueryParameter("AsgMaxSize", "2");
    request.putCustomQueryParameter("IamRoleInstanceProfile", "zeplECSRole");


    RegisterContainerInstanceRequest r = new RegisterContainerInstanceRequest();

    CreateClusterResult result = client.createCluster(request);
    System.out.println("createCluster result : " + result);
  }

  public void createTask() {
    // make task //////////////////////////////////////////////////////////////////
    ContainerDefinition containerDefinition = new ContainerDefinition();

    containerDefinition.setMemory(128);
    List<PortMapping> list = new ArrayList();
    {
      PortMapping p = new PortMapping();
      p.setHostPort(8888);
      p.setContainerPort(80);
      p.setProtocol("tcp");
      list.add(p);
    }
    containerDefinition.setPortMappings(list);
    containerDefinition.setName("shimcontainer");
    containerDefinition.setImage("nginx");  // docker image

    List<ContainerDefinition> lstContainerDefinition = new ArrayList();
    lstContainerDefinition.add(containerDefinition);

    /*
    TaskDefinition taskDefinition = new TaskDefinition();
    taskDefinition.setFamily("testfamily");
    taskDefinition.setContainerDefinitions(lstContainerDefinition);
    */

    // container
    RegisterTaskDefinitionRequest registerTaskDefinitionRequest = new RegisterTaskDefinitionRequest();
    registerTaskDefinitionRequest.setContainerDefinitions(lstContainerDefinition);
    registerTaskDefinitionRequest.setFamily("myfamily");
    RegisterTaskDefinitionResult result = client.registerTaskDefinition(registerTaskDefinitionRequest);

    System.out.println("result->" + result.toString());
  }

  public void runTask() {
    // to run task//////////////////////////////////////////////////////////////////
    StartTaskRequest startTaskRequest = new StartTaskRequest();
    startTaskRequest.setCluster("ecsZepl");
    startTaskRequest.setTaskDefinition("myfamily:2");

    List<String> list = new ArrayList();
    list.add("086ad8f5-c892-46e8-9342-493a78c7df44");
    //list.add("f2155a12-1c6e-4d06-b2ee-48abe4af4b8f");
    startTaskRequest.setContainerInstances(list);

    StartTaskResult result = client.startTask(startTaskRequest);
    //RunTaskRequest runTaskRequest = new RunTaskRequest();
    //RunTaskResult result = client.runTask(runTaskRequest);
    System.out.println("-->" + result.getTasks());
    System.out.println("-->" + result.getSdkResponseMetadata());
  }

  public void listClusters() {
    ListClustersResult result = client.listClusters();

    System.out.println("----------" + client.getRequestMetricsCollector());
    System.out.println("----------" + client.getServiceName());
    System.out.println("----------" + client.toString());

    while(true) {
      String tocken = result.getNextToken();
      if(tocken == null) break;

      System.out.print("tocken -> " + tocken);
    }
  }

  public void listClusters2() {
    ListClustersRequest request = new ListClustersRequest();
    request.setMaxResults(100);

    ListClustersResult result = client.listClusters(request);
    for (String arn : result.getClusterArns()) {
      System.out.println("arn -->" + arn);
    }

    System.out.println("-->" + result.getNextToken());
    System.out.println("-->" + result.toString());
    System.out.println("-->" + result.getSdkResponseMetadata());
  }

  public void describeClusters() {
    DescribeClustersResult result = client.describeClusters();
    System.out.println("------->" + result.toString());
    for (Cluster c : result.getClusters()) {
      System.out.println("cluster -->" + c.getClusterName());
      System.out.println("cluster -->" + c.getStatus());
      System.out.println("cluster -->" + c.getActiveServicesCount());
      System.out.println("cluster -->" + c.getPendingTasksCount());
      System.out.println("cluster -->" + c.getRegisteredContainerInstancesCount());
      System.out.println("cluster -->" + c.getRunningTasksCount());
    }
  }

  public void listContainerInstances() {
    ListContainerInstancesRequest r = new ListContainerInstancesRequest();
    r.setCluster("ecsZepl");
    ListContainerInstancesResult result = client.listContainerInstances(r);
    System.out.println("-->" + result.toString());
    System.out.println("-->" + result.getSdkResponseMetadata());
  }

  public void listTask() {
    ListTasksResult request = new ListTasksResult();
    ListTasksResult result = client.listTasks();
    System.out.println("-->" + result.getSdkResponseMetadata());
  }
}
