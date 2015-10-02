/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package org.mp4parser.boxes.iso14496.part12;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.support.Logger;
import org.mp4parser.support.Matrix;
import org.mp4parser.tools.DateHelper;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <code>
 * Box Type: 'mvhd'<br>
 * Container: {@link MovieBox} ('moov')<br>
 * Mandatory: Yes<br>
 * Quantity: Exactly one<br><br>
 * </code>
 * This box defines overall information which is media-independent, and relevant to the entire presentation
 * considered as a whole.
 */
public class MovieHeaderBox extends AbstractFullBox {
    public static final String TYPE = "mvhd";
    private static Logger LOG = Logger.getLogger(MovieHeaderBox.class);
    private Date creationTime;
    private Date modificationTime;
    private long timescale;
    private long duration;
    private double rate = 1.0;
    private float volume = 1.0f;
    private Matrix matrix = Matrix.ROTATE_0;
    private long nextTrackId;
    private int previewTime;
    private int previewDuration;
    private int posterTime;
    private int selectionTime;
    private int selectionDuration;
    private int currentTime;

    public MovieHeaderBox() {
        super(TYPE);
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
        if (DateHelper.convert(creationTime) >= (1l << 32)) {
            setVersion(1);
        }

    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
        if (DateHelper.convert(modificationTime) >= (1l << 32)) {
            setVersion(1);
        }

    }

    public long getTimescale() {
        return timescale;
    }

    public void setTimescale(long timescale) {
        this.timescale = timescale;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        if (duration >= (1l << 32)) {
            setVersion(1);
        }
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public long getNextTrackId() {
        return nextTrackId;
    }

    public void setNextTrackId(long nextTrackId) {
        this.nextTrackId = nextTrackId;
    }

    protected long getContentSize() {
        long contentSize = 4;
        if (getVersion() == 1) {
            contentSize += 28;
        } else {
            contentSize += 16;
        }
        contentSize += 80;
        return contentSize;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if (getVersion() == 1) {
            creationTime = DateHelper.convert(IsoTypeReader.readUInt64(content));
            modificationTime = DateHelper.convert(IsoTypeReader.readUInt64(content));
            timescale = IsoTypeReader.readUInt32(content);
            duration = content.getLong();

        } else {
            creationTime = DateHelper.convert(IsoTypeReader.readUInt32(content));
            modificationTime = DateHelper.convert(IsoTypeReader.readUInt32(content));
            timescale = IsoTypeReader.readUInt32(content);
            duration = content.getInt();
        }
        if (duration < -1) {
            LOG.logWarn("mvhd duration is not in expected range");
        }


        rate = IsoTypeReader.readFixedPoint1616(content);
        volume = IsoTypeReader.readFixedPoint88(content);
        IsoTypeReader.readUInt16(content);
        IsoTypeReader.readUInt32(content);
        IsoTypeReader.readUInt32(content);

        matrix = Matrix.fromByteBuffer(content);

        previewTime = content.getInt();
        previewDuration = content.getInt();
        posterTime = content.getInt();
        selectionTime = content.getInt();
        selectionDuration = content.getInt();
        currentTime = content.getInt();

        nextTrackId = IsoTypeReader.readUInt32(content);

    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("MovieHeaderBox[");
        result.append("creationTime=").append(getCreationTime());
        result.append(";");
        result.append("modificationTime=").append(getModificationTime());
        result.append(";");
        result.append("timescale=").append(getTimescale());
        result.append(";");
        result.append("duration=").append(getDuration());
        result.append(";");
        result.append("rate=").append(getRate());
        result.append(";");
        result.append("volume=").append(getVolume());
        result.append(";");
        result.append("matrix=").append(matrix);
        result.append(";");
        result.append("nextTrackId=").append(getNextTrackId());
        result.append("]");
        return result.toString();
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        if (getVersion() == 1) {
            IsoTypeWriter.writeUInt64(byteBuffer, DateHelper.convert(creationTime));
            IsoTypeWriter.writeUInt64(byteBuffer, DateHelper.convert(modificationTime));
            IsoTypeWriter.writeUInt32(byteBuffer, timescale);
            byteBuffer.putLong(duration);
        } else {
            IsoTypeWriter.writeUInt32(byteBuffer, DateHelper.convert(creationTime));
            IsoTypeWriter.writeUInt32(byteBuffer, DateHelper.convert(modificationTime));
            IsoTypeWriter.writeUInt32(byteBuffer, timescale);
            byteBuffer.putInt((int) duration);
        }
        IsoTypeWriter.writeFixedPoint1616(byteBuffer, rate);
        IsoTypeWriter.writeFixedPoint88(byteBuffer, volume);
        IsoTypeWriter.writeUInt16(byteBuffer, 0);
        IsoTypeWriter.writeUInt32(byteBuffer, 0);
        IsoTypeWriter.writeUInt32(byteBuffer, 0);

        matrix.getContent(byteBuffer);

        byteBuffer.putInt(previewTime);
        byteBuffer.putInt(previewDuration);
        byteBuffer.putInt(posterTime);
        byteBuffer.putInt(selectionTime);
        byteBuffer.putInt(selectionDuration);
        byteBuffer.putInt(currentTime);

        IsoTypeWriter.writeUInt32(byteBuffer, nextTrackId);
    }

    public int getPreviewTime() {
        return previewTime;
    }

    public void setPreviewTime(int previewTime) {
        this.previewTime = previewTime;
    }

    public int getPreviewDuration() {
        return previewDuration;
    }

    public void setPreviewDuration(int previewDuration) {
        this.previewDuration = previewDuration;
    }

    public int getPosterTime() {
        return posterTime;
    }

    public void setPosterTime(int posterTime) {
        this.posterTime = posterTime;
    }

    public int getSelectionTime() {
        return selectionTime;
    }

    public void setSelectionTime(int selectionTime) {
        this.selectionTime = selectionTime;
    }

    public int getSelectionDuration() {
        return selectionDuration;
    }

    public void setSelectionDuration(int selectionDuration) {
        this.selectionDuration = selectionDuration;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }
}
