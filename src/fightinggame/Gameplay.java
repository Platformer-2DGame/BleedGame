package fightinggame;

import fightinggame.animation.BackgroundAnimation;
import fightinggame.animation.ability.FireBallAnimation;
import fightinggame.animation.player.*;
import fightinggame.animation.enemy.*;
import fightinggame.animation.trap.*;
import fightinggame.animation.item.*;
import fightinggame.animation.item.equipment.FireSwordAnimation;
import fightinggame.entity.*;
import fightinggame.entity.background.*;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import fightinggame.entity.enemy.*;
import fightinggame.resource.*;
import fightinggame.entity.ability.*;
import fightinggame.entity.Character;
import fightinggame.entity.GameMap;
import fightinggame.entity.LoadingScreen;
import fightinggame.entity.ProgressBar;
import fightinggame.entity.Rule;
import fightinggame.entity.TransitionScreen;
import fightinggame.entity.ability.type.EnergyRecovery;
import fightinggame.entity.ability.type.healing.GreaterHeal;
import fightinggame.entity.ability.type.healing.PotionEnergyRecovery;
import fightinggame.entity.ability.type.healing.PotionHeal;
import fightinggame.entity.ability.type.healing.TimeHeal;
import fightinggame.entity.ability.type.increase.AttackIncrease;
import fightinggame.entity.ability.type.throwable.Fireball;
import fightinggame.entity.background.touchable.Chest;
import fightinggame.entity.enemy.type.DiorColor;
import fightinggame.entity.enemy.type.DiorEnemy;
import fightinggame.entity.enemy.type.PirateCat;
import fightinggame.entity.inventory.Inventory;
import fightinggame.entity.item.Item;
import fightinggame.entity.item.collectable.healing.SmallEnergyPotion;
import fightinggame.entity.item.collectable.healing.SmallHealthPotion;
import fightinggame.entity.item.collectable.quest.Key;
import fightinggame.entity.item.equipment.weapon.Sword;
import fightinggame.entity.platform.Platform;
import fightinggame.entity.platform.tile.Tile;
import fightinggame.entity.platform.tile.TrapTile;
import fightinggame.entity.platform.tile.trap.SpearTrap;
import fightinggame.entity.platform.tile.trap.SpikeTrap;
import fightinggame.entity.platform.tile.trap.TrapLocation;
import fightinggame.entity.platform.tile.trap.TrapType;
import fightinggame.entity.quest.Quest;
import fightinggame.entity.quest.QuestType;
import fightinggame.entity.quest.type.EnemyRequired;
import fightinggame.entity.quest.type.ItemRequired;
import fightinggame.entity.state.CharacterState;
import fightinggame.entity.state.GameState;
import fightinggame.input.handler.game.enemy.EnemyMovementHandler;
import fightinggame.input.handler.game.player.PlayerAbilityHandler;
import fightinggame.input.handler.game.player.PlayerMouseHandler;
import fightinggame.input.handler.game.player.PlayerMovementHandler;
import fightinggame.input.handler.menu.OptionKeyboardHandler;
import fightinggame.resource.AudioPlayer;
import fightinggame.resource.DataManager;
import fightinggame.resource.Utils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.swing.JPanel;

public class Gameplay extends JPanel implements Runnable {

    public static final int GRAVITY = 7; //7
    private int currentFps = 0;

    private Background background;
    private GameMap map;
    private Player player;
    private final List<Enemy> enemies = new ArrayList<Enemy>();
    private final Game game;
    private int itemCount = 0;
    private Thread spawnEnemiesThread;
    private AudioPlayer audioPlayer;
    private final List<Item> itemsOnGround = new ArrayList<Item>();
    private Camera camera;
    private Rule rule;
    private OptionKeyboardHandler optionHandler;
    private TransitionScreen transitionScreen;
    private LoadingScreen loadingScreen;
    private Map<String, Entity> playerSpriteSheetMap;

    public Gameplay(Game game) {
        this.game = game;
        setDoubleBuffered(true);
        optionHandler = new OptionKeyboardHandler(ImageManager.loadImagesFromFolderToMap(DataManager.OPTION_GUIS), this);
        game.addKeyListener(optionHandler);
        playerSpriteSheetMap = Entity.loadSpriteSheetFromFolder("assets/res/player");
        transitionScreen = new TransitionScreen(this);
        audioPlayer = new AudioPlayer(DataManager.SOUNDS_PATH);
        Entity loadingBackgroundSheet = new Entity();
        loadingBackgroundSheet.add("assets/res/background/Loading/loading.png");
        BackgroundAnimation animation = new BackgroundAnimation(0, loadingBackgroundSheet, -1);
        loadingScreen = new LoadingScreen(animation, new ProgressBar(ImageManager.loadImagesFromFoldersToList("assets/res/gui/option_menu/Progressbar")),
                new GamePosition(0, 0, game.getWidth(), game.getHeight()));
    }

    public void initCamera() {
        camera = new Camera(player, new GamePosition(0, 0, 0, 0), getWidth(), getHeight(), this);
    }

    public void initFirstScene() {
        File scene = DataManager.getFirstScene(2);
        if (scene == null) {
            return;
        }
        loadScene(DataManager.getSceneDataName(scene), scene.getAbsolutePath());
    }

    public void initBackgroundMusic() {
        audioPlayer.startThread("background_music", true, optionHandler.getOptionMenu().getMusicVolume());
    }

    public void resolutionChange(int width, int height) {
        // error
//        setPreferredSize(new Dimension(width - 16, height - 39));
//        initCamera();
//        initFirstScene();
    }

    public void loadScene(String sceneName, String sceneDataFilePath) {
        loadingScreen.resetLoading();
        Game.STATE = GameState.LOADING_STATE;
        new Thread(new Runnable() {
            @Override
            public void run() {
                initScene(sceneName, sceneDataFilePath);
                rule.setTimeLimit(7);
                Game.STATE = GameState.GAME_STATE;
            }
        }).start();
    }

    public void initScene(String sceneName, String sceneDataFilePath) {
        background = new Background(0, sceneName,
                ImageManager.loadImagesFromFolderToMap(DataManager.WALLPAPER_PATH),
                ImageManager.loadImagesFromFolderToMap(DataManager.TILES_PATH),
                ImageManager.loadImagesFromFolderToMap(DataManager.GAME_OBJECTS_PATH), this,
                sceneDataFilePath, 250, 180);
        map = new GameMap(1, "Map", this,
                new GamePosition(getWidth() - 260, 10, 0, 0),
                ImageManager.loadImagesFromFolderToMap(DataManager.WALLPAPER_PATH),
                ImageManager.loadImagesFromFolderToMap(DataManager.TILES_PATH),
                ImageManager.loadImagesFromFolderToMap(DataManager.GAME_OBJECTS_PATH),
                sceneDataFilePath, 15, 15);
        initObjects();
        initTraps();
        initVictoryPosition();
        enemies.clear();
        itemsOnGround.clear();
        playerInit(getPlatforms().get(11).get(3));
        initEnemies();
//        pirateCatInit(getPlatforms().get(14).get(15));
//        spawnEnemiesThread = new Thread(spawnEnemies());
//        spawnEnemiesThread.start();
        initGameQuests();
        AudioPlayer.audioPlayers.clear();
        initBackgroundMusic();
        transitionScreen.startTransitionBackward();
    }

    public void initTraps() { // 10,43, 10,44
        int currSceneIndex = DataManager.getCurrentSceneIndex();
        if (currSceneIndex <= 0) {
            return;
        }
        File trapsLocFile = DataManager.getFile("traps_" + currSceneIndex);
        if (trapsLocFile != null) {
            List<String> lines = DataManager.readFileToList(trapsLocFile);
            if (lines != null && lines.size() > 0) {
                Map<String, Tile> specialTiles = new HashMap();
                try {
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        if (line == null || line.isEmpty()) {
                            continue;
                        }
                        String[] splits = line.split(":");
                        if (splits == null || splits.length < 2) {
                            continue;
                        }
                        String type = splits[0];
                        String[] data = splits[1].split(",");
                        TrapType trapType = TrapType.valueOf(type.toUpperCase());
                        if (data == null || data.length < 4) {
                            continue;
                        }
                        int row = Utils.getInt(data[0]);
                        int column = Utils.getInt(data[1]);
                        if (row < 0 || column < 0) {
                            continue;
                        }
                        Platform platform = getPlatforms().get(row).get(column);
                        if (platform == null) {
                            continue;
                        }
                        TrapLocation trapLoc = TrapLocation.valueOf(data[2].toUpperCase());
                        Entity spearSheet = null;
                        Entity spikeSheet = null;
                        int width = -1, height = -1;
                        switch (trapType) {
                            case SPEAR:
                                spearSheet = new Entity(ImageManager.loadImage("assets/res/background/Trap/Spear.png"),
                                        0, 0, 16, 64,
                                        0, 0, 16, 64, 7);
                                if (trapLoc == TrapLocation.RIGHT
                                        || trapLoc == TrapLocation.LEFT) {
                                    width = 350;
                                    height = 100;
                                } else {
                                    width = 100;
                                    height = 350;
                                }
                                break;
                            case SPIKE:
                                spikeSheet = new Entity(ImageManager.loadImage("assets/res/background/Trap/Spike_B.png"),
                                        0, 0, 32, 32,
                                        0, 0, 32, 32, 10); // 0, 8, 32, 14
                                if (trapLoc == TrapLocation.RIGHT
                                        || trapLoc == TrapLocation.LEFT) {
                                    width = 150;
                                    height = 120;
                                } else {
                                    width = 200;
                                    height = 150;
                                }
                                break;
                        }
                        if (width < 0 || height < 0) {
                            continue;
                        }
                        String positionStr = data[3].toUpperCase();
                        GamePosition position = null;
                        switch (trapLoc) {
                            case TOP, BOTTOM:
                                switch (positionStr) {
                                    case "MIDDLE":
                                        if (trapLoc == TrapLocation.TOP) {
                                            position = platform.middleTopPlatform(width, height);
                                        } else {
                                            position = platform.middleBottomPlatform(width, height);
                                        }
                                        break;
                                    case "RIGHT_CORNER":
                                        if (trapLoc == TrapLocation.TOP) {
                                            position = platform.rightCornerTopPlatform(width, height);
                                        } else {
                                            position = platform.rightCornerBottomPlatform(width, height);
                                        }
                                        break;
                                    case "LEFT_CORNER":
                                        if (trapLoc == TrapLocation.TOP) {
                                            position = platform.leftCornerTopPlatform(width, height);
                                        } else {
                                            position = platform.leftCornerTopPlatform(width, height);
                                        }
                                        break;
                                }
                                break;
                            case RIGHT, LEFT:
                                switch (positionStr) {
                                    case "MIDDLE":
                                        if (trapLoc == TrapLocation.RIGHT) {
                                            position = platform.middleRightPlatform(width, height);
                                        } else {
                                            position = platform.middleLeftPlatform(width, height);
                                        }
                                        break;
                                    case "UP_CORNER":
                                        if (trapLoc == TrapLocation.RIGHT) {
                                            position = platform.upCornerRightPlatform(width, height);
                                        } else {
                                            position = platform.upCornerLeftPlatform(width, height);
                                        }
                                        break;
                                    case "DOWN_CORNER":
                                        if (trapLoc == TrapLocation.RIGHT) {
                                            position = platform.downCornerRightPlatform(width, height);
                                        } else {
                                            position = platform.downCornerLeftPlatform(width, height);
                                        }
                                        break;
                                }
                                break;
                        }
                        if (position == null) {
                            continue;
                        }
                        TrapTile trapTile = null;
                        String name = Utils.firstCharCapital(type);
                        int index = Utils.getNextIndexDuplicateKey(specialTiles, name);
                        switch (trapType) {
                            case SPEAR:
                                switch (trapLoc) {
                                    case RIGHT:
                                        spearSheet = spearSheet.rotateSheet(90);
                                        break;
                                    case LEFT:
                                        spearSheet = spearSheet.rotateSheet(270);
                                        break;
                                    case BOTTOM:
                                        spearSheet = spearSheet.rotateSheet(180);
                                        break;
                                }
                                TrapAnimation spearAnimation = new TrapAnimation(0, spearSheet, 21);
                                trapTile = new SpearTrap(spearAnimation, 20, trapLoc,
                                        name + index, spearSheet.getImage(0),
                                        position, this);
                                break;
                            case SPIKE:
                                switch (trapLoc) {
                                    case RIGHT:
                                        spikeSheet = spikeSheet.rotateSheet(90);
                                        break;
                                    case LEFT:
                                        spikeSheet = spikeSheet.rotateSheet(270);
                                        break;
                                    case BOTTOM:
                                        spikeSheet = spikeSheet.rotateSheet(180);
                                        break;
                                }
                                TrapAnimation spikeAnimation = new TrapAnimation(0, spikeSheet, 15);
                                trapTile = new SpikeTrap(spikeAnimation, 10, trapLoc,
                                        name + index, spikeSheet.getImage(0),
                                        position, this);
                                break;
                        }
                        specialTiles.put(trapTile.getName(), trapTile);
                    }
                } catch (Exception ex) {

                }
                background.setSpecialTiles(specialTiles);
            }
        }
    }

    public void initGameQuests() {
        //Init Key
        Entity keySheet = new Entity();
        keySheet.add("assets/res/item/key.png");
        KeyAnimation keyAnimation = new KeyAnimation(1, keySheet, -1);
        Key keyItem = new Key(1, "Key Item", keyAnimation, null, this, 1);
        GameObject gameObject = background.getGameObjectsTouchable().get("chest3");
        if (gameObject != null) {
            Chest chest = (Chest) gameObject;
            keyItem.setPosition(chest.getPlatform().middleTopPlatform(Item.ITEM_WIDTH, Item.ITEM_HEIGHT));
            chest.getItems().add(keyItem);
        }

        //Energy Potion x15
        int amount = 15;
        Entity energyPotionSheet = new Entity();
        energyPotionSheet.add("assets/res/item/s_energy_potion.png");
        PotionAnimation energyPotionAnimation = new PotionAnimation(0, energyPotionSheet, -1);
        Item item = new SmallEnergyPotion(itemCount, energyPotionAnimation, null,
                this, amount);
        EnergyRecovery energyRecovery = new PotionEnergyRecovery(10, 0, 1000,
                null, null, null, null, this, null);
        item.getAbilities().add(energyRecovery);
        gameObject = background.getGameObjectsTouchable().get("chest2");
        if (gameObject != null) {
            Chest chest = (Chest) gameObject;
            item.setPosition(chest.getPlatform().middleTopPlatform(Item.ITEM_WIDTH, Item.ITEM_HEIGHT));
            chest.getItems().add(item);
        }
        
        //Init Quest
        if (rule != null) {
            Quest quest = new Quest("QM_1", "The Mystery Key && Dior Firor", QuestType.MAIN_QUEST, "Go Around Map And Find The Key To Open The Way To Next State",
                    player, this);
            quest.getRequireds().add(new ItemRequired(keyItem, 1, "Find Mystery Key", quest));
            quest.getRequireds().add(new EnemyRequired("Dior Firor",
                    15, "Kill Dior Firor", quest));
            quest.getRequireds().add(new EnemyRequired("Zach Fowler",
                    1, "Defeat Zach Fowler", quest));
            rule.addQuest(quest);
        }
    }

    public void initVictoryPosition() {
        int currSceneIndex = DataManager.getCurrentSceneIndex();
        if (currSceneIndex <= 0) {
            return;
        }
        File victoryPosFile = DataManager.getFile("victory_position");
        if (victoryPosFile != null) {
            List<String> lines = DataManager.readFileToList(victoryPosFile);
            if (lines != null && lines.size() > 0) {
                try {
                    String pos = lines.get(currSceneIndex - 1);
                    String[] splits = pos.split(",");
                    if (splits.length == 4) {
                        int row1 = Utils.getInt(splits[0].trim());
                        int column1 = Utils.getInt(splits[1].trim());
                        int row2 = Utils.getInt(splits[2].trim());
                        int column2 = Utils.getInt(splits[3].trim());
                        Platform platform1 = getPlatforms().get(row1).get(column1);
                        Platform platform2 = getPlatforms().get(row2).get(column2);
                        if (platform1 == null) {
                            return;
                        }
                        if (platform2 == null) {
                            return;
                        }
                        GamePosition nPos = new GamePosition(platform1.getPosition().getXPosition(), platform1.getPosition().getYPosition(),
                                platform1.getPosition().getWidth() + platform2.getPosition().getWidth(),
                                platform1.getPosition().getHeight() + platform2.getPosition().getHeight() + 20);
                        rule = new Rule(nPos, platform1, platform2, this);
                    }
                } catch (Exception ex) {

                }
            }
        }
    }

    public void initEnemies() {
        int currSceneIndex = DataManager.getCurrentSceneIndex();
        if (currSceneIndex <= 0) {
            return;
        }
        File enemiesLocation = DataManager.getFile("enemies_" + currSceneIndex);
        if (enemiesLocation != null) {
            List<String> lines = DataManager.readFileToList(enemiesLocation);
            if (lines != null && lines.size() > 0) {
                Platform firstPlatform;
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String[] splits = line.split(":");
                    if (splits.length != 2) {
                        continue;
                    }
                    String enemyType = splits[0].trim();
                    enemyType = enemyType.toLowerCase();
                    String location = splits[1].trim();
                    splits = location.split(",");
                    if (splits.length == 2) {
                        String rowStr = splits[0].trim();
                        String columnStr = splits[1].trim();
                        int row = Utils.getInt(rowStr);
                        int column = Utils.getInt(columnStr);
                        if (row > 0 && column > 0) {
                            EnemyType type = EnemyType.valueOf(enemyType.toUpperCase());
                            firstPlatform = background.getScene().get(row).get(column);
                            switch (type) {
                                case DIOR:
                                    diorInit(firstPlatform);
                                    break;
                                case PIRATE_CAT:
                                    pirateCatInit(firstPlatform);
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void initObjects() {
        // resource loaded
        List<List<Platform>> scene = getPlatforms();
        Map<String, BufferedImage> objects = background.getObjects();

        // Create Object
        int currSceneIndex = DataManager.getCurrentSceneIndex();
        if (currSceneIndex <= 0) {
            return;
        }
        File objectsFile = DataManager.getFile("objects_" + currSceneIndex);
        if (objectsFile != null) {
            List<String> lines = DataManager.readFileToList(objectsFile);
            if (lines != null && lines.size() > 0) {
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String[] splits = line.split(":");
                    if (splits.length != 2) {
                        continue;
                    }
                    String type = splits[0].trim();
                    String gameObjStr = splits[1].trim();
                    splits = gameObjStr.split(",");
                    if (splits.length == 8) {
                        String imgAfterTouchStr = splits[0].trim();
                        String imgDefaultStr = splits[1].trim();
                        String objName = splits[2].trim();
                        String platformPosStr = splits[3].trim();
                        String platformRow = splits[4].trim();
                        String platformColumn = splits[5].trim();
                        String widthStr = splits[6].trim();
                        String heightStr = splits[7].trim();
                        int row = Utils.getInt(platformRow);
                        int column = Utils.getInt(platformColumn);
                        if (row < 0 || column < 0) {
                            continue;
                        }
                        int width = Utils.getInt(widthStr);
                        int height = Utils.getInt(heightStr);
                        Platform platform = scene.get(row).get(column);
                        GamePosition position = null;
                        if (platform != null) {
                            if (width > 0 && height > 0) {
                                if (platformPosStr.equalsIgnoreCase("mid")) {
                                    position = platform.middleTopPlatform(width, height);
                                } else if (platformPosStr.equalsIgnoreCase("left")) {
                                    position = platform.leftCornerTopPlatform(width, height);
                                } else if (platformPosStr.equalsIgnoreCase("right")) {
                                    position = platform.rightCornerTopPlatform(width, height);
                                }
                            } else {
                                if (platformPosStr.equalsIgnoreCase("mid")) {
                                    position = platform.middleTopPlatform();
                                } else if (platformPosStr.equalsIgnoreCase("left")) {
                                    position = platform.leftCornerTopPlatform();
                                } else if (platformPosStr.equalsIgnoreCase("right")) {
                                    position = platform.rightCornerTopPlatform();
                                }
                            }
                            if (position == null) {
                                continue;
                            }
                            GameObjectType objType = GameObjectType.valueOf(type.toUpperCase());
                            GameObject gameObj = null;
                            int index = -1;
                            String key = "";
                            if (objType != GameObjectType.NONTOUCH) {
                                index = Utils.getNextIndexDuplicateKey(background.getGameObjectsTouchable(), objName);
                                key = objName + index;
                            } else {
                                index = Utils.getNextIndexDuplicateKey(background.getGameObjectsNonTouchable(), objName);
                                key = objName + index;
                            }
                            if (index <= 0) {
                                continue;
                            }
                            switch (objType) {
                                case GAMEOBJECT:
                                    gameObj = new GameObject(objects.get(imgDefaultStr), objName, platform, position, this);
                                    if (gameObj == null) {
                                        continue;
                                    }
                                    background.getGameObjectsTouchable().put(key, gameObj);
                                    break;
                                case CHEST:
                                    gameObj = new Chest(objects.get(imgAfterTouchStr), objects.get(imgDefaultStr), objName, platform, position, this);
                                    if (gameObj == null) {
                                        continue;
                                    }
                                    background.getGameObjectsTouchable().put(key, gameObj);
                                    map.getGameObjectsTouchable().put(key, gameObj.clone());
                                    break;
                                case NONTOUCH:
                                    gameObj = new ObjectNonTouchable(objects.get(imgDefaultStr), objName, platform, position, this);
                                    if (gameObj == null) {
                                        continue;
                                    }
                                    background.getGameObjectsNonTouchable().put(key, gameObj);
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        long now;
        long updateTime;
        long wait;

        long lastFpsCheck = 0;
        int totalFrames = 0;
        final int TARGET_FPS = Game.FPS;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
        while (game.isRunning()) {
            now = System.nanoTime();
            totalFrames++;
            if (System.nanoTime() > lastFpsCheck + 1000000000) {
                lastFpsCheck = System.nanoTime();
                currentFps = totalFrames;
                totalFrames = 0;
//                System.out.println("Current Fps: " + currentFps);
            }
            try {
                tick();
//                revalidate();
                repaint();
            } catch (Exception ex) {
//                System.out.println(ex.toString());
            }
            updateTime = System.nanoTime() - now;
            wait = (OPTIMAL_TIME - updateTime) / 1000000;
            try {
                Thread.sleep(wait);
            } catch (Exception e) {
//                System.out.println(e.toString());
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        render(g);
        if (Game.STATE != GameState.LOADING_STATE) {
            g.setColor(Color.red);
            g.setFont(DataManager.getFont(30f));
            g.drawString(currentFps + " FPS", getWidth() - 100, getHeight() - 25);
        }
    }

    public void pirateCatInit(Platform firstPlatform) {
        GamePosition defEnemyPosition = new GamePosition(firstPlatform.getPosition().getXPosition(),
                firstPlatform.getPosition().getYPosition(), 300, 259);
        String loc = EnemyAnimationResources.CAT_BOSS_SHEET_LOC;
        Entity idleLTRSheet = new Entity(ImageManager.loadImage(loc + "/Idle.png"),
                0, 0, 96, 48,
                27, 11, 40, 37, 5);
        Entity hitLTRSheet = new Entity(ImageManager.loadImage(loc + "/Hit.png"),
                0, 0, 96, 48,
                27, 7, 40, 41, 4);
        Entity deathLTRSheet = new Entity(ImageManager.loadImage(loc + "/Death.png"),
                0, 0, 96, 48,
                30, 7, 40, 41, 15);
        Entity runLTRSheet = new Entity(ImageManager.loadImage(loc + "/Run.png"),
                0, 0, 96, 48,
                27, 11, 40, 37, 8);
        Entity attack01LTRSheet = new Entity(ImageManager.loadImage(loc + "/Attack01.png"),
                384, 0, 96, 48,
                29, 11, 65, 37, 2);
        Entity attack02LTRSheet = new Entity(ImageManager.loadImage(loc + "/Attack02.png"),
                0, 0, 96, 48,
                24, 11, 42, 37, 6);
        Entity attack03LTRSheet = new Entity(ImageManager.loadImage(loc + "/Attack03.png"),
                0, 0, 96, 48,
                27, 11, 40, 37, 7);

        Entity idleRTLSheet = idleLTRSheet.convertRTL();
        Entity hitRTLSheet = hitLTRSheet.convertRTL();
        Entity deathRTLSheet = deathLTRSheet.convertRTL();
        Entity runRTLSheet = runLTRSheet.convertRTL();
        Entity attack01RTLSheet = attack01LTRSheet.convertRTL();
        Entity attack02RTLSheet = attack02LTRSheet.convertRTL();
        Entity attack03RTLSheet = attack03LTRSheet.convertRTL();

        EnemyIdle idleRTL = new EnemyIdle(0, idleRTLSheet);
        EnemyIdle idleLTR = new EnemyIdle(1, idleLTRSheet);
        EnemyHit hitRTL = new EnemyHit(2, hitRTLSheet, 25);
        EnemyHit hitLTR = new EnemyHit(3, hitLTRSheet, 25);
        EnemyDeath deathRTL = new EnemyDeath(4, deathRTLSheet, 100);
        EnemyDeath deathLTR = new EnemyDeath(5, deathLTRSheet, 100);
        EnemyRunForward runForward = new EnemyRunForward(6, runRTLSheet, 40);
        EnemyRunBack runBack = new EnemyRunBack(7, runLTRSheet, 40);
        EnemyHeavyAttack attack01RTL = new EnemyHeavyAttack(8, attack01RTLSheet, 25);
        EnemyHeavyAttack attack01LTR = new EnemyHeavyAttack(9, attack01LTRSheet, 25);
        EnemyLightAttack attack02RTL = new EnemyLightAttack(10, attack02RTLSheet, 8);
        EnemyLightAttack attack02LTR = new EnemyLightAttack(11, attack02LTRSheet, 8);
        EnemyHeavyAttack attack03RTL = new EnemyHeavyAttack(12, attack03RTLSheet, 25);
        EnemyHeavyAttack attack03LTR = new EnemyHeavyAttack(13, attack03LTRSheet, 25);

        Map<CharacterState, Animation> enemyAnimations = new HashMap();

        enemyAnimations.put(CharacterState.IDLE_RTL, idleRTL);
        enemyAnimations.put(CharacterState.IDLE_LTR, idleLTR);
        enemyAnimations.put(CharacterState.GET_HIT_RTL, hitRTL);
        enemyAnimations.put(CharacterState.GET_HIT_LTR, hitLTR);
        enemyAnimations.put(CharacterState.DEATH_RTL, deathRTL);
        enemyAnimations.put(CharacterState.DEATH_LTR, deathLTR);
        enemyAnimations.put(CharacterState.RUNFORWARD, runForward);
        enemyAnimations.put(CharacterState.RUNBACK, runBack);
        enemyAnimations.put(CharacterState.ATTACK01_RTL, attack01RTL);
        enemyAnimations.put(CharacterState.ATTACK01_LTR, attack01LTR);
        enemyAnimations.put(CharacterState.ATTACK02_RTL, attack02RTL);
        enemyAnimations.put(CharacterState.ATTACK02_LTR, attack02LTR);
        enemyAnimations.put(CharacterState.ATTACK03_RTL, attack03RTL);
        enemyAnimations.put(CharacterState.ATTACK03_LTR, attack03LTR);

        PirateCat pirateCat = new PirateCat(0, "Zach Fowler", 5000, defEnemyPosition, enemyAnimations, this,
                60);
        pirateCat.setInsidePlatform(firstPlatform);
        EnemyMovementHandler movementHandler = new EnemyMovementHandler("enemy_movement", this, pirateCat);
        pirateCat.getController().add(movementHandler);
        abilitiesCharacterInit(pirateCat.getAbilities(), pirateCat);
        enemies.add(pirateCat);
    }

    public void diorInit(Platform firstPlatform) {
        Random random = new Random();
        int colorChoose = random.nextInt(7);
        String diorColorSheet = "";
        DiorColor diorColor = null;
        switch (colorChoose) {
            case 0:
                diorColorSheet = EnemyAnimationResources.DIOR_RED_SHEET;
                diorColor = DiorColor.Red;
                break;
            case 1:
                diorColorSheet = EnemyAnimationResources.DIOR_BLUE_SHEET;
                diorColor = DiorColor.Blue;
                break;
            case 2:
                diorColorSheet = EnemyAnimationResources.DIOR_GREEN_SHEET;
                diorColor = DiorColor.Green;
                break;
            case 3:
                diorColorSheet = EnemyAnimationResources.DIOR_ORANGE_SHEET;
                diorColor = DiorColor.Orange;
                break;
            case 4:
                diorColorSheet = EnemyAnimationResources.DIOR_PURPLE_SHEET;
                diorColor = DiorColor.Purple;
                break;
            case 5:
                diorColorSheet = EnemyAnimationResources.DIOR_WHITE_SHEET;
                diorColor = DiorColor.White;
                break;
            case 6:
                diorColorSheet = EnemyAnimationResources.DIOR_YELLOW_SHEET;
                diorColor = DiorColor.Yellow;
                break;
        }
        Map<String, Entity> spriteSheetMap = Entity.loadSpriteSheetFromFolder("assets/res/enemy/Dior Firor/"
                + diorColor.toString());
        // Init enemy position
        GamePosition defEnemyPosition = new GamePosition(firstPlatform.getPosition().getXPosition(),
                firstPlatform.getPosition().getYPosition(), 300, 200);
//        SpriteSheet idleRTLSheet = new SpriteSheet(ImageManager.loadImage(diorColorSheet),
//                0, 0, 192, 160,
//                23, 55, 160, 90, 4);
//        SpriteSheet enemyHitRTLSheet = new SpriteSheet(ImageManager.loadImage(diorColorSheet),
//                0, 480, 192, 160,
//                0, 55, 177, 90, 4);
//        SpriteSheet enemyDeathRTLSheet = new SpriteSheet(ImageManager.loadImage(diorColorSheet),
//                0, 1920, 192, 160,
//                23, 55, 167, 90, 4);
//        SpriteSheet enemyRunForward = new SpriteSheet(ImageManager.loadImage(diorColorSheet),
//                0, 1280, 192, 160,
//                23, 55, 160, 90, 4);
//        SpriteSheet enemyRunBack = new SpriteSheet(ImageManager.loadImage(diorColorSheet),
//                0, 1440, 192, 160,
//                10, 55, 160, 90, 4);
//        SpriteSheet enemyAttackRTLSheet = new SpriteSheet(ImageManager.loadImage(diorColorSheet),
//                0, 640, 192, 160,
//                0, 55, 180, 90, 4);
//        SpriteSheet idleLTRSheet = idleRTLSheet.convertRTL();
//        SpriteSheet enemyHitLTRSheet = enemyHitRTLSheet.convertRTL();
//        SpriteSheet enemyDeathLTRSheet = enemyDeathRTLSheet.convertRTL();
//        SpriteSheet enemyAttackLTRSheet = enemyAttackRTLSheet.convertRTL();

//        Export bufferedimage to file
//        ImageManager.writeImages(idleRTLSheet.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Idle_RTL", "Idle_RTL", ImageManager.EXTENSION_PNG);
//        ImageManager.writeImages(idleLTRSheet.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Idle_LTR", "Idle_LTR", ImageManager.EXTENSION_PNG);
//        ImageManager.writeImages(enemyHitRTLSheet.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Hit_RTL", "Hit_RTL", ImageManager.EXTENSION_PNG);
//        ImageManager.writeImages(enemyHitLTRSheet.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Hit_LTR", "Hit_LTR", ImageManager.EXTENSION_PNG);
//        ImageManager.writeImages(enemyDeathRTLSheet.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Death_RTL", "Death_RTL", ImageManager.EXTENSION_PNG);
//        ImageManager.writeImages(enemyDeathLTRSheet.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Death_LTR", "Death_LTR", ImageManager.EXTENSION_PNG);
//        ImageManager.writeImages(enemyRunBack.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Run_LTR", "Run_LTR", ImageManager.EXTENSION_PNG);
//        ImageManager.writeImages(enemyRunForward.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Run_RTL", "Run_RTL", ImageManager.EXTENSION_PNG);
//        ImageManager.writeImages(enemyAttackRTLSheet.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Attack_RTL", "Attack_RTL", ImageManager.EXTENSION_PNG);
//        ImageManager.writeImages(enemyAttackLTRSheet.getImages(), "assets/res/enemy/Dior Firor/" + diorColor.toString() +"/Attack_LTR", "Attack_LTR", ImageManager.EXTENSION_PNG);
//        
        EnemyIdle idleRTL = new EnemyIdle(0, spriteSheetMap.get("Idle_RTL"));
        EnemyIdle idleLTR = new EnemyIdle(1, spriteSheetMap.get("Idle_LTR"));
        EnemyHit hitRTL = new EnemyHit(2, spriteSheetMap.get("Hit_RTL"));
        EnemyHit hitLTR = new EnemyHit(3, spriteSheetMap.get("Hit_LTR"));
        EnemyDeath deathRTL = new EnemyDeath(4, spriteSheetMap.get("Death_RTL"));
        EnemyDeath deathLTR = new EnemyDeath(5, spriteSheetMap.get("Death_LTR"));
        EnemyRunForward runForward = new EnemyRunForward(6, spriteSheetMap.get("Run_RTL"), 25);
        EnemyRunBack runBack = new EnemyRunBack(7, spriteSheetMap.get("Run_LTR"), 25);
        EnemyHeavyAttack attackRTL = new EnemyHeavyAttack(8, spriteSheetMap.get("Attack_RTL"), 25);
        EnemyHeavyAttack attackLTR = new EnemyHeavyAttack(9, spriteSheetMap.get("Attack_LTR"), 25);

        Map<CharacterState, Animation> enemyAnimations = new HashMap();
        enemyAnimations.put(CharacterState.IDLE_RTL, idleRTL);
        enemyAnimations.put(CharacterState.IDLE_LTR, idleLTR);
        enemyAnimations.put(CharacterState.GET_HIT_RTL, hitRTL);
        enemyAnimations.put(CharacterState.GET_HIT_LTR, hitLTR);
        enemyAnimations.put(CharacterState.DEATH_RTL, deathRTL);
        enemyAnimations.put(CharacterState.DEATH_LTR, deathLTR);
        enemyAnimations.put(CharacterState.RUNFORWARD, runForward);
        enemyAnimations.put(CharacterState.RUNBACK, runBack);
        enemyAnimations.put(CharacterState.ATTACK01_RTL, attackRTL);
        enemyAnimations.put(CharacterState.ATTACK01_LTR, attackLTR);
        Enemy enemy = new DiorEnemy(diorColor, 0, "Dior Firor " + diorColor,
                500, defEnemyPosition,
                enemyAnimations, this, 60);
        enemy.setInsidePlatform(firstPlatform);
        EnemyMovementHandler movementHandler = new EnemyMovementHandler("enemy_movement", this, enemy);
        enemy.getController().add(movementHandler);
        abilitiesCharacterInit(enemy.getAbilities(), enemy);
        itemInit(enemy.getInventory(), enemy);
        enemies.add(enemy);
        // level up to 8
//        enemy.getStats().addExperience(10000);
    }

    public void playerInit(Platform firstPlatform) {
        // init player position
        GamePosition middlePlatform = firstPlatform.middleTopPlatform(350, 259);
        GamePosition defPlayerPosition = new GamePosition(middlePlatform.getXPosition(),
                middlePlatform.getYPosition(), 350, 259);

        //Init Attack Move
        Entity attackSpecialLTR = playerSpriteSheetMap.get("Attack01").clone();
        Entity attackSpecialRTL = playerSpriteSheetMap.get("Attack01").convertRTL();
        // Add special attack new sheet
        attackSpecialLTR.getImages().addAll(playerSpriteSheetMap.get("Attack02").getImages());
        attackSpecialLTR.getImages().addAll(playerSpriteSheetMap.get("Attack03").getImages());
        attackSpecialRTL.getImages().addAll(playerSpriteSheetMap.get("Attack02").convertRTL().getImages());
        attackSpecialRTL.getImages().addAll(playerSpriteSheetMap.get("Attack03").convertRTL().getImages());
        // Init Fire Attack Move
        Entity fireAttackSpecialLTR = playerSpriteSheetMap.get("FireAttack01").clone();
        Entity fireAttackSpecialRTL = playerSpriteSheetMap.get("FireAttack01").convertRTL();
        // Add special attack new sheet
        fireAttackSpecialLTR.getImages().addAll(playerSpriteSheetMap.get("FireAttack02").getImages());
        fireAttackSpecialLTR.getImages().addAll(playerSpriteSheetMap.get("FireAttack03").getImages());
        fireAttackSpecialRTL.getImages().addAll(playerSpriteSheetMap.get("FireAttack02").convertRTL().getImages());
        fireAttackSpecialRTL.getImages().addAll(playerSpriteSheetMap.get("FireAttack03").convertRTL().getImages());
        //Init animations tick
        int hit_tick = 16, idle_tick = 10, fire_idle_tick = 10,
                run_tick = 10, attack1_tick = 16, attack2_tick = 13,
                attack3_tick = 11, fire_attack1_tick = 16,
                fire_attack2_tick = 13, fire_attack3_tick = 11,
                death_tick = 50, fireDeath_tick = 50,
                jump_tick = 10, falldown_tick = 50,
                spellcast_tick = 40, crouch_tick = 10,
                spellcast_loop_tick = 15, slide_tick = 30,
                airAttack1_tick = 20, airAttack2_tick = 26,
                airAttack3_tick = 26, fireAirAttack1_tick = 20,
                fireAirAttack2_tick = 20, fireAirAttack3_tick = 20,
                airAttack_loop_tick = 20, jumpRoll_tick = 15,
                getUp_tick = 15, knockDown_tick = 21,
                ledgeClimb_tick = 15, ledgeGrap_tick = 15,
                wallRun_tick = 15, wallSlide_tick = 15,
                sprint_tick = 10;

        //LTR
        PlayerHit hitLTR = new PlayerHit(3, playerSpriteSheetMap.get("HurtAnim01"), hit_tick);
        PlayerIdle idleLTR = new PlayerIdle(0, playerSpriteSheetMap.get("Idle02"), idle_tick);
        PlayerIdle fireIdleLTR = new PlayerIdle(0, playerSpriteSheetMap.get("FireIdle01"), fire_idle_tick);
        PlayerRun_LTR runLTR = new PlayerRun_LTR(1, playerSpriteSheetMap.get("Run01"), run_tick); // 0
        PlayerLightAttack attack01LTR = new PlayerLightAttack(2, playerSpriteSheetMap.get("Attack01"), attack1_tick); // 12
        PlayerHeavyAttack attack02LTR = new PlayerHeavyAttack(2, playerSpriteSheetMap.get("Attack02"), attack2_tick); // 12
        PlayerAttackSpecial_LTR attack03LTR = new PlayerAttackSpecial_LTR(2, attackSpecialLTR, attack3_tick);
        PlayerLightAttack fireAttack01LTR = new PlayerLightAttack(2, playerSpriteSheetMap.get("FireAttack01"), fire_attack1_tick);
        PlayerHeavyAttack fireAttack02LTR = new PlayerHeavyAttack(2, playerSpriteSheetMap.get("FireAttack02"), fire_attack2_tick);
        PlayerAttackSpecial_LTR fireAttack03LTR = new PlayerAttackSpecial_LTR(2, fireAttackSpecialLTR, fire_attack3_tick);
        PlayerDeath deathLTR = new PlayerDeath(4, playerSpriteSheetMap.get("Death01"), death_tick);
        PlayerDeath fireDeathLTR = new PlayerDeath(4, playerSpriteSheetMap.get("FireDeath01"), fireDeath_tick);
        PlayerJump_LTR jumpLTR = new PlayerJump_LTR(5, playerSpriteSheetMap.get("Jump02"), jump_tick);
        PlayerFallDown_LTR fallDownLTR = new PlayerFallDown_LTR(6, playerSpriteSheetMap.get("FallAnim01"), falldown_tick);
        PlayerSpellCast spellCastLTR = new PlayerSpellCast(7, playerSpriteSheetMap.get("Spellcast01"), spellcast_tick);
        PlayerCrouch crouchLTR = new PlayerCrouch(8, playerSpriteSheetMap.get("Crouch01"), crouch_tick);
        PlayerSpellCastLoop spellCastLoopLTR = new PlayerSpellCastLoop(9, playerSpriteSheetMap.get("SpellcastLoop"), spellcast_loop_tick);
        PlayerSlide_LTR slideLTR = new PlayerSlide_LTR(10, playerSpriteSheetMap.get("Slide01"), slide_tick);
        PlayerAirAttack_LTR airAttack01LTR = new PlayerAirAttack_LTR(11, playerSpriteSheetMap.get("AirAttack01"), airAttack1_tick);
        PlayerAirAttack_LTR airAttack02LTR = new PlayerAirAttack_LTR(11, playerSpriteSheetMap.get("AirAttack02"), airAttack2_tick);
        PlayerAirAttack_LTR airAttack03LTR = new PlayerAirAttack_LTR(11, playerSpriteSheetMap.get("AirAttack03"), airAttack3_tick);
        PlayerAirAttack_LTR fireAirAttack01LTR = new PlayerAirAttack_LTR(11, playerSpriteSheetMap.get("FireAirAttack01"), fireAirAttack1_tick);
        PlayerAirAttack_LTR fireAirAttack02LTR = new PlayerAirAttack_LTR(11, playerSpriteSheetMap.get("FireAirAttack02"), fireAirAttack2_tick);
        PlayerAirAttack_LTR fireAirAttack03LTR = new PlayerAirAttack_LTR(11, playerSpriteSheetMap.get("FireAirAttack03"), fireAirAttack3_tick);
        PlayerAirAttack_LTR airAttackLoopLTR = new PlayerAirAttack_LTR(11, playerSpriteSheetMap.get("AirAttack03Loop"), airAttack_loop_tick);
        PlayerJumpRoll_LTR jumpRollLTR = new PlayerJumpRoll_LTR(12, playerSpriteSheetMap.get("JumpRoll01"), jumpRoll_tick);
        PlayerGetUp_LTR getUpLTR = new PlayerGetUp_LTR(13, playerSpriteSheetMap.get("GetUp01"), getUp_tick);
        PlayerKnockDown knockDownLTR = new PlayerKnockDown(14, playerSpriteSheetMap.get("KnockDown01"), knockDown_tick);
        PlayerLedgeAction_LTR ledgeClimbLTR = new PlayerLedgeAction_LTR(15, playerSpriteSheetMap.get("LedgeClimb01"), ledgeClimb_tick);
        PlayerLedgeAction_LTR ledgeGrabLTR = new PlayerLedgeAction_LTR(16, playerSpriteSheetMap.get("LedgeGrab01"), ledgeGrap_tick);
        PlayerWallAction_LTR wallRunLTR = new PlayerWallAction_LTR(17, playerSpriteSheetMap.get("WallRun01"), wallRun_tick);
        PlayerWallAction_LTR wallSlideLTR = new PlayerWallAction_LTR(18, playerSpriteSheetMap.get("WallSlide01"), wallSlide_tick);
        PlayerSprint_LTR sprintLTR = new PlayerSprint_LTR(19, playerSpriteSheetMap.get("Sprint01"), sprint_tick);

        //RTL
        PlayerHit hitRTL = new PlayerHit(3, playerSpriteSheetMap.get("HurtAnim01").convertRTL(), hit_tick);
        PlayerIdle idleRTL = new PlayerIdle(0, playerSpriteSheetMap.get("Idle02").convertRTL(), idle_tick);
        PlayerIdle fireIdleRTL = new PlayerIdle(0, playerSpriteSheetMap.get("FireIdle01").convertRTL(), fire_idle_tick);
        PlayerRun_RTL runRTL = new PlayerRun_RTL(1, playerSpriteSheetMap.get("Run01").convertRTL(), run_tick);
        PlayerLightAttack attack01RTL = new PlayerLightAttack(2, playerSpriteSheetMap.get("Attack01").convertRTL(), attack1_tick);
        PlayerHeavyAttack attack02RTL = new PlayerHeavyAttack(2, playerSpriteSheetMap.get("Attack02").convertRTL(), attack2_tick);
        PlayerAttackSpecial_RTL attack03RTL = new PlayerAttackSpecial_RTL(2, attackSpecialRTL, attack3_tick);
        PlayerLightAttack fireAttack01RTL = new PlayerLightAttack(2, playerSpriteSheetMap.get("FireAttack01").convertRTL(), fire_attack1_tick);
        PlayerHeavyAttack fireAttack02RTL = new PlayerHeavyAttack(2, playerSpriteSheetMap.get("FireAttack02").convertRTL(), fire_attack2_tick);
        PlayerAttackSpecial_RTL fireAttack03RTL = new PlayerAttackSpecial_RTL(2, fireAttackSpecialRTL, fire_attack3_tick);
        PlayerCrouch crouchRTL = new PlayerCrouch(8, playerSpriteSheetMap.get("Crouch01").convertRTL(), crouch_tick);
        PlayerDeath deathRTL = new PlayerDeath(4, playerSpriteSheetMap.get("Death01").convertRTL(), death_tick);
        PlayerDeath fireDeathRTL = new PlayerDeath(4, playerSpriteSheetMap.get("FireDeath01").convertRTL(), fireDeath_tick);
        PlayerJump_RTL jumpRTL = new PlayerJump_RTL(5, playerSpriteSheetMap.get("Jump02").convertRTL(), jump_tick);
        PlayerFallDown_RTL fallDownRTL = new PlayerFallDown_RTL(6, playerSpriteSheetMap.get("FallAnim01").convertRTL(), falldown_tick);
        PlayerSpellCast spellCastRTL = new PlayerSpellCast(7, playerSpriteSheetMap.get("Spellcast01").convertRTL(), spellcast_tick);
        PlayerSpellCastLoop spellCastLoopRTL = new PlayerSpellCastLoop(9, playerSpriteSheetMap.get("SpellcastLoop").convertRTL(), spellcast_loop_tick);
        PlayerSlide_RTL slideRTL = new PlayerSlide_RTL(10, playerSpriteSheetMap.get("Slide01").convertRTL(), slide_tick);
        PlayerAirAttack_RTL airAttack01RTL = new PlayerAirAttack_RTL(11, playerSpriteSheetMap.get("AirAttack01").convertRTL(), airAttack1_tick);
        PlayerAirAttack_RTL airAttack02RTL = new PlayerAirAttack_RTL(11, playerSpriteSheetMap.get("AirAttack02").convertRTL(), airAttack2_tick);
        PlayerAirAttack_RTL airAttack03RTL = new PlayerAirAttack_RTL(11, playerSpriteSheetMap.get("AirAttack03").convertRTL(), airAttack3_tick);
        PlayerAirAttack_RTL fireAirAttack01RTL = new PlayerAirAttack_RTL(11, playerSpriteSheetMap.get("FireAirAttack01").convertRTL(), fireAirAttack1_tick);
        PlayerAirAttack_RTL fireAirAttack02RTL = new PlayerAirAttack_RTL(11, playerSpriteSheetMap.get("FireAirAttack02").convertRTL(), fireAirAttack2_tick);
        PlayerAirAttack_RTL fireAirAttack03RTL = new PlayerAirAttack_RTL(11, playerSpriteSheetMap.get("FireAirAttack03").convertRTL(), fireAirAttack3_tick);
        PlayerAirAttack_RTL airAttackLoopRTL = new PlayerAirAttack_RTL(11, playerSpriteSheetMap.get("AirAttack03Loop").convertRTL(), airAttack_loop_tick);
        PlayerJumpRoll_RTL jumpRollRTL = new PlayerJumpRoll_RTL(12, playerSpriteSheetMap.get("JumpRoll01").convertRTL(), jumpRoll_tick);
        PlayerGetUp_RTL getUpRTL = new PlayerGetUp_RTL(13, playerSpriteSheetMap.get("GetUp01").convertRTL(), getUp_tick);
        PlayerKnockDown knockDownRTL = new PlayerKnockDown(14, playerSpriteSheetMap.get("KnockDown01").convertRTL(), knockDown_tick);
        PlayerLedgeAction_RTL ledgeClimbRTL = new PlayerLedgeAction_RTL(15, playerSpriteSheetMap.get("LedgeClimb01").convertRTL(), ledgeClimb_tick);
        PlayerLedgeAction_RTL ledgeGrabRTL = new PlayerLedgeAction_RTL(16, playerSpriteSheetMap.get("LedgeGrab01").convertRTL(), ledgeGrap_tick);
        PlayerWallAction_RTL wallRunRTL = new PlayerWallAction_RTL(17, playerSpriteSheetMap.get("WallRun01").convertRTL(), wallRun_tick);
        PlayerWallAction_RTL wallSlideRTL = new PlayerWallAction_RTL(18, playerSpriteSheetMap.get("WallSlide01").convertRTL(), wallSlide_tick);
        PlayerSprint_RTL sprintRTL = new PlayerSprint_RTL(19, playerSpriteSheetMap.get("Sprint01").convertRTL(), sprint_tick);
        //Put Animations to HashMap
        Map<CharacterState, Animation> playerAnimations = new HashMap();

        //Run directions
        playerAnimations.put(CharacterState.RUNFORWARD, runLTR);
        playerAnimations.put(CharacterState.RUNBACK, runRTL);
        playerAnimations.put(CharacterState.SPRINT_LTR, sprintLTR);//New
        playerAnimations.put(CharacterState.SPRINT_RTL, sprintRTL);//New
        playerAnimations.put(CharacterState.WALLRUN_LTR, wallRunLTR);//New
        playerAnimations.put(CharacterState.WALLRUN_RTL, wallRunRTL);//New
        playerAnimations.put(CharacterState.WALLSLIDE_LTR, wallSlideLTR);
        playerAnimations.put(CharacterState.WALLSLIDE_RTL, wallSlideRTL);

        //Idle
        playerAnimations.put(CharacterState.IDLE_LTR, idleLTR);
        playerAnimations.put(CharacterState.IDLE_RTL, idleRTL);
        playerAnimations.put(CharacterState.FIREIDLE_LTR, fireIdleLTR);
        playerAnimations.put(CharacterState.FIREIDLE_RTL, fireIdleRTL);

        //Attacks
        playerAnimations.put(CharacterState.ATTACK01_LTR, attack01LTR);
        playerAnimations.put(CharacterState.ATTACK01_RTL, attack01RTL);
        playerAnimations.put(CharacterState.ATTACK02_LTR, attack02LTR);
        playerAnimations.put(CharacterState.ATTACK02_RTL, attack02RTL);
        playerAnimations.put(CharacterState.ATTACK03_LTR, attack03LTR);
        playerAnimations.put(CharacterState.ATTACK03_RTL, attack03RTL);
        playerAnimations.put(CharacterState.AIRATTACK01_LTR, airAttack01LTR); //New
        playerAnimations.put(CharacterState.AIRATTACK01_RTL, airAttack01RTL);//New
        playerAnimations.put(CharacterState.AIRATTACK02_LTR, airAttack02LTR);//New
        playerAnimations.put(CharacterState.AIRATTACK02_RTL, airAttack02RTL);//New
        playerAnimations.put(CharacterState.AIRATTACK03_LTR, airAttack03LTR);//New
        playerAnimations.put(CharacterState.AIRATTACK03_RTL, airAttack03RTL);//New
        playerAnimations.put(CharacterState.AIRATTACKLOOP_LTR, airAttackLoopLTR);//New
        playerAnimations.put(CharacterState.AIRATTACKLOOP_RTL, airAttackLoopRTL);//New

        //Get Hit
        playerAnimations.put(CharacterState.GET_HIT_LTR, hitLTR);
        playerAnimations.put(CharacterState.GET_HIT_RTL, hitRTL);

        //Death Animation
        playerAnimations.put(CharacterState.DEATH_LTR, deathLTR);
        playerAnimations.put(CharacterState.DEATH_RTL, deathRTL);

        //Jump Animation
        playerAnimations.put(CharacterState.JUMP_LTR, jumpLTR);
        playerAnimations.put(CharacterState.JUMP_RTL, jumpRTL);
        playerAnimations.put(CharacterState.JUMPROLL_LTR, jumpRollLTR);
        playerAnimations.put(CharacterState.JUMPROLL_RTL, jumpRollRTL);

        //Crouch Animation
        playerAnimations.put(CharacterState.CROUCH_LTR, crouchLTR);
        playerAnimations.put(CharacterState.CROUCH_RTL, crouchRTL);

        //Slide Animation
        playerAnimations.put(CharacterState.SLIDE_LTR, slideLTR);
        playerAnimations.put(CharacterState.SLIDE_RTL, slideRTL);

        //Falldown
        playerAnimations.put(CharacterState.FALLDOWN_LTR, fallDownLTR);
        playerAnimations.put(CharacterState.FALLDOWN_RTL, fallDownRTL);
        playerAnimations.put(CharacterState.KNOCKDOWN_LTR, knockDownLTR);//New
        playerAnimations.put(CharacterState.KNOCKDOWN_RTL, knockDownRTL);//New
        playerAnimations.put(CharacterState.GETUP_LTR, getUpLTR);//New
        playerAnimations.put(CharacterState.GETUP_RTL, getUpRTL);//New

        //SpellCast
        playerAnimations.put(CharacterState.SPELLCAST_LTR, spellCastLTR);
        playerAnimations.put(CharacterState.SPELLCAST_RTL, spellCastRTL);
        playerAnimations.put(CharacterState.SPELLCASTLOOP_LTR, spellCastLoopLTR);
        playerAnimations.put(CharacterState.SPELLCASTLOOP_RTL, spellCastLoopRTL);

        //Ledge Climb
        playerAnimations.put(CharacterState.LEDGECLIMB_LTR, ledgeClimbLTR);//New
        playerAnimations.put(CharacterState.LEDGECLIMB_RTL, ledgeClimbRTL);//New
        playerAnimations.put(CharacterState.LEDGEGRAB_LTR, ledgeGrabLTR);//New
        playerAnimations.put(CharacterState.LEDGEGRAB_RTL, ledgeGrabRTL);//New

        //Init Inventory
        Entity inventorySheet = new Entity();
        inventorySheet.setImages(ImageManager.loadImagesFromFoldersToList("assets/res/inventory"));

        //Init Player
        player = new Player(0, "Shinobu Windsor", 100, defPlayerPosition,
                playerAnimations, this, inventorySheet);
        //Init platforms
        player.setInsidePlatform(firstPlatform);

        //Init Ability
        abilitiesCharacterInit(player.getAbilities(), player);

        //Init Items
        //Init Fire Sword
        Entity fireSwordSheet = new Entity();
        fireSwordSheet.add("assets/res/item/icon_items/Swords/Fire_Sworld.png");
        FireSwordAnimation fireSwordAnimation = new FireSwordAnimation(1, fireSwordSheet, -1);
        Map<CharacterState, Animation> itemEquipAnimations = new HashMap<CharacterState, Animation>();
        itemEquipAnimations.put(CharacterState.IDLE_LTR, fireIdleLTR);
        itemEquipAnimations.put(CharacterState.IDLE_RTL, fireIdleRTL);
        itemEquipAnimations.put(CharacterState.ATTACK01_LTR, fireAttack01LTR);
        itemEquipAnimations.put(CharacterState.ATTACK01_RTL, fireAttack01RTL);
        itemEquipAnimations.put(CharacterState.ATTACK02_LTR, fireAttack02LTR);
        itemEquipAnimations.put(CharacterState.ATTACK02_RTL, fireAttack02RTL);
        itemEquipAnimations.put(CharacterState.ATTACK03_LTR, fireAttack03LTR);
        itemEquipAnimations.put(CharacterState.ATTACK03_RTL, fireAttack03RTL);
        itemEquipAnimations.put(CharacterState.AIRATTACK01_LTR, fireAirAttack01LTR);
        itemEquipAnimations.put(CharacterState.AIRATTACK01_RTL, fireAirAttack01RTL);
        itemEquipAnimations.put(CharacterState.AIRATTACK02_LTR, fireAirAttack02LTR);
        itemEquipAnimations.put(CharacterState.AIRATTACK02_RTL, fireAirAttack02RTL);
        itemEquipAnimations.put(CharacterState.AIRATTACK03_LTR, fireAirAttack03LTR);
        itemEquipAnimations.put(CharacterState.AIRATTACK03_RTL, fireAirAttack03RTL);
        itemEquipAnimations.put(CharacterState.DEATH_LTR, fireDeathLTR);
        itemEquipAnimations.put(CharacterState.DEATH_RTL, fireDeathRTL);
        Sword fireSword = new Sword(1, "Fire Sword", fireSwordAnimation, null, this, 1, itemEquipAnimations);
        AttackIncrease attackIncrease = new AttackIncrease(1, "Attack Increase", 30, null, null, this, null);
        fireSword.getAbilities().add(attackIncrease);

        // Test Fire Sword
//        fireSword.setCharacter(player);
//        attackIncrease.setCharacter(player);
//        fireSword.use();
        GameObject gameObject = background.getGameObjectsTouchable().get("chest1");
        if (gameObject != null) {
            if (gameObject instanceof Chest) {
                Chest chest = (Chest) gameObject;
                fireSword.setPosition(chest.getPlatform().middleTopPlatform(Item.ITEM_WIDTH + 80, Item.ITEM_HEIGHT + 80));
                fireSword.getPosition().setYPosition(fireSword.getPosition().getYPosition() + 55);
                chest.getItems().add(fireSword);
            }
        }

        //Init Handler
        PlayerAbilityHandler abilityHandler = new PlayerAbilityHandler(player, "player_ability_handler", this);
        player.getAbility(0).getHandlers().add(abilityHandler);
        player.getAbility(1).getHandlers().add(abilityHandler);
        game.addKeyListener(abilityHandler);
        PlayerMovementHandler keyBoardHandler = new PlayerMovementHandler(player, "player_movement", this);
        PlayerMouseHandler mouseHandler = new PlayerMouseHandler(player, "player_mouse", this);
        player.getController().add(keyBoardHandler);
        player.getController().add(mouseHandler);
        game.addKeyListener(keyBoardHandler);
        game.addMouseListener(mouseHandler);

        camera.setPlayer(player);
        // level up to 8
//        player.getStats().addExperience(50000);
    }

    public void itemInit(Inventory inventory, Character character) {
        Random random = new Random();
        int randNum = random.nextInt(2);
        Item item = null;
        if (randNum == 0) {
            Entity healthPotionSheet = new Entity();
            healthPotionSheet.add("assets/res/item/s_health_potion.png");
            PotionAnimation healthPotionAnimation = new PotionAnimation(0, healthPotionSheet, -1);
            item = new SmallHealthPotion(itemCount, healthPotionAnimation, character,
                    this, 1);
            Ability potionHeal = new PotionHeal(10, 0, 1000, null, null, null, null, this, character);
            item.getAbilities().add(potionHeal);
        } else {
            Entity energyPotionSheet = new Entity();
            energyPotionSheet.add("assets/res/item/s_energy_potion.png");
            PotionAnimation energyPotionAnimation = new PotionAnimation(0, energyPotionSheet, -1);
            item = new SmallEnergyPotion(itemCount, energyPotionAnimation, character,
                    this, 1);
            EnergyRecovery energyRecovery = new PotionEnergyRecovery(10, 0, 1000,
                    null, null, null, null, this, character);
            item.getAbilities().add(energyRecovery);
        }
        inventory.addItemToInventory(item);
        itemCount++;
    }

    public void abilitiesCharacterInit(List<Ability> abilities, Character character) {
        List<BufferedImage> fireBallsLTR = ImageManager.loadImagesWithCutFromFolderToList("assets/res/ability/Fire Ball/LTR", 200, 365, 580, 200);
        List<BufferedImage> fireBallsRTL = ImageManager.loadImagesWithCutFromFolderToList("assets/res/ability/Fire Ball/RTL", 30, 365, 580, 200);
        if (character instanceof Player) {
            BufferedImage redBorder = ImageManager.loadImage("assets/res/ability/border-red.png");
            Entity greaterHealSheet = new Entity();
            greaterHealSheet.getImages().add(ImageManager.loadImage("assets/res/ability/Greater-Heal.png"));
            GamePosition firstSkillPosition = new GamePosition(character.getHealthBar().getHealthBarPos().getXPosition(),
                    character.getHealthBar().getHealthBarPos().getMaxY() + 90, 80, 80);
            Ability greaterHeal = new GreaterHeal(15, 1, 2500, 5, greaterHealSheet, null,
                    firstSkillPosition, redBorder, this, character);
            abilities.add(greaterHeal);
            Entity fireballIcon = new Entity();
            fireballIcon.getImages().add(ImageManager.loadImage("assets/res/ability/Fire-Ball.png"));
            Entity sheetLTR = new Entity();
            Entity sheetRTL = new Entity();
            if (fireBallsLTR == null || fireBallsRTL == null) {
                return;
            }
            sheetLTR.setImages(fireBallsLTR);
            sheetRTL.setImages(fireBallsRTL);
            Animation fireBallAnimationLTR = new FireBallAnimation(0, sheetLTR, 2);
            Animation fireBallAnimationRTL = new FireBallAnimation(1, sheetRTL, 2);
            Fireball fireball = new Fireball(150, 30, 2, 1000, 10, fireballIcon,
                    new GamePosition(firstSkillPosition.getMaxX() + 15,
                            firstSkillPosition.getYPosition(), firstSkillPosition.getWidth(), firstSkillPosition.getHeight()),
                    fireBallAnimationLTR, fireBallAnimationRTL, redBorder, this, character);
            abilities.add(fireball);
            return;
        } else {
            if (character instanceof DiorEnemy) {
                if (((DiorEnemy) character).getColor() == DiorColor.Red) {
                    Entity sheetLTR = new Entity();
                    Entity sheetRTL = new Entity();
                    if (fireBallsLTR == null || fireBallsRTL == null) {
                        return;
                    }
                    sheetLTR.setImages(fireBallsLTR);
                    sheetRTL.setImages(fireBallsRTL);
                    Animation fireBallAnimationLTR = new FireBallAnimation(0, sheetLTR, 0);
                    Animation fireBallAnimationRTL = new FireBallAnimation(1, sheetRTL, 0);
                    Fireball fireball = new Fireball(20, 15, 2, 1000, 0, null, null,
                            fireBallAnimationLTR, fireBallAnimationRTL, this, character);
                    abilities.add(fireball);
                }
                return;
            }
            if (character instanceof PirateCat) {
                TimeHeal timeHeal = new TimeHeal(5000, 500, 5, 0,
                        6000, 0, null, null, null, null, this, character);
                abilities.add(timeHeal);
                return;
            }
        }
    }

    public Runnable spawnEnemies() {
        return new Runnable() {
            int spawnCounter = 0;

            @Override
            public void run() {
                initEnemies();
//                long now;
//                long updateTime;
//                long wait;
//
//                final int TARGET_FPS = Game.FPS;
//                final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
//                while (game.isRunning()) {
//                    now = System.nanoTime();
//                    spawnCounter++;
//                    if (spawnCounter > 2000) {
//                        try {
//                            Random random = new Random();
//                            int numberOfEnemies = random.nextInt(5);
//                            int platformStandSize = getPlatforms().get(9).size() - 5;
//                            int platformColumn = platformStandSize;
//                            for (int i = 0; i < numberOfEnemies; i++) {
//                                Platform platform = getPlatforms().get(9).get(platformColumn);
//                                if (platform == null) {
//                                    continue;
//                                }
//                                if (platform instanceof Tile || platform instanceof WallTile) {
//                                    if (i > 0) {
//                                        i--;
//                                    }
//                                } else {
//                                    diorInit(platform);
//                                }
//                                if (platformColumn > 0) {
//                                    platformColumn--;
//                                } else {
//                                    platformColumn = platformStandSize;
//                                }
//                            }
//                        } catch (Exception ex) {
//                            System.out.println(ex.toString());
//                        }
//                        spawnCounter = 0;
//                    }
//                    updateTime = System.nanoTime() - now;
//                    wait = (OPTIMAL_TIME - updateTime) / 1000000;
//                    try {
//                        Thread.sleep(wait);
//                    } catch (Exception e) {
//
//                    }
//                }
            }
        };
    }

    public void tick() {
        if (Game.STATE == GameState.LOADING_STATE) {
            if (loadingScreen != null) {
                loadingScreen.tick();
            }
            return;
        }
        if (Game.STATE == GameState.GAME_STATE) {
            GameTimer.getInstance().tick();
            if (background != null) {
                background.tick();
            }
            if (map != null) {
                map.tick();
            }
            if (camera != null) {
                camera.tick();
            }
            if (enemies != null) {
                if (enemies.size() > 0) {
                    for (int i = 0; i < enemies.size(); i++) {
                        Enemy enemy = enemies.get(i);
                        enemy.tick();
                    }
                }
            }
            if (itemsOnGround.size() > 0) {
                for (int i = 0; i < itemsOnGround.size(); i++) {
                    Item item = itemsOnGround.get(i);
                    if (item != null) {
                        item.tick();
                    }
                }
            }
            if (player != null) {
                player.tick();
            }
            if (rule != null) {
                rule.tick();
            }
            transitionScreen.tick();
        }
        if (Game.STATE == GameState.OPTION_STATE) {
            if (background != null) {
                background.tick();
            }
            if (optionHandler != null) {
                optionHandler.tick();
            }
        }
    }

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (Game.STATE == GameState.LOADING_STATE) {
            if (loadingScreen != null) {
                loadingScreen.render(g2);
            }
            return;
        }
        if (Game.STATE == GameState.OPTION_STATE) {
            if (background != null) {
                background.render(g2);
            }
            if (map != null) {
                map.render(g2);
            }
            if (optionHandler != null) {
                optionHandler.render(g2);
            }
        } else {
            if (background != null) {
                background.render(g2);
            }
            if (camera != null) {
                camera.render(g2);
            }
            if (enemies != null) {
                if (enemies.size() > 0) {
                    for (int i = 0; i < enemies.size(); i++) {
                        Enemy enemy = enemies.get(i);
                        if (camera.checkPositionRelateToCamera(enemy.getPosition())) {
                            enemy.render(g2);
                        }
                    }
                }
            }
            if (map != null) {
                map.render(g2);
            }
            if (itemsOnGround.size() > 0) {
                for (int i = 0; i < itemsOnGround.size(); i++) {
                    Item item = itemsOnGround.get(i);
                    if (item != null) {
                        if (camera.checkPositionRelateToCamera(item.getPosition())) {
                            item.render(g2);
                        }
                    }
                }
            }
            if (rule != null) {
                rule.render(g2);
            }
            if (player != null) {
                player.render(g2);
            }
            transitionScreen.render(g2);
        }
    }

    public Camera getCamera() {
        return camera;
    }

    public List<List<Platform>> getPlatforms() {
        return background.getScene();
    }

    public GamePosition getPositionFromPlatform(Platform platform, int characterWidth, int characterHeight) {
        return new GamePosition(platform.getPosition().getXPosition() + 5,
                platform.getPosition().getYPosition(), characterWidth, characterHeight);
    }

    public List<List<Platform>> getSurroundPlatform(int i, int j) {
        return background.getSurroundPlatform(i, j, background.DEF_SURROUND_TILE,
                0, 0, 0, 0);
    }

    public TransitionScreen getTransitionScreen() {
        return transitionScreen;
    }

    public GamePosition getScenePosition() {
        return background.getPosition();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public Game getGame() {
        return game;
    }

    public boolean isRenderMap() {
        return map.isRenderMap();
    }

    public void setRenderMap(boolean renderMap) {
        map.setIsRenderMap(renderMap);
    }

    public Thread getThread() {
        return spawnEnemiesThread;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public List<Item> getItemsOnGround() {
        return itemsOnGround;
    }

    public Rule getRule() {
        return rule;
    }

    public OptionKeyboardHandler getOptionHandler() {
        return optionHandler;
    }

}
