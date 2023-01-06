package oke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.pulumi.Config;
import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.oci.ContainerEngine.ClusterArgs;

import com.pulumi.oci.ContainerEngine.NodePool;
import com.pulumi.oci.ContainerEngine.NodePoolArgs;
import com.pulumi.oci.ContainerEngine.inputs.ClusterEndpointConfigArgs;
import com.pulumi.oci.ContainerEngine.inputs.ClusterOptionsArgs;
import com.pulumi.oci.ContainerEngine.inputs.ClusterOptionsKubernetesNetworkConfigArgs;
import com.pulumi.oci.ContainerEngine.inputs.GetNodePoolOptionArgs;
import com.pulumi.oci.ContainerEngine.inputs.NodePoolNodeConfigDetailsArgs;
import com.pulumi.oci.ContainerEngine.inputs.NodePoolNodeConfigDetailsPlacementConfigArgs;
import com.pulumi.oci.ContainerEngine.inputs.NodePoolNodeShapeConfigArgs;
import com.pulumi.oci.ContainerEngine.inputs.NodePoolNodeSourceDetailsArgs;
import com.pulumi.oci.ContainerEngine.outputs.GetNodePoolOptionSource;
import com.pulumi.oci.Core.CoreFunctions;
import com.pulumi.oci.Core.inputs.GetImagesArgs;
import com.pulumi.oci.Identity.IdentityFunctions;
import com.pulumi.oci.Identity.inputs.GetAvailabilityDomainsArgs;
import com.pulumi.oci.Identity.outputs.GetAvailabilityDomainsAvailabilityDomain;


public class Cluster {

    public static void main(String[] args) {
        Pulumi.run(Cluster::stack);
    }

    private static Boolean DEBUG = true;
    private static Config config;
    
//             .valueOf(System.currentTimeMillis() % TimeUnit.SECONDS.toMillis(10L));
    private static void initConfig(Context ctx){
        config = ctx.config();                
    }


    private static void logMessage(Context ctx, Output object){
        
    }

    private static void createOKECluster(Context ctx, Config config) {

    }

    private static void createNodePoolOKE(Context ctx) {

    }

    private static void stack(Context ctx) {
        initConfig(ctx); 
        String targetCompartmentId = "ocid1.compartment.oc1..aaaaaaaaqiu6t7sniwnleea5i2hhdco4ahp4426uhk2ikcgisqal64l5i6vq";
        var network_config = config.requireObject("subnet", Map.class);
        var oke_config = config.requireObject("oke", Map.class);
        String kubernetesVersion = (String) oke_config.get("version");
        Utils.getInstance().logMessage(ctx, "oke Version: %s", kubernetesVersion, DEBUG);
        
        
        // List<String> subnetsLbOcid = Arrays
                // .asList("ocid1.subnet.oc1.iad.aaaaaaaan6f3abztjx65brly5eh5v2tbc4tplhqabblg457zluwkkhfzt6pq");
        List<String> subnetsLbOcid = Arrays.asList((String)network_config.get("lb"));
        Utils.getInstance().logMessage(ctx, "lb: %s", subnetsLbOcid.toString(), DEBUG);

        // String subnetWorkernodesOcid = "ocid1.subnet.oc1.iad.aaaaaaaakwjtfpnsizjnbsygilbtzl2fngrpndzfba4ybofgqim3masy3mgq";
        String subnetWorkernodesOcid =  (String) network_config.get("workers");
        Utils.getInstance().logMessage(ctx, "workers: %s", subnetWorkernodesOcid, DEBUG);

        // String subnetEndpointOcid = "ocid1.subnet.oc1.iad.aaaaaaaaxmfgzncuobrh6hmfljwdx26wixerd32wkf4qhx4rdqtqufc3x7mq";
        String subnetEndpointOcid = (String) network_config.get("endpoint");
        // String vcnOcid = "ocid1.vcn.oc1.iad.amaaaaaadoggtjaasym4jch6mdlvym3lm6upcm22mfger4ghqyabh2k6uefa";
        String vcnOcid= ctx.config("oke").require("vcn");
        Utils.getInstance().logMessage(ctx, "target vcn id: %s", vcnOcid, DEBUG);
        String tag_path_source = "jb-dlp/pulumi/java/oke";
        
        Map<String, Object> freeFormTagMap = new HashMap<>();
        freeFormTagMap.put("triggered-by", tag_path_source);

        // String workerNodeOS = "Oracle Linux";
        // String workerNodeOSversion = "7.9";
        // String workerNodeOSRelease = "";
        // String arch = "";
        // int number_worker_nodes = 3;
        
        ClusterEndpointConfigArgs endpointConfig = ClusterEndpointConfigArgs.builder()
                .isPublicIpEnabled(true)
                .subnetId(subnetEndpointOcid)
                .build();

        ClusterOptionsArgs okeClusterOptsArgs = ClusterOptionsArgs.builder()
                .kubernetesNetworkConfig(ClusterOptionsKubernetesNetworkConfigArgs.builder()
                        .podsCidr("10.244.0.0/16")
                        .servicesCidr("10.96.0.0/16")
                        .build())
                .serviceLbSubnetIds(subnetsLbOcid)
                .build();

        com.pulumi.oci.ContainerEngine.Cluster okeCluster = new com.pulumi.oci.ContainerEngine.Cluster("okeJava",
                ClusterArgs.builder()
                        .compartmentId(targetCompartmentId)
                        .kubernetesVersion(kubernetesVersion)
                        .name(Utils.getInstance().randomName(ctx.projectName()))
                        .options(okeClusterOptsArgs)
                        .endpointConfig(endpointConfig)
                        .vcnId(vcnOcid)
                        .freeformTags(freeFormTagMap)
                        .build());
        // cluster Created.
        ctx.export("Cluster OCID", okeCluster.getId());

        ////////////////////////////// Node Pool  //////////////////////////////
        int number_worker_nodes = ctx.config().getInteger("oke.nodes").orElse(2);
        var okeNodePool = new OKENodePool(ctx, Utils.getInstance().randomName(ctx.projectName()+"np"));
        okeNodePool.setCompartment(targetCompartmentId); 
        okeNodePool.setFreeFormTagMap(freeFormTagMap);
        okeNodePool.setKubernetesVersion(kubernetesVersion);
        okeNodePool.setNumber_worker_nodes(number_worker_nodes);
        Boolean useOptimizedOKEImages = (Boolean) oke_config.getOrDefault("use_oke_images",true);
        if(useOptimizedOKEImages){
                String kubernetesWorkerImage = (String) oke_config.get("node_version");
                Utils.getInstance().logMessage(ctx, "okeWorkerNodeImageVersion: %s", kubernetesWorkerImage, DEBUG);
                okeNodePool.setWorkernodeOSImageTag(kubernetesWorkerImage);
                okeNodePool.setUseOptimizedOKEImages();
        }
        else{
                okeNodePool.setRegulerComputeOSImages();
        }
        okeNodePool.setPlacementConfigs(subnetWorkernodesOcid);
        okeNodePool.createNodePool(ctx, okeCluster);
       

        
    }
}
