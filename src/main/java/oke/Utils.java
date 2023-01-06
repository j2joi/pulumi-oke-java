package oke;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.pulumi.Context;
import com.pulumi.core.Output;
import com.pulumi.oci.Identity.outputs.GetAvailabilityDomainsAvailabilityDomain;
import com.pulumi.random.RandomString;
import com.pulumi.random.RandomStringArgs;

public class Utils{

    private RandomString RESOURCE_SUFFIX;    
    private static Utils _utils = new Utils();
    private Utils(){
        RESOURCE_SUFFIX = new RandomString("random", RandomStringArgs.builder()        
        .length(5)
        .overrideSpecial("/@£$")
        .special(true)
        .build());
    }

    public static Utils getInstance(){
        return _utils;
    }

    public Output<String> randomName(String name){
        return RESOURCE_SUFFIX.result().applyValue(result -> String.format(String.format(
                "%s-%s", name, result)));
    }
    public Output<String> randomName(String name, String format){
        return RESOURCE_SUFFIX.result().applyValue(result -> String.format(format, result));
    }

    public void logMessage(Context ctx, String format, String message, boolean DEBUG){
        if (DEBUG) {
                ctx.log().info(String.format(format, message));
        }
    }

    public void logMessage(Context ctx, String format, List<GetAvailabilityDomainsAvailabilityDomain> objects, boolean DEBUG){
        if (DEBUG) {
                ctx.log().info(String.format(format, objects.stream().map(GetAvailabilityDomainsAvailabilityDomain::name).collect(Collectors.toList())));
        }
    }

}