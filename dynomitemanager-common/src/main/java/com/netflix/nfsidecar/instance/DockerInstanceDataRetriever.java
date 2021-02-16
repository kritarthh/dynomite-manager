package com.netflix.nfsidecar.instance;

import com.netflix.nfsidecar.resources.env.IEnvVariables;

/**
 * Looks at local (system) properties for metadata about the running 'instance'.
 * Typically, this is used for locally-deployed testing.
 *
 * @author jason brown
 */
public class DockerInstanceDataRetriever implements InstanceDataRetriever
{
    public String getRac()
    {
        return System.getenv("AZ");
    }

    public String getPublicHostname()
    {
        return System.getenv("EXTERNAL_SERVER_IP");
    }

    public String getPublicIP()
    {
        return System.getenv("EXTERNAL_SERVER_IP");
    }

    public String getInstanceId()
    {
        return System.getenv("INTERNAL_SERVER_IP");
    }

    public String getInstanceType()
    {
        return "docker-task";
    }

    public String getMac() {
        return "11:11:11:11:11";
    }

    @Override
    public String getVpcId() {
        throw new UnsupportedOperationException("Not applicable as running instance is in classic environment");
    }
}
