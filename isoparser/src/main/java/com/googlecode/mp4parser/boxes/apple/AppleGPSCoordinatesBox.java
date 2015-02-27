package com.googlecode.mp4parser.boxes.apple;

import java.nio.ByteBuffer;

import com.coremedia.iso.Ascii;
import com.googlecode.mp4parser.AbstractBox;

/**
 * Created by sannies on 10/15/13.
 */
public class AppleGPSCoordinatesBox extends AbstractBox {
    public AppleGPSCoordinatesBox() {
        super("©alb");
    }

    String coords;
    
	@Override
	protected long getContentSize() {
		return coords.length();
	}

	@Override
	protected void getContent(ByteBuffer byteBuffer) {
		// TODO Auto-generated method stub
		byteBuffer.putInt( coords.length( ) );
		byteBuffer.put( Ascii.convert( coords ) );
	}

	@Override
	protected void _parseDetails(ByteBuffer content) {
		int length = content.getInt();
		byte bytes[] = new byte[length];
		content.get( bytes );
		coords = Ascii.convert( bytes );
	}
    
	public String toString() {
		if( !isParsed() ) {
			parseDetails();
		}
		return "AppleGPSCoordinatesBox[" + coords + "]";
	}
	
}
