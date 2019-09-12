package org.mp4parser.boxes.microsoft;


import org.mp4parser.boxes.microsoft.contentprotection.GenericHeader;
import org.mp4parser.tools.Hex;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public abstract class ProtectionSpecificHeader {
    protected static Map<UUID, Class<? extends ProtectionSpecificHeader>> uuidRegistry = new HashMap<UUID, Class<? extends ProtectionSpecificHeader>>();

    public static ProtectionSpecificHeader createFor(UUID systemId, ByteBuffer bufferWrapper) {
        final Class<? extends ProtectionSpecificHeader> aClass = uuidRegistry.get(systemId);

        ProtectionSpecificHeader protectionSpecificHeader = null;
        if (aClass != null) {
            try {
                protectionSpecificHeader = aClass.newInstance();

            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        if (protectionSpecificHeader == null) {
            protectionSpecificHeader = new GenericHeader();
        }
        protectionSpecificHeader.parse(bufferWrapper);
        return protectionSpecificHeader;

    }

    public abstract UUID getSystemId();

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("somebody called equals on me but that's not supposed to happen.");
    }

    public abstract void parse(ByteBuffer byteBuffer);

    public abstract ByteBuffer getData();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ProtectionSpecificHeader");
        sb.append("{data=");
        ByteBuffer data = getData().duplicate();
        ((Buffer)data).rewind();
        byte[] bytes = new byte[data.limit()];
        data.get(bytes);
        sb.append(Hex.encodeHex(bytes));
        sb.append('}');
        return sb.toString();
    }
}
