package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp4j.protobuf.chat.Chat;
import it.auties.whatsapp4j.protobuf.info.MessageInfo;
import it.auties.whatsapp4j.response.impl.json.UserInformationResponse;
import lombok.NonNull;
import me.laravieira.willy.Willy;
import me.laravieira.willy.kernel.Context;
import me.laravieira.willy.internal.WillyUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WhatsappListener implements it.auties.whatsapp4j.listener.WhatsappListener {
    public void onNewMessage(@NonNull Chat chat, @NonNull MessageInfo message) {
        String id = "whatsapp-"+chat.jid().substring(0, chat.jid().indexOf('@'));
        if(message.container().textMessage() == null)
            return;
        String content = message.container().textMessage().text();

        if(content.isEmpty())
            return;
        if(Willy.getConfig().asBoolean("whatsapp.shared-chat")
        && !WillyUtils.hasWillyCall(content)
        && !Context.getContexts().containsKey(id))
            return;

        content = content.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');

        WhatsappContext context = WhatsappContext.getContext(chat, message, id);
        context.getWatsonMessager().sendTextMessage(content);
    }

    public void onQRCode(@NonNull com.google.zxing.common.BitMatrix matrix) {
        try {
            final int width = matrix.getWidth();
            final int height = matrix.getHeight();
            final int scale = 4;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    image.setRGB(x, y, matrix.get(x, y) ? 0x00000000 : 0x00FFFFFF);

            Image scaled = image.getScaledInstance(
                width*scale,
                height*scale,
                Image.SCALE_REPLICATE);

            image = new BufferedImage(width*scale, height*scale, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = image.createGraphics();
            graphics2D.drawImage(scaled, 0, 0, null);
            graphics2D.dispose();

            String folder = (new File(".").getCanonicalPath());
            File file = new File(folder+File.separator+"whatsapp-qrcode.png");
            if(file.exists()) {
                file.mkdirs();
                file.createNewFile();
            }
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            Willy.getLogger().info("Unable to save Whatsapp qr-code: "+e.getMessage());
        }finally {
            Willy.getLogger().info("Whatsapp QR-Code saved. Read it with your device.");
        }

    }

    public void onLoggedIn(@NonNull UserInformationResponse info) {
        Willy.getLogger().info("Whatsapp instance connected.");
    }

    public void onDisconnected() {
        Willy.getLogger().info("Whatsapp instance disconnected.");
    }
}
