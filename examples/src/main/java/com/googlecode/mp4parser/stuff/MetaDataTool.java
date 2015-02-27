package com.googlecode.mp4parser.stuff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ChunkOffsetBox;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.UnknownBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.googlecode.mp4parser.boxes.apple.Utf8AppleDataBox;
import com.googlecode.mp4parser.boxes.microsoft.XtraBox;
import com.googlecode.mp4parser.util.Path;

/**
 * Hello world!
 *
 */
public class MetaDataTool {
	public static void main(String[] args) {
		String file = "Z:\\temp\\moviestest\\dest.mp4";
		try {
			MetaDataTool mdt = new MetaDataTool( file );
			mdt.dumpBoxes();
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		System.err.println( "Hello!" );
	}
	
	private long originalUserDataSize = 0;
	private XtraBox xtraBox;
	private UserDataBox userDataBox;
	private MetaBox metaBox;
	private IsoFile isoFile;
	
	public MetaDataTool( String path ) throws IOException {
		
		//The source I copied this from created 2 new files, a temp file and a target file
		//I'm not sure this is necessary, but maybe when you make changes it's edited in-place?
		//Anyway, just to be safe I'm keeping it so no operations are done on original file
        File videoFile = new File(path);
        if (!videoFile.exists())
            throw new FileNotFoundException("File " + path + " not exists");

        if (!videoFile.canWrite())
            throw new IllegalStateException("No write permissions to file " + path);

        File tempFile = File.createTempFile("ChangeMetaData", "");
        FileUtils.copyFile(videoFile, tempFile);
        tempFile.deleteOnExit();

        isoFile = new IsoFile(tempFile.getAbsolutePath());
        userDataBox = Path.getPath(isoFile, "/moov/udta");
        if( userDataBox != null ) {
        	originalUserDataSize = userDataBox.getSize();
        }
        
	}
	
	public static final String WM_RATING_TAG = "WM/SharedUserRating";
	public static final int WM_RATING_VALS[] = { 0, 1, 25, 50, 75, 99 };
	public void setWindowsMediaRating( int rating ) { //0-5
		if( rating < 0 || rating > 5 ) {
			throw new RuntimeException( "Invalid rating, 0-5 only" );
		}
		
		if( rating == 0 ) {
			removeWindowsMediaTag( WM_RATING_TAG );
		}
		else {
			setWindowsMediaLong( WM_RATING_TAG, WM_RATING_VALS[rating] );
		}
	}
	
	
	public static final String WM_TAGS_TAG = "WM/Category";
	public void setWindowsMediaTags( String tags[] ) {
		if( tags == null || tags.length == 0 ) {
			removeWindowsMediaTag( WM_TAGS_TAG );
		}
		else {
			setWindowsMediaStrings( WM_TAGS_TAG, tags );
		}
	}
	
	public void setWindowsMediaDate( String tagName, Date dateVal ) {
		XtraBox xb = getXtraBox();
		xb.setTagValue( tagName, dateVal );
	}

	public void setWindowsMediaLong( String tagName, long longVal ) {
		XtraBox xb = getXtraBox();
		xb.setTagValue( tagName, longVal );
	}
	
	public void setWindowsMediaStrings( String tagName, String values[] ) {
		XtraBox xb = getXtraBox();
		xb.setTagValues( tagName, values );
	}
	
	public void removeWindowsMediaTag( String tagName ) {
		XtraBox xb = getXtraBox();
		xb.removeTag( tagName );
	}
	

	private UserDataBox getUserDataBox() {
		if( userDataBox == null ) {
			userDataBox = new UserDataBox();
			isoFile.getMovieBox().addBox( userDataBox );
		}
		return userDataBox;
	}
	
	private MetaBox getMetaBox() {
		if( metaBox == null ) {
			UserDataBox ud = getUserDataBox();
			metaBox = Path.getPath(ud, "meta");
			if( metaBox == null ) {
				metaBox = new MetaBox();
                ud.addBox(metaBox);
			}
		}
		return metaBox;
	}
	
	private XtraBox getXtraBox() {
		if( xtraBox == null ) {
			UserDataBox ud = getUserDataBox(); //Create user data box if necessary
			xtraBox = Path.getPath( ud, "Xtra" );
			if( xtraBox == null ) {
				xtraBox = new XtraBox();
				ud.addBox( xtraBox );
			}
		}
		return xtraBox;
	}
	
	public void writeMp4( String filename ) throws IOException {
        long finalUserDataSize = 0;
		if( userDataBox != null ) {
			finalUserDataSize = userDataBox.getSize();
		}
        if (needsOffsetCorrection(isoFile)) {
            correctChunkOffsets(isoFile, finalUserDataSize - originalUserDataSize);
        }
        FileOutputStream videoFileOutputStream = null;
        try {
	        videoFileOutputStream = new FileOutputStream(filename);
	        isoFile.getBox(videoFileOutputStream.getChannel());
        }
        finally {
        	closeQuietly(isoFile);
        	IOUtils.closeQuietly(videoFileOutputStream);
        }
	}
	
	private static String getIndentation( int indent ) {
		char c[] = new char[indent];
		for( int i = 0; i < indent; i++ ) {
			c[i] = ' ';
		}
		return new String( c );
	}
	
	public void dumpBoxes() {
		dumpBoxes( isoFile, 0 );
	}
	
	private static void dumpBoxes( Container container, int indent ) {
		String meInd = getIndentation( indent );
		String subInd = getIndentation( indent + 2 );
		System.out.println( meInd +  container.getClass().getName() );
		for (Box box : container.getBoxes() ) {
	        	if( box instanceof Container ) {
	        		dumpBoxes( (Container)box, indent + 2 );
	        	}
	        	else {
	        		try {
		        		if( box instanceof UnknownBox ) {
		        			System.out.println( subInd + box.getClass().getName() + "[" + box.getSize() + "/" + box.getType() + "]:" + box.toString() );
		        			UnknownBox ub = (UnknownBox)box;
		        		}
			        	else if( box instanceof Utf8AppleDataBox ) {
		        			System.out.println( subInd + box.getClass().getName() + ": " + box.getType() + ": " + box.toString() + ": " + ((Utf8AppleDataBox)box).getValue() );
			        	}
			        	else {
		        			System.out.println( subInd + box.getClass().getName() + ": " + box.getType() + ": " + box.toString() );
			        	}
	        		}
	        		catch( Exception e ) {
	        			System.err.println( "Error parsing " + box.getClass().getSimpleName() + " box: " + e );
	        			e.printStackTrace( System.err );
	        		}
    		}
        }
	}

    public static boolean needsOffsetCorrection(IsoFile isoFile) {

        if (Path.getPaths(isoFile, "mdat").size() > 1) {
            throw new RuntimeException("There might be the weird case that a file has two mdats. One before" +
                    " moov and one after moov. That would need special handling therefore I just throw an " +
                    "exception here. ");
        }

        if (Path.getPaths(isoFile, "moof").size() > 0) {
            throw new RuntimeException("Fragmented MP4 files need correction, too. (But I would need to look where)");
        }

        for (Box box : isoFile.getBoxes()) {
            if ("mdat".equals(box.getType())) {
                return false;
            }
            if ("moov".equals(box.getType())) {
                return true;
            }
        }
        throw new RuntimeException("Hmmm - shouldn't happen");
    }

    private static void correctChunkOffsets(IsoFile tempIsoFile, long correction) {
        List<Box> chunkOffsetBoxes = Path.getPaths(tempIsoFile, "/moov[0]/trak/mdia[0]/minf[0]/stbl[0]/stco[0]");
        for (Box chunkOffsetBox : chunkOffsetBoxes) {

            LinkedList<Box> stblChildren = new LinkedList<Box>(chunkOffsetBox.getParent().getBoxes());
            stblChildren.remove(chunkOffsetBox);

            long[] cOffsets = ((ChunkOffsetBox) chunkOffsetBox).getChunkOffsets();
            for (int i = 0; i < cOffsets.length; i++) {
                cOffsets[i] += correction;
            }

            StaticChunkOffsetBox cob = new StaticChunkOffsetBox();
            cob.setChunkOffsets(cOffsets);
            stblChildren.add(cob);
            chunkOffsetBox.getParent().setBoxes(stblChildren);
        }
    }
    public static void deleteQuietly( File f ) {
    	try {
    		f.delete();
    	}
    	catch( Exception ioe ) {
    		//ignore
    	}
    }
    public static void closeQuietly(IsoFile input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

}
