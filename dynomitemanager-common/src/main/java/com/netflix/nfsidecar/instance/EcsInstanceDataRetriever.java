package com.netflix.nfsidecar.instance;

import com.netflix.nfsidecar.utils.SystemUtils;

/**
 * Calls AWS ecs metadata to get info on the location of the running instance.
 *
 */
public class EcsInstanceDataRetriever implements InstanceDataRetriever {
    public String getRac() {
        return SystemUtils.getDataFromUrl("http://169.254.169.254/latest/meta-data/placement/availability-zone");
    }

    public String getPublicHostname() {
        return System.getenv("EXTERNAL_SERVER_IP");
    }

    public String getPublicIP() {
        return System.getenv("EXTERNAL_SERVER_IP");
    }

    public String getInstanceId() {
        return System.getenv("TASK_ARN");
        // return SystemUtils.getDataFromUrl("http://169.254.169.254/latest/meta-data/instance-id");
    }

    public String getInstanceType() {
        return SystemUtils.getDataFromUrl("http://169.254.169.254/latest/meta-data/instance-type");
    }

    @Override
    /*
     * @return id of the network interface for running instance
     */
    public String getMac() {
        return SystemUtils.getDataFromUrl("http://169.254.169.254/latest/meta-data/network/interfaces/macs/").trim();
    }

    @Override
    public String getVpcId() {
        throw new UnsupportedOperationException("Not applicable as running instance is in classic environment");
    }

}
