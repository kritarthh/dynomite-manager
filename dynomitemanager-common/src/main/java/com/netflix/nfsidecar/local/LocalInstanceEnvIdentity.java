package com.netflix.nfsidecar.local;

import com.netflix.nfsidecar.identity.InstanceEnvIdentity;
import com.netflix.nfsidecar.instance.InstanceDataRetriever;
import com.netflix.nfsidecar.instance.LocalInstanceDataRetriever;

/**
 * A means to determine if running instance is local
 */
public class LocalInstanceEnvIdentity implements InstanceEnvIdentity {

    private Boolean isLocal = false, isClassic = false, isEcs = false, isDefaultVpc = false, isNonDefaultVpc = false;

    public LocalInstanceEnvIdentity() {
        this.isLocal = true;
    }

    @Override
    public Boolean isClassic() {
        return this.isClassic;
    }

    @Override
    public Boolean isEcs() {
        return this.isEcs;
    }

    @Override
    public Boolean isLocal() {
        return this.isLocal;
    }

    @Override
    public Boolean isDefaultVpc() {
        return this.isDefaultVpc;
    }

    @Override
    public Boolean isNonDefaultVpc() {
        return this.isNonDefaultVpc;
    }

}
