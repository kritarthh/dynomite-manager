/**
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.nfsidecar.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.Instance;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ecs.model.Task;
import com.amazonaws.services.ecs.model.DescribeTasksRequest;
import com.amazonaws.services.ecs.model.DescribeTasksResult;
import com.amazonaws.services.ecs.model.ListTasksRequest;
import com.amazonaws.services.ecs.model.ListTasksResult;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClient;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.netflix.nfsidecar.identity.IMembership;
import com.netflix.nfsidecar.identity.InstanceEnvIdentity;
import com.netflix.nfsidecar.instance.InstanceDataRetriever;
import com.netflix.nfsidecar.resources.env.IEnvVariables;

/**
 * Class to query amazon ASG for its members to provide - Number of valid nodes
 * in the ASG - Number of zones - Methods for adding ACLs for the nodes
 */
public class EcsMembership implements IMembership {
    private static final Logger logger = LoggerFactory.getLogger(EcsMembership.class);
    private final ICredential provider;
    private final ICredential crossAccountProvider;
    private final InstanceEnvIdentity insEnvIdentity;
    private final InstanceDataRetriever retriever;
    private final IEnvVariables envVariables;

    @Inject
    public EcsMembership(ICredential provider, @Named("awsroleassumption") ICredential crossAccountProvider,
            InstanceEnvIdentity insEnvIdentity, InstanceDataRetriever retriever, IEnvVariables envVariables) {
        this.provider = provider;
        this.crossAccountProvider = crossAccountProvider;
        this.insEnvIdentity = insEnvIdentity;
        this.retriever = retriever;
        this.envVariables = envVariables;

    }

    @Override
    public List<String> getRacMembership() {
        AmazonECS client = null;
        try {
            client = getEcsClient();
            ListTasksRequest listReq = new ListTasksRequest().withCluster("qa-dynomite");
            ListTasksResult listRes = client.listTasks(listReq);

            DescribeTasksRequest desReq = new DescribeTasksRequest().withTasks(listRes.getTaskArns());
            DescribeTasksResult desRes = client.describeTasks(desReq);

            List<String> instanceIds = Lists.newArrayList();
            for (Task task: desRes.getTasks()) {
                if (task.getAvailabilityZone().equals(envVariables.getRack()))
                    instanceIds.add(task.getTaskArn());
            }
            logger.info(String.format("Querying Amazon returned following instance in the ASG: %s --> %s",
                    envVariables.getRack(), StringUtils.join(instanceIds, ",")));
            return instanceIds;
        } finally {
            if (client != null)
                client.shutdown();
        }
    }

    @Override
    public List<String> getCrossAccountRacMembership() {
        return getRacMembership();
    }

    /**
     * Actual membership AWS source of truth...
     */
    @Override
    public int getRacMembershipSize() {
        AmazonECS client = null;
        try {
            client = getEcsClient();
            ListTasksRequest listReq = new ListTasksRequest().withCluster("qa-dynomite");
            ListTasksResult listRes = client.listTasks(listReq);

            DescribeTasksRequest desReq = new DescribeTasksRequest().withTasks(listRes.getTaskArns());
            DescribeTasksResult desRes = client.describeTasks(desReq);

            List<String> instanceIds = Lists.newArrayList();
            for (Task task: desRes.getTasks()) {
                if (task.getAvailabilityZone().equals(envVariables.getRack()))
                    instanceIds.add(task.getTaskArn());
            }
            logger.info(String.format("Querying Amazon returned %d instances in the ASG: %s",
                    instanceIds.size(), envVariables.getRack()));
            return instanceIds.size();
        } finally {
            if (client != null)
                client.shutdown();
        }
    }

    /**
     * Cross-account member of AWS
     */
    @Override
    public int getCrossAccountRacMembershipSize() {
        return getRacMembershipSize();
    }

    /**
     * Adding peers' IPs as ingress to the running instance SG. The running
     * instance could be in "classic" or "vpc"
     */
    public void addACL(Collection<String> listIPs, int from, int to) {
    }

    /**
     * removes a iplist from the SG
     */
    public void removeACL(Collection<String> listIPs, int from, int to) {
    }

    /**
     * List SG ACL's
     */
    public List<String> listACL(int from, int to) {
        List<String> ipPermissions = new ArrayList<String>();
        return ipPermissions;
    }

    @Override
    public void expandRacMembership(int count) {
        AmazonAutoScaling client = null;
        try {
            client = getAutoScalingClient();
            DescribeAutoScalingGroupsRequest asgReq = new DescribeAutoScalingGroupsRequest()
                    .withAutoScalingGroupNames(envVariables.getRack());
            DescribeAutoScalingGroupsResult res = client.describeAutoScalingGroups(asgReq);
            AutoScalingGroup asg = res.getAutoScalingGroups().get(0);
            UpdateAutoScalingGroupRequest ureq = new UpdateAutoScalingGroupRequest();
            ureq.setAutoScalingGroupName(asg.getAutoScalingGroupName());
            ureq.setMinSize(asg.getMinSize() + 1);
            ureq.setMaxSize(asg.getMinSize() + 1);
            ureq.setDesiredCapacity(asg.getMinSize() + 1);
            client.updateAutoScalingGroup(ureq);
        } finally {
            if (client != null)
                client.shutdown();
        }
    }

    protected AmazonAutoScaling getAutoScalingClient() {
        AmazonAutoScaling client = new AmazonAutoScalingClient(provider.getAwsCredentialProvider());
        client.setEndpoint("autoscaling." + envVariables.getRegion() + ".amazonaws.com");
        return client;
    }

    protected AmazonAutoScaling getCrossAccountAutoScalingClient() {
        AmazonAutoScaling client = new AmazonAutoScalingClient(crossAccountProvider.getAwsCredentialProvider());
        client.setEndpoint("autoscaling." + envVariables.getRegion() + ".amazonaws.com");
        return client;
    }

    protected AmazonEC2 getEc2Client() {
        AmazonEC2 client = new AmazonEC2Client(provider.getAwsCredentialProvider());
        client.setEndpoint("ec2." + envVariables.getRegion() + ".amazonaws.com");
        return client;
    }

    protected AmazonEC2 getCrossAccountEc2Client() {
        AmazonEC2 client = new AmazonEC2Client(crossAccountProvider.getAwsCredentialProvider());
        client.setEndpoint("ec2." + envVariables.getRegion() + ".amazonaws.com");
        return client;
    }

    protected AmazonECS getEcsClient() {
        AmazonECS client = new AmazonECSClient(provider.getAwsCredentialProvider());
        client.setEndpoint("ecs." + envVariables.getRegion() + ".amazonaws.com");
        return client;
    }
}
