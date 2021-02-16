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
package com.netflix.nfsidecar.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.netflix.nfsidecar.identity.IMembership;
import com.netflix.nfsidecar.identity.InstanceEnvIdentity;
import com.netflix.nfsidecar.instance.InstanceDataRetriever;
import com.netflix.nfsidecar.resources.env.IEnvVariables;
import com.netflix.nfsidecar.utils.SystemUtils;

public class LocalMembership implements IMembership {
    private static final Logger logger = LoggerFactory.getLogger(LocalMembership.class);
    private final InstanceEnvIdentity insEnvIdentity;
    private final InstanceDataRetriever retriever;
    private final IEnvVariables envVariables;

    @Inject
    public LocalMembership(
            InstanceEnvIdentity insEnvIdentity, InstanceDataRetriever retriever, IEnvVariables envVariables) {
        this.insEnvIdentity = insEnvIdentity;
        this.retriever = retriever;
        this.envVariables = envVariables;
    }

    @Override
    public List<String> getRacMembership() {
        try {
            String rack = envVariables.getRack();
            List<String> instanceIds = Lists.newArrayList();
            String subnet = retriever.getPublicIP().substring(0, retriever.getPublicIP().length() - 1);
            for (int i = 0; i <= 9; ++i) {
                String ip = String.format("%s%d", subnet, i);
                try {
                    if (!SystemUtils.getDataFromUrl(String.format("http://%s:8000/", ip)).trim().isEmpty()) {
                        instanceIds.add(ip);
                    }
                }
                catch (Exception e) {
                    logger.warn("failed to fetch data for %s", ip);
                }
            }
            logger.info(String.format("Querying local returned following instance in the Rack: %s --> %s",
                    rack, StringUtils.join(instanceIds, ",")));
            return instanceIds;
        } finally {
        }
    }

    @Override
    public List<String> getCrossAccountRacMembership() {
        return this.getRacMembership();
    }

    /**
     * Actual membership AWS source of truth...
     */
    @Override
    public int getRacMembershipSize() {
        String rack = envVariables.getRack();
        List<String> instanceIds = Lists.newArrayList();
        String subnet = retriever.getPublicIP().substring(0, retriever.getPublicIP().length() - 1);
        for (int i = 0; i <= 9; ++i) {
            String ip = String.format("%s%d", subnet, i);
            try {
                if (!SystemUtils.getDataFromUrl(String.format("http://%s:8000/", ip)).trim().isEmpty()) {
                    instanceIds.add(ip);
                }
            }
            catch (Exception e) {
                logger.warn("failed to fetch data for %s", ip);
            }
        }
        logger.info(String.format("Querying local returned %d instances in the Rack: %s",
                                  instanceIds.size(), rack));
        return instanceIds.size();
    }

    @Override
    public int getCrossAccountRacMembershipSize() {
        return getRacMembershipSize();
    }

    /**
     * Adding peers' IPs as ingress to the running instance SG. The running
     * instance could be in "classic" or "vpc"
     */
    @Override
    public void addACL(Collection<String> listIPs, int from, int to) {
    }

    /*
     * @return SG group id for a group name, vpc account of the running
     * instance. ACLGroupName = Cluster name and VPC-ID is the VPC We need both
     * filters to find the SG for that cluster and that vpc-id
     */

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
    }

}
