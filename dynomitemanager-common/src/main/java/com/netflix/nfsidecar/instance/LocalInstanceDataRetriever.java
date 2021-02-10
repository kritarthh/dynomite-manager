package com.netflix.nfsidecar.instance;

/**
 * Looks at local (system) properties for metadata about the running 'instance'.
 * Typically, this is used for locally-deployed testing.
 *
 * @author jason brown
 */
public class LocalInstanceDataRetriever implements InstanceDataRetriever
{
    private static final String PREFIX = "nemo.localInstance.";

    public String getRac()
    {
        return System.getProperty(PREFIX + "availabilityZone", "localzone");
    }

    public String getPublicHostname()
    {
        return System.getProperty(PREFIX + "publicHostname", "localhost");
    }

    public String getPublicIP()
    {
        return System.getProperty(PREFIX + "publicIp", "127.0.0.1");
    }

    public String getInstanceId()
    {
        return System.getProperty(PREFIX + "instanceId", "127.0.0.1");
    }

    public String getInstanceType()
    {
        return System.getProperty(PREFIX + "instanceType", "local");
    }
    
    public String getMac() {
        return System.getProperty(PREFIX + "instanceMac", "10:10:10:10:10");
    }

    @Override
    public String getVpcId() {
        throw new UnsupportedOperationException("Not applicable as running instance is in classic environment");
    }
}
