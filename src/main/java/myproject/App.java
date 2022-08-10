package myproject;

import com.pulumi.Pulumi;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.pulumi.*;
import com.pulumi.core.Output;
import com.pulumi.oci.ContainerEngine.*;
import com.pulumi.oci.ContainerEngine.inputs.ClusterEndpointConfigArgs;
import com.pulumi.oci.ContainerEngine.inputs.ClusterOptionsArgs;
import com.pulumi.oci.ContainerEngine.inputs.ClusterOptionsKubernetesNetworkConfigArgs;
import com.pulumi.oci.ContainerEngine.inputs.GetNodePoolOptionArgs;
import com.pulumi.oci.ContainerEngine.inputs.NodePoolNodeConfigDetailsArgs;
import com.pulumi.oci.ContainerEngine.inputs.NodePoolNodeConfigDetailsPlacementConfigArgs;
import com.pulumi.oci.ContainerEngine.inputs.NodePoolNodeShapeConfigArgs;
import com.pulumi.oci.ContainerEngine.inputs.NodePoolNodeSourceDetailsArgs;
import com.pulumi.oci.Core.CoreFunctions;
import com.pulumi.oci.Core.inputs.GetImagesArgs;
import com.pulumi.oci.Identity.IdentityFunctions;
import com.pulumi.oci.Identity.inputs.*;
import com.pulumi.oci.Identity.outputs.GetAvailabilityDomainsAvailabilityDomain;
import com.pulumi.oci.ContainerEngine.outputs.GetNodePoolOptionSource;

import static java.util.stream.Collectors.toMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        Pulumi.run(App::stack);
    }

    private static void stack(Context ctx) {
        String targetCompartmentId = "ocid1.compartment.oc1..aaaaaaaad34mhyme6neawehq56sndcl5no4q4yqvdcsak5piqpbmq36mreqa";
        String kubernetesVersion = "v1.23.4";
        String kubernetesWorkerImage = "1.23.4";
        List<String> subnetsLbOcid = Arrays
                .asList("ocid1.subnet.oc1.iad.aaaaaaaan6f3abztjx65brly5eh5v2tbc4tplhqabblg457zluwkkhfzt6pq");

        String subnetWorkernodesOcid = "ocid1.subnet.oc1.iad.aaaaaaaakwjtfpnsizjnbsygilbtzl2fngrpndzfba4ybofgqim3masy3mgq";

        String subnetEndpointOcid = "ocid1.subnet.oc1.iad.aaaaaaaaxmfgzncuobrh6hmfljwdx26wixerd32wkf4qhx4rdqtqufc3x7mq";

        String vcnOcid = "ocid1.vcn.oc1.iad.amaaaaaadoggtjaasym4jch6mdlvym3lm6upcm22mfger4ghqyabh2k6uefa";
        String tag_path_source = "jb-dlp/pulumi/java/oke";
        Map<String, Object> freeFormTagMap = new HashMap<>();
        freeFormTagMap.put("triggered-by", tag_path_source);

        String workerNodeOS = "Oracle Linux";
        String workerNodeOSversion = "7.9";
        String workerNodeOSRelease = "";
        String arch = "";
        int number_worker_nodes = 1;
        double default_node_memory_size = 16;
        double default_node_num_ocpus = 1;

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

        com.pulumi.oci.ContainerEngine.Cluster okeCluster = new Cluster("firstOKEJAVA",
                ClusterArgs.builder()
                        .compartmentId(targetCompartmentId)
                        .kubernetesVersion(kubernetesVersion)
                        .name("okeByJavaPulumi")
                        .options(okeClusterOptsArgs)
                        .endpointConfig(endpointConfig)
                        .vcnId(vcnOcid)
                        .freeformTags(freeFormTagMap)
                        .build());
        // cluster Created.
        ctx.export("Cluster OCID", okeCluster.getId());

    
        var placementConfigs = IdentityFunctions.getAvailabilityDomains(
            GetAvailabilityDomainsArgs.builder()
                .compartmentId(targetCompartmentId).build()
            ).thenApply(ad -> createPlacementConfigs(ad.availabilityDomains(),subnetWorkernodesOcid));
        
        NodePoolNodeConfigDetailsArgs nodeConfigDetails = NodePoolNodeConfigDetailsArgs.builder()
                .placementConfigs(Output.of(placementConfigs))
                .size(number_worker_nodes)
                .freeformTags(freeFormTagMap)
                .build();

        NodePoolNodeShapeConfigArgs nodeShapeConfig = NodePoolNodeShapeConfigArgs.builder()
                .memoryInGbs(default_node_memory_size)
                .ocpus(default_node_num_ocpus)
                .build();

        Output<String> okeImageID; 
        NodePoolArgs nodePoolArgs;        
        NodePoolNodeSourceDetailsArgs nodeSourceDetails;

        var use_oke_images = true;
        if (!use_oke_images) {
            var imageArgs = GetImagesArgs.builder()
                    .operatingSystem("Oracle Linux")
                    .operatingSystemVersion("7.9")
                    .sortBy("TIMECREATED")
                    .sortOrder("DESC")
                    .compartmentId(targetCompartmentId)
                    .build();

            okeImageID = Output.of(CoreFunctions.getImages(imageArgs)
                    .thenApply(result -> result.images().get(0).id()));
            
        } else {
            var nodePoolOpts = GetNodePoolOptionArgs.builder()
                    .nodePoolOptionId("all")
                    .build();

            okeImageID = Output.of(ContainerEngineFunctions.getNodePoolOption(nodePoolOpts).thenApply(
                    result -> getNodePoolImageId("", "", "latest", "", kubernetesWorkerImage, "", result.sources())));

        }
        nodeSourceDetails = NodePoolNodeSourceDetailsArgs.builder()
                    .imageId(okeImageID)
                    .sourceType("IMAGE")
                    .bootVolumeSizeInGbs("60")
                    .build();

        nodePoolArgs = NodePoolArgs.builder()
                .clusterId(okeCluster.getId())
                .compartmentId(targetCompartmentId)
                .kubernetesVersion(kubernetesVersion)
                .nodeShape("VM.Standard.E3.Flex")
                .name("E3Flex")
                .nodeConfigDetails(nodeConfigDetails)
                .nodeShapeConfig(nodeShapeConfig)
                .nodeSourceDetails(nodeSourceDetails)
                .freeformTags(freeFormTagMap)
                .build();

        NodePool node_pool = new NodePool("nodePool", nodePoolArgs);
        ctx.export("Pool OCID", node_pool.getId());        
    }

    private static List<NodePoolNodeConfigDetailsPlacementConfigArgs> createPlacementConfigs(List<GetAvailabilityDomainsAvailabilityDomain> availabilityDomains, String subnetWorkernodesOcid){
        var placementConfigs = Arrays.asList(NodePoolNodeConfigDetailsPlacementConfigArgs.builder()
        .availabilityDomain(availabilityDomains.get(0).id())
        .subnetId(subnetWorkernodesOcid)
        .build());
        
        if (availabilityDomains.size() > 1) {
            var nodePoolPlacementConfigArgs1 = NodePoolNodeConfigDetailsPlacementConfigArgs.builder()
                    .availabilityDomain(availabilityDomains.get(0).name())
                    .subnetId(subnetWorkernodesOcid)
                    .build();

            var nodePoolPlacementConfigArgs2 = NodePoolNodeConfigDetailsPlacementConfigArgs.builder()
                    .availabilityDomain(availabilityDomains.get(1).name())
                    .subnetId(subnetWorkernodesOcid)
                    .build();

            var nodePoolPlacementConfigArgs3 = NodePoolNodeConfigDetailsPlacementConfigArgs.builder()
                    .availabilityDomain(availabilityDomains.get(2).name())
                    .subnetId(subnetWorkernodesOcid)
                    .build();

            placementConfigs = Arrays.asList(nodePoolPlacementConfigArgs1, nodePoolPlacementConfigArgs2,
                    nodePoolPlacementConfigArgs3);
        }
     
        return placementConfigs;
    }

    private static String getNodePoolImageId(String archType, String osDistro, String osVersion, String osRelease,
            String okeVersion, String okeNodePoolBuild, List<GetNodePoolOptionSource> okeImages) {
        // Oracle-Linux-7.9-2022.06.30-0-OKE-1.23.4-392
        // osDistro - osVersion - osRelease -OKE - okeVersion- okeNodePoolBuild

        // Oracle-Linux-/7.9-/Gen2-GPU-/2022.04.25-0-/OKE-/1.23.4-/251
        // Oracle-Linux-/7.9-/aarch64-/2022.04.26-0-/OKE-/1.23.4-/246
        // osDistro - osVersion - archType - osRelease -OKE - okeVersion-
        // okeNodePoolBuild

        // osVersion == Latest && archType = empty =>
        // Oracle-Linux-7.9-2022.06.30-0-OKE-1.23.4-392
        // Sort list by date , match okeVersion => sort by osVersion, osRelease
        // Oracle-Linux-8.6-2022.06.30-0-OKE-1.21.5-392 Oracle-Linux- (latest:8.6)-
        // archTypeEmpty- * -OKE-okeVersion - higher build.

        // osVersion == Number && archType = "Gen2, aarch64, empty" =>
        // Oracle-Linux-osVersion-archType - Sort Order date -OKE - okeVersion - {higher
        // number}

        List<GetNodePoolOptionSource> result = new ArrayList<>(okeImages);
        Comparator<GetNodePoolOptionSource> byImageSourceName = Comparator
                .comparing(GetNodePoolOptionSource::sourceName);
        result.sort(byImageSourceName);
        GetNodePoolOptionSource mostRecentImage = result.get(okeImages.size() - 1);
        String mostRecentOSversion = mostRecentImage.sourceName().split("-")[2];
        var pattern1 = "Oracle-Linux-" + mostRecentOSversion;
        var pattern2 = "-OKE-" + okeVersion;
        final String predicate = pattern1 + "-2"; // -2 is patter for year 2. This code will not exist in 3X year ;)
        result = result.stream().filter(s -> s.sourceName().contains(predicate) && s.sourceName().contains(pattern2))
                .collect(Collectors.toList());
        // for now this using latest.
        if (osVersion.contains("latest")) {
            if (!archType.isEmpty()) {
                final String archPattern = pattern1 + "-" + archType;
                result = result.stream()
                        .filter(s -> s.sourceName().contains(archPattern) && s.sourceName().contains(pattern2))
                        .collect(Collectors.toList());
            }
            result.sort(byImageSourceName);
            mostRecentImage = result.get(result.size() - 1);            
        }
        return mostRecentImage.imageId();
    }
}
