package io.github.jagodevreede.sdkman.api.files;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record ZipExtraInformation(int crc32,
                                  short permissions
                                  ) {

    public static ZipExtraInformation fromField(byte[] extra) {
        ByteBuffer extraData = ByteBuffer.wrap(extra);
        extraData.order(ByteOrder.LITTLE_ENDIAN);

        if (extra.length >= 3) {
            int crc32 = extraData.getInt();
            short permissions = extraData.getShort();
            return new ZipExtraInformation(crc32, permissions);
        }

        return null;
    }
}
