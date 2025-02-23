package fightinggame.resource;

import java.util.Map;

public class Utils {

    public static int getNextIndexDuplicateKey(Map<String, ?> objs, String name) {
        if(objs == null) return 1;
        if(objs.isEmpty()) return 1;
        int index = 1;
        while(true) {
            Object obj = objs.get(name + index);
            if(obj == null) {
                break;
            } else {
                index++;
            }
        }
        return index;
    }
    
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null!");
        }

        String extension = "";

        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            extension = fileName.substring(index + 1);
        }

        return extension;
    }
    public static String getFileNameOnly(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null!");
        }

        String fileNameOnly = "";

        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            fileNameOnly = fileName.substring(0, index);
        }

        return fileNameOnly;
    }
    
    public static String firstCharCapital(String str) {
        if(!str.isEmpty()) {
            return (str.charAt(0) + "").toUpperCase() + str.substring(1);
        }
        return "";
    }
    
    public static int getInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch(Exception ex) {
            
        }
        return -1;
    }
}
