package com.zepl.aws.ecs;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.amazonaws.services.ecs.AmazonECSClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by shim on 2017. 1. 5..
 */
public class Cloudformation {
  private static AmazonECSClient client;

  public Cloudformation() {
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

  // Convert a stream into a single, newline separated string
  public static String convertStreamToString(InputStream in) throws Exception {
    System.out.println("===> " + in);

    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    StringBuilder stringbuilder = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      stringbuilder.append(line + "\n");
    }
    in.close();
    return stringbuilder.toString();
  }

  // Wait for a stack to complete transitioning
  // End stack states are:
  //    CREATE_COMPLETE
  //    CREATE_FAILED
  //    DELETE_FAILED
  //    ROLLBACK_FAILED
  // OR the stack no longer exists
  public static String waitForCompletion(AmazonCloudFormation stackbuilder, String stackName) throws Exception {

    DescribeStacksRequest wait = new DescribeStacksRequest();
    wait.setStackName(stackName);
    Boolean completed = false;
    String  stackStatus = "Unknown";
    String  stackReason = "";

    System.out.print("Waiting");

    while (!completed) {
      List<Stack> stacks = stackbuilder.describeStacks(wait).getStacks();
      if (stacks.isEmpty())
      {
        completed   = true;
        stackStatus = "NO_SUCH_STACK";
        stackReason = "Stack has been deleted";
      } else {
        for (Stack stack : stacks) {
          if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
            stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
            stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString()) ||
            stack.getStackStatus().equals(StackStatus.DELETE_FAILED.toString())) {
            completed = true;
            stackStatus = stack.getStackStatus();
            stackReason = stack.getStackStatusReason();
          }
        }
      }

      // Show we are waiting
      System.out.print(".");

      // Not done yet so sleep for 10 seconds.
      if (!completed) Thread.sleep(10000);
    }

    // Show we are done
    System.out.print("done\n");

    return stackStatus + " (" + stackReason + ")";
  }

  public void createCloudFormation() throws Exception {
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

    AmazonCloudFormation stackbuilder = new AmazonCloudFormationClient(credentials);
    Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    stackbuilder.setRegion(usWest2);

    System.out.println("===========================================");
    System.out.println("Getting Started with AWS CloudFormation");
    System.out.println("===========================================\n");

    String stackName           = "ZeplInterpreterECS";
    String logicalResourceName = "SampleNotificationTopic";

    try {
      // Create a stack
      CreateStackRequest createRequest = new CreateStackRequest();
      createRequest.setStackName(stackName);
      /*
      createRequest.putCustomQueryParameter("AsgMaxSize", "2");
      createRequest.putCustomQueryParameter("DeviceName", "/dev/xvdcz");
      createRequest.putCustomQueryParameter("EbsVolumeSize", "22");
      createRequest.putCustomQueryParameter("EbsVolumeType", "gp2");
      createRequest.putCustomQueryParameter("EcsAmiId", "ami-a2ca61c2");
      createRequest.putCustomQueryParameter("EcsClusterName", "astro-cluster2");
      createRequest.putCustomQueryParameter("EcsInstanceType", "m4.large");
      createRequest.putCustomQueryParameter("IamRoleInstanceProfile", "zeplECSRole");
      createRequest.putCustomQueryParameter("KeyName", "zeppelinhub_oregon");
      createRequest.putCustomQueryParameter("SecurityIngressCidrIp", "0.0.0.0/0");
      createRequest.putCustomQueryParameter("SecurityIngressFromPort", "80");
      createRequest.putCustomQueryParameter("SecurityIngressToPort", "80");
      createRequest.putCustomQueryParameter("SubnetCidr1", "10.0.0.0/24");
      createRequest.putCustomQueryParameter("SubnetCidr2", "10.0.1.0/24");
      createRequest.putCustomQueryParameter("VpcAvailabilityZones", "us-west-2c,us-west-2b,us-west-2a");
      createRequest.putCustomQueryParameter("VpcCidr", "10.0.0.0/16");
      */

      List<Parameter> list = new ArrayList();

      /*
      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("AsgMaxSize");
        p1.setParameterValue("2");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("DeviceName");
        p1.setParameterValue("/dev/xvdcz");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("EbsVolumeSize");
        p1.setParameterValue("22");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("EbsVolumeType");
        p1.setParameterValue("gp2");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("EcsAmiId");
        p1.setParameterValue("ami-a2ca61c2");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("EcsClusterName");
        p1.setParameterValue("astro-cluster2");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("EcsInstanceType");
        p1.setParameterValue("m4.large");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("IamRoleInstanceProfile");
        p1.setParameterValue("zeplECSRole");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("KeyName");
        p1.setParameterValue("zeppelinhub_oregon");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("SecurityIngressCidrIp");
        p1.setParameterValue("0.0.0.0/0");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("SecurityIngressFromPort");
        p1.setParameterValue("80");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("SecurityIngressToPort");
        p1.setParameterValue("80");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("SubnetCidr1");
        p1.setParameterValue("10.0.0.0/24");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("SubnetCidr2");
        p1.setParameterValue("10.0.1.0/24");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("VpcAvailabilityZones");
        p1.setParameterValue("us-west-2c,us-west-2b,us-west-2a");
        list.add(p1);
      }

      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("VpcCidr");
        p1.setParameterValue("10.0.0.0/16");
        list.add(p1);
      }
      */

      //////////////// json /////////////////////////
      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("EC2InstanceProfile");
        p1.setParameterValue("zeplECSRole");
        list.add(p1);
      }
      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("KeyName");
        p1.setParameterValue("zeppelinhub_oregon");
        list.add(p1);
      }
      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("VpcId");
        p1.setParameterValue("vpc-46c30122");
        list.add(p1);
      }
      {
        Parameter p1 = new Parameter();
        p1.setParameterKey("SubnetID");
        p1.setParameterValue("subnet-b043acc6,subnet-bf36fcdb");
        list.add(p1);
      }

      createRequest.setParameters(list);

      createRequest.setTemplateBody(convertStreamToString(this.getClass().getResourceAsStream("cloudformation.template")));
      //createRequest.setTemplateBody(convertStreamToString(SampleECS.class.getResourceAsStream("/Users/shim/awsecs/cloudformation.template")));
      System.out.println("Creating a stack called " + createRequest.getStackName() + ".");
      stackbuilder.createStack(createRequest);

      // Wait for stack to be created
      // Note that you could use SNS notifications on the CreateStack call to track the progress of the stack creation
      System.out.println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackbuilder, stackName));

      // Show all the stacks for this account along with the resources for each stack
      for (Stack stack : stackbuilder.describeStacks(new DescribeStacksRequest()).getStacks()) {
        System.out.println("Stack : " + stack.getStackName() + " [" + stack.getStackStatus().toString() + "]");

        DescribeStackResourcesRequest stackResourceRequest = new DescribeStackResourcesRequest();
        stackResourceRequest.setStackName(stack.getStackName());
        for (StackResource resource : stackbuilder.describeStackResources(stackResourceRequest).getStackResources()) {
          System.out.format("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(), resource.getLogicalResourceId(), resource.getPhysicalResourceId());
        }
      }

      // Lookup a resource by its logical name
      DescribeStackResourcesRequest logicalNameResourceRequest = new DescribeStackResourcesRequest();
      logicalNameResourceRequest.setStackName(stackName);
      logicalNameResourceRequest.setLogicalResourceId(logicalResourceName);
      System.out.format("Looking up resource name %1$s from stack %2$s\n", logicalNameResourceRequest.getLogicalResourceId(), logicalNameResourceRequest.getStackName());
      for (StackResource resource : stackbuilder.describeStackResources(logicalNameResourceRequest).getStackResources()) {
        System.out.format("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(), resource.getLogicalResourceId(), resource.getPhysicalResourceId());
      }

      /*
      // Delete the stack
      DeleteStackRequest deleteRequest = new DeleteStackRequest();
      deleteRequest.setStackName(stackName);
      System.out.println("Deleting the stack called " + deleteRequest.getStackName() + ".");
      stackbuilder.deleteStack(deleteRequest);
    */
      // Wait for stack to be deleted
      // Note that you could used SNS notifications on the original CreateStack call to track the progress of the stack deletion
      System.out.println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackbuilder, stackName));

    } catch (AmazonServiceException ase) {
      System.out.println("Caught an AmazonServiceException, which means your request made it "
        + "to AWS CloudFormation, but was rejected with an error response for some reason.");
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    } catch (AmazonClientException ace) {
      System.out.println("Caught an AmazonClientException, which means the client encountered "
        + "a serious internal problem while trying to communicate with AWS CloudFormation, "
        + "such as not being able to access the network.");
      System.out.println("Error Message: " + ace.getMessage());
    }
  }

  public void create(String stackName, String templatePath, Collection<Parameter> parameters) throws Exception {
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

    AmazonCloudFormation stackbuilder = new AmazonCloudFormationClient(credentials);
    Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    stackbuilder.setRegion(usWest2);

    System.out.println("===========================================");
    System.out.println("Getting Started with AWS CloudFormation " + templatePath);
    System.out.println("===========================================\n");

    try {
      // Create a stack
      CreateStackRequest createRequest = new CreateStackRequest();
      createRequest.setStackName(stackName);
      createRequest.setParameters(parameters);

      createRequest.setTemplateBody(convertStreamToString(this.getClass().getResourceAsStream(templatePath)));
      //createRequest.setTemplateBody(convertStreamToString(SampleECS.class.getResourceAsStream("/Users/shim/awsecs/cloudformation.template")));
      System.out.println("Creating a stack called " + createRequest.getStackName() + ".");
      stackbuilder.createStack(createRequest);

      // Wait for stack to be created
      // Note that you could use SNS notifications on the CreateStack call to track the progress of the stack creation
      System.out.println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackbuilder, stackName));

      // Show all the stacks for this account along with the resources for each stack
      for (Stack stack : stackbuilder.describeStacks(new DescribeStacksRequest()).getStacks()) {
        System.out.println("Stack : " + stack.getStackName() + " [" + stack.getStackStatus().toString() + "]");

        DescribeStackResourcesRequest stackResourceRequest = new DescribeStackResourcesRequest();
        stackResourceRequest.setStackName(stack.getStackName());
        for (StackResource resource : stackbuilder.describeStackResources(stackResourceRequest).getStackResources()) {
          System.out.format("    %1$-40s %2$-25s %3$s\n", resource.getResourceType(), resource.getLogicalResourceId(), resource.getPhysicalResourceId());
        }
      }

      System.out.println("Stack creation completed, the stack " + stackName + " completed with " + waitForCompletion(stackbuilder, stackName));

    } catch (AmazonServiceException ase) {
      System.out.println("Caught an AmazonServiceException, which means your request made it "
        + "to AWS CloudFormation, but was rejected with an error response for some reason.");
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    } catch (AmazonClientException ace) {
      System.out.println("Caught an AmazonClientException, which means the client encountered "
        + "a serious internal problem while trying to communicate with AWS CloudFormation, "
        + "such as not being able to access the network.");
      System.out.println("Error Message: " + ace.getMessage());
    }
  }

  public void createSecurityGroup(String stackName, String templatePath) throws Exception {
    List<Parameter> parameters = new ArrayList();
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("EnvironmentName");
      p1.setParameterValue("ecsZepl");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("VPC");
      p1.setParameterValue("vpc-46c30122");
      parameters.add(p1);
    }
    create("ecsSecurityGroup", "security-groups.yaml", parameters);
  }


  public void createALB(String stackName, String templatePath) throws Exception {
    List<Parameter> parameters = new ArrayList();
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("EnvironmentName");
      p1.setParameterValue("ecsZepl");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("VPC");
      p1.setParameterValue("vpc-46c30122");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("Subnets");
      p1.setParameterValue("subnet-b836fcdc,subnet-b343acc5");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("SecurityGroup");
      p1.setParameterValue("sg-3b5a9543");
      parameters.add(p1);
    }
    create("ecsALB", "load-balancers.yaml", parameters);
  }

  public void createEcsCluster(String stackName, String templatePath) throws Exception {
    List<Parameter> parameters = new ArrayList();
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("EnvironmentName");
      p1.setParameterValue("ecsZepl");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("VPC");
      p1.setParameterValue("vpc-46c30122");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("Subnets");
      p1.setParameterValue("subnet-b836fcdc,subnet-b343acc5");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("SecurityGroup");
      p1.setParameterValue("sg-90a769e8");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("KeyName");
      p1.setParameterValue("zeppelinhub_oregon");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("ECSInstanceProfile");
      p1.setParameterValue("zeplECSRole");
      parameters.add(p1);
    }
    create("ecsCluster", "ecs-cluster.yaml", parameters);
  }

  public void createEcsService(String stackName, String templatePath) throws Exception {
    List<Parameter> parameters = new ArrayList();
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("Cluster");
      p1.setParameterValue("ecsZepl");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("VPC");
      p1.setParameterValue("vpc-46c30122");
      parameters.add(p1);
    }
    {
      Parameter p1 = new Parameter();
      p1.setParameterKey("Listener");
      // this value from output of ALB
      p1.setParameterValue("arn:aws:elasticloadbalancing:us-west-2:812024471871:listener/app/ecsZepl/8873805f1a8f39c3/9eb0b8f66338a8f6");
      parameters.add(p1);
    }
    create("ecsService", "service.yaml", parameters);
  }

  public void createEcsTask() throws Exception {
    List<Parameter> parameters = new ArrayList();
    create("ecsTask", "task.yaml", parameters);
  }

  public static void main(String [] args) throws IOException {
    Cloudformation ecs = new Cloudformation();
    try {
      // create securityGroup
      // retrun value : ECSHostSecurityGroup, LoadBalancerSecurityGroup
      //ecs.createSecurityGroup("ecsSecurityGroup", "security-groups.yaml");

      // launch load-balancers.yaml
      // input value : LoadBalancerSecurityGroup
      // retrun value : Listener arn, LoadBalancerUrl, LoadBalancer
      // We don't need the ALB
      //ecs.createALB("ecsALB", "load-balancers.yaml");

      // launch ecs-cluster.yaml
      // input value : ECSHostSecurityGroup
      // retrun value :
      //ecs.createEcsCluster("ecsCluster", "ecs-cluster.yaml");

      // launch service.yaml
      // input value :
      // retrun value :
      // We don't need the Service
      //ecs.createEcsService("ecsService", "service.yaml");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
