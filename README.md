# pulumi-oke-java
Create a Single Oracle Kubernetes Engine cluster in Oracle Cloud Infrastructure

This is a demo Purpose repo.


##Create new Stack  add fields
 oke-mvn:oke:
    node_version: 1.23.4 #If using optimized OKE OCI images. specificy OS Image version.
    nodes: 2  # cluster nodes
    version: v1.23.4   # Kubernetes Version
    use_oke_images: true
 oke-mvn:subnet:
    endpoint: ocid1.subnet.oc1.xxx.jdasfjadsfadsf    # Subnet for OKE API endpoint 
    lb: ocid1.subnet.oc1.yyy.asdfjaldsf.asdf    # Subnet for ingress/LBs
    workers: ocid1.subnet.oc1.zzz.ljasdfjladsfjldsaf  # Subnet for OKE nodes
