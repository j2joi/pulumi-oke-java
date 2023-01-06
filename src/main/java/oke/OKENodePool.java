package oke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pulumi.Context;
import com.pulumi.core.Output;
import com.pulumi.oci.ContainerEngine.NodePoolArgs;
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
import com.pulumi.oci.ContainerEngine.ContainerEngineFunctions;
import com.pulumi.oci.ContainerEngine.NodePool;

public class OKENodePool {
    private String compartment;
    private Output<String> okeImageID;
    private String bootVolumeSize;
    private String kubernetesVersion;
    private String workernodeOSImageTag;
    private Double node_memory;
    private Double number_ocpus;
    private Map<String, Object> freeFormTagMap = new HashMap<>();
    private Boolean useOptimizedOKEImages;
    private Integer number_worker_nodes;
    private Output<String> nodePoolName;
    
    public void setNumber_worker_nodes(Integer number_worker_nodes) {
        this.number_worker_nodes = number_worker_nodes;
    }

    private Output<List<NodePoolNodeConfigDetailsPlacementConfigArgs>> placementConfigs;
    private Context context;

    public void setWorkernodeOSImageTag(String workernodeOSImageTag) {
        this.workernodeOSImageTag = workernodeOSImageTag;
    }

    public void setCompartment(String compartment) {
        this.compartment = compartment;
    }

    public void setOkeImageID(Output<String> okeImageID) {
        this.okeImageID = okeImageID;
    }

    public void setBootVolumeSize(String bootVolumeSize) {
        this.bootVolumeSize = bootVolumeSize;
    }

    public void setKubernetesVersion(String kubernetesVersion) {
        this.kubernetesVersion = kubernetesVersion;
    }

    public Double getNode_memory() {
        return this.node_memory;
    }

    public void setNode_memory(Double node_memory) {
        this.node_memory = node_memory;
    }

    public Double getNumber_ocpus() {
        return this.number_ocpus;
    }

    public void setNumber_ocpus(Double number_ocpus) {
        this.number_ocpus = number_ocpus;
    }

    public Map<String,Object> getFreeFormTagMap() {
        return this.freeFormTagMap;
    }

    public void setFreeFormTagMap(Map<String,Object> freeFormTagMap) {
        this.freeFormTagMap = freeFormTagMap;
    }

    public Boolean isUseOptimizedOKEImages() {
        return this.useOptimizedOKEImages;
    }

    public Boolean getUseOptimizedOKEImages() {
        return this.useOptimizedOKEImages;
    }

    public void setUseOptimizedOKEImages(){
        var nodePoolOpts = GetNodePoolOptionArgs.builder()
                    .nodePoolOptionId("all")
                    .build();

        this.okeImageID = ContainerEngineFunctions.getNodePoolOption(nodePoolOpts).applyValue(
                    result -> getNodePoolImageId("", "", "latest", "", this.workernodeOSImageTag, "", result.sources()));

    }
    
    public void setRegulerComputeOSImages() {
        var imageArgs = GetImagesArgs.builder()
                    .operatingSystem("Oracle Linux")
                    .operatingSystemVersion("7.9")
                    .sortBy("TIMECREATED")
                    .sortOrder("DESC")
                    .compartmentId(this.compartment)
                    .build();

        this.okeImageID = CoreFunctions.getImages(imageArgs)
                    .applyValue(result -> result.images().get(0).id());                    
    }
    // public OKENodePool() {
    //     this.bootVolumeSize="60";
    //     this.node_memory=Double.valueOf(16);
    //     this.number_ocpus= Double.valueOf(2);        
    // }

    public OKENodePool(Context ctx, Output<String> _nodePoolName) {
        this.context = ctx;
        this.bootVolumeSize="60";
        this.node_memory=Double.valueOf(16);
        this.number_ocpus= Double.valueOf(2);
        this.nodePoolName = _nodePoolName;
    }

    public void createNodePool(Context ctx, com.pulumi.oci.ContainerEngine.Cluster okeCluster){
            NodePoolNodeConfigDetailsArgs nodeConfigDetails = NodePoolNodeConfigDetailsArgs.builder()
            .placementConfigs(this.placementConfigs)        
    // .placementConfigs(Output.of(placementConfigs))
            .size(this.number_worker_nodes)
            .freeformTags(freeFormTagMap)
            .build();

    NodePoolNodeShapeConfigArgs nodeShapeConfig = NodePoolNodeShapeConfigArgs.builder()
            .memoryInGbs(this.node_memory)
            .ocpus(this.number_ocpus)
            .build();
        NodePoolArgs nodePoolArgs;        
        NodePoolNodeSourceDetailsArgs nodeSourceDetails;
        nodeSourceDetails = NodePoolNodeSourceDetailsArgs.builder()
                    .imageId(this.okeImageID)
                    .sourceType("IMAGE")
                    .bootVolumeSizeInGbs(this.bootVolumeSize)
                    .build();

        nodePoolArgs = NodePoolArgs.builder()
                .clusterId(okeCluster.getId())
                .compartmentId(this.compartment)
                .kubernetesVersion(this.kubernetesVersion)
                .nodeShape("VM.Standard.E3.Flex")
                .name(this.nodePoolName)
                .nodeConfigDetails(nodeConfigDetails)
                .nodeShapeConfig(nodeShapeConfig)
                .nodeSourceDetails(nodeSourceDetails)
                .freeformTags(freeFormTagMap)
                .build();

        NodePool node_pool = new NodePool("nodePool", nodePoolArgs);
        ctx.export("Pool OCID", node_pool.getId());               
    }

    public void createNodePool(){
        this.createNodePool(null,null);
    }

    public void setPlacementConfigs(String workersNet){
        var placementConfigs = IdentityFunctions.getAvailabilityDomains(
            GetAvailabilityDomainsArgs.builder()
                .compartmentId(this.compartment).build()
            ).applyValue(ad -> createPlacementConfigs(ad.availabilityDomains(),workersNet));
        this.placementConfigs = placementConfigs;
    }

    private String getNodePoolImageId(String archType, String osDistro, String osVersion, String osRelease,
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

    private List<NodePoolNodeConfigDetailsPlacementConfigArgs> createPlacementConfigs(List<GetAvailabilityDomainsAvailabilityDomain> availabilityDomains, String subnetWorkernodesOcid){
        Utils.getInstance().logMessage(this.context, "avail Domains: %s", availabilityDomains, true);
        var placementConfigs = Arrays.asList(NodePoolNodeConfigDetailsPlacementConfigArgs.builder()
        .availabilityDomain(availabilityDomains.get(0).name())
        .subnetId(subnetWorkernodesOcid)
        .build());
        
        if (availabilityDomains.size() > 1) {
            Utils.getInstance().logMessage(this.context, "Region with Multiple AD: %s", availabilityDomains, true);
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
    
}



