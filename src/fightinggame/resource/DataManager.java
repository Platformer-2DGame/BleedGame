package fightinggame.resource;

import fightinggame.Gameplay;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager {

    public static final String WALLPAPER_PATH = "assets/res/background/Wallpaper";
    public static final String TILES_PATH = "assets/res/background/Tiles";
    public static final String GAME_OBJECTS_PATH = "assets/res/background/Objects";
    public static final String OPTION_GUIS = "assets/res/gui/option_menu";
    public static final String SOUNDS_PATH = "assets/res/sound";
    public static final String DATA_FOLDER = "data/";
    public static final String SCENE_FILENAME = "scene_";
    public static final String SAVE_DATA_PATH = "data/Saves/";
    public static File CUSTOM_FONT_FILE = new File("assets/res/gui/font/AbaddonBold.ttf");

    private static final Map<String, File> sceneData = new HashMap<>();
    private static int sceneIndex = 0;

    public static Font getFont(float size) {
        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, CUSTOM_FONT_FILE).deriveFont(size);
//            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//            //register the font
//            ge.registerFont(customFont);
            return customFont;
        } catch (FontFormatException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static File getFile(String name) {
        if (sceneData.isEmpty()) {
            return null;
        }
        return sceneData.get(name);
    }

    public static File getFirstScene(int index) {
        if (index > 0) {
            File firstScene = sceneData.get(SCENE_FILENAME + index);
            if (firstScene == null) {
                return null;
            }
            sceneIndex = index;
            return firstScene;
        }
        return null;
    }

    public static File getNextScene() {
        sceneIndex++;
        File file = sceneData.get(SCENE_FILENAME + sceneIndex);
        if (file == null) {
            sceneIndex = 1;
            return sceneData.get(SCENE_FILENAME + sceneIndex);
        }
        return file;
    }

    public static File getCurrentScene() {
        File file = sceneData.get(SCENE_FILENAME + sceneIndex);
        if (file == null) {
            sceneIndex = 0;
            return null;
        }
        return file;
    }

//    public static File getFile(int index) {
//        if (sceneData.isEmpty()) {
//            return null;
//        }
//        int i = sceneData.size() - 1;
//        for (String key : sceneData.keySet()) {
//            if (i == index) {
//                System.out.println(key);
//                return sceneData.get(key);
//            }
//            i--;
//        }
//        return null;
//    }
    public static List<String> readFileToList(File file) {
        if (file == null) {
            return null;
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                return null;
            }
            try {
                return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static int getCurrentSceneIndex() {
        if (sceneIndex <= 0) {
            return -1;
        }
        return sceneIndex;
    }

    // need to code
    public boolean saveData(Gameplay gameplay) {
        return false;
    }

    public static boolean loadSceneData() {
        return loadSceneData(DATA_FOLDER);
    }

    public static boolean loadSceneData(String folderPath) {
        File folder = new File(folderPath);
        if (folder == null) {
            return false;
        }
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file != null) {
                        if (file.exists()) {
                            if (file.isDirectory()) {
                                loadSceneData(file.getAbsolutePath());
                            } else {
                                sceneData.put(Utils.getFileNameOnly(file.getName()), file);
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static String getSceneDataName(File file) {
        return Utils.firstCharCapital(Utils.getFileNameOnly(file.getName()));
    }

    public static Map<String, File> getSceneData() {
        return sceneData;
    }

}
