package oke;

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

/* * Java Program to convert a List to Map in Java 8. * We'll convert an ArrayList of String to an HashMap * where key is String and value is their length */

public class Demo {

     class OKEImages{
         /**
         * @return The OCID of the image.
         * 
         */
        private final String imageId;
        /**
         * @return The user-friendly name of the entity corresponding to the OCID.
         * 
         */
        private final String sourceName;
        /**
         * @return The source type of this option. `IMAGE` means the OCID is of an image.
         * 
         */
        private final String sourceType;

     
        public OKEImages(String imageId, String sourceName, String sourceType){
            this.imageId = imageId;
            this.sourceName = sourceName;
            this.sourceType = sourceType;
        }

        /**
         * @return The OCID of the image.
         * 
         */
        public String imageId() {
            return this.imageId;
        }
        /**
         * @return The user-friendly name of the entity corresponding to the OCID.
         * 
         */
        public String sourceName() {
            return this.sourceName;
        }
        /**
         * @return The source type of this option. `IMAGE` means the OCID is of an image.
         * 
         */
        public String sourceType() {
            return this.sourceType;
        }
        
        public String toString(){
            return this.imageId+" --> "+this.sourceName+"\n";
        }
    }
    public static void main(String[] args) throws Exception { // an ArrayList of String object
        var demo = new Demo();
        List<OKEImages> listOfString = new ArrayList<OKEImages>();
        // OKEImages images = new OKEImages("id","","IMAGE");
        var images = demo.new OKEImages("id","Oracle-Linux-7.9-Gen2-GPU-2022.06.30-0","IMAGE");
        listOfString.add(images);
        images = demo.new OKEImages("id","Oracle-Linux-7.9-Gen2-GPU-2022.05.31-0","IMAGE");
        listOfString.add(images);
        images = demo.new OKEImages("id","Oracle-Linux-7.9-Gen2-GPU-2022.04.25-0","IMAGE");
        listOfString.add(images);
        images = demo.new OKEImages("id","Oracle-Linux-7.9-aarch64-2022.06.30-0-OKE-1.21.5-392","IMAGE");
        listOfString.add(images);
        images = demo.new OKEImages("id","Oracle-Linux-8.6-aarch64-2022.05.30-0-OKE-1.21.5-311","IMAGE");
        listOfString.add(images);
        images = demo.new OKEImages("id","Oracle-Linux-8.6-aarch64-2022.05.30-0-OKE-1.21.5-392","IMAGE");
        listOfString.add(images);
        images = demo.new OKEImages("id","Oracle-Linux-8.6-2022.06.30-0-OKE-1.21.5-392","IMAGE");
        listOfString.add(images);
        images = demo.new OKEImages("id","Oracle-Linux-8.5-aarch64-2022.04.26-0-OKE-1.21.5-246","IMAGE");
        listOfString.add(images);
        images = demo.new OKEImages("id","Oracle-Linux-8.6-2022.06.30-0-OKE-1.21.5-394","IMAGE");
        listOfString.add(images);
        // OKEImages images = new OKEImages("id","","IMAGE");
        // OKEImages images = new OKEImages("id","","IMAGE");
        // OKEImages images = new OKEImages("id","","IMAGE");
        System.out.println("list of string: " + listOfString);
        // converting ArrayList to HashMap before Java 8
        Map<String, OKEImages> map = new HashMap<>();
        for (OKEImages image : listOfString) {
            map.put(image.sourceName, image);
        }
        System.out.println("generated map: " + map);
        // converting List to Map in Java 8 using lambda expression
        // TreeMap<String, OKEImages> map8 = listOfString.stream().collect(toMap(s -> s.sourceName(), s -> s));
        TreeMap<String, OKEImages> map8 = listOfString.stream().collect(toMap(OKEImages::sourceName,Function.identity(), (o, n) -> n, TreeMap::new));
        System.out.println("generated map8: " + map8);

        // List<OKEImages> values =  map.keySet().stream().filter(key -> key.contains("1.21.5")&& key.contains("OKE")).map(map::get).collect(Collectors.toList());
        List<OKEImages> values =  map.keySet().stream().filter(key -> key.contains("1.21.5")).map(map::get).collect(Collectors.toList());
        System.out.println("filtered map8: " + values);

        String okeVersion= "1.21.5";
        // System.out.println("filtered list8: " + listOfString.stream().filter(key->key.sourceName.contains("1.21.5")));  
        Map<String,OKEImages> thisisIt = listOfString.stream().filter(key->key.sourceName.contains(okeVersion)&& !key.sourceName.contains("arch")).collect(toMap(OKEImages::sourceName,Function.identity(), (o, n) -> n, TreeMap::new));
        System.out.println("resulst most recent: " + thisisIt.size());  


        // Comparator<OKEImages> byImageId = (b1, b2) -> b1.sourceName().compareTo(b2.sourceName());
        
        String arch= "";
        OKEImages last;
        OKEImages recent;
        String recentOSversion;
        Comparator<OKEImages> byImageSourceName = Comparator.comparing(OKEImages::sourceName);
        listOfString.sort(byImageSourceName);
        OKEImages mostRecentImage = listOfString.get(listOfString.size()-1);
        String mostRecentOSversion = mostRecentImage.sourceName.split("-")[2];
        var okeImage="latest";
        if (okeImage.contains("latest")){
            // var pattern="Oracle-Linux-"+mostRecentImage+"-2"+"([0-9]+(.[0-9]+)+).*-OKE-.*0";
            var pattern1 ="Oracle-Linux-"+mostRecentOSversion;
            var pattern2 ="-OKE-"+okeVersion; 
            if (arch.isEmpty()){
                final String predicate=pattern1 + "-2"; // -2 is patter for year 2. This code will not exist in 3X year ;)
                values=listOfString.stream().filter(s -> s.sourceName().contains(predicate)&& s.sourceName().contains(pattern2)).collect(Collectors.toList());
            }
            else{
                final String predicate=pattern1+"-"+arch;
                values=listOfString.stream().filter(s -> s.sourceName().contains(predicate)&& s.sourceName().contains(pattern2)).collect(Collectors.toList());
            } 
            System.out.println("\n\n patterns : "+pattern1+"...."+pattern2);
            // var listResult=listOfString.stream().filter(s -> s.sourceName().contains(pattern1)&& s.sourceName().contains(pattern2)).collect(Collectors.toList());
            values.sort(byImageSourceName);
            var image = values.get(values.size()-1);
            System.out.println("\n\n latest : "+image);
            // final Pattern pattern = Pattern.compile("Oracle-Linux-\\d\\.\\d-2022\\.06\\.30-0-OKE-1\\.21\\.5-392", Pattern.CASE_INSENSITIVE);
            // Match regex against input
            // final Matcher matcher = pattern.matcher(input);
        }
        
        // if (arch.isEmpty()){            
        //     List<OKEImages> listOfString2 = listOfString.stream().filter(key->key.sourceName.contains(okeVersion)&& !key.sourceName.contains("")).collect(Collectors.toList());       
        // }
        // List<OKEImages> listOfString2 = listOfString.stream().filter(key->key.sourceName.contains(okeVersion)&& !key.sourceName.contains("")).collect(Collectors.toList());
        // listOfString2.sort(byImageSourceName);
        // System.out.println("sorted list: " +listOfString2);
        // OKEImages image = listOfString2.get(listOfString2.size()-1);
        // System.out.println("\n\n most recent : "+image);
        
        // using method reference
        // map8 = listOfString.stream().collect(toMap(Function.identity(), String::length));
        // convert list with duplicate keys to HashMap
        // listOfString.add("Java");
        // images = demo.new OKEImages("id","Oracle-Linux-7.9-Gen2-GPU-2022.05.31-0","IMAGE");
        // listOfString.add(images);
        // System.out.println("list of string with duplicates: " + listOfString);
        // HashMap<String, OKEImages> hash = listOfString.stream()
        //         .collect(toMap(Function.identity(), String::length, (e1, e2) -> e2, HashMap::new));
        // System.out.println("generated hashmap:" + hash);
        // // keep the order same as original list while conversion
        // LinkedHashMap<String, OKEImages> linked = listOfString.stream()
        //         .collect(toMap(Function.identity(), String::length, (e1, e2) -> e2, LinkedHashMap::new));
        // System.out.println("generated linkedhashmap:" + linked);
    }

    
}
