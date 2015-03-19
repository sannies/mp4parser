package com.googlecode.mp4parser.boxes.apple;

import java.nio.ByteBuffer;

import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractBox;

/**
 * Created by marwatk on 02/27/15
 */
public class AppleGPSCoordinatesBox extends AbstractBox {
    public static final String TYPE = "©xyz";
	private static final int DEFAULT_LANG = 5575; //Empirical
    
    String coords;
    int lang = DEFAULT_LANG; //? Docs says lang, but it doesn't match anything in the traditional language map
	
	public AppleGPSCoordinatesBox() {
        super( TYPE );
    }
    
	public String getValue() {
		return coords;
	}
	
	public void setValue( String iso6709String ) {
		lang = DEFAULT_LANG;
		coords = iso6709String;
	}
	
	@Override
	protected long getContentSize() {
		return 4 + Utf8.utf8StringLengthInBytes( coords );
	}

	@Override
	protected void getContent(ByteBuffer byteBuffer) {
		byteBuffer.putShort( (short)coords.length( ) );
		byteBuffer.putShort( (short)lang );
		byteBuffer.put( Utf8.convert( coords ) );
	}

	@Override
	protected void _parseDetails(ByteBuffer content) {
		int length = content.getShort();
        lang = content.getShort(); //Not sure if this is accurate. It always seems to be 15 c7
		byte bytes[] = new byte[length];
		content.get( bytes );
		coords = Utf8.convert( bytes );
	}
    
	public String toString() {
		return "AppleGPSCoordinatesBox[" + coords + "]";
	}
	
}
