package ru.d5kostya.skinswitcher.client.api;

import com.google.gson.JsonObject;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ContentType;

import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.alinlib.info.Player;
import ru.d5kostya.skinswitcher.client.skin_switcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

import static ru.kelcuprum.alinlib.utils.GsonHelper.getStringInJSON;
import static ru.kelcuprum.alinlib.utils.GsonHelper.jsonElementIsNull;
import static ru.d5kostya.skinswitcher.client.skin_switcher.getPath;

public class SkinOption {
    public String name;
    public String skin;
    public String cape;
    public PlayerSkin.Model model;
    public PlayerSkin playerSkin;
    public SkinType type;
    public File file;

    public SkinOption(String name, String skin, String cape, PlayerSkin.Model model, SkinType type, File file){
        this.name = name;
        this.skin = skin;
        this.cape = cape;
        this.model = model;
        this.type = type;
        this.file = file;
    }
    public SkinOption(String name, PlayerSkin playerSkin, File file){
        this.name = name;
        this.playerSkin = playerSkin;
        this.skin = playerSkin.textureUrl();
        this.cape = "";
        this.model = playerSkin.model();
        this.type = SkinType.URL;
        this.file = file;
    }

    public PlayerSkin getPlayerSkin() throws IOException {
        ResourceLocation skin = playerSkin == null ? getTextureSkin() : playerSkin.texture();
        ResourceLocation cape = playerSkin == null ? getTextureCape() : playerSkin.capeTexture();
        ResourceLocation elytra = playerSkin == null ? getTextureCape() : playerSkin.elytraTexture();
        return new PlayerSkin(skin, name, cape, elytra, model, true);
    }



    public ResourceLocation getTextureSkin() throws IOException {
        ResourceLocation location = GuiUtils.getResourceLocation("skin_switcher", "skins_"+ (file == null ? name.toLowerCase() : file.getName()));
        if(skin.isBlank()) return DefaultPlayerSkin.getDefaultTexture();
        if(!skin_switcher.cacheResourceLocations.containsKey(skin)) {
            BufferedImage image = getSourceSkin();
            if(image == null) return DefaultPlayerSkin.getDefaultTexture();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            NativeImage nativeImage = NativeImage.read(is);
            Minecraft.getInstance().execute(() -> {
                DynamicTexture texture =
                        //#if MC >= 12105
                        new DynamicTexture(() -> name, nativeImage);
                //#else
                //$$ new DynamicTexture(nativeImage);
                //#endif
                Minecraft.getInstance().getTextureManager().register(location, texture);
            });
            skin_switcher.cacheResourceLocations.put(skin, location);
        }
        return location;
    }
    public ResourceLocation getTextureCape(){
        return skin_switcher.capes.getOrDefault(cape, null);
    }

    public BufferedImage getSourceSkin() throws IOException {
        BufferedImage image = getTexture();
        if(image == null){
            return null;
        }
        if(image.getWidth() > 64 || image.getHeight() > 64) throw new RuntimeException("Too high skin resolution!");
        return image;
    }

    public long lastUpdate = System.currentTimeMillis();

    public static HashMap<String, BufferedImage> resourceLocationMap = new HashMap<>();
    public static HashMap<String, Boolean> urls = new HashMap<>();
    public BufferedImage getTexture() {
        if (resourceLocationMap.containsKey(skin)) return resourceLocationMap.get(skin);
        else {
            if (!urls.getOrDefault(skin, false)) {
                if(System.currentTimeMillis() - lastUpdate > 1500) {
                    lastUpdate = System.currentTimeMillis();
                    urls.put(skin, true);
                    new Thread(() -> {
                        BufferedImage image = null;
                        try {
                            switch (type){
                                case URL -> image = ImageIO.read(new URL(skin));
                                case FILE -> image = ImageIO.read(new File(skin));
                                case NICKNAME -> {
                                    try {
                                        JsonObject json = MojangAPI.getSkinURL(skin);
                                        if(json == null) return;
                                        model = jsonElementIsNull("metadata.model", json) ? PlayerSkin.Model.WIDE : PlayerSkin.Model.SLIM;
                                        image = ImageIO.read(new URL(getStringInJSON("url", json, "https://textures.minecraft.net/texture/d5c4ee5ce20aed9e33e866c66caa37178606234b3721084bf01d13320fb2eb3f")));
                                    } catch (Exception exception){
                                        exception.printStackTrace();
                                    }
                                }
                            }
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }
                        resourceLocationMap.put(skin, image);
                    }).start();
                }
            }
            return null;
        }
    }

    public SkinOption setSkinTexture(SkinType type, String skin){
        this.type = type;
        skin_switcher.cacheResourceLocations.remove(skin);
        this.skin = skin;
        return this;
    }

    public SkinOption setCape(String cape){
        this.cape = cape;
        return this;
    }

    public void save() throws IOException {
        if(file == null) throw new RuntimeException("File == null");
        else Files.writeString(file.toPath(), toString(), StandardCharsets.UTF_8);
    }

    public void delete() throws IOException {
        if(file == null) throw new RuntimeException("File == null");
        else Files.delete(file.toPath());
    }

    public void uploadToMojangAPI() throws IOException {
        uploadSkinToMojangAPI();
        if(cape.isBlank()) hideCapeToMojangAPI();
        else activeCapeToMojangAPI();
        if(AlinLib.MINECRAFT.level != null && !AlinLib.MINECRAFT.isSingleplayer() && !AlinLib.MINECRAFT.isLocalServer())
            new ToastBuilder().setTitle(Component.literal("skin_switcher")).setMessage(Component.translatable("skin_switcher.upload.multiplayer")).setType(ToastBuilder.Type.WARN).setDisplayTime(15000).buildAndShow();
        new ToastBuilder().setTitle(Component.literal("skin_switcher")).setMessage(Component.translatable("skin_switcher.upload.done", name)).buildAndShow();
    }

    public void uploadSkinToMojangAPI() throws IOException {
        String accessToken = AlinLib.MINECRAFT.getUser().getAccessToken();
        HttpPost http = new HttpPost("https://api.minecraftservices.com/minecraft/profile/skins");
        HttpClient httpClient = HttpClientBuilder.create().build();
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("variant", model.id().equals("default") ? "classic" : "slim");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(getSourceSkin(), "png", byteArrayOutputStream);

        File file = new File(getPath()+"/temp/"+System.currentTimeMillis()+".png");
        Files.createDirectories(file.getParentFile().toPath());
        Files.write(file.toPath(), byteArrayOutputStream.toByteArray());


        if(AlinLib.MINECRAFT.isSingleplayer()){
            skin_switcher.logger.log("[dev!!] singleplayer");
        }

    
        try {
            builder.addBinaryBody("file", file);
            http.setEntity(builder.build());
            http.addHeader("Authorization", "Bearer " + accessToken);

            // Загружаем на временный хостинг и получаем URL
            String hostedUrl = uploadToCatbox(file);
            
            if(skin_switcher.config.getBoolean("COMMAND.ON.SELECT.ENABLED", true)){
                String commandString = skin_switcher.config.getString("COMMAND.ON.SELECT.COMAND");
                
                if(commandString != null && commandString.contains("{URL}")) {
                    commandString = commandString.replace("{URL}", hostedUrl);
                }

                final String finalCommandString = commandString;

                // Отправляем команду
                Minecraft.getInstance().execute(() -> {
                    if(finalCommandString.startsWith("")) {
                        Minecraft.getInstance().player.connection.sendCommand(finalCommandString.substring(1));
                    } else {
                        Minecraft.getInstance().player.connection.sendCommand(finalCommandString);
                    }
                });
            }

            HttpResponse response = httpClient.execute(http);
            if(response.getStatusLine().getStatusCode() == 200){
                skin_switcher.logger.log("ok");
            }
            else {
                throw new RuntimeException("[SKIN] "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }
        } finally {}
    }

    // Метод для загрузки на временный хостинг (пример для i.clovi.ru)
    public String uploadToCatbox(File file) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost("https://catbox.moe/user/api.php");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("fileToUpload", file, ContentType.DEFAULT_BINARY, file.getName());
        builder.addTextBody("reqtype", "fileupload");
        builder.addTextBody("userhash", ""); // если есть

        httpPost.setEntity(builder.build());
        HttpResponse response = httpClient.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity());
        if (response.getStatusLine().getStatusCode() == 200) {
            return responseString.trim(); // URL загруженного файла
        } else {
            throw new RuntimeException("Ошибка загрузки на Catbox: " + responseString);
        }
    }

    public void activeCapeToMojangAPI() throws IOException{
        String accessToken = AlinLib.MINECRAFT.getUser().getAccessToken();
        HttpPut http = new HttpPut("https://api.minecraftservices.com/minecraft/profile/capes/active");
        HttpClient httpClient = HttpClientBuilder.create().build();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("capeId", cape);
        http.setEntity(new StringEntity(jsonObject.toString()));
        http.addHeader("Authorization", "Bearer " + accessToken);
        http.addHeader("Content-Type", "application/json");
        HttpResponse response = httpClient.execute(http);
        if(response.getStatusLine().getStatusCode() == 200){
            skin_switcher.logger.log("ok");
        }
        else {
            throw new RuntimeException("[CAPE] "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        }
    }
    public void hideCapeToMojangAPI() throws IOException{
        String accessToken = AlinLib.MINECRAFT.getUser().getAccessToken();
        HttpDelete http = new HttpDelete("https://api.minecraftservices.com/minecraft/profile/capes/active");
        HttpClient httpClient = HttpClientBuilder.create().build();
        http.addHeader("Authorization", "Bearer " + accessToken);
        HttpResponse response = httpClient.execute(http);
        if(response.getStatusLine().getStatusCode() == 200){
            skin_switcher.logger.log("ok");
        }
        else {
            throw new RuntimeException("[CAPE] "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        }
    }

    // Хуета
    public JsonObject toJSON(){
        JsonObject json = new JsonObject();
        json.addProperty("skin", skin);
        json.addProperty("cape", cape);
        json.addProperty("type", type == SkinType.URL ? "url" : type == SkinType.FILE ? "file" : "nickname");
        json.addProperty("model", model.id());
        return json;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public static SkinOption getSkinOption(File file) throws IOException {
        JsonObject json = GsonHelper.parse(Files.readString(file.toPath()));
        SkinType skinType = switch (getStringInJSON("type", json, "file")){
            case "url" -> SkinType.URL;
            case "nickname" -> SkinType.NICKNAME;
            default -> SkinType.FILE;
        };
        return new SkinOption(
                getStringInJSON("name", json, "Player skin"),
                getStringInJSON("skin", json, ""),
                getStringInJSON("cape", json, ""),
                PlayerSkin.Model.byName(getStringInJSON("model", json, "default")),
                skinType,
                file
        );
    }
    public static SkinOption getSkinOption(JsonObject json, File file) {
        SkinType skinType = switch (getStringInJSON("type", json, "file")){
            case "url" -> SkinType.URL;
            case "nickname" -> SkinType.NICKNAME;
            default -> SkinType.FILE;
        };
        return new SkinOption(
                getStringInJSON("name", json, "Player skin"),
                getStringInJSON("skin", json, ""),
                getStringInJSON("cape", json, ""),
                PlayerSkin.Model.byName(getStringInJSON("model", json, "default")),
                skinType,
                file
        );
    }

    public enum SkinType {
        FILE(Component.translatable("skin_switcher.types.file")),
        URL(Component.translatable("skin_switcher.types.url")),
        NICKNAME(Component.translatable("skin_switcher.types.nickname"));
        final Component name;
        SkinType(Component name){
            this.name = name;
        }
    }
}
